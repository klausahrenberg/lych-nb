package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 * @param <R>
 * @param <T>
 */
public class LTimerTask<R, T extends Throwable> extends LTask<R, T> {

    final long _delay;
    final long _duration;
    final boolean _looping;
    long _now, _startAnim;

    public LTimerTask(ILRunnable<R, T> runnable, long delay, long duration, boolean looping) {
        super(runnable);        
        _delay = delay;
        _duration = duration;
        _looping = looping;
    }

    public void restart() {
        _now = 0;
        _startAnim = System.currentTimeMillis();
    }
    
    public long now() {
        return _now;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run() {
        T error = null;
        R result = null;
        restart();
        while ((!isCancelled()) && (_delay > 0) && (_now < _delay)) {
            try {
                Thread.sleep(30);
            } catch (Exception ex) {
            }
            _now = System.currentTimeMillis() - _startAnim;
        }
        if (!isCancelled()) {
            restart();
            do {
                try {
                    result = _runnable.run(this);
                } catch (Throwable e) {
                    error = (T) e;
                }
                if (_future != null) {
                    _future._finish(error, (!_cancelled ? result : null), _cancelled);
                }
                if ((!isCancelled()) && (_duration > 0)) {
                    try {
                        Thread.sleep(30);
                    } catch (Exception ex) {
                    }
                    _now = System.currentTimeMillis() - _startAnim;
                    if ((_looping) && (_now >= _duration)) {
                        _now = 0;
                        _startAnim = System.currentTimeMillis();
                    }
                }
            } while ((!isCancelled()) && (_duration > 0) && ((_now < _duration) || (_looping)));
        }
    }

}
