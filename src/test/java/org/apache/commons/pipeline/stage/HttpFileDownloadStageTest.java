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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.commons.pipeline.testFramework.TestFeeder;
import org.apache.commons.pipeline.testFramework.TestStageContext;


/**
 * Test cases for HttpFileDownloadStage.
 */
public class HttpFileDownloadStageTest extends TestCase {
    
    private URL google;
    private URL fileUrl;
    
    public HttpFileDownloadStageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        this.google = new URL("http://www.google.com");
        this.fileUrl = this.getClass().getClassLoader().getResource("http-download.txt");
    
        assertNotNull(fileUrl);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(HttpFileDownloadStageTest.class);
        
        return suite;
    }
    
    /**
     * Test of process() method, of class org.apache.commons.pipeline.stage.HttpFileDownloadStage.
     */
    public void testHttpUrlProcess() throws Exception {
        this.execTestHttpProcess(this.google);
        this.execTestHttpProcess(this.google.toExternalForm());
        //this.execTestFileUrlProcess(this.fileUrl);
        //this.execTestFileUrlProcess(this.fileUrl.toExternalForm());
    }
    
    /**
     * Utility method for testing processing of HTTP URLs.
     */
    private void execTestHttpProcess(Object arg) throws Exception {
        HttpFileDownloadStage stage = new HttpFileDownloadStage();
        
        //initialize the testing context
        TestStageContext testContext = new TestStageContext();
        TestFeeder testFeeder = new TestFeeder();
        testContext.registerDownstreamFeeder(stage, testFeeder);
        stage.init(testContext);
        
        stage.process(arg);
        
        assertEquals(1, testFeeder.receivedValues.size());
        
        Object o = testFeeder.receivedValues.get(0);
        assertNotNull(o);
        assertTrue(o instanceof File);
        
        File file = (File) o;
        FileReader rf = new FileReader(file);
        BufferedReader br = new BufferedReader(rf);
        try {
            String line = br.readLine();
            assertNotNull(line);
            assertTrue("actual line:" + line, line.contains("oogle"));
        } finally {
            rf.close();
            br.close();
        }
    }
    
    /**
     * Utility method for testing processing of system/file URLs.
     */
    public void execTestFileUrlProcess(Object arg) throws Exception {
        HttpFileDownloadStage stage = new HttpFileDownloadStage();
        
        //initialize the testing context
        TestStageContext testContext = new TestStageContext();
        TestFeeder testFeeder = new TestFeeder();
        testContext.registerDownstreamFeeder(stage, testFeeder);
        stage.init(testContext);
        
        stage.process(arg);
        
        assertEquals(1, testFeeder.receivedValues.size());
        
        Object o = testFeeder.receivedValues.get(0);
        assertNotNull(o);
        assertTrue(o instanceof File);
        File file = (File) o;
        
        FileReader rf = new FileReader(file);
        BufferedReader br = new BufferedReader(rf);
        try {
            String line = br.readLine();
            assertNotNull(line);
            assertEquals("This is a test file.", line);
        } finally {
            rf.close();
            br.close();
        }
        
    }
    
}
