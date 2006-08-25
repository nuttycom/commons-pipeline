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

import java.util.ArrayList;
import java.util.List;

/**
 * This interface is used to define how processing for a stage is started,
 * stopped, and run. StageDriver implementations may run stages in one or
 * more threads, and use the {@link StageMonitor} interface to provide communication
 * between the stage, the driver, and the enclosing pipeline.
 */
public abstract class StageDriver {
    /**
     * This enumeration represents possible states of the a stage driver during
     * processing.
     */
    public enum State { 
        /** The stage driver has started and the preprocess() method is being run. */
        STARTED, 
        /** Preprocessing is complete and objects are being processed.*/
        RUNNING, 
        /** A stop has been requested - the stage will finish processing, 
         * then postprocess and shut down. */
        STOP_REQUESTED, 
        /** Postprocessing tasks are complete; the stage is shutting down. */
        FINISHED, 
        /** Resources have been released and all stage activity has stopped. 
         * This is the default state. */
        STOPPED, 
        /** A fatal error has occurred that has caused the driver to stop in an 
         * inconsistent state. The driver cannot be restarted from the error state. 
         * The error(s) can be obtained using the getFatalErrors method. */
        ERROR 
            }
    
    /**
     * The stage to run.
     */
    protected Stage stage;
    
    /**
     * The context for the stage being run
     */
    protected StageContext context;
    
    /**
     * List of processing failures that have occurred.
     */
    protected List<ProcessingFailure> processingFailures = new ArrayList<ProcessingFailure>();

    /**
     * List of errors that have occurred.
     */
    protected List<Throwable> errors = new ArrayList<Throwable>();
    
    /**
     * Creates a StageDriver for the specified stage.
     * @param stage The stage for which the driver will be created
     * @param context The context in which to run the stage
     */
    public StageDriver(Stage stage, StageContext context) {
        if (stage == null) throw new IllegalArgumentException("Stage may not be null.");
        if (context == null) throw new IllegalArgumentException("Context may not be null.");
        this.stage = stage;
        this.context = context;
        }
    
    /**
     * Returns the Stage being run by this StageDriver.
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
     * Returns the current state of stage processing.
     * @return The current state
     */
    public abstract State getState();

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
     * Returns a list of unrecoverable errors that occurred during stage
     * processing.
     * @return A list of unrecoverable errors that occurred during stage processing.
     */
    public List<Throwable> getFatalErrors() {
        return this.errors;
            }
    
    /**
     * Store a fatal error.
     * @param error The error to be stored for later analysis
     */
    protected void recordFatalError(Throwable error) {
        this.errors.add(error);
        }
    
    /**
     * Returns a list of errors that occurred while processing data objects,
     * along with the objects that were being processed when the errors
     * were generated.
     * @return The list of non-fatal processing errors.
     */
    public List<ProcessingFailure> getProcessingFailures() {
        return this.processingFailures;
    }
    
    /**
     * Store processing failure information for the specified data object.
     * @param data The data being processed at the time of the error
     * @param error The error encountered
     */
    protected void recordProcessingFailure(Object data, Throwable error) {
        this.processingFailures.add(new ProcessingFailure(data, error));
    }
    
    /**
     * FailedProcess objects are used to store detailed information about
     * processing failures including the failing data, the driver state
     * at the time of failure
     */
    public class ProcessingFailure {
        private Object data;
        private Throwable throwable;
        private State driverState;
        
        /** Creates a new instance of FailedProcess
         *@param data The object which was not able to be processed.
         *@param throwable The exception that occurred.
         */
        protected ProcessingFailure(Object data, Throwable throwable){
            this.data = data;
            this.throwable = throwable;
            this.driverState = StageDriver.this.getState();
        }
        
        /** Returns the data
         *@return The object which was not able to be processed.
     */
        public Object getData(){
            return this.data;
        }
        
        /** Returns the exception
         *@return The throwable
         */
        public Throwable getThrowable(){
            return this.throwable;
        }
        
        /** Returns the stage which threw the exception.
         *@return Stage
         */
        public Stage getStage() {
            return StageDriver.this.getStage();
        }
        
        /**
         * This method is used to determine what stage driver handled a particular error.
         * @return A reference to the stage driver that encountered the error.
         */
        public StageDriver getStageDriver() {
            return StageDriver.this;
        }
    }
    
}
