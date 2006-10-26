/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */ 


package org.apache.commons.pipeline.driver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageDriverFactory;
import org.apache.commons.pipeline.util.BlockingQueueFactory;

/**
 * This factory is used to create DedicatedThreadStageDriver instances configured
 * to run specific stages.
 *
 */
public class DedicatedThreadStageDriverFactory implements StageDriverFactory {
    
    /** Creates a new instance of DedicatedThreadStageDriverFactory */
    public DedicatedThreadStageDriverFactory() {
    }
    
    /**
     * Creates the new {@link DedicatedThreadStageDriver} based upon the configuration
     * of this factory instance
     * @param stage The stage to be run by the newly created driver
     * @param context The context in which the stage will be run
     * @return the newly created driver
     */
    public StageDriver createStageDriver(Stage stage, StageContext context) {
        try {
            return new DedicatedThreadStageDriver(stage, context, queueFactory.createQueue(), timeout, faultTolerance);
        } catch (Exception e) {
            throw new IllegalStateException("Instantiation of driver failed due to illegal factory state.", e);
        }
    }
        
    /**
     * Holds value of property timeout.
     */
    private long timeout = 500;
    
    /**
     * Timeout (in milliseconds) for queue polling to ensure deadlock cannot 
     * occur on thread termination. Default value is 500 ms.
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
     * Holds value of property faultTolerance.
     */
    private FaultTolerance faultTolerance = FaultTolerance.NONE;
    
    /**
     * Getter for property faultTolerance. See {@link FaultTolerance} for valid values
     * and enumation meanings.
     * @return Value of property faultTolerance.
     */
    public FaultTolerance getFaultTolerance() {
        return this.faultTolerance;
    }
    
    /**
     * Setter for property faultTolerance.
     * 
     * @param faultTolerance New value of property faultTolerance.
     */
    public void setFaultTolerance(FaultTolerance faultTolerance) {
        this.faultTolerance = faultTolerance;
    }
    
    /**
     * Convenience setter for property faultTolerance for use by Digester.
     * 
     * @param level New value of property level ("ALL","CHECKED", or "NONE").
     */
    public void setFaultToleranceLevel(String level) {
        this.faultTolerance = FaultTolerance.valueOf(level);
    }

    /**
     * Holds value of property queueFactory.
     */
    private BlockingQueueFactory<?> queueFactory = new BlockingQueueFactory.LinkedBlockingQueueFactory();

    /**
     * Getter for property queueFactory.
     * @return Value of property queueFactory.
     */
    public BlockingQueueFactory<?> getQueueFactory() {
        return this.queueFactory;
    }

    /**
     * Setter for property queueFactory.
     * @param queueFactory New value of property queueFactory.
     */
    public void setQueueFactory(BlockingQueueFactory<?> queueFactory) {
        this.queueFactory = queueFactory;
    }    
}
