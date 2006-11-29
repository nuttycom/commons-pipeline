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
 * <p>A Stage represents a set of tasks that can be performed on objects
 * in a queue, and methods used to communicate with other stages
 * in a {@link Pipeline}.</p>
     */
public interface Stage {
    
    /**
     * <p>Initialization takes place when the stage is added to a pipeline.
     * Implementations of this method should perform any necessary setup that
     * is required for the driver to be able to correctly run the stage.</p>
     * <p><strong>NOTE:</strong> Since this method is run when the stage is 
     * added to the pipeline, certain information (such as the downstream
     * feeder for the stage) may not yet be available until the pipeline is
     * fully constructoed.</p>
     * @param context the {@link StageContext} within which the stage sill be run
     */
    public void init(StageContext context);
    
    /**
     * Implementations of this method should perform any necessary setup that
     * needs to be done before any data is processed.
     * Preprocessing is performed after initialization.
     *
     * @throws StageException any checked Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public void preprocess() throws StageException;
    
    /**
     * Implementations of this method should atomically process a single data
     * object and transfer any feed objects resulting from this processing to
     * the downstream {@link Feeder}. This {@link Feeder} can be obtained from 
     * the stage context made available during {@link #init initialization}.
     *
     * NOTE: Implementations of this method must be thread-safe!
     *
     * @param obj an object to be processed
     * @throws StageException any checked Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public void process(Object obj) throws StageException;
    
    /**
     * Implementations of this method should do any additional processing or
     * finalization necessary after all data objects have been processed
     * by the stage.
     * @throws StageException any checked Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public void postprocess() throws StageException;
    
    /**
     * Implementations of this method should clean up any lingering resources
     * that might otherwise be left allocated if an exception is thrown during
     * processing (or pre/postprocessing).
     */
    public void release();
}