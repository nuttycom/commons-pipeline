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

package org.apache.commons.pipeline;

import java.util.Collection;
import java.util.Collections;
import java.util.EventObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.pipeline.driver.SynchronousStageDriverFactory;
import org.apache.commons.pipeline.event.ObjectProcessedEvent;
import org.apache.commons.pipeline.listener.ObjectProcessedEventCounter;
import org.apache.commons.pipeline.testFramework.TestStage;

/**
 * Test cases
 */
public class PipelineTest extends TestCase {
    
    public PipelineTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(PipelineTest.class);
        
        return suite;
    }
    
    /**
     * Test of registerListener method, of class org.apache.commons.pipeline.Pipeline.
     */
    public void testRegisterListener() {
        StageEventListener listener = new ObjectProcessedEventCounter();
        Pipeline instance = new Pipeline();
        
        instance.registerListener(listener);
        
        assertEquals(1, instance.getRegisteredListeners().size());
    }
    
    /**
     * Test of getRegisteredListeners method, of class org.apache.commons.pipeline.Pipeline.
     */
    public void testGetRegisteredListeners() {
        Pipeline instance = new Pipeline();
        
        Collection<StageEventListener> expResult = Collections.EMPTY_LIST;
        Collection<StageEventListener> result = instance.getRegisteredListeners();
        assertEquals(expResult, result);
    }
    
    /**
     * Test of raise method, of class org.apache.commons.pipeline.Pipeline.
     */
    public void testRaise() throws Exception {
        Stage testStage = new TestStage(0);
        EventObject ev = new ObjectProcessedEvent(testStage, "Hello, World!");
        Pipeline instance = new Pipeline();
        ObjectProcessedEventCounter counter = new ObjectProcessedEventCounter();
        instance.registerListener(counter);
        
        instance.raise(ev);
        
        synchronized(counter) {
            while (counter.getCounts().get(testStage) == null) counter.wait(100);
        }
        
        assertNotNull("No events were received.", counter.getCounts().get(testStage));
        assertEquals("Only one event should have been received.", 1, counter.getCounts().get(testStage).intValue());
    }
    
    public void testRaiseOnBranch() throws Exception {
        Pipeline root = new Pipeline();
        
        Pipeline branch1 = new Pipeline();
        root.addBranch("b1", branch1);
        
        Pipeline branch2 = new Pipeline();
        root.addBranch("b2", branch2);
        
        ObjectProcessedEventCounter counter = new ObjectProcessedEventCounter();
        branch2.registerListener(counter);
        
        Stage testStage = new TestStage(0);
        EventObject ev = new ObjectProcessedEvent(testStage, "Hello, World!");
        branch1.raise(ev);
        
        synchronized(counter) {
            while (counter.getCounts().get(testStage) == null) counter.wait(100);
        }
        
        assertNotNull(counter.getCounts().get(testStage));
        assertEquals(1, counter.getCounts().get(testStage).intValue());
    }
    
    /**
     * Test of getDownstreamFeeder method, of class org.apache.commons.pipeline.Pipeline.
     */
    public void testGetDownstreamFeeder() throws Exception {
        Stage stage1 = new TestStage(0);
        Stage stage2 = new TestStage(1);
        StageDriverFactory sdf = new SynchronousStageDriverFactory();

        Pipeline instance = new Pipeline();
        instance.addStage(stage1, sdf);
        instance.addStage(stage2, sdf);

        Feeder expResult = instance.getStageDriver(stage2).getFeeder();
        Feeder result = instance.getDownstreamFeeder(stage1);
        assertSame(expResult, result);
    }
    
    /**
     * Test of getBranchFeeder method, of class org.apache.commons.pipeline.Pipeline.
     */
    public void testGetBranchFeeder() throws Exception {
        String branchKey = "b1";
        Pipeline root = new Pipeline();
        Pipeline branch = new Pipeline();
        root.addBranch(branchKey, branch);
        
        Feeder expResult = branch.getTerminalFeeder(); //no feeders registered
        Feeder result = root.getBranchFeeder(branchKey);
        assertSame(expResult, result);
        
        StageDriverFactory sdf = new SynchronousStageDriverFactory();
        Stage testStage = new TestStage(0);
        branch.addStage(testStage, sdf);
        
        expResult = branch.getStageDriver(testStage).getFeeder();
        result = root.getBranchFeeder(branchKey);
        assertSame(expResult, result);        
    }
    
