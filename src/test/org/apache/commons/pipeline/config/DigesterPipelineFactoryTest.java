/*
 * DigesterPipelineFactoryTest.java
 * JUnit based test
 *
 * Created on October 28, 2004, 4:01 PM
 */

package org.apache.commons.pipeline.config;

import junit.framework.TestCase;
import junit.framework.*;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.pipeline.PipelineCreationException;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.PipelineFactory;
import org.apache.log4j.xml.DOMConfigurator;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 *
 * @author kjn
 */
public class DigesterPipelineFactoryTest extends TestCase {
    private ResourceBundle testResources = ResourceBundle.getBundle("TestResources");
    private String keyBase = "test.DigesterPipelineFactoryTest";
    
    public DigesterPipelineFactoryTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        //set up logging
        InputStream istream = this.getClass().getClassLoader().getResourceAsStream(testResources.getString(keyBase + ".logConfig"));
        try {
            Document document  = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(istream);
            DOMConfigurator.configure(document.getDocumentElement());
        }
        finally {
            if (istream != null) istream.close();
        }
    }
    
    protected void tearDown() throws java.lang.Exception {
    }
    
    public static junit.framework.Test suite() {        
        junit.framework.TestSuite suite = new junit.framework.TestSuite(DigesterPipelineFactoryTest.class);
        
        return suite;
    }
    
    
    public void testCreatePipeline() throws Exception {
        InputStream istream = this.getClass().getClassLoader().getResourceAsStream(testResources.getString(keyBase + ".configFile"));
        try {
            PipelineFactory factory = new DigesterPipelineFactory(istream);
            
            Pipeline pipeline = factory.createPipeline();
            Assert.assertNotNull(pipeline);
            
            int i = 0;
            for (Stage stage : pipeline) {
                Assert.assertNotNull(stage);
                Assert.assertEquals(stage.getClass(), Class.forName(testResources.getString(keyBase + ".stage" + i + ".class")));
                i++;
            }
            
            pipeline.run();
        }
        finally {
            if (istream != null) istream.close();
        }
    }
    
    
}
