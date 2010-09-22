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

import junit.framework.TestCase;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.testFramework.TestStage;
import org.apache.commons.pipeline.util.BlockingQueueFactory;
import org.apache.commons.pipeline.validation.ValidationException;

public class ToyBalancedPipelineTest extends TestCase {
    public void testToyFactory() throws ValidationException, StageException {
        Pipeline pipeline = new Pipeline();
        CountingDriverController controller = new CountingDriverController();
        controller.setMinimumEventsToHandle(20);
        EqualizingDriverControlStrategy controlStrategy = new EqualizingDriverControlStrategy();
        controller.setDriverControlStrategy(controlStrategy);
        controlStrategy.setAllowableDelta(100);
        pipeline.addLifecycleJob(controller);
        TestFeeder terminalFeeder = new TestFeeder();
        pipeline.setTerminalFeeder(terminalFeeder);
        
        BalancedPoolStageDriverFactory driverFactory = new BalancedPoolStageDriverFactory();
        driverFactory.setInitialPriority(1);
        driverFactory.setQueueFactory(new BlockingQueueFactory.LinkedBlockingQueueFactory());
        
        TestStage moldBody = new MoldBodyStage(1);
        TestStage bodyPaint = new BodyPaintStage(2);
        TestStage addWheels = new AddWheelsStage(3);
        TestStage addAI = new AddAIStage(4);
        
        pipeline.addStage(moldBody, driverFactory);
        pipeline.addStage(bodyPaint, driverFactory);
        pipeline.addStage(addWheels, driverFactory);
        pipeline.addStage(addAI, driverFactory);
        
        for (StageDriver driver : pipeline.getStageDrivers()) {
            if (driver instanceof PrioritizableStageDriver) controller.addManagedStageDriver((PrioritizableStageDriver) driver);
        }
        
        DriverMonitor m = new DriverMonitor(pipeline);
        m.start();
        pipeline.start();
        
        for (int i = 0; i < 100; i++) {
            pipeline.getSourceFeeder().feed(new Car(i, "I'm a hunk of metal!"));
        }
        
        pipeline.finish();
        m.finish();
        
        assertEquals("Incorrect number of objects received.", 100, terminalFeeder.receivedValues.size());
        for (Object obj : terminalFeeder.receivedValues) {
            assertTrue("Object is not a car!", obj instanceof Car);
            assertEquals("I AM ALIVE!", ((Car) obj).message);
        }
    }
    
    private class DriverMonitor extends Thread {
        private volatile boolean done = false;
        private Pipeline pipeline;
        public DriverMonitor(Pipeline pipeline) {
            this.pipeline = pipeline;
        }
        
        public void run() {
            while (!done) {
                StringBuilder b = new StringBuilder();
                for (StageDriver driver : pipeline.getStageDrivers()) {
                    PrioritizableStageDriver d = (PrioritizableStageDriver) driver;
                    b.append(d.getStage()).append(": ").append(d.getPriority()).append(";  ");
                }
                System.out.println("Driver priorities: " + b.toString());
                
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}                
            }
        }
        
        public void finish() {
            this.done = true;
        }
    }
    
    private static class Car {
        public int id;
        public String message;
        
        public Car(int id, String message) {
            this.id = id;
            this.message = message;
        }
        
        public String toString() {
            return "Car " + this.id + ": " + this.message;
        }
    }
    
    
    private static class MoldBodyStage extends CPUBoundTestStage {
        public MoldBodyStage(int id) {
            super(id, 50);
        }
        
        public void process(Object obj) throws StageException {
            Car car = (Car) obj;
            if ("I'm a hunk of metal!".equals(car.message)) {
                car.message = "Now I'm a car body!";
                super.process(car);
            } else {
                throw new StageException(this, "Whoa! " + obj);
            }
        }
    }
    
    private static class BodyPaintStage extends IOBoundTestStage {
        public BodyPaintStage(int id) {
            super(id, 10, 250);
        }
        
        public void process(Object obj) throws StageException {
            Car car = (Car) obj;
            if ("Now I'm a car body!".equals(car.message)) {
                car.message = "I've been painted!";
                super.process(car);
            } else {
                throw new StageException(this, "Whoa! " + obj);
            }
        }
    }
    
    private static class AddWheelsStage extends TestStage {
        public AddWheelsStage(int id) {
            super(id);
        }
        
        public void process(Object obj) throws StageException {
            Car car = (Car) obj;
            if ("I've been painted!".equals(car.message)) {
                car.message = "Got my wheels!";
                super.process(car);
            } else {
                throw new StageException(this, "Whoa! " + obj);
            }
        }
    }
    
    private static class AddAIStage extends CPUBoundTestStage {
        public AddAIStage(int id) {
            super(id, 500);
        }
        
        public void process(Object obj) throws StageException {
            Car car = (Car) obj;
            if ("Got my wheels!".equals(car.message)) {
                car.message = "I AM ALIVE!";
                super.process(car);
            } else {
                throw new StageException(this, "Whoa! " + obj);
            }
        }
    }
    
}
