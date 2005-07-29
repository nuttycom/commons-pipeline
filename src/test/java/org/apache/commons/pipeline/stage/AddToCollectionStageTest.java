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

import java.util.ArrayList;
import junit.framework.*;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.stage.AddToCollectionStage;
import org.apache.commons.pipeline.driver.SingleThreadStageDriver;


/**
 * Test cases for AddToCollectionStage
 */
public class AddToCollectionStageTest extends TestCase {
    
        Pipeline pipeline;
        ArrayList list;
    
    public AddToCollectionStageTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        pipeline = new Pipeline();
        list = new ArrayList();
        AddToCollectionStage stage = new AddToCollectionStage(list);
        pipeline.addStage(stage, new SingleThreadStageDriver());
        pipeline.start();
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AddToCollectionStageTest.class);
        
        return suite;
    }

    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.AddToCollectionStage.
     */
    public void testProcess() throws Exception {
        Object o = new Object();
        pipeline.enqueue(o);
        pipeline.finish();
        assertEquals(1,list.size());
        Object pipedO = list.get(0);
        assertSame(o,pipedO);
    }
    
}
