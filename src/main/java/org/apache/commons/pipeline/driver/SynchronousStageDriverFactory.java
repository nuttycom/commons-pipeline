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

package org.apache.commons.pipeline.driver;

import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageDriverFactory;

/**
 * Factory for SynchronousStageDriver objects.
 *
 *
 */
public class SynchronousStageDriverFactory implements StageDriverFactory {
    
    /** Creates a new instance of SynchronousStageDriverFactory */
    public SynchronousStageDriverFactory() {
    }

    /**
     * Creates a new {@link SynchronousStageDriver} based upon this factory's configuration.
     * @param stage the stage to be run by the newly created driver
     * @param context the context in which the stage will be run
     * @return the newly created and configured driver
     */
    public StageDriver createStageDriver(Stage stage, StageContext context) {
        return new SynchronousStageDriver(stage, context, this.faultTolerance);
    }    

    /**
     * Holds value of property faultTolerance. Default value is {@link FaultTolerance.NONE}.
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
}
