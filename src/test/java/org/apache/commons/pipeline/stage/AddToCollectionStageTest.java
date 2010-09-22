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

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for AddToCollectionStage
 */
public class AddToCollectionStageTest extends AbstractStageTest {
    
    public AddToCollectionStageTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(AddToCollectionStageTest.class);
        
        return suite;
    }

    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.AddToCollectionStage.
     */
    public void testProcess() throws Exception {
        //create the stage instance
        List<String> testCollection = new ArrayList<String>();
        AddToCollectionStage<String> stage = new AddToCollectionStage<String>(testCollection, true);
        
        //initialize the testing context
        this.init(stage);
        
        //run the process() method
        stage.process("Hello, World!");
        
        assertEquals("Incorrect number of objects in collection", 1, testCollection.size());
        assertEquals("Incorrect object stored in collection", "Hello, World!", testCollection.get(0));       
        assertEquals("Incorrect number of objects received downstream", 1, testFeeder.receivedValues.size());
        assertEquals("Incorrect value received downstream", "Hello, World!", testFeeder.receivedValues.get(0));
    }
}
