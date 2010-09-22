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
 *
 *
 */
public class InvokeMethodStageTest extends AbstractStageTest {
    
    public InvokeMethodStageTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(InvokeMethodStageTest.class);
        
        return suite;
    }
    
    /**
     * Test of reflection-based constructor
     */
    public void testReflectionConstructor() throws Exception {
        InvokeMethodStage stage = new InvokeMethodStage("java.lang.String", "lastIndexOf", "foo");
        Method method = stage.getMethod();
        assertNotNull(method);
        assertSame(String.class, method.getDeclaringClass());
        assertEquals("lastIndexOf", method.getName());
    }
    
    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.InvokeMethodStage.
     */
    public void testProcess() throws Exception {
        InvokeMethodStage stage = new InvokeMethodStage("java.lang.String", "toUpperCase", new Object[0]);
        this.init(stage);
        
        stage.process("some text");
        
        assertEquals(1, testFeeder.receivedValues.size());
        
        Object o = testFeeder.receivedValues.get(0);
        assertNotNull(o);
        assertTrue(o instanceof String);
        assertEquals("SOME TEXT", o);
    }
}
