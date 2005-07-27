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
 *
 * Created on July 21, 2005, 10:07 AM
 *
 * $Log: URLToInputStreamStageTest.java,v $
 * Revision 1.4  2005/07/25 22:19:17  kjn
 * Updated licenses, documentation.
 *
 */

package org.apache.commons.pipeline.stage;

import java.io.IOException;
import java.io.InputStream;
import junit.framework.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;


/**
 * Test cases for URLToInputStreamStaticStage.
 *
 * @author Travis Stevens, National Geophysical Data Center, NOAA
 */
public class URLToInputStreamStageTest extends TestCase {
    
    URL url;
    URLToInputStreamStage stage;
    Pipeline pipe;
    List<InputStream> results;
    
    public URLToInputStreamStageTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        url = this.getClass().getClassLoader().getResource("url-input-to-stream-test.txt");
        assertNotNull(url);
        results = new ArrayList<InputStream>();
        Stage finalStage = new AddToCollectionStage<InputStream>(results);
        stage = new URLToInputStreamStage();
        List<Stage> stages = new ArrayList<Stage>();
        stages.add(stage);
        stages.add(finalStage);
        pipe = new Pipeline(stages);
    }
    
    protected void tearDown() throws Exception {
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(URLToInputStreamStageTest.class);
        
        return suite;
    }
    
    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.URLToInputStreamStage.
     */
    public void testProcess() throws Exception {
        pipe.start();
        pipe.enqueue(url);
        assertEquals(1,results.size());
        InputStream is = results.get(0);
        assertNotNull(is);
        byte[] buffer = new byte[128];
        int bytes = is.read(buffer);
        pipe.finish();
    }
    
    /**
     * Test of postprocess method, of class org.apache.commons.pipeline.stage.URLToInputStreamStage.
     */
    public void testPostprocess() throws Exception {
        pipe.start();
        pipe.enqueue(url);
        pipe.finish();
        InputStream is = results.get(0);
        try {
            byte[] buffer = new byte[128];
            int bytes = is.read(buffer);
            fail("input stream should have been closed, so reading should throw an exception.");
        } catch (IOException expected){
            
        }
        
    }
    
}
