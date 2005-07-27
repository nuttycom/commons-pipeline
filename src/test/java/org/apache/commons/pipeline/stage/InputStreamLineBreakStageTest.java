/*
 * Copyright 2004 The Apache Software Foundation
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
 * $Log: InputStreamLineBreakStageTest.java,v $
 * Revision 1.2  2005/07/26 18:20:28  tns
 * apache license.
 *
 *
 */

package org.apache.commons.pipeline.stage;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import junit.framework.*;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;

/**
 *
 * @author tns
 */
public class InputStreamLineBreakStageTest extends TestCase {
    
    List results;
    Pipeline pipe;
    URL url;
    
    public InputStreamLineBreakStageTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        url = this.getClass().getClassLoader().getResource("url-input-to-stream-test.txt");
        assertNotNull(url);
        
        URLToInputStreamStage stage1 = new URLToInputStreamStage();
        InputStreamLineBreakStage stage2 = new InputStreamLineBreakStage();
        results = new ArrayList();
        Stage stage3 = new AddToCollectionStage(results);
        ArrayList stages = new ArrayList();
        stages.add(stage1);
        stages.add(stage2);
        stages.add(stage3);
        pipe = new Pipeline(stages);
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(InputStreamLineBreakStageTest.class);
        
        return suite;
    }

    /**
     * Test of process method, of class org.apache.commons.pipeline.stage.InputStreamLineBreakStage.
     */
    public void testProcess() throws Exception {
        pipe.start();
        pipe.enqueue(url);
        pipe.finish();
        
        assertEquals(3,results.size());
        String s0 = (String) results.get(0);
        assertEquals("line 1", s0);
        String s1 = (String) results.get(1);
        assertEquals("line 2", s1);
        String s2 = (String) results.get(2);
        assertEquals("line 3", s2);
    }
    
}
