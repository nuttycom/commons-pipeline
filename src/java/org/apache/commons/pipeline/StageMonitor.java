/*
 *   Copyright 2004 The Apache Software Foundation
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
 *
 */

package org.apache.commons.pipeline;


import java.util.*;

/**
 * A monitor used to control concurrent processing of data in a stage.
 *
 * @author <a href="mailto:directory-dev@incubator.apache.org">Apache Directory Project</a>
 * @version $Rev$
 */
public class StageMonitor {
    /** Enumeration of possible states for the stage. */
    public enum Status { STARTING, RUNNING, STOP_REQUESTED, STOPPED }
    private Status status = Status.STOPPED;
    private List<Throwable> errors = new ArrayList<Throwable>();
    
    /**
     * StageDriver has been requested to start stage processing.
     * Implementations of this method should change the monitor's status to
     * {@link Status#STARTING}.
     */
    public synchronized void startRequested() {
        if (this.status == Status.STOPPED) this.status = Status.STARTING;
    }
    
    /**
     * StageDriver has started execution.
     * Implementations of this method should change the monitor's status to
     * {@link Status#RUNNING}.
     */
    public synchronized void driverStarted() {
        if (this.status == Status.STOPPED || this.status == Status.STARTING) this.status = Status.RUNNING;
    }
    
    /**
     * StageDriver has been requested to halt stage processing.
     * Implementations of this method should change the monitor's status to
     * {@link Status#STOPPING}.
     */
    public synchronized void stopRequested() {
        this.status = Status.STOP_REQUESTED;
        this.notifyAll();
    }
    
    /**
     * StageDriver has finished execution.
     * Implementations of this method should change the monitor's status to
     * {@link Status#STOPPED}.
     */
    public synchronized void driverStopped() {
        this.status = Status.STOPPED;
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
     * Monitors driver thread interruption failures.
     *
     * @param fault the faulting exception
     */
    public void driverFailed( InterruptedException fault ) {
        this.errors.add(fault);
    }
    
    /**
     * Monitors handler failures.
     *
     * @param data the data that was being processed as the fault occurred
     * @param fault the faulting exception
     */
    public void processingFailed( Object data, Throwable fault ) {
        this.errors.add(fault);
    }
    
    /**
     * Returns the current status of stage processing.
     */
    public synchronized Status status() {
        return this.status;
    }
    
    /**
     * Returns a list of errors recorded by this monitor
     */
    public List<Throwable> getErrors() {
        return errors;
    }
}

