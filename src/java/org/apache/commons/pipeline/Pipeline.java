/*
 * Copyright 2004 The Apache Software Foundation
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

import java.lang.Iterable;
import java.util.*;
import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.OrderedMapIterator;
import org.apache.commons.collections.map.ListOrderedMap;



/**
 * This class represents a processing system consisting of a number of stages
 * and branches. Each stage contains a queue and manages one or more threads
 * that process data in that stage's queue and allow processed data to be
 * passed along to subsequent stages and onto branches of the pipeline.<P>
 *
 * This class allows all stages in the pipeline to be managed collectively
 * with methods to start and stop processing for all stages, as well as
 * a simple framework for asynchronous event-based communication between stages.
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision$
 */
public final class Pipeline implements Iterable<Stage>, Runnable {
    private List<StageEventListener> listeners = new ArrayList<StageEventListener>();

    /**
     * Ordered map of stages in the pipeline where the keys are stages and the
     * values are the associated StageDrivers.
     */
    protected OrderedMap stages = new ListOrderedMap();
    //private OrderedMap<Stage,StageDriver> stages = new ListOrderedMap<Stage,StageDriver>();
    
    /**
     * Map of pipeline branches where the keys are branch names.
     */
    protected Map<String,Pipeline> branches = new HashMap<String,Pipeline>();
    
    
    /**
     * Creates a new Pipeline
     */
    public Pipeline() {  }
    
    
    /**
     * Adds a Stage object to the end of this Pipeline. The pipeline will use
     * the specified StageDriver to run the stage.
     *
     * It is critical that all stages added to a pipeline have distinct hash codes
     * to maintain stage ordering integrity. For this reason, it is
     * strongly suggested that Stage implementations <i>do not</i> override
     * the default {@link java.lang.Object#hashCode() hashCode()} implementation
     * in java.lang.Object.
     *
     * @todo throw IllegalStateException if the stage is being used in a different pipeline
     */
    public void addStage(Stage stage, StageDriver driver) {
        if (stage == null) throw new IllegalArgumentException("Argument \"stage\" for call to Pipeline.addStage() may not be null.");
        if (driver == null) throw new IllegalArgumentException("Argument \"driver\" for call to Pipeline.addStage() may not be null.");
        
        stage.setPipeline(this);        
        this.stages.put(stage, driver);
    }
    
    
    /**
     * Returns the first stage in the pipeline
     */
    public Stage head() {
        return (Stage) stages.firstKey();
    }
    
    /**
     * Returns the stage after the specified stage in the pipeline
     */
    public Stage getNextStage(Stage stage) {
        return (Stage) stages.nextKey(stage);
    }
    
    /**
     * Returns an Iterator for stages in the pipeline.
     */
    public Iterator<Stage> iterator() {
        return (Iterator<Stage>) stages.mapIterator();
    }
    
    /**
     * Adds a branch to the pipeline.
     */
    public void addBranch(String key, Pipeline pipeline) {
        if (key == null) throw new IllegalArgumentException("Branch key may not be null.");
        if (pipeline == null) throw new IllegalArgumentException("Illegal attempt to set reference to null branch.");
        if (pipeline == this || this.hasBranch(pipeline))
            throw new IllegalArgumentException("Illegal attempt to set reference to self as a branch (infinite recursion potential)");
        
        this.branches.put(key, pipeline);
    }
    
    
    /**
     * Runs the pipeline from start to finish.
     */
    public void run() {
        try {
            start();
            finish();
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
    
    
    /**
     * This method iterates over the stages in the pipeline, looking up a {@link StageDriver}
     * for each stage and using that driver to start the stage. Startups
     * may occur sequentially or in parallel, depending upon the stage driver
     * used.
     */
    public void start() {
        for (OrderedMapIterator iter = stages.orderedMapIterator(); iter.hasNext();) {
            Stage stage = (Stage) iter.next();
            StageDriver driver = (StageDriver) iter.getValue();
            driver.start(stage);
        }
        
        for (Pipeline branch : branches.values()) {
            branch.start();
        }
    }
    
    
    /**
     * This method iterates over the stages in the pipeline, looking up a {@link StageDriver}
     * for each stage and using that driver to request that the stage finish
     * execution. The {@link StageDriver#finish(Stage)}
     * method will block until the stage's queue is exhausted, so this method
     * may be used to safely finalize all stages without the risk of
     * losing data in the queues.
     *
     * @throws InterruptedException if a worker thread was interrupted at the time
     * a stage was asked to finish execution.
     */
    public void finish() throws InterruptedException {
        for (OrderedMapIterator iter = stages.orderedMapIterator(); iter.hasNext();) {
            Stage stage = (Stage) iter.next();
            StageDriver driver = (StageDriver) iter.getValue();
            driver.finish(stage);
        }
        
        for (Pipeline pipeline : branches.values()) {
            pipeline.finish();
        }
    }
    
    
    /**
     * Enqueues an object on the first stage if the pipeline is not empty
     * @param o the object to enque
     */
    public void enqueue(Object o){
        if (!stages.isEmpty()) ((Stage) stages.firstKey()).enqueue(o);
    }
    
    
    /**
     * This method is used by stages to pass data from one stage to the next.
     */
    public void pass(Stage source, Object data) {
        Stage next = (Stage) this.stages.nextKey(source);
        if (next != null) next.enqueue(data);
    }
    
    
    /**
     * Simple method that recursively checks whether the specified
     * pipeline is a branch of this pipeline.
     */
    private boolean hasBranch(Pipeline pipeline) {
        if (branches.containsValue(pipeline)) return true;
        for (Pipeline branch : branches.values()) {
            if (branch.hasBranch(pipeline)) return true;
        }
        
        return false;
    }
    
    
    /**
     * Adds an EventListener to the pipline that will be notified by calls
     * to {@link Stage#raise(StageEvent)}.
     */
    public void addEventListener(StageEventListener listener) {
        listeners.add(listener);
    }
    
    
    /**
     * Sequentially notifies each listener in the list of an event, and propagates
     * the event to any attached branches
     */
    public void notifyListeners(final java.util.EventObject ev) {
        new Thread() {
            public void run() {
                for (Iterator iter = listeners.iterator(); iter.hasNext();) {
                    ((StageEventListener) iter.next()).notify(ev);
                }
                
                for (Iterator iter = branches.values().iterator(); iter.hasNext();) {
                    ((Pipeline) iter.next()).notifyListeners(ev);
                }
            }
        }.start();
    }
}