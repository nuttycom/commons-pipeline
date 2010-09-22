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

import junit.framework.TestCase;

import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageDriverFactory;
import org.apache.commons.pipeline.testFramework.FaultingTestStage;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.testFramework.TestStage;
import org.apache.commons.pipeline.testFramework.TestStageContext;

/**
 *
 *
 */
public class StageDriverTestUtils {    
    
    /**
     * Tests a stage driver created by the provided StageDriverFactory
     * with a single test stage.
     */
    public static void testSingleStage(TestCase test, StageDriverFactory driverFactory) throws Exception {
        TestStageContext context = new TestStageContext();
        TestFeeder terminalFeeder = new TestFeeder();

        TestStage stage = new TestStage(0);
        context.registerDownstreamFeeder(stage, terminalFeeder);
        stage.init(context);
        
        StageDriver instance = driverFactory.createStageDriver(stage, context);
        
        instance.start();
        instance.getFeeder().feed("Hello, world!");
        instance.getFeeder().feed("How are you?");
        instance.finish();
        
        test.assertEquals("Incorrect processed object count from stage.", 2, stage.processedObjects.size());
        test.assertEquals("Incorrect final processed object count.", 2, terminalFeeder.receivedValues.size());
        test.assertTrue(terminalFeeder.receivedValues.get(0) instanceof String);
        test.assertTrue(terminalFeeder.receivedValues.get(1) instanceof String);
        test.assertEquals("Hello, world!", (String) terminalFeeder.receivedValues.get(0));
        test.assertEquals("How are you?", (String) terminalFeeder.receivedValues.get(1));
    }
    
    /**
     * Tests a stage driver created by the provided StageDriverFactory
     * with a set of three test stages.
     */
    public static void testMultiStage(TestCase test, StageDriverFactory driverFactory) throws Exception {
        TestStageContext context = new TestStageContext();
        TestFeeder terminalFeeder = new TestFeeder();
        
        TestStage stage0 = new TestStage(0);
        StageDriver d0 = driverFactory.createStageDriver(stage0, context);
        
        TestStage stage1 = new TestStage(1);
        StageDriver d1 = driverFactory.createStageDriver(stage1, context);
        
        TestStage stage2 = new TestStage(2);
        StageDriver d2 = driverFactory.createStageDriver(stage2, context);
        
        context.registerDownstreamFeeder(stage0, d1.getFeeder());
        context.registerDownstreamFeeder(stage1, d2.getFeeder());
        context.registerDownstreamFeeder(stage2, terminalFeeder);
        
        stage0.init(context);
        stage1.init(context);
        stage2.init(context);
        
        d0.start();
        d1.start();
        d2.start();
        d0.getFeeder().feed("Hello, world!");
        d0.getFeeder().feed("How are you?");
        d0.finish();
        d1.finish();
        d2.finish();
        
        test.assertEquals("Incorrect processed object count from stage 0.", 2, stage0.processedObjects.size());
        test.assertEquals("Incorrect processed object count from stage 1.", 2, stage1.processedObjects.size());
        test.assertEquals("Incorrect processed object count from stage 2.", 2, stage2.processedObjects.size());
        
        test.assertEquals("Incorrect final processed object count.", 2, terminalFeeder.receivedValues.size());
        test.assertTrue(terminalFeeder.receivedValues.get(0) instanceof String);
        test.assertTrue(terminalFeeder.receivedValues.get(1) instanceof String);
        test.assertEquals("Hello, world!", (String) terminalFeeder.receivedValues.get(0));
        test.assertEquals("How are you?", (String) terminalFeeder.receivedValues.get(1));        
    }    
    
    /**
     * Tests a stage driver created by the provided StageDriverFactory
     * with a set of three test stages.
     */
    public static void testMultiFaultingStage(TestCase test, StageDriverFactory driverFactory) throws Exception {
        TestStageContext context = new TestStageContext();
        TestFeeder terminalFeeder = new TestFeeder();
        
        TestStage stage0 = new TestStage(0);
        StageDriver d0 = driverFactory.createStageDriver(stage0, context);
        
        TestStage stage1 = new FaultingTestStage(1);
        StageDriver d1 = driverFactory.createStageDriver(stage1, context);
        
        TestStage stage2 = new TestStage(2);
        StageDriver d2 = driverFactory.createStageDriver(stage2, context);
        
        context.registerDownstreamFeeder(stage0, d1.getFeeder());
        context.registerDownstreamFeeder(stage1, d2.getFeeder());
        context.registerDownstreamFeeder(stage2, terminalFeeder);
        
        stage0.init(context);
        stage1.init(context);
        stage2.init(context);
        
        d0.start();
        d1.start();
        d2.start();
        d0.getFeeder().feed("Hello, world!");
        d0.getFeeder().feed("How are you?");
        d0.getFeeder().feed("I am fine!");
        d0.finish();
        d1.finish();
        d2.finish();
        
        test.assertEquals("Incorrect processed object count from stage 0.", 3, stage0.processedObjects.size());
        test.assertEquals("Incorrect processed object count from stage 1.", 2, stage1.processedObjects.size());
        test.assertEquals("Incorrect number of processing failures recorded by driver 2.", 1, d1.getProcessingExceptions().size());
        test.assertEquals("Incorrect processed object count from stage 2.", 2, stage2.processedObjects.size());
        
        test.assertEquals("Incorrect final processed object count.", 2, terminalFeeder.receivedValues.size());
        test.assertTrue(terminalFeeder.receivedValues.get(0) instanceof String);
        test.assertTrue(terminalFeeder.receivedValues.get(1) instanceof String);
        test.assertEquals("Hello, world!", (String) terminalFeeder.receivedValues.get(0));
        test.assertEquals("I am fine!", (String) terminalFeeder.receivedValues.get(1));        
    }    
    
}
