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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.validation.ConsumedTypes;
import org.apache.commons.pipeline.validation.ProducesConsumed;

/**
 * This is a simple base class for Stages with no-op implementations of the
 * {@link #preprocess()}, {@link #process()}, {@link #postprocess()},
 * and {@link #release()} methods.
 */
@ConsumedTypes(Object.class)
@ProducesConsumed()
public abstract class BaseStage implements Stage {
    private Log log = LogFactory.getLog(BaseStage.class);
    
    /**
     * The context in which the stage runs.
     */
    protected StageContext context;
    
    /**
     * Feeder for the next downstream stage in the pipeline. This value
     * is lazily initialized by the process() method.
     */
    private Feeder downstreamFeeder;
    
    /**
     * This implementation of init() simply stores a reference to the
     * stage context.
     */
    public void init(StageContext context) {
        this.context = context;
    }
    
    /**
     * No-op implementation. This method should be overridden to provide
     * preprocessing capability for the stage.
     *
     * @throws StageException an Exception thrown by an overriding implementation should
     * be wrapped in a {@link StageException}
     * @see Stage#preprocess()
     */
    public void preprocess() throws StageException {
    }
    
    /**
     * The only operation performed by this implementation of process()
     * is to feed the specified object to the downstream feeder.
     * This method should be overridden to provide processing capability for the stage.
     *
     * @param obj Object to be passed to downstream pipeline.
     * @throws StageException an Exception thrown by an overriding implementation should
     * be wrapped in a {@link StageException}
     */
    public void process(Object obj) throws StageException {
        this.emit(obj);
    }
    
    /**
     * No-op implementation. This method should be overridden to provide
     * postprocessing capability for the stage.
     *
     * @throws StageException an Exception thrown by an overriding implementation should
     * be wrapped in a {@link StageException}
     */
    public void postprocess() throws StageException {
    }
    
    /**
     * No-op implementation. This method should be overridden to provide
     * resource release capability for the stage.
     *
     * Implementations overriding this method should clean up any lingering resources
     * that might otherwise be left allocated if an exception is thrown during
     * processing.
     *
     * @see Stage#release()
     */
    public void release() {
    }
    
    /**
     * Convenience method to feed the specified object to the next stage downstream.
     */
    public final void emit(Object obj) {
        if (log.isDebugEnabled()) log.debug(this.getClass() + " is emitting object " + obj);
        if (this.downstreamFeeder == null) this.downstreamFeeder = context.getDownstreamFeeder(this);
        this.downstreamFeeder.feed(obj);
    }
    
    /**
     * Convenience method to feed the specified object to the first stage of the specified branch.
     */
    public final void emit(String branch, Object obj) {
        this.context.getBranchFeeder(branch).feed(obj);
    }
}
