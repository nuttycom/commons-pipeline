/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.pipeline.driver.control;

import static org.apache.commons.pipeline.StageDriver.State.*;
import static org.apache.commons.pipeline.driver.FaultTolerance.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.driver.FaultTolerance;

/**
 * This {@link StageDriver} implementation uses a pool of threads
 * to process objects from an input queue.
 */
public class ExecutorStageDriver extends AbstractPrioritizableStageDriver {
    private final Log log = LogFactory.getLog(ExecutorStageDriver.class);
    
    //executor service for parallel processing
    private final ThreadPoolExecutor threadPoolExecutor;
    
    //executor service for synchronous processing
    private final Executor directExecutor = new Executor(){
        public void execute(Runnable r) { r.run(); }
    };
    
    //signal that indicates it's okay to process objects
    private final CountDownLatch startSignal;
    
    //reference to the executor that is currently in use by the feeder
    private volatile Executor executor;
    
    //maximum number of threads in the pool
    private int maxThreads;
    
    //average number of threads in the pool
    private int coreThreads;
    
    //feeder used to feed data to this stage's queue
    private final Feeder feeder = new Feeder() {
        public void feed(final Object obj) {
            if (isInState(ERROR)) throw new IllegalStateException("Stage " + stage + " is in state ERROR and is hence unable to process data.");
            
            executor.execute(new Runnable() {
                public void run() {
                    try {
                        startSignal.await();
                        ExecutorStageDriver.this.process(obj);
                    } catch (InterruptedException e) {
                        throw new Error("Assertion failure: interrupted while awaiting start signal.", e);
                    } catch (StageException e) {
                        recordProcessingException(obj, e);
                        if (faultTolerance == NONE) setState(ERROR);
                    } catch (RuntimeException e) {
                        recordProcessingException(obj, e);
                        if (faultTolerance == CHECKED || faultTolerance == NONE) throw e;
                    }
                }
            });
        }
    };
    
    /**
     * Creates a new ExecutorStageDriver.
     *
     * @param stage The stage that the driver will run
     * @param context the context in which to run the stage
     * @param queue The object queue to use for storing objects prior to processing. The
     * default is {@link LinkedBlockingQueue}
     * @param timeout The amount of time, in milliseconds, that the worker thread
     * will wait before checking the processing state if no objects are available
     * in the thread's queue.
     * @param faultTolerance Flag determining the behavior of the driver when
     * an error is encountered in execution of {@link Stage#process(Object)}.
     * If this is set to false, any exception thrown during {@link Stage#process(Object)}
     * will cause the worker thread to halt without executing {@link Stage#postprocess()}
     * ({@link Stage#release()} will be called.)
     * @param numThreads Number of threads that will be simultaneously reading from queue
     */
    public ExecutorStageDriver(Stage stage, StageContext context, FaultTolerance faultTolerance, int coreThreads, int maxThreads) {
        super(stage, context, faultTolerance);
        this.threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.threadPoolExecutor.setCorePoolSize(coreThreads);
        this.threadPoolExecutor.setMaximumPoolSize(maxThreads);
        this.threadPoolExecutor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            public void rejectedExecution(Runnable command, ThreadPoolExecutor exec) {
                ExecutorStageDriver.this.directExecutor.execute(command);
            }
        });
        
        this.executor = (maxThreads == 0) ? this.directExecutor : this.threadPoolExecutor;
        this.startSignal = new CountDownLatch(1);
    }
    
    /**
     * Return the Feeder used to feed data to the queue of objects to be processed.
     * @return The feeder for objects processed by this driver's stage.
     */
    public Feeder getFeeder() {
        return this.feeder;
    }
    
    /**
     * Start the processing of the stage. Creates threads to poll items
     * from queue.
     * @throws org.apache.commons.pipeline.StageException Thrown if the driver is in an illegal state during startup
     */
    public synchronized void start() throws StageException {
        if (this.currentState == STOPPED) {
            setState(STARTED);
            if (log.isDebugEnabled()) log.debug("Preprocessing stage " + stage + "...");
            this.stage.preprocess();
            if (log.isDebugEnabled()) log.debug("Preprocessing for stage " + stage + " complete.");
            
            // let threads know they can start
            testAndSetState(STARTED, RUNNING);
            this.startSignal.countDown();
        }
    }
    
    /**
     * Causes processing to shut down gracefully. Waits until all worker threads
     * have completed.
     * @throws org.apache.commons.pipeline.StageException Thrown if the driver is in an illegal state for shutdown.
     */
    public synchronized void finish() throws StageException {
        try {
            testAndSetState(RUNNING, STOP_REQUESTED);
            this.threadPoolExecutor.shutdown();
            testAndSetState(STOP_REQUESTED, STOPPED);
            
            while (!this.threadPoolExecutor.isShutdown() && this.currentState != ERROR) this.wait();
            
            if (log.isDebugEnabled()) log.debug("Postprocessing stage " + stage + "...");
            this.stage.postprocess();
            if (log.isDebugEnabled()) log.debug("Postprocessing for stage " + stage + " complete.");
        } catch (InterruptedException e) {
            throw new RuntimeException("Unexpectedly interrupted while awaiting thread pool shutdown.", e);
        } finally {
            if (log.isDebugEnabled()) log.debug("Releasing resources for stage " + stage + "...");
            stage.release();
            if (log.isDebugEnabled()) log.debug("Stage " + stage + " released.");
            
            testAndSetState(STOPPED, FINISHED);
        }
    }
    
    public synchronized void increasePriority(double amount) {
        this.maxThreads += amount;
        this.coreThreads += (int) (amount / 1.5);
        this.threadPoolExecutor.setCorePoolSize(coreThreads);
        this.threadPoolExecutor.setMaximumPoolSize(maxThreads);
        if (this.executor == this.directExecutor || maxThreads > 0) {
            this.executor = this.threadPoolExecutor;
        }
    }
    
    public synchronized void decreasePriority(double amount) {
        this.maxThreads = (amount / 1.5 > maxThreads) ? 0 : maxThreads - (int) (amount / 1.5);
        this.coreThreads = (amount > coreThreads) ? 0 : coreThreads - (int) amount;
        this.threadPoolExecutor.setCorePoolSize(coreThreads);
        this.threadPoolExecutor.setMaximumPoolSize(maxThreads);
        if (maxThreads == 0) {
            this.executor = this.directExecutor;
        }
    }
    
    public double getPriority()
    {
        return maxThreads;
    }
}
