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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import junit.framework.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.StageDriver.State;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.driver.*;
import org.apache.commons.pipeline.testFramework.TestStage;
import org.apache.commons.pipeline.util.BlockingQueueFactory;
import static org.apache.commons.pipeline.StageDriver.State.*;
import static org.apache.commons.pipeline.driver.FaultTolerance.*;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.testFramework.TestStageContext;

public class BalancedPoolStageDriverTest extends AbstractStageDriverTest {
    private Log log;
    
    public BalancedPoolStageDriverTest(String testName) {
        super(testName);
        this.log = LogFactory.getLog(BalancedPoolStageDriverTest.class);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /**
     * Test of getFeeder method, of class org.apache.commons.pipeline.driver.control.BalancedPoolStageDriver.
     */
    public void testGetFeeder() {
        System.out.println("testGetFeeder");
        BalancedPoolStageDriver instance = new BalancedPoolStageDriver(stage, context, new BlockingQueueFactory.LinkedBlockingQueueFactory(), 0, FaultTolerance.NONE, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
        Feeder feeder = instance.getFeeder();
        assertNotNull(feeder);
    }
    
    /**
     * Test of finish method, of class org.apache.commons.pipeline.driver.control.BalancedPoolStageDriver.
     */
    public void testFinish() throws Exception {
        System.out.println("testStart");
        BalancedPoolStageDriver instance = new BalancedPoolStageDriver(stage, context, new BlockingQueueFactory.LinkedBlockingQueueFactory(), 0, FaultTolerance.NONE, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
        instance.start(); //must start the driver before we can tell it to finish
        instance.finish();
        
        assertEquals(0, instance.getWorkerCount());
        assertEquals(State.STOPPED, instance.getState());
    }
    
    /**
     * Test of decreasePriority method, of class org.apache.commons.pipeline.driver.control.BalancedPoolStageDriver.
     */
    public void testAlterPriority() throws Exception {
        System.out.println("decreasePriority");
        
        BalancedPoolStageDriver instance = new BalancedPoolStageDriver(stage, context, new BlockingQueueFactory.LinkedBlockingQueueFactory(), 0, FaultTolerance.NONE, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
        assertSame("Driver should not be initialized in state " + instance.getState(), instance.getState(), State.STOPPED);
        instance.start();
        assertSame("Driver is not running after instance start.", instance.getState(), State.RUNNING);
        
        int threads = instance.getWorkerCount();
        instance.increasePriority(2);
        assertEquals( 2, instance.getWorkerCount() );
        
        threads = instance.getWorkerCount();
        instance.decreasePriority(1);
        assertEquals( 1, instance.getWorkerCount() );
        
        assertNotSame("Driver has unexpectedly stopped.", instance.getState(), State.STOPPED);
        instance.finish();
        assertSame("Driver failed to shut down correctly", instance.getState(), State.STOPPED);
    }
    
    /**
     * Integration test of combined feed/priority change for objects.
     */
    public void testSingleStage() throws Exception {
        TestStage stage = new TestStage(0);
        PrioritizableStageDriverTestUtils.testSingleStage(this, new BalancedPoolStageDriverFactory(), stage);
    }
    
    /**
     * Integration test of combined feed/priority change for objects.
     */
    public void testMultiStage() throws Exception {
        log.debug("testMultiStage -------------------------------------------");
        TestStage[] stages = {
            new TestStage(0),
            new CPUBoundTestStage(1, 50),
            new IOBoundTestStage(2, 50, 250)
        };
        PrioritizableStageDriverTestUtils.testMultiStage(this, new BalancedPoolStageDriverFactory(), 30, stages);
    }
    
    public void testCPUBound() throws Exception {
        TestStage cpuStage = new CPUBoundTestStage( 1, 50 );
        PrioritizableStageDriverTestUtils.testSingleStage(this, new BalancedPoolStageDriverFactory(), cpuStage );
    }
    
    public void testIOBound() throws Exception {
        TestStage ioStage = new IOBoundTestStage( 1, 50, 250 );
        PrioritizableStageDriverTestUtils.testSingleStage( this, new BalancedPoolStageDriverFactory(), ioStage );
    }
    
    /**
     * Test of increasePriority method, sof class org.apache.commons.pipeline.driver.control.BalancedPoolStageDriver.
     */
    public void testMultiThreadExecution() throws Exception {
        System.out.println("multiThreadExecution");
        
        //start with multiple threads
        int count = 5;
        final Set threadNames = Collections.synchronizedSet(new HashSet());
        TestStage threadNameTrackingStage = new TestStage( 1 ) {
            public void process( Object obj ) throws StageException {
                super.process( obj );
                threadNames.add( Thread.currentThread().getName() );
                //yield thread control
                Thread.currentThread().yield();
            }
        };
        
        BalancedPoolStageDriver instance = new BalancedPoolStageDriver(threadNameTrackingStage, context, new BlockingQueueFactory.LinkedBlockingQueueFactory(), 0, FaultTolerance.NONE, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
        instance.increasePriority( count );
        assertEquals( count, instance.getWorkerCount() );
        
        TestStageContext context = new TestStageContext();
        TestFeeder terminalFeeder = new TestFeeder();
        
        context.registerDownstreamFeeder(threadNameTrackingStage, terminalFeeder);
        threadNameTrackingStage.init(context);
        
        instance.start();
        
        //ensure that with 1000 fed objects all the threads get exercised
        int fedObjCount = 100;
        for( int i = 0; i < fedObjCount; i++ ) {
            instance.getFeeder().feed( i );
        }
        
        instance.finish();
        
        assertEquals( count, threadNames.size() );
    }
    
}
