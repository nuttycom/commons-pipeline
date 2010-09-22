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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;
import org.apache.commons.pipeline.PipelineLifecycleJob;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.testFramework.TestStage;
import org.apache.commons.pipeline.testFramework.TestStageContext;

public class PrioritizableStageDriverTestUtils {
    
    /** Creates a new instance of PrioritizableStageDriverTestUtils */
    private PrioritizableStageDriverTestUtils() {
    }

    /**
     * Tests a stage driver created by the provided PrioritizableStageDriverFactory
     * with a single test stage.
     */
    public static void testSingleStage(TestCase test, PrioritizableStageDriverFactory driverFactory, TestStage stage) throws Exception {
        TestStageContext context = new TestStageContext();
        TestFeeder terminalFeeder = new TestFeeder();

        context.registerDownstreamFeeder(stage, terminalFeeder);
        stage.init(context);

        PrioritizableStageDriver instance = driverFactory.createStageDriver(stage, context);

        instance.start();
        instance.getFeeder().feed("Hello, world!");
        instance.increasePriority(1);
        instance.getFeeder().feed("How are you?");
        instance.increasePriority(1);
        instance.getFeeder().feed("Feeling blue?");
        instance.decreasePriority(2);
        instance.getFeeder().feed("There's naught to do,");
        instance.decreasePriority(1);
        instance.getFeeder().feed("but sing about shoes!");
        instance.finish();

        test.assertEquals("Incorrect processed object count from stage.", 5, stage.processedObjects.size());
        test.assertEquals("Incorrect final processed object count.", 5, terminalFeeder.receivedValues.size());
        for (int i = 0; i < 5; i++) {
            test.assertTrue("Received value " + i + " is not a String!", terminalFeeder.receivedValues.get(i) instanceof String);
        }
    }

    /**
     * Tests a stage driver created by the provided StageDriverFactory
     * with a set of three test stages.
     */
    public static void testMultiStage(TestCase test, PrioritizableStageDriverFactory driverFactory, int objectsToFeed, TestStage... stages) throws Exception {
        TestStageContext context = new TestStageContext();
        TestFeeder terminalFeeder = new TestFeeder();

        List<PrioritizableStageDriver> drivers = new ArrayList<PrioritizableStageDriver>();
        for (TestStage stage : stages) {
            drivers.add(driverFactory.createStageDriver(stage, context));
            stage.init(context);
        }

        for (int i = 0; i < drivers.size(); i++) {
            if (i < drivers.size() - 1) {
                context.registerDownstreamFeeder(stages[i], drivers.get(i+1).getFeeder());
            } else {
                context.registerDownstreamFeeder(stages[i], terminalFeeder);
    }
        }

        Random random = new Random(0);
        for (StageDriver driver : drivers) driver.start();
        for( int i = 0; i < objectsToFeed; i++ ) {
            drivers.get(0).getFeeder().feed( i );

            //randomly permute driver priority
            if (random.nextBoolean()) {
                drivers.get(random.nextInt(drivers.size())).increasePriority(1);
            } else {
                drivers.get(random.nextInt(drivers.size())).decreasePriority(1);
            }
        }
        for (StageDriver driver : drivers) driver.finish();

        for (TestStage stage : stages) {
            test.assertEquals("Incorrect processed object count from stage " + stage, objectsToFeed, stage.processedObjects.size());
        }

        test.assertEquals("Incorrect final processed object count.", objectsToFeed, terminalFeeder.receivedValues.size());
    }

    /**
     * Tests that the given PrioritizableStageDriverFactory priority is driven
     * towards 0 given a number of CPU tasks and a controller
     */
    public static void testDriverControllerCPUBound(TestCase test, PrioritizableStageDriverFactory driverFactory, AbstractDriverController controller ) throws Exception {
        TestStage stage = new CPUBoundTestStage(0,300);
        TestFeeder terminalFeeder = new TestFeeder();
        TestStageContext context = new TestStageContext();
        context.registerDownstreamFeeder(stage, terminalFeeder);
        context.registerListener(controller);

        stage.init(context);

        //start the drivercontroller
        if( controller instanceof PipelineLifecycleJob ) {
            ((PipelineLifecycleJob) controller).onStart( null );
        }

        PrioritizableStageDriver instance = driverFactory.createStageDriver(stage, context);
    
        controller.addManagedStageDriver( instance );
        long initialPriority = 2;
        instance.increasePriority( initialPriority );
        test.assertEquals( initialPriority, Math.round( instance.getPriority()));

        instance.start();
        int numFeeds = 500;
        for( int i = 0; i < numFeeds; i++ ) {
            instance.getFeeder().feed("Hello, world!");
        }

        long startTime = System.currentTimeMillis();
        long feedTime = 15000;
        while( (System.currentTimeMillis()-startTime)<feedTime)
        {
            instance.getFeeder().feed("Hello, world!");
            Thread.currentThread().sleep( 200 );
        }
        test.assertTrue( "Controller should have decreased priority below "+
                initialPriority+", instead was "+instance.getPriority(),
                instance.getPriority() < initialPriority );
        instance.finish();
    }

    /**
     * Tests that the given PrioritizableStageDriverFactory priority is driven
     * up given a number of IO tasks and a controller
     */
    public static void testDriverControllerIOBound(TestCase test, PrioritizableStageDriverFactory driverFactory, AbstractDriverController controller ) throws Exception {
        TestStage stage = new IOBoundTestStage(0,0,150);
        TestFeeder terminalFeeder = new TestFeeder();
        TestStageContext context = new TestStageContext();
        context.registerDownstreamFeeder(stage, terminalFeeder);
        context.registerListener(controller);

        stage.init(context);

        //start the drivercontroller
        if( controller instanceof PipelineLifecycleJob ) {
            ((PipelineLifecycleJob) controller).onStart( null );
        }

        PrioritizableStageDriver instance = driverFactory.createStageDriver(stage, context);

        controller.addManagedStageDriver( instance );
        long initialPriority = 1;
        instance.increasePriority( initialPriority );
        test.assertEquals( initialPriority, Math.round( instance.getPriority()));

        instance.start();
        long startTime = System.currentTimeMillis();
        long feedTime = 20000;
        while( (System.currentTimeMillis()-startTime)<feedTime)
        {
            instance.getFeeder().feed("Hello, world!");
            Thread.currentThread().sleep( 100 );
        }

        test.assertTrue( "Controller should have increased priority above "+
                initialPriority+", instead was "+instance.getPriority(),
                instance.getPriority() > initialPriority );

        instance.finish();
    }

    /**
     * Consume resources in O(n^2) fashion
     *
     * Return the aggregate value so the sqrt operation is less likely to be
     * optimized by the JRE
     */
    public static double consumeNSquared( long operations ){
        double aggregate = 0;
        for( int i = 0; i < operations; i++ ) {
            for( int j = 0; j < operations; j++ ) {
                aggregate += Math.sqrt( i + j );
            }
        }
        return aggregate;
    }

    /**
     * Consume resources in O(n^3) fashion
     *
     * Return the aggregate value so the sqrt operation is less likely to be
     * optimized by the JRE
     */
    public static double consumeNCubed( long operations ){
        double aggregate = 0;
        for( int i = 0; i < operations; i++ ) {
            for( int j = 0; j < operations; j++ ) {
                for( int k = 0; k < operations; k++ ) {
                    aggregate += Math.sqrt( i + j );
                }
            }
        }
        return aggregate;
    }
}
