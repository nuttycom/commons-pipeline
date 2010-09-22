/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.pipeline.driver.control;

import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.driver.*;
import org.apache.commons.pipeline.util.BlockingQueueFactory;
import static org.apache.commons.pipeline.StageDriver.State.*;
import static org.apache.commons.pipeline.driver.FaultTolerance.*;

public class BalancedPoolStageDriver extends AbstractPrioritizableStageDriver {
    public enum Runnability {RUNNABLE, STOPPABLE, NOT_RUNNABLE};
    
    private Log log = LogFactory.getLog(BalancedPoolStageDriver.class);
    
    //signal telling threads to start polling queue
    private final CountDownLatch startSignal;
    
    //counter for worker identity assignment
    private int nextWorkerId = 0;
    
    //queue of worker instances - each is associated with a running thread
    private final Queue<BalancedWorker> workers = new ConcurrentLinkedQueue<BalancedWorker>();
    
    //number of threads to start initially
    private int initialThreads;
    
    //wait timeout to ensure deadlock cannot occur on thread termination
    private long timeout;
    
    //units for timeout wait
    private TimeUnit timeoutTimeUnit;
    
    //feeder instance for the stage
    private final SwitchingFeeder feeder;
    
    /**
     * This Feeder implementation will switch between synchronous and multithreaded
     * processing depending upon how many worker threads are available.
     */
    private class SwitchingFeeder implements Feeder {
        //queue to hold data to be processed.
        private final BlockingQueue queue;
        
        public SwitchingFeeder(BlockingQueue queue) {
            this.queue = queue;
        }
        
        public void feed(Object obj) {
            synchronized(BalancedPoolStageDriver.this) {
                if (    !isInState(RUNNING, STOP_REQUESTED)
                        || workers.size() > 1
                        || (workers.size() == 1 && workers.peek().runnability == Runnability.RUNNABLE)) {
                    try {
                        if (log.isDebugEnabled()) log.debug(stage + ": Queueing object: " + obj);
                        this.queue.put(obj);
                    } catch (InterruptedException e) {
                        throw new Error("Assertion failure: thread interrupted while attempting to enqueue data object.", e);
                    }
                    
                    return; //short circuit out of here
                }
            }
            
            try {
                if (log.isDebugEnabled()) log.debug(stage + ":Processing object directly: " + obj);
                BalancedPoolStageDriver.this.process(obj);
            } catch (StageException e) {
                recordProcessingException(obj, e);
                if (faultTolerance == FaultTolerance.NONE) throw fatalError(e);
            }
        }
    };
    
    /**
     * This StageDriver implementation runs stage processing in a pool of threads
     *
     */
    public BalancedPoolStageDriver(Stage stage, StageContext context, BlockingQueueFactory queueFactory,
            int initialThreads, FaultTolerance faultTolerance,
            long timeout, TimeUnit timeoutTimeUnit) {
        super(stage, context, faultTolerance);
        
        this.feeder = new SwitchingFeeder(queueFactory.createQueue());
        this.startSignal = new CountDownLatch(1);
        this.initialThreads = initialThreads;
        this.timeout = timeout;
        this.timeoutTimeUnit = timeoutTimeUnit;
    }
    
    /**
     * Accessor method for this stage's Feeder.
     * @return the Feeder for this stage.
     */
    public Feeder getFeeder() {
        return this.feeder;
    }
    
    public void start() throws StageException {
        if (this.currentState == STOPPED) {
            setState(STARTED);
            
            if (log.isDebugEnabled()) log.debug("Preprocessing stage " + stage + "...");
            stage.preprocess();
            if (log.isDebugEnabled()) log.debug("Preprocessing for stage " + stage + " complete.");
            
            log.debug("Starting worker threads for stage " + stage + ".");
            this.addWorkers(initialThreads);
            
            // let threads know they can start
            testAndSetState(STARTED, RUNNING);
            startSignal.countDown();
            
            log.debug("Worker threads for stage " + stage + " started.");
        } else {
            throw new IllegalStateException("Attempt to start driver in state " + this.currentState);
        }
        
    }
    
    public void finish() throws StageException {
        if (this.currentState == STOPPED) {
            throw new IllegalStateException("The driver is not currently running.");
        }
        
        try {
            //it may be the case that finish() is called when the driver is still in the process
            //of starting up, so it is necessary to wait to enter the running state before
            //a stop can be requested
            while ( !(this.currentState == RUNNING || this.currentState == ERROR) ) this.wait(this.timeout);
            
            //ask the worker threads to shut down
            testAndSetState(RUNNING, STOP_REQUESTED);
            
            if (log.isDebugEnabled()) log.debug("Waiting for worker threads to stop for stage " + stage + ".");
            while (!workers.isEmpty()) {
                BalancedWorker worker = workers.remove();
                worker.awaitCompletion();
            }
            if (log.isDebugEnabled()) log.debug("Worker threads for stage " + stage + " halted");
            
            //transition into finished state (not used internally?)
            testAndSetState(STOP_REQUESTED, FINISHED);
            
            //do not run postprocessing if the driver is in an error state
            if (this.currentState != ERROR) {
                if (log.isDebugEnabled()) log.debug("Postprocessing stage " + stage + "...");
                this.stage.postprocess();
                if (log.isDebugEnabled()) log.debug("Postprocessing for stage " + stage + " complete.");
            }
        } catch (StageException e) {
            log.error("An error occurred during postprocessing of stage " + stage , e);
            recordFatalError(e);
            setState(ERROR);
        } catch (InterruptedException e) {
            throw new StageException(this.getStage(), "StageDriver unexpectedly interrupted while waiting for shutdown of worker threads.", e);
        } finally {
            if (log.isDebugEnabled()) log.debug("Releasing resources for stage " + stage + "...");
            stage.release();
            if (log.isDebugEnabled()) log.debug("Stage " + stage + " released.");
        }
        
        testAndSetState(FINISHED, STOPPED);
    }
    
