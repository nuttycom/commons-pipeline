/*
 * Copyright 2004 The Apache Software Foundation
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
 *
 * Created on September 20, 2004, 12:30 PM
 */

package org.apache.commons.pipeline;

/**
 * Defines an interface for methods available to a StageQueue for execution
 * by individual worker threads. Handlers should not implement this interface
 * directly, but should extend {@link org.apache.commons.pipeline.Pipeline$Stage Stage} instead.
 *  
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision: 1.1 $
 */
public interface StageHandler {
    /**
     * Implementations of this method should perform any necessary setup that
     * needs to be done before any data is processed from the {@link org.apache.commons.pipeline.Pipeline$Stage Stage}'s queue.
     *
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public void preprocess() throws StageException;    
    
    /**
     * Implementations of this method should atomically process a single data
     * object. 
     *
     * @throws ClassCastException if the object is of an incorrect type
     * for the processing operation
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public abstract void process(Object obj) throws StageException;
    
    
    /**
     * Implementations of this method should do any additional processing or
     * finalization necessary after all data has been processed. This method
     * usually runs following a call to the implementing {@link org.apache.commons.pipeline.Pipeline$Stage Stage}'s
     * {@link StageQueue#finish()} method.
     *
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public abstract void postprocess() throws StageException;   
    
    
    /**
     * Implementations of this method should clean up any lingering resources
     * that might otherwise be left allocated if an exception is thrown during
     * processing.
     */
    public abstract void release();
}
