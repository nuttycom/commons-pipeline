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
 * Created on February 12, 2004, 3:42 PM
 */

package org.apache.commons.pipeline.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.pipeline.Pipeline;
import org.xml.sax.SAXException;



/**
 * This factory is designed to simplify creating a pipeline using Digester.
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision: 1.1 $
 */
public class PipelineFactory {
    
    /** Digester rule sets used to configure the Digester instance. */
    private List ruleSets = new ArrayList();
    
    
    /** Creates a new instance of PipelineFactory */
    public PipelineFactory() {
        //PipelineRuleSet needs a reference to {@link org.apache.commons.digester.RuleSet RuleSet}s 
        //used to parse the configuration file in case configuration is split up between multiple
        //files.
        ruleSets.add(new PipelineRuleSet(ruleSets));
    }            
    
    
    /**
     * Creates a pipeline from the specified Digester configuration file.
     */
    public Pipeline getPipeline(String configFile) throws IOException, SAXException, ParserConfigurationException  {
        Digester digester = new Digester();
        this.initDigester(digester);
        
        File conf = new File(configFile);
        return (Pipeline) digester.parse(conf);
    }
    
    
    /**
     * Creates a pipeline from the specified input stream. Useful for instances where
     * a pipeline configuration is being read from inside a jarfile.
     */
    public Pipeline getPipeline(InputStream configStream) throws IOException, SAXException, ParserConfigurationException  {
        Digester digester = new Digester();
        this.initDigester(digester);
        
        return (Pipeline) digester.parse(configStream);
    }
    
    
    /**
     * Initialize a Digester instance with the rule sets provided to this factory.
     */
    public void initDigester(Digester digester) {
        for (Iterator iter = ruleSets.iterator(); iter.hasNext();) {
            digester.addRuleSet((RuleSet) iter.next());
        }
    }
    
    
    /**
     * Adds a RuleSet to the list of rules available to Digester for parsing 
     * the configuration file.
     */
    public void addRuleSet(RuleSet ruleSet) {
        this.ruleSets.add(ruleSet);
    }
    
    
    /**
     * The simplest possible main method that creates a pipeline from a configuration file,
     * then runs the pipeline processing from start to finish.
     *
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        try {
            PipelineFactory factory = new PipelineFactory();
            Pipeline pipeline = factory.getPipeline(argv[0]);
            for (int i = 1; i < argv.length; i++) {
                pipeline.enqueue(argv[i]);
            }
            
            System.out.println("Pipeline created, about to begin processing...");
            
            pipeline.start();
            pipeline.finish();
            
            System.out.println("Pipeline successfully finished processing. See logs for details.");
        }
        catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
