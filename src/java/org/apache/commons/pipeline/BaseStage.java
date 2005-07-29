/*
 * Copyright 2005 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This is a simple base class for Stages with no-op implementations of the
 * {@link #preprocess()}, {@link #process()}, {@link #postprocess()},
 * and {@link #release()} methods.
 */
public class BaseStage extends Stage {
    
    /** Creates a new instance of BaseStage using an unbounded BlockingQueue. */
    public BaseStage() {
        this(new LinkedBlockingQueue<Object>());
    }
    
    
    /** Creates a new instance of BaseStage with the specified queue. */
    public BaseStage(Queue<Object> queue) {
        super(queue);
    }
    
    /**
     * No-op implementation. This method should be overridden to provide
     * preprocessing capability for the stage.
     *
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     * @see Stage#preprocess()
     */
    public void preprocess() throws StageException {
    }
    
    /**
     * The only operation performed by this implementation of process()
     * is to exqueue the specified object, passing it to the subsequent stage.
     * This method should be overridden to provide
     * processing capability for the stage.
     *
     * @throws ClassCastException if the object is of an incorrect type
     * for the processing operation
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public void process(Object obj) throws StageException {
        this.exqueue(obj);
    }
    
    /**
     * No-op implementation. This method should be overridden to provide
     * postprocessing capability for the stage.
     *
     * @throws StageException an Exception thrown by the implementation should
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
     * @see Stage#release()
     */
    public void release() {
    }
}
