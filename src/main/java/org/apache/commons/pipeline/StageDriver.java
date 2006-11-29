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

package org.apache.commons.pipeline;

import java.util.List;

/**
 * This interface is used to define how processing for a stage is started,
 * stopped, and run. StageDriver implementations may run stages in one or
 * more threads, and use the {@link StageContext} interface to provide
 * communication between the stage and the context it is run, usually
 * a pipeline.
 *
 *
 */
public interface StageDriver {
    /**
     * This enumeration represents possible states of the a stage driver during
     * processing.
     */
    public enum State {
        /** Resources have been released and all stage activity has stopped, or
         * the stage has never been started. This is the default state. */
        STOPPED,
        /** The stage driver has started and the preprocess() method is being run. */
        STARTED,
        /** Preprocessing is complete and objects are being processed.*/
        RUNNING,
        /** A stop has been requested - the stage will finish processing,
         * then postprocess and shut down. */
        STOP_REQUESTED,
        /** Postprocessing tasks are complete; the stage is shutting down. */
        FINISHED,
        /** A fatal error has occurred that has caused the driver to stop in an
         * inconsistent state. The driver cannot be restarted from the error state.
         * The error(s) can be obtained using the getFatalErrors method. */
        ERROR
    }
    
    /**
     * This method is used to start the driver, run the
     * {@link Stage#preprocess() preprocess()} method of the attached stage
     * and to then begin processing any objects fed to this driver's Feeder.
     *
     * @throws org.apache.commons.pipeline.StageException Thrown if there is an error during stage startup. In most cases, such errors
     * will be handled internally by the driver.
     */
    public void start() throws StageException;
    
    /**
     * This method waits for the stage(s) queue(s) to empty and any processor thread(s) to exit
     * cleanly and then calls release() to release any resources acquired during processing, if possible.
     *
     * @throws org.apache.commons.pipeline.StageException Thrown if there is an error during driver shutdown. Ordinarily such
     * exceptions will be handled internally.
     */
    public void finish() throws StageException;
    
    /**
     * This method is used to provide a communication channel between the context
     * in which the driver is being run and the managed stage.
     *
     * @return the Feeder used to feed objects to the managed stage for processing.
     */
    public Feeder getFeeder();
    
    /**
     * Returns the Stage being run by this StageDriver.
     *
     * @return The stage being run by this StageDriver instance
     */
    public Stage getStage();
    
    /**
     * Returns the current state of stage processing.
     *
     * @return The current state
     */
    public State getState();
    
    /**
     * Returns a list of unrecoverable errors that occurred during stage
     * processing.
     *
     * @return A list of unrecoverable errors that occurred during stage processing.
     */
    public List<Throwable> getFatalErrors();
    
    /**
     * Returns a list of errors that occurred while processing data objects,
     * along with the objects that were being processed when the errors
     * were generated.
     *
     * @return The list of non-fatal processing errors.
     */
    public List<ProcessingException> getProcessingExceptions();
    
}
