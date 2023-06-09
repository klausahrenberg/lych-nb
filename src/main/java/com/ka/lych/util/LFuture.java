package com.ka.lych.util;

import com.ka.lych.list.LList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

/**
 *
 * @author klausahrenberg
 * @param <R> type of result after successful future operation
 */
public abstract class LFuture<R, T extends Throwable>
        implements ILRegistration {

    private final LTask service;
    private final CountDownLatch latch;
    private T error;
    private R value;
    private enum LFutureHandlerType {VALUE, ERROR, CANCEL, COMPLETE};
    private record LFutureHandler(LFutureHandlerType handlerType, Consumer handler) { };
    private final LList<LFutureHandler> handlers;

    public LFuture(LTask service) {
        this.service = service;
        this.latch = new CountDownLatch(1);
        this.error = null;
        this.value = null;
        this.handlers = LList.empty();
    }

    @SuppressWarnings("unchecked")
    protected void finish(T exception, R value, boolean cancelled) {
        this.error = exception;
        this.value = (exception == null ? value : null);
        latch.countDown();
        if (!cancelled) {
            if (hasError()) {                
                this.handlers.forEachIf(fh -> fh.handlerType() == LFutureHandlerType.ERROR, eh -> eh.handler().accept(this.error));
            } else {                
                this.handlers.forEachIf(fh -> fh.handlerType() == LFutureHandlerType.VALUE, eh -> eh.handler().accept(this.value));
            }
        } else {
            this.handlers.forEachIf(fh -> fh.handlerType() == LFutureHandlerType.CANCEL, eh -> eh.handler().accept(null));
        }
        this.handlers.forEachIf(fh -> fh.handlerType() == LFutureHandlerType.COMPLETE, eh -> eh.handler().accept(null));
        this.handlers.clear();       
    }

    public LFuture<R, T> await() {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            LLog.error(service, ex.getMessage(), ex);
        }
        return this;
    }

    /**
     * onError() mirrors a try-catch. In case of an error, then() will not be called
     * @param exceptionHandler
     * @return 
     */
    
    public LFuture<R, T> onError(Consumer<? super T> exceptionHandler) {
        if (latch.getCount() > 0) {
            this.handlers.add(new LFutureHandler(LFutureHandlerType.ERROR, exceptionHandler));
            //this.exceptionHandlers.add(exceptionHandler);
        } else if (hasError()) {
            exceptionHandler.accept(this.error);
        }
        return this;
    }
    
    /**
     * onCancel() will be called at cancellation of operation, no then() or onError() will be fired
     * @param cancelHandler
     * @return 
     */
    public LFuture<R, T> onCancel(Consumer<Void> cancelHandler) {
        if (latch.getCount() > 0) {
            this.handlers.add(new LFutureHandler(LFutureHandlerType.CANCEL, cancelHandler));
            //this.exceptionHandlers.add(exceptionHandler);
        } else if (hasError()) {
            cancelHandler.accept(null);
        }
        return this;
    }
    
    /**
     * whenComplete() is the equivalent of ‘finally’. The callback registered within whenComplete() 
     * is called when whenComplete()’s receiver completes, whether it does so with a value,
     * with an error or was cancelled.
     * @param completeHandler
     * @return 
     */    
    public LFuture<R, T> whenComplete(Consumer<Void> completeHandler) {
        if (latch.getCount() > 0) {
            this.handlers.add(new LFutureHandler(LFutureHandlerType.COMPLETE, completeHandler));            
        } else if (hasError()) {
            completeHandler.accept(null);
        }
        return this;
    }

    /**
     * then() will be called, if operation ended normally with a return value.
     * @param valueHandler handler of result value of operation
     * @return 
     */
    public LFuture<R, T> then(Consumer<R> valueHandler) {
        if (latch.getCount() > 0) {
            this.handlers.add(new LFutureHandler(LFutureHandlerType.VALUE, valueHandler));
            //this.valueHandlers.add(valueHandler);
        } else if (!hasError()) {
            valueHandler.accept(this.value);
        }
        return this;
    }

    public boolean hasError() {
        return (error != null);
    }

    public T error() {
        return error;
    }

    public R value() {
        return value;
    }

    public R awaitOrElseThrow() throws T {
        try {
            latch.await();
            if (hasError()) {
                throw error;
            }
        } catch (InterruptedException ex) {
            LLog.error(service, ex.getMessage(), ex);
        }
        return this.value;
    }

    @SuppressWarnings("unchecked")
    public static <R, T extends Throwable> LFuture<R, T> error(T error) {
        LFuture waiter = new LFuture(null) {
            @Override
            public void remove() {
            }
        };
        waiter.finish(error, null, false);
        return waiter;
    }

    @SuppressWarnings("unchecked")
    public static <T> LFuture value(T value) {
        LFuture waiter = new LFuture(null) {
            @Override
            public void remove() {
            }
        };
        waiter.finish(null, value, false);
        return waiter;
    }

    protected static LList<LTask> runningTasks;

    protected static void ensureRunningServices() {
        if (runningTasks == null) {
            runningTasks = new LList<>();
        }
    }        

    public static <R, T extends Throwable> LFuture<R, T> execute(ILRunnable<R, T> runnable) {
        return LFuture.<R, T>execute(runnable, 0, 0, false);
    }
    
    public static <R, T extends Throwable> LFuture<R, T> execute(ILRunnable<R, T> runnable, long delay, long duration, boolean looping) {               
        //create list with services, if necessary
        ensureRunningServices();
        //Create and start service                        
        LTask<R, T> task = ((delay > 0) || (duration > 0) ? new LTimerTask<R, T>(runnable, delay, duration, looping) : new LTask<R, T>(runnable));
        LFuture<R, T> future = new LFuture<>(task) {
            @Override
            public void remove() {
                runningTasks.remove(task);
            }
            
        };
        task.future = future;
        runningTasks.add(task);        
        task.start();
        return future;
    }    

    public static <R, T extends Throwable> void cancel(LFuture<R, T> future) {
        if (runningTasks != null) {
            runningTasks.forEachIf(s -> s == future.service, s -> {                
                s.cancelService();});
        }
    }
    
    public static <R, T extends Throwable> void restart(LFuture<R, T> future) {
        if (runningTasks != null) {
            runningTasks.forEachIf(s -> s == future.service, s -> {
                if (s instanceof LTimerTask) {                    
                    ((LTimerTask) s).restart();
                }
            });
        }
    }

    public static <R, T extends Throwable> void cancel(ILRunnable<R, T> runnable) {
        if (runningTasks != null) {
            runningTasks.forEachIf(s -> s.getRunnable() == runnable, s -> s.cancelService());
        }
    }

    public static <R, T extends Throwable> boolean isExecuting(ILRunnable<R, T> runnable) {
        return runningTasks.getIf(service -> service.getRunnable() == runnable) != null;
    }

    public static <R, T extends Throwable> boolean isExecuting(LFuture<R, T> future) {
        return runningTasks.getIf(s -> s == future.service) != null;
    }

}
