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

import java.util.List;

import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageDriverFactory;

/**
 * This interface is used as the basis for validation strategies that may be
 * used to check the validity of a pipeline under construction.
 *
 *
 */
public interface PipelineValidator {
    /**
     * Implementations of this method should validate the overall structure of 
     * the pipeline.
     * @param pipeline The pipeline to be validated
     * @return The list of validation errors encountered. An empty list is returned if no
     * errors are found.
     */
    public List<ValidationFailure> validate(Pipeline pipeline);
    
    /**
     * Implementations of this method should validate whether or not the specified 
     * stage can be added to the pipeline in its current state.
     * @param pipeline The pipeline to which the stage is being added
     * @param stage The added stage
     * @param driverFactory The StageDriverFactory used to create a StageDriver for the stage
     * @return The list of validation errors encountered, or an empty list if none were
     * encountered
     */
    public List<ValidationFailure> validateAddStage(Pipeline pipeline, Stage stage, StageDriverFactory driverFactory);
    
    /**
     * Implementations of this method should validate whether or not the specified
     * branch can be added to the specified pipeline with the given key.
     * @param pipeline The pipeline to which the branch is being added
     * @param branchKey The key used to identify the new branch
     * @param branch The new branch pipeline
     * @return The list of validation failures, or an empty list if validation passes.
     */
    public List<ValidationFailure> validateAddBranch(Pipeline pipeline, String branchKey, Pipeline branch);
}
