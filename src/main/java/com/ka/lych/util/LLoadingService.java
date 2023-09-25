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
        //2023-09-04 commented out, otherwise null objects will only be added once
        //if (!queue.contains(loadable)) {            
        queue.push(loadable);
        //}
        if (!LFuture.isExecuting(this)) {
            LFuture.execute(this);
        }
    }

    @Override
    public Object run(LTask task) throws Throwable {
        while ((!task.isCancelled()) && (queue != null) && (!queue.isEmpty())) {
            LLog.test("queue: %s", queue.size());
            //Get loadable from stack without removing
            var loadable = queue.peek();
            loadable.load();//.await().ifError(ex -> LLog.error(this, "Can't finished loading of canvas", (Throwable) ex));
            //Remove loadable from stack
            queue.pop();
        }
        return null;
    }

}
