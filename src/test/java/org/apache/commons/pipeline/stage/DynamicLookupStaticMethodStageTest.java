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

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for DynamicLookupStaticMethodStage.
 */
public class DynamicLookupStaticMethodStageTest extends AbstractStageTest {
    
    public DynamicLookupStaticMethodStageTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DynamicLookupStaticMethodStageTest.class);
        
        return suite;
    }

    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.DynamicLookupStaticMethodStage.
     */
    public void testProcess() throws Exception {
        //create the stage instance for test
        DynamicLookupStaticMethodStage stage = new DynamicLookupStaticMethodStage(this.getClass(),"runMethod");
        
        //initialize the testing context
        this.init(stage);
        
        stage.process("A String");
        
        assertEquals("Incorrect number of objects received downstream", 1, testFeeder.receivedValues.size());
        assertNotNull("Unexpected null value received downstream.", testFeeder.receivedValues.get(0));
        assertTrue("Object received downstream is of class: " + testFeeder.receivedValues.get(0).getClass(), 
                testFeeder.receivedValues.get(0) instanceof String);
        assertEquals("Incorrect value received downstream", "Ran String Method", testFeeder.receivedValues.get(0));        
    
        stage.process(5);
    
        assertEquals("Incorrect number of objects received downstream", 2, testFeeder.receivedValues.size());
        assertNotNull("Unexpected null value received downstream.", testFeeder.receivedValues.get(1));
        assertTrue("Object received downstream is of class: " + testFeeder.receivedValues.get(1).getClass(), 
                testFeeder.receivedValues.get(1) instanceof String);
        assertEquals("Incorrect value received downstream", "Ran Integer Method", testFeeder.receivedValues.get(1));        
        
        Object[] args = {"Hello, World!", 5};
        
        stage.process(args);
        
        assertEquals("Incorrect number of objects received downstream", 3, testFeeder.receivedValues.size());
        assertNotNull("Unexpected null value received downstream.", testFeeder.receivedValues.get(2));
        assertTrue("Object received downstream is of class: " + testFeeder.receivedValues.get(2).getClass(), 
                testFeeder.receivedValues.get(2) instanceof String);
        assertEquals("Incorrect value received downstream", "Ran multiple-argument method", testFeeder.receivedValues.get(2));                
    }
    
    /**
     * Sample string-argument method for test
     */
    public static String runMethod(String object){
        return "Ran String Method";
    }
    
    /**
     * Sample integer-argument method for test
     */
    public static String runMethod(Integer integer){
        return "Ran Integer Method";
    }
    
    /**
     * Sample multiple-argument method for test
     */
    public static String runMethod(String arg1, Integer arg2) {
        return "Ran multiple-argument method";
    }
}
