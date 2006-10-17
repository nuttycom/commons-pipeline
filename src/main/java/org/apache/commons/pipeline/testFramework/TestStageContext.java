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

package org.apache.commons.pipeline.testFramework;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.pipeline.*;


/**
 * Stage Context for test harness.
 */
public class TestStageContext implements StageContext {
    public List<StageEventListener> listeners = new ArrayList<StageEventListener>();
    public List<EventObject> raisedEvents = new ArrayList<EventObject>();
    public Map<String, TestFeeder> branchFeeders = new HashMap<String,TestFeeder>();
    public Map<Stage, Feeder> downstreamFeeders = new HashMap<Stage,Feeder>();
    public Map<String, Object> env = new HashMap<String, Object>();
    
    public void registerListener(StageEventListener listener) {
        this.listeners.add(listener);
    }
    
    public void raise(EventObject ev) {
        this.raisedEvents.add(ev);
    }
    
    /**
     * Dynamically adds branch feeders as needed to provide a feeder for
     * the requested branch key.
     */
    public Feeder getBranchFeeder(String key) {
        if (branchFeeders.containsKey(key)) {
            return branchFeeders.get(key);
        } else {
            TestFeeder feeder = new TestFeeder();
            branchFeeders.put(key, feeder);
            return feeder;
        }
    }
    
    /**
     * Dynamically adds downstream feeders as needed to provide a downstream
     * feeder for the specified stage.
     */
    public Feeder getDownstreamFeeder(Stage stage) {
        if (downstreamFeeders.containsKey(stage)) {
            return downstreamFeeders.get(stage);
        } else {
            TestFeeder feeder = new TestFeeder();
            downstreamFeeders.put(stage, feeder);
            return feeder;
        }
    }
    
    /**
     * This method is used by the test implementation to set up the feeders
     * for a stage as though they were provided by drivers in a pipeline.
     */
    public void registerDownstreamFeeder(Stage stage, Feeder feeder) {
        this.downstreamFeeders.put(stage, feeder);
    }
    
    public Collection<StageEventListener> getRegisteredListeners() {
        return this.listeners;
    }

    /**
     * This method allows objects in the global environment
     * to be accessed by the stages running in this context.
     * 
     * @return the object corresponding to the specified string key, or null
     * if no such key exists.
     */
    public Object getEnv(String key) {
        return this.env.get(key);
    }    
}