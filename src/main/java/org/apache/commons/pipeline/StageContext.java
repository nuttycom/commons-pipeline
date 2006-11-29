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

import java.util.Collection;
import java.util.EventObject;

/**
 * This interface represents the context in which a stage is run. Ordinarily,
 * the context will be provided by the pipeline in which the stage is embedded;
 * however, this interface is also useful for creating isolated test environments
 * in which a stage can be run.
 *
 *
 */
public interface StageContext {
    /**
     * Adds a {@link StageEventListener} to the context that will be notified by calls
     * to {@link #raise(EventObject)}.
     * @param listener The listener to be registered with the context.
     */
    public void registerListener(StageEventListener listener);
    
    /**
     * Returns the collection of {@link StageEventListener}s registered with the
     * context.
     * @return  the collection of {@link StageEventListener}s registered with the
     * context.
     */
    public Collection<StageEventListener> getRegisteredListeners();
    
    /**
     * Notifies each registered listener of an event and propagates
     * the event to any attached branches
     * @param ev The event to be passed to registered listeners
     */
    public void raise(EventObject ev);
    
    /**
     * Return the source feeder for the specified pipeline branch.
     * @param branch the string identifer of the branch for which a feeder will be retrieved
     * @return the {@link Feeder} for the first stage of the specified branch
     */
    public Feeder getBranchFeeder(String branch);
    
    /**
     * This method is used by a stage driver to pass data from one stage to the next.
     * @return the feeder for the downstream stage, or {@link Feeder#VOID} if no downstream
     * stage exists.
     * @param stage The stage from which "downstream" will be determined. Ordinarily a Stage implementation
     * will call this method with a reference to itself.
     * @return The {@link Feeder} for the subsequent stage.
     */
    public Feeder getDownstreamFeeder(Stage stage);
    
    /**
     * A StageContext implementation provides a global environment for the
     * stages being run. This method allows objects in the global environment
     * to be accessed by the stages running in this context.
     *
     * @return the object corresponding to the specified string key, or null
     * if no such key exists.
     */
    public Object getEnv(String key);
}
