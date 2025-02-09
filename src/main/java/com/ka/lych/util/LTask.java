package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 * @param <R> type of result of service
 * @param <T>
 */
public class LTask<R, T extends Throwable> extends Thread {

    final ILRunnable<R, T> _runnable;
    protected LFuture<R, T> _future;
    protected boolean _cancelled;

    public LTask(ILRunnable<R, T> runnable) {
        super(LTask.class.getSimpleName() + (runnable != null ? "-" + runnable.getClass().getSimpleName() + "-" + runnable.getClass().hashCode() : ""));
        _runnable = runnable;
        _cancelled = false;
        this.setDaemon(true);
    }

    public ILRunnable<R, T> runnable() {
        return _runnable;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        T error = null;
        R result = null;
        try {
            result = _runnable.run(this);
        } catch (Throwable e) {
            error = (T) e;
        }
        if (_future != null) {
            _future._finish(error, (!_cancelled ? result : null), _cancelled);
        }
    }

    public boolean isCancelled() {
        return _cancelled;
    }

    public void cancel() {
        _cancelled = true;
    }

}
