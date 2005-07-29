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

import java.util.List;

/**
 * This interface is used to define how processing for a stage is started,
 * stopped, and run. StageDriver implementations may run stages in one or
 * more threads, and use the {@link StageMonitor} interface to provide communication
 * between the stage, the driver, and the enclosing pipeline.
 */
public abstract class StageDriver {
    
    /**
     * Creates and starts new worker thread(s) to process items in the queue.
     */
    public final void start(Stage stage) throws StageException {
        synchronized (stage) {
            if (stage.monitor == null || stage.monitor.getState() == StageMonitor.State.STOPPED) {
                if (stage.monitor == null) stage.setStageDriver(this); // this will set the monitor on the stage using a callback.
                stage.monitor.startRequested();
                this.startInternal(stage);
            }
        }
    }    
    
    /**
     * Implementations of this method must guarantee that the
     * {@link StageMonitor#driverStarted()} method will be called on the
     * specified stage's {@link Stage#getStageMonitor() monitor} when
     * preprocessing is complete.
     */
    protected abstract void startInternal(Stage stage) throws StageException;
    
    /**
     * This method waits for the stage(s) queue(s) to empty and any processor thread(s) to exit
     * cleanly and then calls release() to release any resources acquired during processing, if possible.
     */
    public final void finish(Stage stage) throws StageException {
        synchronized (stage) {
            if (stage.monitor != null && stage.monitor.getState() != StageMonitor.State.STOPPED) {
                stage.monitor.stopRequested();
                this.finishInternal(stage);
            }
        }
    }
    
    /**
     * Implementations of this method must guarantee that the 
     * {@link StageMonitor#driverStopped()} method will be called on the
     * specified stage's {@link Stage#getStageMonitor() monitor} when
     * postprocessing is complete and all stage resources have been released.
     */
    protected abstract void finishInternal(Stage stage) throws StageException;
    
    /**
     * This factory method must return a new instance of a {@link StageMonitor}
     * that can be used to monitor processing for the specified stage
     * in conjunction with this driver.
     */
    protected abstract StageMonitor createStageMonitor(Stage stage);
    
}
