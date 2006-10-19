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

package org.apache.commons.pipeline.driver;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageDriverFactory;

/**
 * This factory is used to create {@link DedicatedThreadStageDriver} instances
 * configured to run specific stages. The {@link BlockingQueue} used to buffer
 * input to the stage is created with a finite capacity, which can be configured
 * with the {@link #setQueueSize(int)} method.
 */
public class BoundedQueueDedicatedThreadSDF implements StageDriverFactory {
    private final Log log = LogFactory.getLog(BoundedQueueDedicatedThreadSDF.class);
    
    /**
     * Creates a new instance of FiniteDedicatedThreadStageDriverFactory
     */
    public BoundedQueueDedicatedThreadSDF() {
    }
    
    /**
     * Creates the new {@link DedicatedThreadStageDriver} based upon the 
     * configuration of this factory instance
     *
     * @param stage The stage to be run by the newly created driver
     * @param context The context in which the stage will be run
     * @return the newly created driver
     */
    public StageDriver createStageDriver(Stage stage, StageContext context) {
        BlockingQueue queue = new LinkedBlockingQueue(this.queueSize);
        
        log.info("Creating a StageDriver with queue size " + this.queueSize);
        try {
            return new DedicatedThreadStageDriver(stage, context, queue, timeout, faultTolerance);
        } catch (Exception e) {
            throw new IllegalStateException("Instantiation of driver failed due to illegal factory state.", e);
        }
    }
    
    /**
     * Holds value of property timeout.
     */
    private long timeout = 500;
    
    /**
     * Timeout for wait to ensure deadlock cannot occur on thread termination.
     * Default is 500
     *
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
     * Getter for property faultTolerance. See {@link FaultTolerance} for valid 
     * values.
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
     * Holds value of property queueSize.
     * The default capacity is 100 elements.
     */
    private int queueSize = 100;
    
    /**
     * Gets the queueSize, used to set the capacity of BlockingQueues that buffer
     * input to the stages. The default queue size is 100 objects.
     * The queueSize is passed to the <CODE>LinkedBlockingQueue</CODE>
     * constructor as a capacity argument. The no arg constructor has an
     * unlimited capacity, so the fixed capacity constructor called by this
     * factory avoids Java <CODE>OutOfMemoryError</CODE>s
     * due to overflowing queues.
     *
     * @return Value of property queueSize.
     */
    public int getQueueSize() {
        return this.queueSize;
    }
    
    /**
     * Set the capacity of queues that buffer objects between the stages.
     *
     * @param capacity New value of property queueSize.
     */
    public void setQueueSize(int capacity) {
        this.queueSize = capacity;
    }
    
}
