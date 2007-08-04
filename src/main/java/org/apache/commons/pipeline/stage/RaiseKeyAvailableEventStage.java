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


package org.apache.commons.pipeline.stage;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.event.KeyAvailableEvent;
import org.apache.commons.pipeline.util.KeyFactory;

/**
 *
 *
 */
public class RaiseKeyAvailableEventStage extends BaseStage {
    
    /** Creates a new instance of RaiseKeyAvailableEventStage */
    public RaiseKeyAvailableEventStage() {
    }
    
    /**
     * This implementation of process() simply generates a key for the 
     * processed object and raises a KeyAvailableEvent with the generated
     * key, then emits the processed object unchanged.
     */
    public void process(Object obj) throws StageException {
        this.context.raise(new KeyAvailableEvent<Object>(this, keyFactory.generateKey(obj)));
        this.emit(obj);
    }    
    
    /**
     * Holds value of property keyFactory.
     */
     private KeyFactory<Object,Object> keyFactory;
    
    /**
     * Returns the KeyFactory used to create keys for the objects processed
     * by this stage.
     */
    public KeyFactory<Object,Object> getKeyFactory() {
        return keyFactory;
    }    
    
    /**
     * Sets the KeyFactory used to create keys for the objects processed
     * by this stage.
     */
    public void setKeyFactory(KeyFactory<Object,Object> keyFactory) {
        this.keyFactory = keyFactory;
    }    
}
