/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.commons.pipeline.config;

import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.AbstractLoggingTestCase;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.PipelineFactory;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageEventListener;
import org.apache.commons.pipeline.listener.ObjectProcessedEventCounter;
import org.apache.commons.pipeline.stage.KeyWaitBufferStage;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.util.BlockingQueueFactory;

/**
 * Test cases for the DigesterPipelineFactory.
 */
public class DigesterPipelineFactoryTest extends AbstractLoggingTestCase {
    private ResourceBundle testResources = ResourceBundle.getBundle("TestResources");
    private String keyBase = "test.DigesterPipelineFactoryTest";
    
    public DigesterPipelineFactoryTest(String testName) {
        super(testName);
    }
    
    public static junit.framework.Test suite() {
        junit.framework.TestSuite suite = new junit.framework.TestSuite(DigesterPipelineFactoryTest.class);
        
        return suite;
    }
    
    public void testCreatePipeline() throws Exception {
        Log log = LogFactory.getLog(this.getClass());
        URL confURL = this.getClass().getClassLoader().getResource(testResources.getString(keyBase + ".configFile"));
        PipelineFactory factory = new DigesterPipelineFactory(confURL);
        
        Pipeline pipeline = factory.createPipeline();
        TestFeeder terminalFeeder = new TestFeeder();
        pipeline.setTerminalFeeder(terminalFeeder);
        assertNotNull("Pipeline exists.", pipeline);
        
        int i = 0;
        for (Stage stage : pipeline.getStages()) {
            assertNotNull("Stage is not null.", stage);
            assertEquals(stage.getClass(), Class.forName(testResources.getString(keyBase + ".stage" + i + ".class")));
            i++;
            
            if (stage instanceof KeyWaitBufferStage) {
                assertTrue(((KeyWaitBufferStage) stage).getQueueFactory() instanceof BlockingQueueFactory.ArrayBlockingQueueFactory);
            }
        }
        
        pipeline.run();
        
        boolean eventsRecorded = false;
        assertTrue("Pipeline has at least one listener.", pipeline.getRegisteredListeners().size() > 0);
        for (StageEventListener l : pipeline.getRegisteredListeners()) {
            if (l instanceof ObjectProcessedEventCounter) {
                log.info(((ObjectProcessedEventCounter) l).getCounts().size() + " event sources found.");
                assertTrue("No events were recorded by the ObjectProcessedEventListener.", ((ObjectProcessedEventCounter) l).getCounts().size() > 0);
                eventsRecorded = true;
            }
        }
        assertTrue("Events were not raised and properly recorded by the listener.", eventsRecorded);
        
        assertNotNull(pipeline.getEnv("testDate"));
        assertEquals("Hello, World!", pipeline.getEnv("testEnvVar"));
        assertTrue("Terminal feeder did not receive any data.", terminalFeeder.receivedValues.size() > 0);
    }
}
