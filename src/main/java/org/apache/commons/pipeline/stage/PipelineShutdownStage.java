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

package org.apache.commons.pipeline.stage;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.event.PipelineShutdownRequest;

/**
 *
 *
 */
public class PipelineShutdownStage extends BaseStage {
    
    private int numberOfObjects = 1;
    private int count;
    
    /** Creates a new instance of PipelineShutdownStage */
    public PipelineShutdownStage() {
        super();
    }
    
    /** Creates a new instance of PipelineShutdownStage
     *@param numberOfObjects The number of objects to process before shutting down.
     */
    public PipelineShutdownStage(int numberOfObjects){
        this.numberOfObjects = numberOfObjects;
    }

    /** Maintains a count of objects.  If the count equals or exceeds the numberOfObjects
     * then the pipeline is shut down.
     *@param obj The objects.
     */
    public void process(Object obj) throws StageException {
        this.emit(obj);
        if (count++ >= numberOfObjects){
            context.raise(new PipelineShutdownRequest(this, "Maximum of " + count + " objects processed."));
        }
    }
    
}
