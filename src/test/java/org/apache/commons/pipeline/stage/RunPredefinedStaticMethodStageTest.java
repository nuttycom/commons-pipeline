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

package org.apache.commons.pipeline.stage;

import java.lang.reflect.Method;
import java.util.ArrayList;
import junit.framework.*;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.stage.AddToCollectionStage;
import org.apache.commons.pipeline.stage.RunPredefinedStaticMethodStage;
import org.apache.commons.pipeline.driver.SimpleStageDriver;


/**
 * Test cases for RunPredefinedStaticMethodStage.
 */
public class RunPredefinedStaticMethodStageTest extends TestCase {
    
    Pipeline pipe;
    ArrayList list;
    AddToCollectionStage collectionStage;
    
    
    public RunPredefinedStaticMethodStageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        pipe = new Pipeline();
        list = new ArrayList();
        collectionStage = new AddToCollectionStage(list, false);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(RunPredefinedStaticMethodStageTest.class);
        
        return suite;
    }
    
    /**
     * Test of newInstance method, of class org.apache.commons.pipeline.stage.RunPredefinedStaticMethodStage.
     */
    public void testNewInstance() throws Exception {
        RunPredefinedStaticMethodStage stage = RunPredefinedStaticMethodStage.newInstance("java.lang.Integer", "valueOf", "java.lang.String");
        Method method = stage.getMethod();
        assertSame(Integer.class,method.getDeclaringClass());
        Class[] params = method.getParameterTypes();
        assertNotNull(params);
        assertEquals(1,params.length);
        assertSame(String.class, params[0]);
        assertEquals("valueOf",method.getName());
    }
    
    /**
     * Test of process() method, of class org.apache.commons.pipeline.stage.RunPredefinedStaticMethodStage.
     */
    public void testProcess() throws Exception {
        Class integerClass = Integer.class;
        Method method = integerClass.getMethod("valueOf", String.class);
        RunPredefinedStaticMethodStage stage = new RunPredefinedStaticMethodStage(method);
        pipe.addStage(stage, new SimpleStageDriver());
        pipe.addStage(collectionStage, new SimpleStageDriver());
        pipe.start();
        pipe.enqueue("5");
        pipe.finish();
        
        assertEquals(1,list.size());
        Object o = list.get(0);
        assertNotNull(o);
        assertTrue(o instanceof Integer);
        Integer i = (Integer)o;
        assertEquals(5,i.intValue());
    }
    
}
