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

import java.util.*;


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
 * @version $Revision: 1.1 $
 */
public final class Pipeline {
    private List stages = new LinkedList();
    private List listeners = new ArrayList();
    private Map branches = new HashMap();
    
    
    /** Creates a new Pipeline */
    public Pipeline() {  }
    
    
    /**
     * Adds a Stage object to the end of this Pipeline.
     *
     * @todo throw IllegalStateException if the stage is being used in a different pipeline
     */
    public final void addStage(Stage stage) {
        if (!stages.isEmpty()) ((Stage) stages.get(stages.size() - 1)).next = stage;
        stage.pipeline = this;
        stages.add(stage);
    }
    
    
    /**
     * Adds a branch to the pipeline.
     */
    public final void addBranch(String key, Pipeline pipeline) {
        if (key == null) throw new IllegalArgumentException("Branch key may not be null.");
        if (pipeline == null) throw new IllegalArgumentException("Illegal attempt to set reference to null branch.");
        if (pipeline == this) throw new IllegalArgumentException("Illegal attempt to set reference to self as a branch (infinite recursion potential)");
        
        this.branches.put(key, pipeline);
    }
    
    
    /**
     * Calls {@link StageQueue#start() start()} on
     * the {@link StageQueue} of each stage in the pipeline in the order they were added.
     */
    public final void start() {
        if (!stages.isEmpty()) ((Stage) stages.get(0)).startAll();
        for (Iterator iter = branches.values().iterator(); iter.hasNext();) ((Pipeline) iter.next()).start();
    }
    
    
    /**
     * Calls {@link StageQueue#finish() finish()} 
     * on the {@link StageQueue} of each stage in the order they were added to the pipeline. 
     * The {@link StageQueue#finish() finish()}
     * method blocks until the stage's queue is exhausted, so this method
     * may be used to safely finalize all stages without the risk of
     * losing data in the queues.
     *
     * @throws InterruptedException if a worker thread was interrupted at the time
     * a stage was asked to finish execution.
     */
    public final void finish() throws InterruptedException {
        if (!stages.isEmpty()) ((Stage) stages.get(0)).finishAll();
        for (Iterator iter = branches.values().iterator(); iter.hasNext();) ((Pipeline) iter.next()).finish();
    }
    
    
    /**
     * Enqueues an object on the first stage if the pipeline is not empty
     * @param o the object to enque
     */
    public final void enqueue(Object o){
        if (!stages.isEmpty()) ((Stage) stages.get(0)).enqueue(o);
    }
    
    
    /**
     * Adds an EventListener to the pipline that will be notified by calls
     * to {@link Stage#raise(StageEvent)}.
     */
    public final void addEventListener(StageEventListener listener) {
        listeners.add(listener);
    }
    
    
    /**
     * Sequentially notifies each listener in the list of an event, and propagates
     * the event to any attached branches
     */
    private void notifyListeners(final StageEvent ev) {
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
    
        
    /**
     * This abstract base class provides a foundation for processing stages in 
     * the pipeline.
     *
     * @todo This should probably be a non-static inner class so that
     * we can avoid the absolute reference to the enclosing Pipeline if somebody
     * can figure out how to properly handle the constructor using Digester.
     */
    public static abstract class Stage implements StageHandler {
        private StageQueue queue;
        private Pipeline pipeline;
        private Stage next;
        
        
        /** Builds a new stage that wraps the specified StageQueue */
        public Stage(StageQueue queue) {
            queue.setStageHandler(this);
            this.queue = queue;
        }
        
        
        /**
         * This method recursively starts each element in the process chain in sequence.
         */
        private final void startAll() throws IllegalThreadStateException {
            System.out.println("Starting " + this.getClass().getName());
            queue.start();
            if (next != null) next.startAll();
        }
        
        
        /**
         * Calls the finish() method on the wrapped worker queue (which waits for the
         * worker thread(s) to die) then calls the next chain element's finishAll() method.
         * This method attempts to finish all threads even if exceptions are thrown.
         */
        private final void finishAll() throws InterruptedException {
            try {
                queue.finish();
            }
            finally {
                if (next != null) next.finishAll();
            }
        }
        
        
        /**
         * Delegate method of the wrapped {@link StageQueue}.
         */
        public void enqueue(Object obj) {
            queue.enqueue(obj);
        }
        
        
        /**
         * Enqueues the specified object onto the next stage in the pipeline
         * if such a stage exists.
         */
        public void exqueue(Object obj) {
            if (this.next != null) this.next.enqueue(obj);
        }
        
        
        /**
         * Enqueues the specified object onto the first stage in the pipeline 
         * branch corresponding to the specified key, if such a brach exists.
         */
        public void exqueue(String key, Object obj) {
            Pipeline branch = (Pipeline) this.pipeline.branches.get(key);
            if (branch != null && !branch.stages.isEmpty()) {
                ((Stage) branch.stages.get(0)).enqueue(obj);
            }
        }
        
        
        /**
         * Raises an event on the pipeline. Any listeners registered with the pipeline
         * will be notified.
         */
        public final void raise(StageEvent ev) {
            this.pipeline.notifyListeners(ev);
        }
        
        /** Do nothing default implementation */
        public void process(Object obj) {
        }
        
        /** Do nothing default implementation */
        public void release() {
        }
        
        /** Do nothing default implementation */
        public void postprocess() {
        }
        
        /** Do nothing default implementation */
        public void preprocess() {
        }        
    }
}