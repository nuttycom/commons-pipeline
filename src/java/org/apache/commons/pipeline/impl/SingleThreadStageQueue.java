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

package org.apache.commons.pipeline.impl;

import java.util.LinkedList;
import java.util.List;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.StageQueue;
import org.apache.log4j.Logger;

/**
 * This is a simple implementation of a work queue, based upon the example given 
 * in Effective Java, by Joshua Bloch &copyright; Sun Microsystems 2001, Pg 196.
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision: 1.1 $
 */
public class SingleThreadStageQueue extends StageQueue {
    private Logger log = Logger.getLogger(this.getClass());
    
    private final List queue = new LinkedList();
    private volatile boolean failureTolerant = true;
    private volatile boolean running = false;
    private WorkerThread workerThread;
    
    
    /**
     * Default constructor
     */
    public SingleThreadStageQueue() {
    }
    
    
    /**
     * Add an object to the tail of the queue.
     */
    public final void enqueue(Object obj) {
        synchronized (queue) {
            queue.add(obj);
            queue.notify();
        }
    }
    
    
    /**
     * Creates and starts a new worker thread to process items in the queue.
     */
    public final void start() throws IllegalThreadStateException {
        if (running) {
            throw new IllegalThreadStateException("Processor thread has already been started.");
        }
        else {
            this.running = true;
            this.workerThread = new WorkerThread();
            
            log.debug("Starting worker thread.");
            this.workerThread.start();
        }
    }
    
    
    /**
     * This method waits for the queue to empty and the processor thread to exit 
     * cleanly and release any resources acquired during processing, if possible.
     */
    public void finish() throws InterruptedException {
        log.debug("Finishing processing...");
        synchronized (queue) {
            this.running = false;
            queue.notify();
        }
        
        this.workerThread.join();
        log.debug("Finished; worker thread stopped.");
    }
    
    
    /**
     * Returns the status of the loop that writes data from the queue.
     */
    public final boolean isRunning() {
        return this.running;
    }
    
    
    /**
     * This worker thread removes and processes data objects from the incoming
     * queue. It first calls preprocess(), then begins a loop that calls the process()
     * method to process data from the queue. This loop runs as long as the
     * {@link getRunning() running} property is true or the queue is not empty. To break the loop the
     * calling code must run the writer's finish() method to set the running property to false.
     * At this point the loop will continue to run until the queue is empty, then the loop will
     * exit and the postprocess() method is called.<P>
     *
     * @throws StageException if an error is encountered during data processing
     * and failureTolerant is set to false.
     */
    private class WorkerThread extends Thread {
        public final void run() {
            try {
                log.debug("preprocessing...");
                SingleThreadStageQueue.this.stageHandler.preprocess();
                
                while (true) {
                    Object obj = null;
                    synchronized (queue) {
                        while (running && queue.isEmpty()) queue.wait();
                        
                        if (!running && queue.isEmpty()) break;
                        obj = queue.remove(0);
                    }
                    
                    try {
                        //if (log.isDebugEnabled()) log.debug("Running method \"process()\" on object " + obj.toString());
                        SingleThreadStageQueue.this.stageHandler.process(obj);
                    }
                    catch (StageException e) {
                        if (!failureTolerant) throw e;
                        log.error(e.getMessage(), e);
                    }
                }
                
                log.debug("postprocessing...");
                SingleThreadStageQueue.this.stageHandler.postprocess();
            }
            catch (InterruptedException e) {
                log.fatal(e.getMessage(), e);
            }
            catch (StageException e) {
                log.fatal(e.getMessage(), e);
                throw e;
            }
            finally {
                running = false;
                SingleThreadStageQueue.this.stageHandler.release();
            }
        }
    }
        
    
    /**
     * Sets the failure tolerance flag for the worker thread. If failureTolerant
     * is set to true, {@link StageException StageException}s thrown by 
     * the process() method will not interrupt queue processing, but will simply 
     * be logged with a severity of ERROR. 
     */
    public final void setFailureTolerant(boolean failureTolerant) {
        this.failureTolerant = failureTolerant;
    }
    
    
    /**
     * Getter for property failureTolerant.
     * @return Value of property failureTolerant.
     */
    public boolean isFailureTolerant() {
        return this.failureTolerant;
    }
}
