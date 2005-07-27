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
 *
 * Created on July 19, 2005, 9:38 AM
 * 
 * $Log: DynamicLookupStaticMethodStageTest.java,v $
 * Revision 1.2  2005/07/25 22:19:17  kjn
 * Updated licenses, documentation.
 *
 */

package org.apache.commons.pipeline.stage;

import java.util.ArrayList;
import junit.framework.*;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.driver.SingleThreadStageDriver;

/**
 * Test cases for DynamicLookupStaticMethodStage.
 *
 * @author Travis Stevens, National Geophysical Data Center, NOAA
 */
public class DynamicLookupStaticMethodStageTest extends TestCase {
    
    Pipeline pipe;
    ArrayList list;
    AddToCollectionStage collectionStage;
    
    public DynamicLookupStaticMethodStageTest(String testName) {
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
        TestSuite suite = new TestSuite(DynamicLookupStaticMethodStageTest.class);
        
        return suite;
    }

    /**
     * Test of newInstance method, of class org.apache.commons.pipeline.stage.DynamicLookupStaticMethodStage.
     */
    public void testNewInstance() throws Exception {
        DynamicLookupStaticMethodStage stage = DynamicLookupStaticMethodStage.newInstance("java.lang.Integer", "toString");
        assertEquals("toString",stage.getMethodName());
        assertSame(Integer.class, stage.getMethodClass());
    }

    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.DynamicLookupStaticMethodStage.
     */
    public void testProcess() throws Exception {
        DynamicLookupStaticMethodStage methodStage = new DynamicLookupStaticMethodStage(this.getClass(),"runMethod");
        pipe.addStage(methodStage, new SingleThreadStageDriver());
        pipe.addStage(collectionStage, new SingleThreadStageDriver());
        pipe.start();
        pipe.enqueue("A String");
        pipe.finish();
        assertEquals(1,list.size());
        Object object = list.remove(0);
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals("Ran String Method",object.toString());
        
        
        pipe.start();
        pipe.enqueue(new Integer(5));
        pipe.finish();
        assertEquals(1,list.size());
        object = list.remove(0);
        assertTrue(object instanceof String);
        assertEquals("Ran Integer Method",object.toString());
    }
    
    
    public static String runMethod(String object){
        return "Ran String Method";
    }
    
    public static String runMethod(Integer integer){
        return "Ran Integer Method";
    }
    
    
}
