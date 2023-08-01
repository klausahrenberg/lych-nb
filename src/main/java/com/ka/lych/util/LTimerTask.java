package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 * @param <R>
 * @param <T>
 */
public class LTimerTask<R, T extends Throwable> extends LTask<R, T> {

    protected final long delay;
    protected final long duration;
    protected final boolean looping;
    protected long now, startAnim;

    public LTimerTask(ILRunnable<R, T> runnable, long delay, long duration, boolean looping) {
        super(runnable);
        LLog.test(this, "timer task created %s %s", delay, looping);
        this.delay = delay;
        this.duration = duration;
        this.looping = looping;
    }

    public void restart() {
        LLog.test(this, "restart now is 0");
        now = 0;
        startAnim = System.currentTimeMillis();
    }
    
    public long now() {
        return now;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        T error = null;
        R result = null;
        restart();
        while ((!isCancelled()) && (delay > 0) && (now < delay)) {
            try {
                Thread.sleep(30);
            } catch (Exception ex) {
            }
            now = System.currentTimeMillis() - startAnim;
        }
        if (!isCancelled()) {
            restart();
            do {
                try {
                    result = runnable.run(this);
                } catch (Throwable e) {
                    error = (T) e;
                }
                if (this.future != null) {
                    this.future.finish(error, (!cancelled ? result : null), cancelled);
                }
                if ((!isCancelled()) && (duration > 0)) {
                    try {
                        Thread.sleep(30);
                    } catch (Exception ex) {
                    }
                    now = System.currentTimeMillis() - startAnim;
                    if ((looping) && (now >= duration)) {
                        now = 0;
                        startAnim = System.currentTimeMillis();
                    }
                    LLog.test(this, "now is %s", now);
                }
            } while ((!isCancelled()) && (duration > 0) && ((now < duration) || (looping)));
        }
    }

}
