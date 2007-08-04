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

package org.apache.commons.pipeline.driver;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.StageDriver.State;


/**
 *
 *
 */
public class SynchronousStageDriverTest extends AbstractStageDriverTest {
    
    public SynchronousStageDriverTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SynchronousStageDriverTest.class);
        
        return suite;
    }
    
    /**
     * Test of getFeeder method, of class org.apache.commons.pipeline.driver.SynchronousStageDriver.
     */
    public void testGetFeeder() {
        SynchronousStageDriver instance = new SynchronousStageDriver(stage, context, FaultTolerance.NONE);
        
        Feeder feeder = instance.getFeeder();
        assertNotNull(feeder);
    }
    
    /**
     * Due to the design of the SynchronousStageDriver, it is meaningless
     * to independently test the start or finish methods; however, testing
     * both together is meaningful. This test also provides verification of
     * proper behavior of the getState() method.
     */
    public void testStartFinish() throws Exception {
        SynchronousStageDriver instance = new SynchronousStageDriver(stage, context, FaultTolerance.NONE);
        
        assertEquals(instance.getState(), State.STOPPED);
        
        instance.start();
        
        assertEquals(instance.getState(), State.RUNNING);
        
        instance.finish();
        
        assertEquals(instance.getState(), State.STOPPED);
    }
    
    /*********************
     * INTEGRATION TESTS *
     *********************/
    
    public void testSingleStage() throws Exception {
        StageDriverTestUtils.testSingleStage(this, new SynchronousStageDriverFactory());
    }
    
    public void testMultiStage() throws Exception {
        StageDriverTestUtils.testMultiStage(this, new SynchronousStageDriverFactory());
    }
    
}
