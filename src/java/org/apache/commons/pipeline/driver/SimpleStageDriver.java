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
 *
 * Created on July 19, 2005, 4:26 PM
 *
 * $Log: SimpleStageDriver.java,v $
 * Revision 1.4  2005/07/25 22:04:54  kjn
 * Corrected Apache licensing, documentation.
 *
 * Revision 1.3  2005/07/22 23:22:51  kjn
 * Changes to reflect changes in StageDriver base class, consolidation with
 * SimpleStageMonitor code to eliminate unnecessary public class.
 */

package org.apache.commons.pipeline.driver;

import java.util.Collections;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.StageMonitor;

/**
 * This is a non-threaded version of the StageDriver.
 *
 * @author Travis Stevens, National Geophysical Data Center, NOAA
 */
public class SimpleStageDriver extends StageDriver {
    
    private static final Log log = LogFactory.getLog(SimpleStageDriver.class);
    
    /** Creates a new instance of SimpleStageDriver */
    public SimpleStageDriver() {
        super();
    }
    
    /** Sets a new monitor on the stage and notifies the monitor we have started */
    protected void startInternal(Stage stage) throws StageException {
        stage.preprocess();
        stage.getMonitor().driverStarted();
        
        try {
            Object o = stage.poll();
            while (o != null){
                stage.process(o);
                o = stage.poll();
            }
        } catch (Exception e) {
            stage.release();
            throw new StageException(e);
        }
    }
    
    /** Notify the monitor that we have stopped */
    protected void finishInternal(Stage stage) throws StageException {
        try {
            stage.postprocess();
        } finally {
            stage.release();
        }
    }
    
    /**
     * Factory method for StageMonitor that works with this driver.
     */
    protected StageMonitor createStageMonitor(Stage stage) {
        return new AbstractStageMonitor(stage) {
            /**
             * StageDriver has been requested to start stage processing.
             * Implementations of this method should change the monitor's state to
             * {@link State#STARTING}.
             */
            public void startRequested() {
                if (this.state == State.STOPPED) this.state = State.STARTING;
            }
            
            /**
             * StageDriver has started execution.
             * Implementations of this method should change the monitor's state to
             * {@link State#RUNNING}.
             */
            public void driverStarted() {
                if (this.state == State.STOPPED || this.state == State.STARTING) this.state = State.RUNNING;
            }
            
            /**
             * StageDriver has been requested to halt stage processing.
             * Implementations of this method should change the monitor's state to
             * {@link State#STOPPING}.
             */
            public void stopRequested() {
                this.state = State.STOP_REQUESTED;
            }
            
            /**
             * StageDriver has finished execution.
             * Implementations of this method should change the monitor's state to
             * {@link State#STOPPED}.
             */
            public void driverStopped() {
                this.state = State.STOPPED;
            }
            
            /** Returns the state */
            public org.apache.commons.pipeline.StageMonitor.State getState() {
                return this.state;
            }
            
            /** Returns all errors */
            public java.util.List<Throwable> getErrors() {
                return Collections.unmodifiableList(this.errors);
            }
            
            public void enqueueOccurred() {
                if (this.state != State.RUNNING) {
                    try {
                        this.stage.getStageDriver().start(this.stage);
                    } catch (StageException e){
                        this.preprocessFailed(e);
                    }
                } else {
                    Object object = stage.poll();
                    try {
                        stage.process(object);
                    } catch (StageException e){
                        this.processingFailed(object, e);
                    }
                }
            }
        };
    }
    
}
