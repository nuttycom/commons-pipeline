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

import org.apache.commons.pipeline.AbstractLoggingTestCase;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.testFramework.TestStage;
import org.apache.commons.pipeline.testFramework.TestStageContext;

/**
 * Abstract base class for stage driver tests.
 *
 *
 */
public abstract class AbstractStageDriverTest extends AbstractLoggingTestCase {
    protected TestStage stage;
    protected TestStageContext context;
    protected TestFeeder feeder;
    
    public AbstractStageDriverTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {       
        this.stage = new TestStage(0);
        this.context = new TestStageContext();
        this.feeder = new TestFeeder();
        
        //make sure that anything fed to the stage has somewhere to go
        this.context.registerDownstreamFeeder(this.stage, this.feeder);
        
        //give the stage the context to provide access to the feeder
        this.stage.init(this.context);
    }
    
    protected void tearDown() throws Exception {
        this.stage = null;
        this.context = null;
        this.feeder = null;
    }    
}
