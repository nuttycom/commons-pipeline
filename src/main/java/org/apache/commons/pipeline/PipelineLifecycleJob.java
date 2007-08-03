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

/**
 * This interface specifies a job or set of tasks that are to be run at a 
 * well-specified points in the pipeline lifecycle. It is intended to be a
 * means by which third-party plugins can be added to the pipeline framework.
 */
public interface PipelineLifecycleJob {
    
    /**
     * This is called by the pipeline engine once the pipeline is fully configured,
     * just prior to stage driver start.
     */
    public void onStart(Pipeline pipeline);
    
    /**
     * This is called by the pipeline engine after all data processing has completed.
     */
    public void onFinish(Pipeline pipeline);
    
}
