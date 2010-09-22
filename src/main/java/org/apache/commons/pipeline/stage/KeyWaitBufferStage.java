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

import java.util.EventObject;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageEventListener;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.event.KeyAvailableEvent;
import org.apache.commons.pipeline.util.KeyFactory;
import org.apache.commons.pipeline.util.QueueFactory;

/**
 *
 * @author kjn
 */
public class KeyWaitBufferStage extends BaseStage implements StageEventListener {
    
    private Set<Object> receivedKeys = new TreeSet<Object>();
    private Map<Object,Queue<Object>> buffers = new TreeMap<Object,Queue<Object>>();
    
    /** Creates a new instance of KeyWaitBufferStage */
    public KeyWaitBufferStage() {
    }
    
    public void notify(EventObject ev) {
        if (ev instanceof KeyAvailableEvent) {
            KeyAvailableEvent e = (KeyAvailableEvent) ev;
            synchronized(receivedKeys) {
                receivedKeys.add(e.getKey());
            }
            
            //at this point, we know that no more objects will be added to
            //the pending queue for the key, so we can remove and empty it.
            if (buffers.containsKey(e.getKey())) {
                for (Object obj : buffers.remove(e.getKey())) this.emit(obj);
            }
        }
    }
    
    public void init(StageContext context) {
        super.init(context);
        context.registerListener(this);
    }
    
    public void process(Object obj) throws StageException {
        Object key = keyFactory.generateKey(obj);
        synchronized(receivedKeys) {
            if (!receivedKeys.contains(key)) {
                //store the object in a pending queue.
                if (!buffers.containsKey(key)) buffers.put(key, queueFactory.createQueue());
                buffers.get(key).add(obj);
                return;
            }
        }
        
        this.emit(obj);
    }

    /**
     * Holds value of property keyFactory.
     */
    private KeyFactory<Object,? extends Object> keyFactory;

    /**
     * Getter for property keyFactory.
     * @return Value of property keyFactory.
     */
    public KeyFactory<Object,? extends Object> getKeyFactory() {
        return this.keyFactory;
    }

    /**
     * Setter for property keyFactory.
     * @param keyFactory New value of property keyFactory.
     */
    public void setKeyFactory(KeyFactory<Object,? extends Object> keyFactory) {
        this.keyFactory = keyFactory;
    }

    /**
     * Holds value of property queueFactory.
     */
    private QueueFactory<Object> queueFactory;

    /**
     * Getter for property queueFactory.
     * @return Value of property queueFactory.
     */
    public QueueFactory<Object> getQueueFactory() {
        return this.queueFactory;
    }

    /**
     * Setter for property queueFactory.
     * @param queueFactory New value of property queueFactory.
     */
    public void setQueueFactory(QueueFactory<Object> queueFactory) {
        this.queueFactory = queueFactory;
    }
}
