package com.ka.lych.util;

/**
 *
 * @author klausahrenberg
 */
public class LLoadingService
        implements ILRunnable {

    protected final LStack<ILLoadable> queue;
    //protected final LPriorityQueue<ILLoadable> queue;

    public LLoadingService() {
        queue = new LStack<>(false);
    }

    @SuppressWarnings("unchecked")
    public void addLoadable(ILLoadable loadable, int priority) {        
        if (!queue.contains(loadable)) {            
            queue.push(loadable);
        }
        if (!LFuture.isExecuting(this)) {
            LFuture.execute(this);
        }
    }

    @Override
    public Object run(LTask task) throws LException {
        /*while ((!service.isCancelled()) && (queue != null) && (!queue.isEmpty())) {
            //Get loadable from stack without removing
            var loadable = queue.peek();
            loadable.load();//.await().ifError(ex -> LLog.error(this, "Can't finished loading of canvas", (Throwable) ex));
            //Remove loadable from stack
            queue.pop();
        }*/
        return null;
    }

}
