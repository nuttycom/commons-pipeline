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
 * A collection of utility methods used by the validation system.
 *
 */
public class ValidationUtils {
    
    /** Prevent instantiation */
    private ValidationUtils() {  }
    
    /**
     * Tests whether the specified downstream stage can succeed the specified
     * upstream stage.
     * @return true or false on definitive identification of compatibility or
     * null if unable to determine compatibility due to missing metadata.
     * @param upstream the upstream stage
     * @param downstream the stage consuming data produced by the upstream stage
     */
    public static final Boolean canSucceed(Stage upstream, Stage downstream) {
        if (upstream.getClass().isAnnotationPresent(ProducedTypes.class) &&
                downstream.getClass().isAnnotationPresent(ConsumedTypes.class)) {
            ProducedTypes p = upstream.getClass().getAnnotation(ProducedTypes.class);
            ConsumedTypes c = downstream.getClass().getAnnotation(ConsumedTypes.class);
            return compatible(p.value(), c.value());
        }
        
        return null;
    }
    
    /**
     * Tests whether the specified downstream stage can succeed the specified
     * upstream stage on a branch pipeline identified by the given branch key.
     * @return true or false on definitive identification of compatibility or
     * null if unable to determine compatibility due to missing metadata.
     * @param upstream the upstream stage
     * @param downstreamBranchKey the key identifying the branch receiving data from the upstream stage
     * @param downstream the stage consuming data produced by the upstream stage
     */
    public static final Boolean canSucceedOnBranch(Stage upstream, String downstreamBranchKey, Stage downstream) {
        if (downstream.getClass().isAnnotationPresent(ConsumedTypes.class)) {
            ConsumedTypes c = downstream.getClass().getAnnotation(ConsumedTypes.class);
            if (upstream.getClass().isAnnotationPresent(ProductionOnBranch.class)) {
                ProductionOnBranch pob = upstream.getClass().getAnnotation(ProductionOnBranch.class);
                
                if (!downstreamBranchKey.equals(pob.branchKey())) {
                    return false;
                } else {
                    return compatible(pob.producedTypes(), c.value());
                }
            } else if (upstream.getClass().isAnnotationPresent(Branches.class)) {
                Branches branches = upstream.getClass().getAnnotation(Branches.class);
                for (ProductionOnBranch pob : branches.productionOnBranches()) {
                    if (downstreamBranchKey.equals(pob.branchKey())) {
                        return compatible(pob.producedTypes(), c.value());
                    }
                }
                
                return false;
            }
        }
        
        return null;
    }
    
    /**
     * Check if the specified production is compatible with the specified consumption.
     */
    private static Boolean compatible(Class<?>[] producedTypes, Class<?>[] consumedTypes) {
        for (Class<?> consumed : consumedTypes) {
            for (Class<?> produced : producedTypes) { //usually just one type
                if (consumed.isAssignableFrom(produced)) return true;
            }
        }
        
        //none of what is produced can be consumed
        return false;
    }    
}
