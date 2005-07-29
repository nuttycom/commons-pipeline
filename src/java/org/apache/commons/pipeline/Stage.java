/*
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.pipeline;

import java.util.Queue;

/**
 * <P>A Stage represents a set of tasks that can be performed on objects
 * in a queue, and methods used to communicate with other stages
 * in a {@link Pipeline}.</P>
 * <P>A Stage must provide a unique {@link StageMonitor} object to allow for
 * proper handling of multiple processing threads to the {@link StageDriver}
 * that runs the stage. Because Stage does not specify the exact behavior of the
 * queue (whether it is capacity-bounded or automatically synchronizes accesses,
 * etc) the monitor is necessary to provide proper synchronization.</P>
 * <P>Stages extending this abstract base class automatically establish a relationship
 * with a pipeline when added to that pipeline.</P>
 */
public abstract class Stage {
    
    /**
     * The {@link Pipeline} in which this stage will run.
     */
    protected Pipeline pipeline;
    
    /**
     * The {@link StageMonitor} that will monitor stage changes and act
     * as an intermediary between this stage and its {@link StageDriver}.
     */
    protected StageMonitor monitor;
    
    // StageDriver that is used to run this stage
    private StageDriver stageDriver;
    
    // Queue for objects this stage is to process
    private Queue<Object> queue;
    
    /**
     * Builds a new stage with the specified queue.
     */
    public Stage(Queue<Object> queue) {
        this.queue = queue;
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
     * Enqueues an object on the wrapped queue. Classes wishing to override this
     * method should override {@link #innerEnqueue(Object)} instead.
     */
    public final void enqueue(Object obj) {
        this.innerEnqueue(obj);
        if (this.monitor != null) this.monitor.enqueueOccurred();
    }
    
    /**
     * This protected method is designed to be overridden in cases where
     * additional processing should be performed when an object is enqueued.
     * Classes that override this method must also override {@link #poll()} if
     * the underlying queue supporting the stage is changed.
     */
    protected void innerEnqueue(Object obj) {
        queue.add(obj);
    }
    
    /**
     * Retrieves an object from the head of the wrapped queue, or null
     * if the queue is empty. Classes that override this method must also
     * override {@link #innerEnqueue(Object)} if the underlying queue supporting the
     * stage is changed.
     */
    public Object poll() {
        synchronized (queue) {
            return queue.poll();
        }
    }
    
    /**
     * Setter for property stageDriver.
     * @param stageDriver New value of property stageDriver.
     */
    protected final void setStageDriver(StageDriver stageDriver) {
        this.stageDriver = stageDriver;
        this.monitor = stageDriver.createStageMonitor(this);
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
        Pipeline branch = this.pipeline.branches.get(key);
        if (branch != null && !branch.stages.isEmpty()) {
            branch.head().enqueue(obj);
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
    public final Queue getQueue() {
        return this.queue;
    }
    
    /**
     * Getter for property stageDriver.
     * @return Value of property stageDriver.
     */
    public final StageDriver getStageDriver() {
        return this.stageDriver;
    }
    
    /**
     * Returns the monitor for this stage.
     */
    public final StageMonitor getMonitor() {
        return this.monitor;
    }
    
    /**
     * Stages may not further override hashCode(). This is necessary to maintain stage
     * ordering integrity within the pipeline.
     */
    public final int hashCode() {
        int retValue;
        
        retValue = super.hashCode();
        return retValue;
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
    
}