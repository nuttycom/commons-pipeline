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
 * An implementation of DriverControlStrategy that measures stage execution times
 * and increases thread counts that are taking longer than other stages on average
 *
 * @author mirror
 */
public class EqualizingDriverControlStrategy implements DriverControlStrategy {
    
    private static class Tuple {
        private int count = 0;
        private long duration = 0;
        
        Tuple() { }
        
        public void add(long duration) {
            count++;
            this.duration += duration;
        }
    }
    
    /** Creates a new instance of EqualizingDriverControlStrategy */
    public EqualizingDriverControlStrategy() {
    }
    
    public void handleEvents(List<PrioritizableStageDriver> drivers, List<StageProcessTimingEvent> events) {
        if (events.isEmpty()) return;
        
        Map<Stage, Tuple> timings = new HashMap<Stage,Tuple>();
        long total = 0;
        for (StageProcessTimingEvent ev : events) {
            Tuple tuple = timings.get((Stage) ev.getSource());
            if (tuple == null) {
                tuple = new Tuple();
                timings.put((Stage) ev.getSource(), tuple);
            }
            
            tuple.add(ev.getLatency());
            total += ev.getLatency();
        }
        
        //System.out.println("Events handled: " + events.size());
        System.out.print("Stage latencies: ");
        for (Map.Entry<Stage,Tuple> entry : timings.entrySet()) {
            System.out.print(entry.getKey() + ": " + entry.getValue().duration / entry.getValue().count + "; ");
        }
        System.out.println();
        //System.out.println("Total latency: " + total);
        
        double mean = total / events.size();
        //System.out.println("Mean latency: " + mean);
        
        for (PrioritizableStageDriver driver : drivers) {
            Tuple tuple = timings.get(driver.getStage());
            if (tuple != null) {
            long averageDuration = tuple.duration / tuple.count;
            if (averageDuration > mean + allowableDelta) {
                    System.out.println("Increasing priority for stage " + driver.getStage() + " with average duration " + averageDuration);
                driver.increasePriority(1);
            } else if (averageDuration < mean - allowableDelta) {
                driver.decreasePriority(1);
                    System.out.println("Decreasing priority for stage " + driver.getStage() + " with average duration " + averageDuration);
                }
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
