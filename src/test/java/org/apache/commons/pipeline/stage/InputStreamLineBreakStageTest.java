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

import java.io.InputStream;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test cases for InputStreamLineBreakStage.
 */
public class InputStreamLineBreakStageTest extends AbstractStageTest {
    
    public InputStreamLineBreakStageTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(InputStreamLineBreakStageTest.class);
        
        return suite;
    }

    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.InputStreamLineBreakStage.
     */
    public void testProcess() throws Exception {
        InputStreamLineBreakStage stage = new InputStreamLineBreakStage();
        this.init(stage);
        
        InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("url-input-to-stream-test.txt");
        try {
            stage.process(in);
        } finally {
            in.close();
        }
        
        assertEquals(3, testFeeder.receivedValues.size());
        assertEquals("line 1", testFeeder.receivedValues.get(0));
        assertEquals("line 2", testFeeder.receivedValues.get(1));
        assertEquals("line 3", testFeeder.receivedValues.get(2));
    }
    
//    /**
//     * Test of isIgnoringBlankLines method, of class org.apache.commons.pipeline.stage.InputStreamLineBreakStage.
//     */
//    public void testIsIgnoringBlankLines() {
//        System.out.println("isIgnoringBlankLines");
//        
//        InputStreamLineBreakStage instance = new InputStreamLineBreakStage();
//        
//        boolean expResult = true;
//        boolean result = instance.isIgnoringBlankLines();
//        assertEquals(expResult, result);
//        
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setIgnoringBlankLines method, of class org.apache.commons.pipeline.stage.InputStreamLineBreakStage.
//     */
//    public void testSetIgnoringBlankLines() {
//        System.out.println("setIgnoringBlankLines");
//        
//        boolean ignoringBlankLines = true;
//        InputStreamLineBreakStage instance = new InputStreamLineBreakStage();
//        
//        instance.setIgnoringBlankLines(ignoringBlankLines);
//        
//        fail("The test case is a prototype.");
//    }
    
}
