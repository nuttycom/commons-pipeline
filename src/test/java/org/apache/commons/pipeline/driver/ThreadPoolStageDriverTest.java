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

import java.util.concurrent.LinkedBlockingQueue;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.StageDriver.State;

/**
 *
 *
 */
public class ThreadPoolStageDriverTest extends AbstractStageDriverTest {
    private Log log;
    
    public ThreadPoolStageDriverTest(String testName) {
        super(testName);
        this.log = LogFactory.getLog(ThreadPoolStageDriverTest.class);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(ThreadPoolStageDriverTest.class);
        
        return suite;
    }
    
    /**
     * Test of getFeeder method, of class {@link ThreadPoolStageDriver}.
     */
    public void testGetFeeder() {
        log.debug("testGetFeeder ---------------------------------------------");
        ThreadPoolStageDriver instance = new ThreadPoolStageDriver(stage, context, new LinkedBlockingQueue<Object>(), 500, FaultTolerance.NONE, 5);
        
        Feeder feeder = instance.getFeeder();
        assertNotNull(feeder);
    }
    
    /**
     * Due to the design of the {@link ThreadPoolStageDriver}, it is meaningless
     * to independently test the start or finish methods; however, testing
     * both together is meaningful. This test also provides verification of
     * proper behavior of the getState() method.
     */
    public void testStartFinish() throws Exception {
        log.debug("testStartFinish -------------------------------------------");
        ThreadPoolStageDriver instance = new ThreadPoolStageDriver(stage, context, new LinkedBlockingQueue<Object>(), 500, FaultTolerance.NONE, 5);
        
        assertEquals(State.STOPPED, instance.getState());
        
        instance.start();
        
        assertTrue(instance.getState() == State.STARTED || instance.getState() == State.RUNNING);
        
        instance.finish();
        
        assertEquals(State.STOPPED, instance.getState());
    }
    
    
    /*********************
     * INTEGRATION TESTS *
     *********************/
    
    public void testSingleStage() throws Exception {
        log.debug("testSingleStage -------------------------------------------");
        StageDriverTestUtils.testSingleStage(this, new ThreadPoolStageDriverFactory());
    }
    
    public void testMultiStage() throws Exception {
        log.debug("testMultiStage --------------------------------------------");
        StageDriverTestUtils.testMultiStage(this, new ThreadPoolStageDriverFactory());
    }
    
    public void testMultiFaultingStage() throws Exception {
        log.debug("testMultiFaultingStage ------------------------------------");
        ThreadPoolStageDriverFactory factory = new ThreadPoolStageDriverFactory();
        factory.setFaultTolerance(FaultTolerance.CHECKED);
        
        StageDriverTestUtils.testMultiFaultingStage(this, factory);
    }
}
