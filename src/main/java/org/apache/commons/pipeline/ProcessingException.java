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

package org.apache.commons.pipeline;

import org.apache.commons.pipeline.StageDriver.State;

/**
 * This exception class is used to store detailed information about
 * a failure in the processing step of a stage including the failing data,
 * the driver state at the time of failure, and any exceptions encountered.
 */
public class ProcessingException extends StageException {
    private final Object data;
    private final State driverState;
    
    /**
     * Creates a new instance of ProcessingException
     *
     * @param data The object which was not able to be processed.
     * @param throwable The exception that occurred.
     */
    public ProcessingException(Stage stage, Throwable cause, Object data, State driverState) {
        super(stage, cause);
        this.data = data;
        this.driverState = driverState;
    }
    
    /**
     * Returns the object that was being processed at the time of failure.
     * @return The object which was not able to be processed.
     */
    public Object getData(){
        return this.data;
    }
    
    /**
     * Returns the saved driver state at the time of processing failure.
     * @return the driver state at the time of processing failure.
     */
    public State getDriverState() {
        return this.driverState;
    }
}