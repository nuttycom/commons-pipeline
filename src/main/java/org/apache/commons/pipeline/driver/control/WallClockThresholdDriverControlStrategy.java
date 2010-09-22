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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.pipeline.Stage;

/**
 * An implementation of DriverControlStrategy that gauges performance by the
 * number of queued objects for each stage.  Those stages with consistently high 
 * numbers of queued objects have threads added.
 */
public class WallClockThresholdDriverControlStrategy implements DriverControlStrategy {
    /**
     * The threshold at which a stage is considered to be functioning correctly.
     * If the time taken to process a stage is on average greater than this 
     * number, more priority is added to the stage
     */
    private long thresholdMs = 500;
    
    private class Timing {
        private int count = 0;
        private long duration = 0;
        
        Timing() { }
        
        public void add(long duration) {
            count++;
            this.duration += duration;
        }
    }
    
    /** Creates a new instance of EqualizingDriverControlStrategy */
    public WallClockThresholdDriverControlStrategy() {
    }
    
    public WallClockThresholdDriverControlStrategy( int thresholdMs ){
        this.thresholdMs = thresholdMs;
    }
    
    public void handleEvents(List<PrioritizableStageDriver> drivers, List<StageProcessTimingEvent> events) {
        Map<Stage, Timing> timings = new HashMap<Stage,Timing>();
        long total = 0;
        for (StageProcessTimingEvent ev : events) {
            Timing timing = timings.get((Stage) ev.getSource());
            if (timing == null) {
                timing = new Timing();
                timings.put((Stage) ev.getSource(), timing);
            }
            
            timing.add(ev.getLatency());
            total += ev.getLatency();
        }
        
        long mean = total / timings.size();
        
        for (PrioritizableStageDriver driver : drivers) {
            Timing timing = timings.get(driver.getStage());
            long averageDuration = timing.duration / timing.count;
            if( averageDuration >= thresholdMs )
            {
                driver.increasePriority( 1 );
            }
        }
    }

    /**
     * Holds value of property allowableDelta.
     */
    private long allowableDelta;

    /**
     * Getter for property allowableDelta.
     * @return Value of property allowableDelta.
     */
    public long getAllowableDelta() {
        return this.allowableDelta;
    }

    /**
     * Setter for property allowableDelta.
     * @param allowableDelta New value of property allowableDelta.
     */
    public void setAllowableDelta(long allowableDelta) {
        this.allowableDelta = allowableDelta;
    }
    
}