    /**
     * This method obtains a lock to set the current state of processing
     * to error, records the error and returns a RuntimeException encapsulating
     * the specified throwable.
     */
    private RuntimeException fatalError(Throwable t) {
        try {
            setState(ERROR);
            this.recordFatalError(t);
            stage.release();
            this.notifyAll();
        } catch (Exception e) {
            this.recordFatalError(e);
        }
        
        return new RuntimeException("Fatal error halted processing of stage: " + stage);
    }
    
    /**
     *
     */
    private synchronized void addWorkers(int count) {
        while (count-- > 0) {
            BalancedWorker worker = new BalancedWorker(nextWorkerId, this.feeder.queue);
            Thread workerThread = new Thread(worker);
            workers.add(worker);
            nextWorkerId++;
            workerThread.start();
        }
    }
    
    /**
     *
     */
    private synchronized void removeWorkers(int count) throws InterruptedException {
        while (count-- > 0 && !workers.isEmpty()) {
            if (workers.size() > 1) {
            BalancedWorker worker = workers.remove();
                worker.deactivate(false);
            worker.awaitCompletion();
            } else {
                BalancedWorker worker = workers.peek();
                worker.deactivate(true);
            }
        }
    }
    
    /**
     * Increases the priority of the managed stage by increasing the number of
     * threads in which the stage is running.
     */
    public void increasePriority(double amount) {
        this.addWorkers((int) amount);
    }
    
    /**
     * Decreases the priority of the managed stage by decreasing the number of
     * threads in which the stage is running.
     */
    public void decreasePriority(double amount) {
        try {
            this.removeWorkers((int) amount);
        } catch (InterruptedException e) {
            throw new Error("Assertion failure: interrupted while awaiting worker thread stop for pool size reduction.", e);
        }
    }
    
    public double getPriority()
    {
        return getWorkerCount();
    }
    
    /**
     * Return the nuber of worker threads currently allocated.
     */
    public synchronized int getWorkerCount() {
        return this.workers.size();
    }
    
    /**
     * The worker thread
     */
    private class BalancedWorker implements Runnable {
        private volatile Runnability runnability = Runnability.RUNNABLE;
        private final int workerId;
        private final BlockingQueue queue;
        private final CountDownLatch doneSignal;
        
        public BalancedWorker(int workerId, BlockingQueue queue) {
            this.workerId  = workerId;
            this.queue = queue;
            this.doneSignal = new CountDownLatch(1);
        }
        
        public void run() {
            try {
                BalancedPoolStageDriver.this.startSignal.await();
                
                running: while (runnability != Runnability.NOT_RUNNABLE && currentState != ERROR) {
                    try {
                        Object obj = queue.poll(timeout, TimeUnit.MILLISECONDS);
                        if (obj == null) {
                            if (currentState == STOP_REQUESTED || runnability == Runnability.STOPPABLE) break running;
                            //else continue running;
                        } else {
                            try {
                                if (log.isDebugEnabled()) log.debug(stage + ": processing asynchronously: " + obj);
                                BalancedPoolStageDriver.this.process(obj);
                            } catch (StageException e) {
                                recordProcessingException(obj, e);
                                if (faultTolerance == NONE) throw e;
                            } catch (RuntimeException e) {
                                recordProcessingException(obj, e);
                                if (faultTolerance == CHECKED || faultTolerance == NONE) throw e;
                            }
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException("Worker thread " + this.workerId + " unexpectedly interrupted while waiting on data for stage " + stage, e);
                    }
                }
            } catch (StageException e) {
                log.error("An error occurred in the stage " + stage + " (workerID: " + this.workerId + ")", e);
                recordFatalError(e);
                setState(ERROR);
            } catch (InterruptedException e) {
                log.error("Stage " + stage + " (workerId: " + workerId + ") interrupted while waiting for barrier", e);
                recordFatalError(e);
                setState(ERROR);
            } finally {
                doneSignal.countDown();
            }
        }
        
        public void deactivate(boolean waitForQueue) {
            if (waitForQueue) {
                this.runnability = Runnability.STOPPABLE;
            } else {
                this.runnability = Runnability.NOT_RUNNABLE;
            }
        }
        
        public void awaitCompletion() throws InterruptedException {
            this.doneSignal.await();
        }
    }
}
