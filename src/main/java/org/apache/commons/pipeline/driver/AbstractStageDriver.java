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

package org.apache.commons.pipeline.driver;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.pipeline.*;

/**
 * This interface is used to define how processing for a stage is started,
 * stopped, and run. AbstractStageDriver implementations may run stages in one or
 * more threads, and use the {@link StageMonitor} interface to provide communication
 * between the stage, the driver, and the enclosing pipeline.
 */
public abstract class AbstractStageDriver implements StageDriver {
    
    /**
     * The stage to run.
     */
    protected Stage stage;
    
    /**
     * The context for the stage being run
     */
    protected StageContext context;    
    
    /**
     * The current state of processing. In most drivers, this is used for
     * thread control.
     */
    protected volatile State currentState = State.STOPPED;    
            
    /**
     * Enumerated value indicating the fault tolerance level of the StageDriver.
     */
    protected FaultTolerance faultTolerance = FaultTolerance.NONE;
    
    /**
     * List of processing failures that have occurred.
     */
    protected List<ProcessingException> processingExceptions = new ArrayList<ProcessingException>();

    /**
     * List of errors that have occurred.
     */
    protected List<Throwable> errors = new ArrayList<Throwable>();
    
    /**
     * Creates a StageDriver for the specified stage.
     * 
     * @param stage The stage for which the driver will be created
     * @param context The context in which to run the stage
     */
    public AbstractStageDriver(Stage stage, StageContext context) {
        this(stage, context, FaultTolerance.NONE);
    }
    
    /**
     * Creates a StageDriver for the specified stage.
     * 
     * @param stage The stage for which the driver will be created
     * @param context The context in which to run the stage
     */
    public AbstractStageDriver(Stage stage, StageContext context, FaultTolerance faultTolerance) {
        if (stage == null) throw new IllegalArgumentException("Stage may not be null.");
        if (context == null) throw new IllegalArgumentException("Context may not be null.");
        this.stage = stage;
        this.context = context;
        this.faultTolerance = faultTolerance;
    }
    
    /**
     * Returns the Stage being run by this StageDriver.
     * 
     * @return The stage being run by this StageDriver instance
     */
    public Stage getStage() {
        return this.stage;
    }
    
    /**
     * This method is used to provide a communication channel between the context 
     * in which the driver is being run and the managed stage.
     * @return the Feeder used to feed objects to the managed stage for processing.
     */
    public abstract Feeder getFeeder();    

    /**
     * Return the current state of stage processing.
     * @return the current state of processing
     */
    public State getState() {
        return this.currentState;
    }
    
    /**
     * Atomically tests to determine whether or not the driver is in the one of
     * the specified states.
     */
    protected synchronized boolean isInState(State... states) {
        for (State state : states) if (state == currentState) return true;
        return false;
    }
    
    /**
     * Set the current state of stage processing and notify any listeners
     * that may be waiting on a state change.
     */
    protected synchronized void setState(State nextState) {
        this.currentState = nextState;
        this.notifyAll();
    }
    
    /**
     * This method performs an atomic conditional state transition change
     * to the value specified by the nextState parameter if and only if the
     * current state is equal to the test state.
     */
    protected synchronized boolean testAndSetState(State testState, State nextState) {
        if (currentState == testState) {
            setState(nextState);
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * This method is used to start the driver, run the 
     * {@link Stage#preprocess() preprocess()} method of the attached stage
     * and to then begin processing any objects fed to this driver's Feeder.
     *
     * @throws org.apache.commons.pipeline.StageException Thrown if there is an error during stage startup. In most cases, such errors
     * will be handled internally by the driver.
     */
    public abstract void start() throws StageException;
    
    /**
     * This method waits for the stage(s) queue(s) to empty and any processor thread(s) to exit
     * cleanly and then calls release() to release any resources acquired during processing, if possible.
     * @throws org.apache.commons.pipeline.StageException Thrown if there is an error during driver shutdown. Ordinarily such 
     * exceptions will be handled internally.
     */
    public abstract void finish() throws StageException;

    /**
     * Sets the failure tolerance flag for the worker thread. If faultTolerance
     * is set to CHECKED, {@link StageException StageException}s thrown by
     * the {@link Stage#process(Object)} method will not interrupt queue
     * processing, but will simply be logged with a severity of ERROR.
     * If faultTolerance is set to ALL, runtime exceptions will also be
     * logged and otherwise ignored.
     * @param faultTolerance the flag value
     */
    public final void setFaultTolerance(FaultTolerance faultTolerance) {
        this.faultTolerance = faultTolerance;
    }
    
    /**
     * Getter for property faultTolerant.
     * @return Value of property faultTolerant.
     */
    public final FaultTolerance getFaultTolerance() {
        return this.faultTolerance;
    }    
    
    /**
     * Store a fatal error.
     * @param error The error to be stored for later analysis
     */
    protected void recordFatalError(Throwable error) {
        this.errors.add(error);
    }

    /**
     * Returns a list of unrecoverable errors that occurred during stage
     * processing.
     * @return A list of unrecoverable errors that occurred during stage processing.
     */
    public List<Throwable> getFatalErrors() {
        return this.errors;
    }
    
    /**
     * Store processing failure information for the specified data object.
     * @param data The data being processed at the time of the error
     * @param error The error encountered
     */
    protected void recordProcessingException(Object data, Throwable error) {
        ProcessingException ex = new ProcessingException(this.stage, error, data, this.getState());  
        this.processingExceptions.add(ex);
    }    
    
    /**
     * Returns a list of errors that occurred while processing data objects,
     * along with the objects that were being processed when the errors
     * were generated.
     * @return The list of non-fatal processing errors.
     */
    public List<ProcessingException> getProcessingExceptions() {
        return this.processingExceptions;
    }
}
