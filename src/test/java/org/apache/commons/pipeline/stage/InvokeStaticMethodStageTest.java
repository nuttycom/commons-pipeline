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

import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestSuite;


/**
 * Test cases for InvokeStaticMethodStage.
 */
public class InvokeStaticMethodStageTest extends AbstractStageTest {
    
    public InvokeStaticMethodStageTest(String testName) {
        super(testName);
    }    
    
    public static Test suite() {
        TestSuite suite = new TestSuite(InvokeStaticMethodStageTest.class);
        
        return suite;
    }
    
    /**
     * Test for reflection-based constructor
     */
    public void testConstructor() throws Exception {
        InvokeStaticMethodStage stage = new InvokeStaticMethodStage("java.lang.Integer", "valueOf", "java.lang.String");
        
        Method method = stage.getMethod();
        assertSame(Integer.class,method.getDeclaringClass());
        
        Class[] params = method.getParameterTypes();
        assertNotNull(params);
        assertEquals(1,params.length);
        assertSame(String.class, params[0]);
        assertEquals("valueOf",method.getName());
    }
    
    /**
     * Test of process() method, of class org.apache.commons.pipeline.stage.InvokeStaticMethodStage.
     */
    public void testProcess() throws Exception {
        Class integerClass = Integer.class;
        Method method = integerClass.getMethod("valueOf", String.class);
        InvokeStaticMethodStage stage = new InvokeStaticMethodStage(method);
        this.init(stage);
        
        stage.process("5");
        
        assertEquals(1, testFeeder.receivedValues.size());
        
        Object o = testFeeder.receivedValues.get(0);
        assertNotNull(o);
        assertTrue(o instanceof Integer);
        assertEquals(5, o);
    }    
}
