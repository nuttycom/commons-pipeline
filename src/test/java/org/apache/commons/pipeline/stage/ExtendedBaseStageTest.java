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

import java.util.regex.Pattern;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.pipeline.driver.SynchronousStageDriverFactory;
import org.apache.commons.pipeline.event.ObjectProcessedEvent;
import org.apache.commons.pipeline.listener.ObjectProcessedEventCounter;
import org.apache.commons.pipeline.testFramework.TestStage;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.StageException;

/**
 * Test cases for the extended base stage. Since it is abstract these tests have
 * to use a subclass. In order to prevent dependency problems this subclass is defined 
 * as a private class.
 */
public class ExtendedBaseStageTest extends AbstractStageTest {
    TestStage stage = null;

    public ExtendedBaseStageTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        super.setUp();
        stage = new TestStage();
        this.init(stage);
    }

    public void testPreprocess() throws Exception {
        stage.preprocess();
        assertTrue("Preprocess call failed to call inner method", stage.preprocessed);
        assertEquals("Incorrectly reporting objects recieved", 0,stage.getObjectsReceived());
        assertEquals("Incorrectly reporting objects emitted", 0,stage.getTotalEmits());
    }

    public void testPostprocess() throws Exception {
        stage.preprocess();
        stage.postprocess();
        assertTrue("Postprocess call failed to call inner method", stage.postprocessed);
        assertEquals("Incorrectly reporting objects recieved", 0,stage.getObjectsReceived());
        assertEquals("Incorrectly reporting objects emitted", 0,stage.getTotalEmits());
    }

    /**
     * Verify the emit statistics are correctly reported.
     */
    public void testEmits() throws Exception {
        // Redefine the process method.
        stage = defineStage(new TestStage() {
            @Override
            public void innerProcess(Object obj) {
                super.emit(obj);
            }
        });
        Object value = new Object();
        stage.process(value);
        // Verify output.
        assertEquals("Stage did not emit value as expected",
                     value, testFeeder.receivedValues.get(0));
        // Verify stats correctly recorded.
        assertEquals("Incorrectly reporting objects recieved", 1,stage.getObjectsReceived());
        assertEquals("Incorrectly reporting objects emitted", 1,stage.getTotalEmits());
        // Make sure the stats message is correct.
        String message = stage.getStatusMessage();
        assertMatches("Total objects received:1", message);
        assertMatches("Total objects emitted:1 \\(100\\.000%\\)", message);
    }

    /**
     * Verify the statistics are correctly reported when nothing is emitted
     */
    public void testNonEmit() throws Exception {
        stage.preprocess();
        Object value = new Object();
        stage.process(value);
        // Verify output.
        assertTrue("Stage emitted something unexpectedly", testFeeder.receivedValues.isEmpty());
        // Verify stats correctly recorded.
        assertEquals("Incorrectly reporting objects recieved", 1,stage.getObjectsReceived());
        assertEquals("Incorrectly reporting objects emitted", 0,stage.getTotalEmits());
        // Make sure the stats message is correct.
        String message = stage.getStatusMessage();
        assertMatches("Total objects received:1", message);
        // No % display on 0 emits.
        assertMatches("Total objects emitted:0", message);
    }

    public void testProcessingTimeStats() throws Exception {
        // Redefine the process method.
        stage = defineStage(new TestStage() {
            @Override
            public void innerProcess(Object obj) {
                try {
                    Thread.sleep(33);
                } catch (InterruptedException e) {
                    // Ignored.
                }
                super.emit(obj);
            }
        });
        Object value = new Object();
        stage.process(value);
        // Verify stats correctly recorded.
        assertEquals("Incorrectly reporting objects recieved", 1,stage.getObjectsReceived());
        assertEquals("Incorrectly reporting objects emitted", 1,stage.getTotalEmits());
        // Make sure the stats message is correct.
        String message = stage.getStatusMessage();
        assertMatches("% time working:100\\.000", message);
        assertMatches("% time blocking:0\\.000", message);
        // Should be between 0.032 and 0.034 seconds. Allow up to 0.099 for busy systems
        assertMatches("Total net processing time \\(sec\\):0\\.0[0-9]{2}",
                      message);
        assertMatches("Total gross processing time \\(sec\\):0\\.0[0-9]{2}",
                      message);
        assertMatches("Average net processing time \\(sec/obj\\):0\\.0[0-9]{2}",
                      message);
        assertMatches("Average gross processing time \\(sec/obj\\):0\\.0[0-9]{2}",
                      message);
        // Moving window display
        assertMatches("Average gross processing time in last 1 \\(sec/obj\\):0\\.0[0-9]{2}",
                      message);
    }

    public void testBranchStatistics() throws Exception {
        // In order to log the stats we need some blocking. Redefine one of the two branches
        // used below to block.
        testContext.branchFeeders.put("A",new TestFeeder() {
            @Override
            public void feed(Object obj) {
                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    // Ignored.
                }
                super.feed(obj);
            }
        });
        // Redefine the process method.
        stage = defineStage(new TestStage() {
            @Override
            public void innerProcess(Object obj) {
                // Hald to one queue, half to another.
                if (getObjectsReceived()%2 == 0) {
                    super.emit("A", obj);
                } else {
                    super.emit("B", obj);
                }
            }
        });
        stage.setCollectBranchStats(true);
        stage.process("Object 1");
        stage.process("Object 2");
        // Verify overall stats correctly recorded.
        assertEquals("Incorrectly reporting objects recieved", 2,stage.getObjectsReceived());
        assertEquals("Incorrectly reporting objects emitted", 2,stage.getTotalEmits());
        // Check the stats.
        String message = stage.getStatusMessage();
        // Should be 100, maybe 99 in the off chance of a very busy system.
        assertMatches("% branch A:[0-9]{2,3}\\.[0-9]{3}", message);
        assertMatches("% branch B:0\\.000", message);
    }

    public void testStatusInterval() {
        stage.setStatusInterval(500L);
        assertEquals("Failed to set value correctly", 500L, stage.getStatusInterval().longValue());
    }

    public void testStatusBatchSize() {
        stage.setStatusBatchSize(500);
        assertEquals("Failed to set value correctly", 500, stage.getStatusBatchSize().longValue());
    }

    public void testCollectBranchStats() {
        stage.setCollectBranchStats(true);
        assertTrue("Failed to set value correctly", stage.getCollectBranchStats());
    }

    public void testCurrentStatWindowSize() {
        stage.setCurrentStatWindowSize(200);
        assertEquals("Failed to set value correctly", 200, stage.getCurrentStatWindowSize().intValue());
    }

    public void testJmxEnabled() {
        stage.setJmxEnabled(true);
        assertTrue("Failed to set value correctly", stage.isJmxEnabled());
        stage.setJmxEnabled(false);
        assertFalse("Failed to set value correctly", stage.isJmxEnabled());
    }

    public void testStageName() {
        stage.setStageName("foo");
        assertEquals("Failed to set value correctly", "foo", stage.getStageName());
    }

    private void assertMatches(String pattern, String string) {
        assertMatches("Failed to match expected pattern '" + pattern + "' in \n" + string,
                      pattern, string);
    }

    private void assertMatches(String message, String pattern, String string) {
        Pattern pat = Pattern.compile(".*" + pattern + ".*", Pattern.DOTALL|Pattern.CASE_INSENSITIVE);
        assertTrue(message, pat.matcher(string).matches());
    }

    /**
     * Make sure the preprocessing steps are always completed. 
     */
    private TestStage defineStage(TestStage stage) throws StageException {
        this.init(stage);
        stage.preprocess();
        return stage;
    }

    /**
     * Private stub stage to record some information.
     */
    private class TestStage extends ExtendedBaseStage {
        boolean preprocessed = false;
        boolean postprocessed = false;

        public void innerPreprocess() {
            preprocessed = true;
        }

        public void innerPostprocess() {
            postprocessed = true;
        }

        public void innerProcess(Object obj) {
            // Overridden by some tests. For others this is a NO-OP
        }

        public String status() {
            return null;
        }
    }
}
