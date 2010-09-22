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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageDriverFactory;

/**
 * This is a simple default implementation of the PipelineValidator interface
 * that checks stage and branch connectivity. It assumes that any un-annotated
 * stage simply passes data through and can accept any type of object (as though
 * it were annotated with @ConsumedTypes({Object.class}) and @ProducesConsumed.
 *
 */
public class SimplePipelineValidator implements PipelineValidator {
    
    /** Creates a new instance of PipelineValidator */
    public SimplePipelineValidator() {
    }
    
    /**
     * This method validates the entire structure of the pipeline, ensuring that
     * the data produced by each stage can be consumed by the subsequent
     * stage and/or relevant branch pipelines.
     * @param pipeline The pipeline to be validated
     * @return The list of validation errors encountered.
     */
    public List<ValidationFailure> validate(Pipeline pipeline) {
        List<ValidationFailure> errors = new ArrayList<ValidationFailure>();
        Stage previous = null;
        for (Iterator<Stage> iter = pipeline.getStages().iterator(); iter.hasNext();) {
            Stage stage = iter.next();
            
            //only check that the stage can succeed known production
            if (previous != null) {
                if (!ValidationUtils.canSucceed(previous, stage)) {
                    errors.add(new ValidationFailure(ValidationFailure.Type.STAGE_CONNECT,
                            "Stage cannot consume output of prior stage.", previous, stage));
                }
            }
            
            if (stage.getClass().isAnnotationPresent(ProductionOnBranch.class)) {
                ProductionOnBranch pob = stage.getClass().getAnnotation(ProductionOnBranch.class);
                errors.addAll(validateBranchConnect(pipeline, pob.branchKey(), stage));
            } else if (stage.getClass().isAnnotationPresent(Branches.class)) {
                Branches branches = stage.getClass().getAnnotation(Branches.class);
                
                for (ProductionOnBranch pob : branches.productionOnBranches()) {
                    errors.addAll(validateBranchConnect(pipeline, pob.branchKey(), stage));
                }
                
                //only check that each branch can consume from known production.
                if (previous != null) {
                    for (String branchKey : branches.producesConsumedOnBranches()) {
                        errors.addAll(validateBranchConnect(pipeline, branchKey, previous));
                    }
                }
            }
            
            //only update the previous stage reference if the stage has non-null
            //and non-pass-through production
            if (stage.getClass().isAnnotationPresent(ProducedTypes.class)) {
                //stop if the stage produces nothing, and raise an error if not at the end of the pipeline
                if (stage.getClass().getAnnotation(ProducedTypes.class).value().length == 0) {
                    if (iter.hasNext()) errors.add(new ValidationFailure(ValidationFailure.Type.STAGE_CONNECT,
                            "Stage with no production is not at terminus of pipeline.", stage, iter.next()));
                    break;
                }
                
                previous = stage;
            }
        }
        
        // recursively perform validation on the branch pipelines
        for (Pipeline branch : pipeline.getBranches().values()) {
            errors.addAll(validate(branch));
        }
        
        return errors;
    }
    
    /**
     * Utility method for validating connection between stages and branches.
     */
    private List<ValidationFailure> validateBranchConnect(Pipeline pipeline, String branchKey, Stage upstreamStage) {
        List<ValidationFailure> errors = new ArrayList<ValidationFailure>();
        Pipeline branch = pipeline.getBranches().get(branchKey);
        
        if (branch == null) {
            errors.add(new ValidationFailure(ValidationFailure.Type.BRANCH_NOT_FOUND,
                    "Branch not found for production key " + branchKey, upstreamStage, null));
        } else if (branch.getStages().isEmpty()) {
            errors.add(new ValidationFailure(ValidationFailure.Type.BRANCH_CONNECT,
                    "Branch pipeline for key " + branchKey + " has no stages.", upstreamStage, null));
        } else if (!ValidationUtils.canSucceed(upstreamStage, branch.getStages().get(0))) {
            errors.add(new ValidationFailure(ValidationFailure.Type.BRANCH_CONNECT,
                    "Branch " + branchKey + " cannot consume data produced by stage.", upstreamStage, branch.getStages().get(0)));
        }
        
        return errors;
    }
    
    /**
     * Validate whether or not a stage can be added to the pipeline.
     * @param pipeline The pipeline to which the stage is being added
     * @param stage The stage to be added
     * @param driverFactory the StageDriverFactory used to create a driver for the stage
     * @return The list of validation errors encountered, or an empty list if the add
     * passed validation.
     */
    public List<ValidationFailure> validateAddStage(Pipeline pipeline, Stage stage, StageDriverFactory driverFactory) {
        if (pipeline.getStages().isEmpty()) return Collections.emptyList(); //trivial case
        
        //establish list of errors to be returned, initially empty
        List<ValidationFailure> errors = new ArrayList<ValidationFailure>();
        
        //search backwards along pipeline for known production
        Stage previous = null;
        for (int i = pipeline.getStages().size() - 1; i >= 0; i--) {
            Stage test = pipeline.getStages().get(i);
            if (test.getClass().isAnnotationPresent(ProducedTypes.class)) {
                if (test.getClass().getAnnotation(ProducedTypes.class).value().length == 0) {
                    errors.add(new ValidationFailure(ValidationFailure.Type.STAGE_CONNECT,
                            "Attempt to add stage to pipeline with no production at terminus.", test, stage));
                } else {
                    previous = test;
                }
                
                break;
            }
        }
        
        if (previous != null) {
            if (!ValidationUtils.canSucceed(previous, stage)) {
                errors.add(new ValidationFailure(ValidationFailure.Type.STAGE_CONNECT,
                        "Stage cannot consume output of prior stage.", previous, stage));
                
                //TODO: Add checks to determine whether the branch production of the
                //stage can be consumed by branch pipelines.
            }
        }
        
        return errors;
    }
    
    /**
     * Validate whether or not the specified branch pipeline can be added
     * with the specified key.
     * @param pipeline The pipeline to which the branch is being added
     * @param branchKey The identifier for the newly added branch
     * @param branch The branch pipeline being added
     * @return The list of validation errors, or an empty list if no errors were found.
     */
    public List<ValidationFailure> validateAddBranch(Pipeline pipeline, String branchKey, Pipeline branch) {
        return Collections.emptyList(); //all default validation rules exist in pipeline
    }
}
