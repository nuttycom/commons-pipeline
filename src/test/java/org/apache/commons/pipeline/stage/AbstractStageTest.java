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

import org.apache.commons.pipeline.AbstractLoggingTestCase;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.testFramework.TestStageContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Simple abstract base class for stage tests.
 *
 *
 */
public abstract class AbstractStageTest extends AbstractLoggingTestCase {
    
    //initialize the testing context
    protected TestStageContext testContext;
    protected TestFeeder testFeeder;
    
    public AbstractStageTest(String testName) {
        super(testName);
    }

    @Before
    @Override
    protected void setUp() throws Exception {
        this.testContext = new TestStageContext();
        this.testFeeder = new TestFeeder();
    }

    @After
    @Override
    protected void tearDown() throws Exception {
        this.testContext = null;
        this.testFeeder = null;
    }
    
    protected void init(Stage stage) {
        testContext.registerDownstreamFeeder(stage, testFeeder);
        stage.init(testContext);        
    }

    /** This is to make the test framework happy, it seems to require at least
     * one test in this file if this class is not abstract.
     */
//    @Test
//    public void testNothing() {
//        assertTrue(true);
//    }
}
