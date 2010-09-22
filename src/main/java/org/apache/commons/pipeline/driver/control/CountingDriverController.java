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

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.StageEventListener;
import org.apache.commons.pipeline.PipelineLifecycleJob;

/**
 *
 */
public class CountingDriverController extends AbstractDriverController implements PipelineLifecycleJob {
    
    // flag used to signal that controller thread should stop
    private volatile boolean running;
    
    /** Creates a new instance of AbstractPriorityController */
    public CountingDriverController() { }
    
    public void onStart(Pipeline pipeline) {
        if (pipeline != null) pipeline.registerListener(CountingDriverController.this);
        running = true;
        
        new Thread() {
            public void run() {
                while (running) {
                    List<StageProcessTimingEvent> eventsToHandle;
                    synchronized(CountingDriverController.this) {
                        while (events.size() < minimumEventsToHandle && running) {
                            try {
                                CountingDriverController.this.wait();
                            } catch (InterruptedException e) {
                                throw new Error("Assertion failure: interrupted while waiting for events", e);
                            }
                        }
                        
                        eventsToHandle = events;
                        events = new ArrayList<StageProcessTimingEvent>();
                    }
                    
                    driverControl.handleEvents(drivers, eventsToHandle);
                }
            }
        }.start();
    }
    
    /**
     * Holds value of property minimumEventsToHandle.
     */
    private int minimumEventsToHandle;
    
    /**
     * Getter for property minimumEventsToHandle.
     * @return Value of property minimumEventsToHandle.
     */
    public int getMinimumEventsToHandle() {
        return this.minimumEventsToHandle;
    }
    
    /**
     * Setter for property minimumEventsToHandle.
     * @param minimumEventsToHandle New value of property minimumEventsToHandle.
     */
    public void setMinimumEventsToHandle(int minimumEventsToHandle) {
        this.minimumEventsToHandle = minimumEventsToHandle;
    }
    
    public void onFinish(Pipeline pipeline) {
        this.running = false;
    }
}
