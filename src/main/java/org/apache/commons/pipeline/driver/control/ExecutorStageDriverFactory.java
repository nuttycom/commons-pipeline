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

import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageDriverFactory;
import org.apache.commons.pipeline.driver.FaultTolerance;

/**
 *
 */
public class ExecutorStageDriverFactory implements PrioritizableStageDriverFactory<ExecutorStageDriver> {
    
    /** Creates a new instance of ExecutorStageDriverFactory */
    public ExecutorStageDriverFactory() {
    }

    public ExecutorStageDriver createStageDriver(Stage stage, StageContext context) {
        return new ExecutorStageDriver(stage, context, faultTolerance, coreThreads, maxThreads);
    }

    /**
     * Holds value of property faultTolerance.
     */
    private FaultTolerance faultTolerance;

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
     * Holds value of property coreThreads.
     */
    private int coreThreads;

    /**
     * Getter for property coreThreads.
     * @return Value of property coreThreads.
     */
    public int getCoreThreads() {
        return this.coreThreads;
    }

    /**
     * Setter for property coreThreads.
     * @param coreThreads New value of property coreThreads.
     */
    public void setCoreThreads(int coreThreads) {
        this.coreThreads = coreThreads;
    }

    /**
     * Holds value of property maxThreads.
     */
    private int maxThreads;

    /**
     * Getter for property maxThreads.
     * @return Value of property maxThreads.
     */
    public int getMaxThreads() {
        return this.maxThreads;
    }

    /**
     * Setter for property maxThreads.
     * @param maxThreads New value of property maxThreads.
     */
    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }
    
    public void setInitialPriority(double priority) {
        this.setCoreThreads((int) priority);
        this.setMaxThreads((int) priority);
    }
    
}
