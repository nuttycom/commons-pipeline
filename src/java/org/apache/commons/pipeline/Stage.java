/*
 * Stage.java
 *
 * Created on November 18, 2004, 10:34 AM
 */

package org.apache.commons.pipeline;

import java.util.*;

/**
 * <P>
 * A Stage represents a set of tasks that can be performed on objects
 * in a queue, and methods used to communicate with other stages
 * in a {@link Pipeline}.
 * </P>
 * <P>
 * A Stage must provide a unique {@link StageMonitor} object to allow for
 * proper handling of multiple processing threads to the {@link StageDriver}
 * that runs the stage. Because Stage does not specify the exact behavior of the
 * queue (whether it is capacity-bounded or automatically synchronizes accesses,
 * etc) the monitor is necessary to provide proper synchronization.
 * </P>
 * <P>
 * Stages extending this abstract base class automatically establish a relationship
 * with a pipeline when added to that pipeline.
 * </P>
 *
 * @author  Kris Nuttycombe
 */
public abstract class Stage {
    private Queue<Object> queue;
    protected Pipeline pipeline;
    protected StageMonitor monitor;
    
    /**
     * Builds a new stage with the specified queue.
     */
    public Stage(Queue<Object> queue) {
        this.queue = queue;
        this.monitor =  new StageMonitor();
    }
    
    /**
     * Default implementation of setPipeline. This method may be overridden
     * to provide additional initialization functions for when the stage
     * is added to a pipeline.
     */
    protected void setPipeline(Pipeline pipeline) {
        if (this.pipeline != null) throw new IllegalStateException("A pipeline has already been associated with this stage.");
        this.pipeline = pipeline;
    }
    
    /**
     * Enqueues an object on the wrapped queue. Classes that override this
     * method must also override {@link #poll()}.
     */
    public void enqueue(Object obj) {
        queue.add(obj);
        this.monitor.enqueueOccurred();
    }
    
    /**
     * Retrieves an object from the head of the wrapped queue, or null
     * if the queue is empty. Classes that override this method must also
     * override {@link #enqueue(Object)}
     */
    public Object poll() {
        synchronized (queue) {
            return queue.poll();
        }
    }
    
    /**
     * Enqueues the specified object onto the next stage in the pipeline
     * if such a stage exists.
     */
    public final void exqueue(Object obj) {
        this.pipeline.pass(this, obj);
    }
    
    /**
     * Enqueues the specified object onto the first stage in the pipeline
     * branch corresponding to the specified key, if such a brach exists.
     */
    public final void exqueue(String key, Object obj) {
        Pipeline branch = (Pipeline) this.pipeline.branches.get(key);
        if (branch != null && !branch.stages.isEmpty()) {
            ((Stage) branch.stages.firstKey()).enqueue(obj);
        }
    }
    
    /**
     * Raises an event on the pipeline. Any listeners registered with the pipeline
     * will be notified.
     */
    public final void raise(java.util.EventObject ev) {
        this.pipeline.notifyListeners(ev);
    }
    
    /**
     * Getter for wrapped queue.
     * @return Value of property queue.
     */
    public Queue getQueue() {
        return this.queue;
    }
    
    /**
     * Setter for wrapped queue.
     * @param queue New value of property queue.
     */
    public void setQueue(Queue<Object> queue) {
        this.queue = queue;
    }
    
    /**
     * Returns the monitor for this stage.
     */
    public StageMonitor getMonitor() {
        return this.monitor;
    }
    
    /**
     * Implementations of this method should perform any necessary setup that
     * needs to be done before any data is processed from this Stage's queue.
     *
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public abstract void preprocess() throws StageException;
    
    /**
     * Implementations of this method should atomically process a single data
     * object.
     *
     * @throws ClassCastException if the object is of an incorrect type
     * for the processing operation
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public abstract void process(Object obj) throws StageException;
    
    /**
     * Implementations of this method should do any additional processing or
     * finalization necessary after all data has been processed. This method
     * usually runs following a call to the implementing {@link org.apache.commons.pipeline.Pipeline$Stage Stage}'s
     * {@link StageQueue#finish()} method.
     *
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public abstract void postprocess() throws StageException;
    
    /**
     * Implementations of this method should clean up any lingering resources
     * that might otherwise be left allocated if an exception is thrown during
     * processing.
     */
    public abstract void release();
    
    /**
     * Stages may not further override hashCode(). This is necessary to maintain stage
     * ordering integrity within the pipeline.
     */
    public final int hashCode() {
        int retValue;
        
        retValue = super.hashCode();
        return retValue;
    }
}