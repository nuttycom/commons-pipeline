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

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.pipeline.event.KeyAvailableEvent;
import org.apache.commons.pipeline.util.KeyFactory;
import org.apache.commons.pipeline.util.QueueFactory;

/**
 * Test case for the KeyWaitBufferStage
 */
public class KeyWaitBufferStageTest extends AbstractStageTest {
    
    public KeyWaitBufferStageTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(KeyWaitBufferStageTest.class);
        
        return suite;
    }

    /**
     * Test of notify and process methods, of class org.apache.commons.pipeline.stage.KeyWaitBufferStage.
     * The tests of these methods are coupled since the process method buffers
     * data waiting for notify() to be called with an appropriate event.
     */
    public void testProcessAndNotify() throws Exception {
        //System.out.println("processAndNotify");
        
        String obj = "Hello, World!";
        KeyFactory<Object,Integer> keyFactory = new KeyFactory.HashKeyFactory();
        EventObject ev = new KeyAvailableEvent<Integer>(this, keyFactory.generateKey(obj));

        KeyWaitBufferStage instance = new KeyWaitBufferStage();
        instance.setKeyFactory(keyFactory);
        instance.setQueueFactory(new QueueFactory.LinkedListFactory<Object>());
        
        this.init(instance);
                
        instance.process(obj);
        
        assertTrue("The process object was not buffered correctly.", this.testFeeder.receivedValues.isEmpty());
                
        instance.notify(ev);
        
        assertTrue("The buffer was not properly flushed upon receiving the event.", this.testFeeder.receivedValues.contains(obj));
    }

    /**
     * Test of init method, of class org.apache.commons.pipeline.stage.KeyWaitBufferStage.
     */
    public void testInit() {
        KeyWaitBufferStage instance = new KeyWaitBufferStage();
        
        instance.init(this.testContext);
        
        assertTrue("The automatic registration of the stage as a StageEventListener failed.", this.testContext.listeners.contains(instance));
    }
}
