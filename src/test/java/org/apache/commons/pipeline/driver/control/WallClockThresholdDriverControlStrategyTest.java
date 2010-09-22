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

public class WallClockThresholdDriverControlStrategyTest extends TestCase {
    
    public WallClockThresholdDriverControlStrategyTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(WallClockThresholdDriverControlStrategyTest.class);
        
        return suite;
    }

    public void testCPUBoundControl() throws Exception
    {
        //System.out.println( "WallClock: testCPUBoundControl");
        CountingDriverController controller = new CountingDriverController();
        controller.setMinimumEventsToHandle( 10 );
        controller.setDriverControlStrategy( new WallClockThresholdDriverControlStrategy() );
        PrioritizableStageDriverTestUtils.testDriverControllerCPUBound( 
                this, 
                new BalancedPoolStageDriverFactory(), 
                controller );
    }
    
    public void testIOBoundControl() throws Exception
    {
        //System.out.println( "WallClock: testIOBoundControl");
        CountingDriverController controller = new CountingDriverController();
        controller.setMinimumEventsToHandle( 10 );
        controller.setDriverControlStrategy( new WallClockThresholdDriverControlStrategy() );
        PrioritizableStageDriverTestUtils.testDriverControllerIOBound( 
                this, 
                new BalancedPoolStageDriverFactory(), 
                controller );
    }
}
