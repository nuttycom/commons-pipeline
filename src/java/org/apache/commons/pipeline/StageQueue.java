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

/**
 * Defines an abstract base class that connects a {@link StageHandler} to
 * a work queue implementation, with methods to start and stop the
 * execution of the worker threads.
 *  
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision: 1.1 $
 */
public abstract class StageQueue { 
    
    /**
     * Holds value of property stageHandler.
     */
    protected StageHandler stageHandler;
    
    /**
     * Default constructor (StageHandler uninitialized)
     */
    public StageQueue() { }
    
    /**
     * Creates a StageQueue with the specified StageHandler.
     */
    public StageQueue(StageHandler stageHandler){
        this.stageHandler = stageHandler;
    }
        
    /**
     * Adds an object to the tail of the queue.
     */
    public abstract void enqueue(Object obj);
    
    /**
     * Creates and starts new worker thread(s) to process items in the queue.
     */
    public abstract void start() throws IllegalThreadStateException;
    
    /**
     * This method waits for the queue to empty and any processor thread(s) to exit
     * cleanly and then calls release() to release any resources acquired during processing, if possible.
     * Implementations of this method must block until all items have been removed
     * from the queue.
     */
    public abstract void finish() throws InterruptedException;
    
    
    /**
     * Returns the status of the thread(s) that process data from the queue.
     */
    public abstract boolean isRunning(); 
    
    /**
     * Getter for property stageHandler.
     * @return Value of property stageHandler.
     */
    public StageHandler getStageHandler() {
        return this.stageHandler;
    }    
    
    /**
     * Setter for property stageHandler.
     * @param stageHandler New value of property stageHandler.
     */
    public void setStageHandler(StageHandler stageHandler) {
        this.stageHandler = stageHandler;
    }  
}
