package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 * @param <R> type of result of service
 * @param <T>
 */
public class LTask<R, T extends Throwable> extends Thread {

    protected final ILRunnable<R, T> runnable;    
    protected LFuture<R, T> future;
    protected boolean cancelled;

    public LTask(ILRunnable<R, T> runnable) {
        super(LTask.class.getSimpleName() + "-" + runnable.getClass().getSimpleName() + "-" + runnable.getClass().hashCode());
        this.runnable = runnable;        
        this.cancelled = false;
        this.setDaemon(true);
    }

    public ILRunnable<R, T> getRunnable() {
        return runnable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        T error = null;
        R result = null;                        
        try {
            result = runnable.run(this);            
        } catch (Throwable e) {    
            error = (T) e;
        }
        if (this.future != null) {
            this.future.finish(error, (!cancelled ? result : null), cancelled);
        }
    }

    public boolean isCancelled() {
        return cancelled;
    }
    
    public void cancelService() {
        this.cancelled = true;
    }    

}
