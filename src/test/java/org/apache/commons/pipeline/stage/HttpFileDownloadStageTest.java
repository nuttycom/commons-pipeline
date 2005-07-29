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

package org.apache.commons.pipeline.stage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;


/**
 * Test cases for HttpFileDownloadStage.
 */
public class HttpFileDownloadStageTest extends TestCase {
    
    Pipeline pipeline;
    List results;
    URL fileUrl;
    URL google;
    
    
    public HttpFileDownloadStageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        results = new ArrayList();
        ArrayList<Stage> stages = new ArrayList<Stage>();
        stages.add(new HttpFileDownloadStage());
        stages.add(new AddToCollectionStage(results));
        this.fileUrl = this.getClass().getClassLoader().getResource("http-download.txt");
        assertNotNull(fileUrl);
        google = new URL("http://www.google.com");
        pipeline = new Pipeline(stages);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(HttpFileDownloadStageTest.class);
        
        return suite;
    }
    
    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.HttpFileDownloadStage.
     */
//    public void testFileUrlStringProcess() throws Exception {
//        pipeline.start();
//        pipeline.enqueue(this.fileUrl.toExternalForm());
//        pipeline.finish();
//        assertEquals(1,results.size());
//        Object o = results.get(0);
//        assertNotNull(o);
//        assertTrue(o instanceof File);
//        File file = (File) o;
//        
//        FileReader rf = new FileReader(file);
//        BufferedReader br = new BufferedReader(rf);
//        try {
//            String line = br.readLine();
//            assertNotNull(line);
//            assertEquals("This is a test file.",line);
//        } finally {
//            rf.close();
//            br.close();
//        }
//        
//        
//    }
//    
//    /**
//     * Test of setWorkDir method, of class org.apache.commons.pipeline.stage.HttpFileDownloadStage.
//     */
//    public void testFileUrlProcess() throws Exception {
//        pipeline.start();
//        pipeline.enqueue(this.fileUrl);
//        pipeline.finish();
//        assertEquals(1,results.size());
//        Object o = results.get(0);
//        assertNotNull(o);
//        assertTrue(o instanceof File);
//        File file = (File) o;
//        
//        FileReader rf = new FileReader(file);
//        BufferedReader br = new BufferedReader(rf);
//        try {
//            String line = br.readLine();
//            assertNotNull(line);
//            assertEquals("This is a test file.",line);
//        } finally {
//            rf.close();
//            br.close();
//        }
//        
//    }
    
    /**
     * Test of process() method, of class org.apache.commons.pipeline.stage.HttpFileDownloadStage.
     */
    public void testHttpUrlString() throws Exception {
        pipeline.start();
        pipeline.enqueue(this.google.toExternalForm());
        pipeline.finish();
        assertEquals(1,results.size());
        Object o = results.get(0);
        assertNotNull(o);
        assertTrue(o instanceof File);
        File file = (File) o;
        
        FileReader rf = new FileReader(file);
        BufferedReader br = new BufferedReader(rf);
        try {
            String line = br.readLine();
            assertNotNull(line);
            assertTrue("actual line:" + line,line.contains("oogle"));
        } finally {
            rf.close();
            br.close();
        }
    }
    
    /**
     * Test of process() method, of class org.apache.commons.pipeline.stage.HttpFileDownloadStage.
     */
    public void testHttpUrl() throws Exception {
        pipeline.start();
        pipeline.enqueue(this.google);
        pipeline.finish();
        assertEquals(1,results.size());
        Object o = results.get(0);
        assertNotNull(o);
        assertTrue(o instanceof File);
        File file = (File) o;
        
        FileReader rf = new FileReader(file);
        BufferedReader br = new BufferedReader(rf);
        try {
            String line = br.readLine();
            assertNotNull(line);
            assertTrue("actual line:" + line,line.contains("oogle"));
        } finally {
            rf.close();
            br.close();
        }
    }
}
