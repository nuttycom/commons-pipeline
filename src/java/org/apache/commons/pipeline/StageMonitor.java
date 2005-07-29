/*
 *   Copyright 2005 The Apache Software Foundation
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package org.apache.commons.pipeline;

import java.util.List;

/**
 * A monitor used to control concurrent processing of data in a stage.
 */
public interface StageMonitor {
    public enum State { STARTING, RUNNING, STOP_REQUESTED, STOPPED, ERROR }
    
    /**
     * StageDriver has been requested to start stage processing.
     * Implementations of this method should change the driver's state to
     * {@link State#STARTING}.
     */
    public void startRequested();
    
    /**
     * StageDriver has started execution.
     * Implementations of this method should change the driver's state to
     * {@link State#RUNNING}.
     */
    public void driverStarted();
    
    /**
     * StageDriver has been requested to halt stage processing.
     * Implementations of this method should change the driver's state to
     * {@link State#STOPPING}.
     */
    public void stopRequested();
    
    /**
     * StageDriver has finished execution.
     * Implementations of this method should change the driver's state to
     * {@link State#STOPPED}.
     */
    public void driverStopped();
    
    /**
     * Notify the driver of successful enqueue operations on any stage managed
     * by the driver. 
     */
    public void enqueueOccurred();
    
    /**
     * Monitors driver thread interruption failures.
     *
     * @param fault the faulting exception
     */
    public void driverFailed( InterruptedException fault );
    
    /**
     * Monitors preprocessing failures.
     *
     * @param fault the faulting exception
     */
    public void preprocessFailed(Throwable fault);
    
    /**
     * Monitors handler failures.
     *
     * @param data the data that was being processed as the fault occurred
     * @param fault the faulting exception
     */
    public void processingFailed( Object data, Throwable fault);
        
    /**
     * Monitors preprocessing failures.
     *
     * @param fault the faulting exception
     */
    public void postprocessFailed(Throwable fault);
    
    /**
     * Returns the current state of stage processing.
     */
    public State getState();
    
    /**
     * Returns a list of errors recorded by this StageDriver
     */
    public List<Throwable> getErrors();
}

