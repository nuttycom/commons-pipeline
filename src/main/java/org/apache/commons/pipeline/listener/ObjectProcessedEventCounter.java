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

package org.apache.commons.pipeline.listener;

import java.util.Collections;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageEventListener;
import org.apache.commons.pipeline.event.ObjectProcessedEvent;

/**
 * This listener keeps track of the number of {@link ObjectProcessedEvent}s
 * received from each {@link Stage}
 *
 *
 */
public class ObjectProcessedEventCounter implements StageEventListener {
    //private final Log log = LogFactory.getLog(ObjectProcessedEventCounter.class);
    
    private Map<Stage,Integer> counts = Collections.synchronizedMap(new HashMap<Stage, Integer>());
    
    public synchronized void notify(EventObject evo) {
        if (evo instanceof ObjectProcessedEvent) {
        ObjectProcessedEvent ev = (ObjectProcessedEvent) evo;
        if (!counts.containsKey(ev.getSource())) counts.put(ev.getSource(), 1);
    }
    }
    
    public synchronized Map<Stage, Integer> getCounts() {
        return this.counts;
    }
}
