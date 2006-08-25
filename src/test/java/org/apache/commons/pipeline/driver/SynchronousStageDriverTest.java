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

package org.apache.commons.pipeline.driver;

import junit.framework.*;
import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.StageDriver.State;
import static org.apache.commons.pipeline.StageDriver.State.*;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.testFramework.TestStage;
import org.apache.commons.pipeline.testFramework.TestStageContext;

/**
 *
 * @author kjn
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
        SynchronousStageDriver instance = new SynchronousStageDriver(stage, context);
        
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
        SynchronousStageDriver instance = new SynchronousStageDriver(stage, context);
        
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
