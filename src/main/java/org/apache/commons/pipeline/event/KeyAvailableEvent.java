/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */ 

package org.apache.commons.pipeline.event;

import java.util.EventObject;

/**
 * This event is used to signal the availability of the specified key. This is
 * usually used for inter-branch synchronization using the
 * {@link org.apache.commons.pipeline.stage.RaiseKeyAvailableEventStage RaiseKeyAvailableEventStage}
 * and/or the {@link org.apache.commons.pipeline.stage.KeyWaitBufferStage KeyWaitBufferStage}.
 */
public class KeyAvailableEvent<T> extends EventObject {
    /**
	 * 
	 */
	private static final long serialVersionUID = -7704631412431162164L;
    private T key;
    
    /** Creates a new instance of KeyAvailableEvent */
    public KeyAvailableEvent(Object source, T key) {
        super(source);
        this.key = key;
    }
    
    /**
     * Returns the key.
     */
    public T getKey() {
        return this.key;
    }
}
