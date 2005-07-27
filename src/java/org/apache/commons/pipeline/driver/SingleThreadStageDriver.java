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
 * @version $Revision$
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
     * Default constructor. This sets the thread wait timeout and fault tolerance
     * values to 500 ms and false, respectively.
     */
    public SingleThreadStageDriver() {
        this(500, false);
    }
    
    /**
     * Creates a new SingleThreadStageDriver with the specified thread wait
     * timeout and fault tolerance values.
     * 
     * @param timeout The amount of time, in milliseconds, that the worker thread
     * will wait before checking the processing state if no objects are available
     * in the thread's queue.
     * @param faultTolerant Flag determining the behavior of the driver when
     * an error is encountered in execution of {@link Stage#process(Object)}. 
     * If this is set to false, any exception thrown during {@link Stage#process(Object)}
     * will cause the worker thread to halt without executing {@link Stage#postprocess()}
     * ({@link Stage#release()} will be called.)
     */
    public SingleThreadStageDriver(long timeout, boolean faultTolerant) {
        this.timeout = timeout;
        this.faultTolerant = faultTolerant;
    }
    
    /**
     * Creates and starts a new worker thread to process items in the stage's queue.
     */
    protected void startInternal(Stage stage) throws StageException {
        log.debug("Starting worker thread for stage " + stage + ".");
        Thread workerThread = new WorkerThread(stage);
        workerThread.start();
        this.threadMap.put(stage, workerThread);
        log.debug("Worker thread for stage " + stage + " started.");
    }
    
    /**
     * This method waits for the queue to empty and the processor thread to exit
     * cleanly and release any resources acquired during processing, if possible.
     */
    protected void finishInternal(Stage stage) throws StageException {
        log.debug("Waiting for worker thread stop for stage " + stage + ".");
        try {
            this.threadMap.remove(stage).join();
        } catch (InterruptedException e){
            throw new StageException(e);
        }
        log.debug("Worker thread for stage " + stage + " has finished.");
        stage.getMonitor().driverStopped();
    }
    
    /**
     * Sets the failure tolerance flag for the worker thread. If faultTolerant
     * is set to true, {@link StageException StageException}s thrown by
     * the {@link Stage#process(Object)} method will not interrupt queue 
     * processing, but will simply be logged with a severity of ERROR.
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
     * Creates a new StageMonitor for the specified stage.
     */
    protected StageMonitor createStageMonitor(Stage stage) {
        return new AbstractStageMonitor(stage) {
            /**
             * StageDriver has been requested to start stage processing.
             * Implementations of this method should change the monitor's state to
             * {@link State#STARTING}.
             */
            public synchronized void startRequested() {
                if (this.state == State.STOPPED) this.state = State.STARTING;
            }
            
            /**
             * StageDriver has started execution.
             * Implementations of this method should change the monitor's state to
             * {@link State#RUNNING}.
             */
            public synchronized void driverStarted() {
                if (this.state == State.STOPPED || this.state == State.STARTING) this.state = State.RUNNING;
            }
            
            /**
             * StageDriver has been requested to halt stage processing.
             * Implementations of this method should change the monitor's state to
             * {@link State#STOPPING}.
             */
            public synchronized void stopRequested() {
                this.state = State.STOP_REQUESTED;
                this.notifyAll();
            }
            
            /**
             * StageDriver has finished execution.
             * Implementations of this method should change the monitor's state to
             * {@link State#STOPPED}.
             */
            public synchronized void driverStopped() {
                this.state = State.STOPPED;
            }
            
            /**
             * Monitor for successful enqueue operations on the stage. Implementations
             * overriding this method must call {@link Object#notifyAll() this.notifyAll()} to
             * ensure that any threads waiting on this monitor are notified.
             */
            public synchronized void enqueueOccurred() {
                this.notifyAll();
            }
            
            /**
             * Returns the current state of stage processing.
             */
            public synchronized State getState() {
                return this.state;
            }
            
            /**
             * Returns a list of errors recorded by this monitor
             */
            public List<Throwable> getErrors() {
                return errors;
            }
        };
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
                log.debug("preprocessing stage " + stage + "...");
                try {
                    stage.preprocess();
                } catch (Exception t) {
                    monitor.preprocessFailed(t);
                    return;
                }
                
                monitor.driverStarted();
                
                running: while (true) {
                    Object obj = null;
                    try {
                        obj = stage.poll();
                        if (obj == null) {
                            synchronized (monitor) {
                                //log.debug("Monitor getState is: " + monitor.getState());
                                if (monitor.getState() == StageMonitor.State.STOP_REQUESTED
                                        || monitor.getState() == StageMonitor.State.ERROR) break running;
                                
                                monitor.wait(timeout);
                                continue running;
                            }
                        } else {
                            stage.process(obj);
                        }
                    } catch (InterruptedException e) {
                        monitor.driverFailed(e);
                        throw new RuntimeException("Driver thread unexpectedly interrupted for stage " + stage + ".", e);
                    } catch (Exception t) {
                        monitor.processingFailed(obj, t);
                        if (!faultTolerant) {
                            log.error("Aborting due to error.", t);
                            throw new RuntimeException("Stage processing for stage " + stage + " failed, check monitor for details.", t);
                        }
                    }
                }
                
                log.debug("postprocessing stage " + stage + "...");
                try {
                    stage.postprocess();
                } catch (Exception t) {
                    monitor.postprocessFailed(t);
                    return;
                }
            } finally {
                stage.release();
            }
        }
    }
}
