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

import java.util.concurrent.TimeUnit;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageDriverFactory;
import org.apache.commons.pipeline.driver.FaultTolerance;
import org.apache.commons.pipeline.util.BlockingQueueFactory;

/**
 *
 */
public class BalancedPoolStageDriverFactory implements PrioritizableStageDriverFactory<BalancedPoolStageDriver> {
    
    /** Creates a new instance of BalancedPoolStageDriverFactory */
    public BalancedPoolStageDriverFactory() {
    }

    public BalancedPoolStageDriver createStageDriver(Stage stage, StageContext context) {
        return new BalancedPoolStageDriver(stage, context, queueFactory, initialThreads, faultTolerance, timeout, timeoutTimeUnit);
    }

    /**
     * Holds value of property initialThreads.
     */
    private int initialThreads = 0;

    /**
     * Getter for property initialThreads.
     * @return Value of property initialThreads.
     */
    public int getInitialThreads() {
        return this.initialThreads;
    }

    /**
     * Setter for property initialThreads.
     * @param initialThreads New value of property initialThreads.
     */
    public void setInitialThreads(int initialThreads) {
        this.initialThreads = initialThreads;
    }

    /**
     * Holds value of property queueFactory.
     */
    private BlockingQueueFactory queueFactory = new BlockingQueueFactory.LinkedBlockingQueueFactory();

    /**
     * Getter for property queueFactory.
     * @return Value of property queueFactory.
     */
    public BlockingQueueFactory getQueueFactory() {
        return this.queueFactory;
    }

    /**
     * Setter for property queueFactory.
     * @param queueFactory New value of property queueFactory.
     */
    public void setQueueFactory(BlockingQueueFactory queueFactory) {
        this.queueFactory = queueFactory;
    }

    /**
     * Holds value of property faultTolerance.
     */
    private FaultTolerance faultTolerance = FaultTolerance.NONE;

    /**
     * Getter for property faultTolerance.
     * @return Value of property faultTolerance.
     */
    public FaultTolerance getFaultTolerance() {
        return this.faultTolerance;
    }

    /**
     * Setter for property faultTolerance.
     * @param faultTolerance New value of property faultTolerance.
     */
    public void setFaultTolerance(FaultTolerance faultTolerance) {
        this.faultTolerance = faultTolerance;
    }

    /**
     * Holds value of property timeout.
     */
    private long timeout = 500;

    /**
     * Getter for property timeout.
     * @return Value of property timeout.
     */
    public long getTimeout() {
        return this.timeout;
    }

    /**
     * Setter for property timeout.
     * @param timeout New value of property timeout.
     */
    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    /**
     * Holds value of property timeoutTimeUnit.
     */
    private TimeUnit timeoutTimeUnit = TimeUnit.MILLISECONDS;

    /**
     * Getter for property timeoutTimeUnit.
     * @return Value of property timeoutTimeUnit.
     */
    public TimeUnit getTimeoutTimeUnit() {
        return this.timeoutTimeUnit;
    }

    /**
     * Setter for property timeoutTimeUnit.
     * @param timeoutTimeUnit New value of property timeoutTimeUnit.
     */
    public void setTimeoutTimeUnit(TimeUnit timeoutTimeUnit) {
        this.timeoutTimeUnit = timeoutTimeUnit;
    }
    
    /**
     * Sets the initial priority of the driver instance in an implementation-specific
     * manner.
     * @param priority An arbitrary priority value. In this implementation, this
     * corresponds directly to the number of threads initially assigned to the
     * managed stage.
     */
    public void setInitialPriority(double priority) {
        this.setInitialThreads((int) priority);
    }
    
}
