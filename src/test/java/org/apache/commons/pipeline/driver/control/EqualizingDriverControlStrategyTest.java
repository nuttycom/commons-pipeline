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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.pipeline.Stage;

public class EqualizingDriverControlStrategyTest extends TestCase {
    
    public EqualizingDriverControlStrategyTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of handleEvents method, of class org.apache.commons.pipeline.driver.control.EqualizingDriverControlStrategy.
     */
    public void testHandleEvents() {
        System.out.println("handleEvents");
        
        List<PrioritizableStageDriver> drivers = null;
        List<StageProcessTimingEvent> events = null;
        EqualizingDriverControlStrategy instance = new EqualizingDriverControlStrategy();
        
        instance.handleEvents(drivers, events);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getAllowableDelta method, of class org.apache.commons.pipeline.driver.control.EqualizingDriverControlStrategy.
     */
    public void testGetAllowableDelta() {
        System.out.println("getAllowableDelta");
        
        EqualizingDriverControlStrategy instance = new EqualizingDriverControlStrategy();
        
        long expResult = 0L;
        long result = instance.getAllowableDelta();
        assertEquals(expResult, result);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of setAllowableDelta method, of class org.apache.commons.pipeline.driver.control.EqualizingDriverControlStrategy.
     */
    public void testSetAllowableDelta() {
        System.out.println("setAllowableDelta");
        
        long allowableDelta = 0L;
        EqualizingDriverControlStrategy instance = new EqualizingDriverControlStrategy();
        
        instance.setAllowableDelta(allowableDelta);
        
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
