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

package org.apache.commons.pipeline.validation;

import org.apache.commons.pipeline.Stage;

/**
 * This class is used to store a collection of information about a particular 
 * validation failure.
 *
 */
public class ValidationFailure {
    /**
     * Enumeration of possible causes of validation failure
     */
    public enum Type {
        /**
         * Indicates that a stage appears to be unable to consume the output of the previous stage
         */
        STAGE_CONNECT, 
        /**
         * Indicates that a branch could not consume the output (type mismatch) of pipeline
         * stages feeding the branch.
         */
        BRANCH_CONNECT, 
        /**
         * Indicates that a branch to consume the branch output of a stage could not be found.
         */
        BRANCH_NOT_FOUND,
        /**
         * Other validation error - see detail message.
         */
        OTHER
    };
    
    private Type type;
    private String message;
    private Stage upstream;
    private Stage downstream;
    
    /**
     * Creates a new instance of ValidationError
     * @param type The type of problem encountered
     * @param message A message with more detailed information about the problem
     * @param upstream A reference to the upstream stage
     * @param downstream A reference to a downstream stage
     */
    public ValidationFailure(Type type, String message, Stage upstream, Stage downstream) {
        this.type = type;
        this.message = message;
        this.upstream = upstream;
        this.downstream = downstream;        
    }
    
    /**
     * Type identifying what sort of problem was encountered
     * @return the type of problem encountered
     */
    public Type getType() {
        return this.type;
    }
    
    /**
     * Returns the descriptive message about the error
     * @return message describing the error
     */
    public String getMessage() {
        return this.message;
    }
    
    /**
     * The stage upstream of the connection that could not be validated
     * @return reference to the upstream Stage
     */
    public Stage getUpstreamStage() {
        return this.upstream;
    }
    
    /**
     * The stage downstream of the connection that could not be validated
     * @return reference to the downstream Stage
     */
    public Stage getDownstreamStage() {
        return this.downstream;
    }
}