//    /**
//     * Test of addStage method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testAddStage() throws Exception {
//        System.out.println("addStage");
//
//        Stage stage = null;
//        StageDriverFactory driverFactory = null;
//        Pipeline instance = new Pipeline();
//
//        instance.addStage(stage, driverFactory);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStages method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testGetStages() {
//        System.out.println("getStages");
//
//        Pipeline instance = new Pipeline();
//
//        List<Stage> expResult = null;
//        List<Stage> result = instance.getStages();
//        assertEquals(expResult, result);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStageDriver method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testGetStageDriver() {
//        System.out.println("getStageDriver");
//
//        Stage stage = null;
//        Pipeline instance = new Pipeline();
//
//        StageDriver expResult = null;
//        StageDriver result = instance.getStageDriver(stage);
//        assertEquals(expResult, result);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getStageDrivers method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testGetStageDrivers() {
//        System.out.println("getStageDrivers");
//
//        Pipeline instance = new Pipeline();
//
//        List<StageDriver> expResult = null;
//        List<StageDriver> result = instance.getStageDrivers();
//        assertEquals(expResult, result);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of addBranch method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testAddBranch() throws Exception {
//        System.out.println("addBranch");
//
//        String key = "";
//        Pipeline branch = null;
//        Pipeline instance = new Pipeline();
//
//        instance.addBranch(key, branch);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getBranches method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testGetBranches() {
//        System.out.println("getBranches");
//
//        Pipeline instance = new Pipeline();
//
//        Map<String, Pipeline> expResult = null;
//        Map<String, Pipeline> result = instance.getBranches();
//        assertEquals(expResult, result);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getSourceFeeder method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testGetSourceFeeder() {
//        System.out.println("getSourceFeeder");
//
//        Pipeline instance = new Pipeline();
//
//        Feeder expResult = null;
//        Feeder result = instance.getSourceFeeder();
//        assertEquals(expResult, result);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getTerminalFeeder method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testGetTerminalFeeder() {
//        System.out.println("getTerminalFeeder");
//
//        Pipeline instance = new Pipeline();
//
//        Feeder expResult = null;
//        Feeder result = instance.getTerminalFeeder();
//        assertEquals(expResult, result);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setTerminalFeeder method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testSetTerminalFeeder() {
//        System.out.println("setTerminalFeeder");
//
//        Feeder terminalFeeder = null;
//        Pipeline instance = new Pipeline();
//
//        instance.setTerminalFeeder(terminalFeeder);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of start method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testStart() throws Exception {
//        System.out.println("start");
//
//        Pipeline instance = new Pipeline();
//
//        instance.start();
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of finish method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testFinish() throws Exception {
//        System.out.println("finish");
//
//        Pipeline instance = new Pipeline();
//
//        instance.finish();
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of run method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testRun() {
//        System.out.println("run");
//
//        Pipeline instance = new Pipeline();
//
//        instance.run();
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getValidator method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testGetValidator() {
//        System.out.println("getValidator");
//
//        Pipeline instance = new Pipeline();
//
//        PipelineValidator expResult = null;
//        PipelineValidator result = instance.getValidator();
//        assertEquals(expResult, result);
//
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setValidator method, of class org.apache.commons.pipeline.Pipeline.
//     */
//    public void testSetValidator() {
//        System.out.println("setValidator");
//
//        PipelineValidator validator = null;
//        Pipeline instance = new Pipeline();
//
//        instance.setValidator(validator);
//
//        fail("The test case is a prototype.");
//    }
    
}
