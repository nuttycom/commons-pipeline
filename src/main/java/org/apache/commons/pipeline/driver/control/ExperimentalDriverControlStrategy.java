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
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Stage;

/**
 * An implementation of DriverControlStrategy that every so often experimentally
 * increases and decreases priorities to see if performance is improved.  If
 * a performance improvement is found, additional experiments are done in the
 * same direction
 */
public class ExperimentalDriverControlStrategy implements DriverControlStrategy {

    private Log log = LogFactory.getLog(ExperimentalDriverControlStrategy.class);
    
    /**
     * The minimum time difference (in percent) between different analyses of a stage for
     * modifications to take place.  In other words, if a stage is stable with
     * its current priority, no changes are made
     */
    private int minDifferencePercent = 3;

    private enum Action {

        Decrease {

            void execute(PrioritizableStageDriver driver) {
                driver.decreasePriority(1);
            }
        },
        Increase {

            void execute(PrioritizableStageDriver driver) {
                driver.increasePriority(1);
            }
        },
        None {

            void execute(PrioritizableStageDriver driver) { /*do nothing*/ }
        };

        abstract void execute(PrioritizableStageDriver driver);
    }

    private class Tuple {

        private int count = 0;
        private long duration = 0;
        private Action lastAction = Action.None;

        Tuple() {
        }

        public void add(long duration) {
            count++;
            this.duration += duration;
        }
    }
    private Map<Stage, Tuple> lastTimings = new HashMap<Stage, Tuple>();

    /** Creates a new instance of EqualizingDriverControlStrategy */
    public ExperimentalDriverControlStrategy() {
    }

    public ExperimentalDriverControlStrategy(int minDifferencePercent) {
        if (minDifferencePercent < 0 || minDifferencePercent > 100) {
            throw new IllegalArgumentException("Minimum difference percent must be between 0 and 100");
        }
        this.minDifferencePercent = minDifferencePercent;
    }

    public void handleEvents(List<PrioritizableStageDriver> drivers, List<StageProcessTimingEvent> events) {
        Map<Stage, Tuple> timings = new HashMap<Stage, Tuple>();
        for (StageProcessTimingEvent ev : events) {
            Tuple tuple = timings.get(ev.getSource());
            if (tuple == null) {
                tuple = new Tuple();
                timings.put((Stage) ev.getSource(), tuple);
            }

            tuple.add(ev.getLatency());
        }

        for (PrioritizableStageDriver driver : drivers) {
            Tuple mostRecentTiming = timings.get(driver.getStage());
            Tuple previousTiming = lastTimings.get(driver.getStage());
            double avgMostRecentDuration = mostRecentTiming.duration / mostRecentTiming.count;
            //first time around, try increasing priority
            if (previousTiming == null) {
                mostRecentTiming.lastAction = Action.Increase;
                driver.increasePriority(1);
            }

            if (previousTiming != null) {
                double avgPreviousTiming = previousTiming.duration / previousTiming.count;
                //if the performance has decreased significantly...
                double timingDifference = avgPreviousTiming - avgMostRecentDuration;

                log.debug("Performance went from " + avgPreviousTiming + " to " + avgMostRecentDuration + "(" + timingDifference + ")");
                //if the timing difference was significant enough to work with...
                double minDifference = avgPreviousTiming * (minDifferencePercent / 100.0);
                if (Math.abs(timingDifference) >= minDifference) {
                    //if the diff is positive, we have a performance improvement
                    if (timingDifference > 0) {
                        //continue whatever we did last time to try and get further
                        //improvement
                        if (previousTiming.lastAction == Action.Increase) {
                            driver.increasePriority(1);
                            mostRecentTiming.lastAction = Action.Increase;
                        } else if (previousTiming.lastAction == Action.Decrease) {
                            driver.decreasePriority(1);
                            mostRecentTiming.lastAction = Action.Decrease;
                        } //there was no last action.  Try a random action
                        else {
                            log.debug("Significant performance change without a previous action: RANDOM action");
                            Action randomAction = getRandomAction();
                            mostRecentTiming.lastAction = randomAction;
                            randomAction.execute(driver);
                        }
                    } //there was a performance degradation, reverse our last step
                    else {
                        //reverse whatever we did last time to try and get further
                        //improvement
                        if (previousTiming.lastAction == Action.Increase) {
                            driver.decreasePriority(1);
                            mostRecentTiming.lastAction = Action.Decrease;
                        } else if (previousTiming.lastAction == Action.Decrease) {
                            driver.increasePriority(1);
                            mostRecentTiming.lastAction = Action.Increase;
                        } //there was no last action.  Try a random action
                        else {
                            log.debug("Significant performance change without a previous action: RANDOM action");
                            Action randomAction = getRandomAction();
                            mostRecentTiming.lastAction = randomAction;
                            randomAction.execute(driver);
                        }
                    }
                } else {
                    mostRecentTiming.lastAction = Action.None;
                }
            }

            log.debug("Action=" + mostRecentTiming.lastAction + ", current priority=" + driver.getPriority());
            //take our most recent timings and roll them into the previous timings
            lastTimings.put(driver.getStage(), mostRecentTiming);
        }
    }

    private Action getRandomAction() {
        int val = new Random().nextInt();
        if (val < 0) {
            val *= -1;
        }
        int actionVal = val % 3;
        switch (actionVal) {
            case 0:
                return Action.None;
            case 1:
                return Action.Increase;
            case 2:
                return Action.Decrease;
            default:
                throw new IllegalStateException();
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
