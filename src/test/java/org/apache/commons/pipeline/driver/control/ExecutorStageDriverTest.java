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


package org.apache.commons.pipeline.driver.control;

import junit.framework.*;
import static org.apache.commons.pipeline.StageDriver.State.*;
import static org.apache.commons.pipeline.driver.FaultTolerance.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.driver.FaultTolerance;

public class ExecutorStageDriverTest extends TestCase {
    
    public ExecutorStageDriverTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(ExecutorStageDriverTest.class);
        
        return suite;
    }

    /**
     * Test of getFeeder method, of class org.apache.commons.pipeline.driver.control.ExecutorStageDriver.
     */
    public void testGetFeeder() {
        System.out.println("getFeeder");
        
        ExecutorStageDriver instance = null;
        
        Feeder expResult = null;
        Feeder result = instance.getFeeder();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of start method, of class org.apache.commons.pipeline.driver.control.ExecutorStageDriver.
     */
    public void testStart() throws Exception {
        System.out.println("start");
        
        ExecutorStageDriver instance = null;
        
        instance.start();
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of finish method, of class org.apache.commons.pipeline.driver.control.ExecutorStageDriver.
     */
    public void testFinish() throws Exception {
        System.out.println("finish");
        
        ExecutorStageDriver instance = null;
        
        instance.finish();
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of increasePriority method, of class org.apache.commons.pipeline.driver.control.ExecutorStageDriver.
     */
    public void testIncreasePriority() {
        System.out.println("increasePriority");
        
        double amount = 0.0;
        ExecutorStageDriver instance = null;
        
        instance.increasePriority(amount);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of decreasePriority method, of class org.apache.commons.pipeline.driver.control.ExecutorStageDriver.
     */
    public void testDecreasePriority() {
        System.out.println("decreasePriority");
        
        double amount = 0.0;
        ExecutorStageDriver instance = null;
        
        instance.decreasePriority(amount);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
