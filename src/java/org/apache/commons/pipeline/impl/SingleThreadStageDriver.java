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

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageMonitor;
import org.apache.commons.pipeline.Stage;
import org.apache.log4j.Logger;
import java.util.*;

/**
 * This is a very simple implementation of a StageDriver which spawns
 * only a single thread to process a stage. A SingleThreadStageDriver
 * may not be used to manipulate more than a single stage.
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision: 1.1 $
 */
public class SingleThreadStageDriver implements StageDriver {
    private Logger log = Logger.getLogger(this.getClass());
    
    //wait timeout to ensure deadlock cannot occur on thread termination
    private long timeout = 500;
    
    //flag describing whether or not the driver is fault tolerant
    private boolean faultTolerant = false;
    
    //map of stages to worker threads
    private Map<Stage, Thread> threadMap = new HashMap<Stage,Thread>();
        
    /**
     * Default constructor
     */
    public SingleThreadStageDriver() {
    }
    
    /**
     *
     */
    public SingleThreadStageDriver(long timeout, boolean faultTolerant) {
        this.timeout = timeout;
        this.faultTolerant = faultTolerant;
    }
        
    /**
     * Creates and starts a new worker thread to process items in the stage's queue.
     */
    public final void start(Stage stage) throws IllegalThreadStateException {
        StageMonitor monitor = stage.getMonitor();
        synchronized (monitor) {
            if (monitor.status() != StageMonitor.Status.STOPPED) {
                throw new IllegalThreadStateException("Processor thread has already been started.");
            }
        }
        
        monitor.startRequested();
        
        log.debug("Starting worker thread.");
        Thread workerThread = new WorkerThread(stage);
        workerThread.start();
        this.threadMap.put(stage, workerThread);
        log.debug("Worker thread started.");
    }
    
    
    /**
     * This method waits for the queue to empty and the processor thread to exit
     * cleanly and release any resources acquired during processing, if possible.
     */
    public void finish(Stage stage) throws InterruptedException {
        StageMonitor monitor = stage.getMonitor();
        
        log.debug("Requesting worker thread stop.");
        monitor.stopRequested();
        
        synchronized (monitor) {
            if (monitor.status() == StageMonitor.Status.STOPPED) return;
        }
        
        log.debug("Waiting for worker thread stop.");
        this.threadMap.remove(stage).join();
        log.debug("Worker thread has finished.");
        
        monitor.driverStopped();
    }
    
    
    /**
     * Sets the failure tolerance flag for the worker thread. If faultTolerant
     * is set to true, {@link StageException StageException}s thrown by
     * the process() method will not interrupt queue processing, but will simply
     * be logged with a severity of ERROR.
     */
    public final void setFaultTolerant(boolean faultTolerant) {
        this.faultTolerant = faultTolerant;
    }
    
    /**
     * Getter for property faultTolerant.
     * @return Value of property faultTolerant.
     */
    public boolean isFaultTolerant() {
        return this.faultTolerant;
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
     * and faultTolerant is set to false.
     */
    private class WorkerThread extends Thread {
        /** The Stage this thread will work on */
        private Stage stage;               
        
        public WorkerThread(Stage stage) {
            this.stage = stage;
        }
        
        public final void run() {
            StageMonitor monitor = stage.getMonitor();
            try {
                log.debug("preprocessing...");
                stage.preprocess();
                
                monitor.driverStarted();
                
                running: while (true) {
                    Object obj = null;
                    try {
                        obj = stage.poll();
                        if (obj == null) {
                            synchronized (monitor) {
                                log.debug("Monitor status is: " + monitor.status());
                                if (monitor.status() == StageMonitor.Status.STOP_REQUESTED) break running;
                                monitor.wait(timeout);
                                continue running;
                            }
                        }
                        else {
                            stage.process(obj);
                        }
                    }
                    catch (InterruptedException e) {
                        monitor.driverFailed(e);
                        throw new RuntimeException("Driver thread unexpectedly interrupted.", e);
                    }
                    catch (Throwable t) {
                        monitor.processingFailed(obj, t);
                        if (!faultTolerant) {
                            log.error("Aborting due to error.", t);
                            throw new RuntimeException("Stage processing failed, check monitor for details.", t);
                        }
                    }
                }
                
                log.debug("postprocessing...");
                stage.postprocess();
            }
            finally {
                stage.release();
            }
        }
    }
}
