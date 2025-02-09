package com.ka.lych.util;

import com.ka.lych.list.LList;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 * @author klausahrenberg
 * @param <R> type of result after successful _future operation
 * @param <T>
 */
public abstract class LFuture<R, T extends Throwable>
        implements ILRegistration {

    private final LTask service;
    private final CountDownLatch latch;
    private T error;
    private R value;

    private enum LFutureHandlerType {
        VALUE, ERROR, CANCEL, COMPLETE
    };

    private record LFutureHandler(LFutureHandlerType handlerType, Consumer handler) {

    }
    ;
    
    final LList<LFutureHandler> _handlers = LList.empty();
    LList<Function<R, Boolean>> _verifiers = null;

    public LFuture(LTask service) {
        this.service = service;
        this.latch = new CountDownLatch(1);
        this.error = null;
        this.value = null;
    }

    @SuppressWarnings("unchecked")
    void _finish(T exception, R value, boolean cancelled) {
        this.error = exception;
        this.value = (exception == null ? value : null);
        latch.countDown();
        if (!cancelled) {
            if (hasError()) {
                if (_handlers.getIf(fh -> fh.handlerType() == LFutureHandlerType.ERROR) != null) {
                    _handlers.forEachIf(fh -> fh.handlerType() == LFutureHandlerType.ERROR, eh -> eh.handler().accept(this.error));
                } else {
                    //if no handler for errors is there, print stack trace
                    this.error.printStackTrace();
                }
            } else {
                _handlers.forEachIf(fh -> fh.handlerType() == LFutureHandlerType.VALUE, eh -> eh.handler().accept(this.value));
            }
        } else {
            _handlers.forEachIf(fh -> fh.handlerType() == LFutureHandlerType.CANCEL, eh -> eh.handler().accept(null));
        }
        _handlers.forEachIf(fh -> fh.handlerType() == LFutureHandlerType.COMPLETE, eh -> eh.handler().accept(null));
        _handlers.clear();
    }

    public boolean verifyResult(R value) {
        if (_verifiers != null) {
            var itv = _verifiers.iterator();
            while (itv.hasNext()) {
                var v = itv.next();
                if (!v.apply(value)) {
                    return false;
                }
            }
        }
        return true;
    }

    public LFuture<R, T> await() {
        try {
            latch.await();
        } catch (InterruptedException ex) {
            LLog.error(ex.getMessage(), ex);
        }
        return this;
    }

    /**
     * onError() mirrors a try-catch. In case of an error, then() will not be
     * called
     *
     * @param exceptionHandler
     * @return
     */
    public LFuture<R, T> onError(Consumer<? super T> exceptionHandler) {
        if (latch.getCount() > 0) {
            _handlers.add(new LFutureHandler(LFutureHandlerType.ERROR, exceptionHandler));
        } else if (hasError()) {
            exceptionHandler.accept(this.error);
        }
        return this;
    }

    /**
     * onCancel() will be called at cancellation of operation, no then() or
     * onError() will be fired
     *
     * @param cancelHandler
     * @return
     */
    public LFuture<R, T> onCancel(Consumer<Void> cancelHandler) {
        if (latch.getCount() > 0) {
            _handlers.add(new LFutureHandler(LFutureHandlerType.CANCEL, cancelHandler));
        } else if (hasError()) {
            cancelHandler.accept(null);
        }
        return this;
    }

    /**
     * whenComplete() is the equivalent of ‘finally’. The callback registered
     * within whenComplete() is called when whenComplete()’s receiver completes,
     * whether it does so with a value, with an error or was cancelled.
     *
     * @param completeHandler
     * @return
     */
    public LFuture<R, T> whenComplete(Consumer<Void> completeHandler) {
        if (latch.getCount() > 0) {
            _handlers.add(new LFutureHandler(LFutureHandlerType.COMPLETE, completeHandler));
        } else if (hasError()) {
            completeHandler.accept(null);
        }
        return this;
    }

    /**
     * then() will be called, if operation ended normally with a return value.
     *
     * @param valueHandler handler of result value of operation
     * @return
     */
    public LFuture<R, T> then(Consumer<R> valueHandler) {
        if (latch.getCount() > 0) {
            _handlers.add(new LFutureHandler(LFutureHandlerType.VALUE, valueHandler));
        } else if (!hasError()) {
            valueHandler.accept(this.value);
        }
        return this;
    }

    public LFuture<R, T> verify(Function<R, Boolean> valueVerifier) {
        if (latch.getCount() > 0) {
            if (_verifiers == null) {
                _verifiers = LList.empty();
            }
            _verifiers.add(valueVerifier);
        } else if (!hasError()) {
            valueVerifier.apply(value);
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
            LLog.error(ex.getMessage(), ex);
        }
        return this.value;
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
        return LFuture.<R, T>executeTask(((delay > 0) || (duration > 0) ? new LTimerTask<R, T>(runnable, delay, duration, looping) : new LTask<R, T>(runnable)));
    }

    public static <R, T extends Throwable> LFuture<R, T> executeTask(LTask<R, T> task) {
        //create list with services, if necessary
        ensureRunningServices();
        //Create and start service 
        LFuture<R, T> future = new LFuture<>(task) {
            @Override
            public void remove() {
                runningTasks.remove(task);
            }

        };
        task._future = future;
        runningTasks.add(task);
        task.start();
        return future;
    }

    public static <R, T extends Throwable> void cancel(LFuture<R, T> future) {
        if (runningTasks != null) {
            runningTasks.forEachIf(s -> s == future.service, s -> s.cancel());
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
            runningTasks.forEachIf(s -> s.runnable() == runnable, s -> s.cancel());
        }
    }

    public static <R, T extends Throwable> boolean isExecuting(ILRunnable<R, T> runnable) {
        return runningTasks.getIf(service -> service.runnable() == runnable) != null;
    }

    public static <R, T extends Throwable> boolean isExecuting(LFuture<R, T> future) {
        return runningTasks.getIf(s -> s == future.service) != null;
    }

    /**
     * Delays execution, if debugging is enabled. Can be used to simulate slow
     * execution during debugging
     *
     * @param millis
     */
    public static void delayDebug(long millis) {
        if (LLog.LOG_LEVEL == LLog.LLogLevel.DEBUGGING) {
            LLog.debug("...slow down... %s", millis);
            try {
                Thread.sleep(millis);
            } catch (InterruptedException ex) {

            }
            LLog.debug("...move on...");
        }
    }

}
