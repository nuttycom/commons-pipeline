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
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.pipeline.PipelineCreationException;
import org.apache.commons.pipeline.Pipeline;


/**
 * This factory is designed to simplify creating a pipeline using Digester.
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision: 1.1 $
 */
public class DigesterPipelineFactory implements org.apache.commons.pipeline.PipelineFactory {
    
    /** Digester rule sets used to configure the Digester instance. */
    private List<RuleSet> ruleSets = new ArrayList<RuleSet>();
    
    /**
     * Factory will create a pipeline from the specified Digester configuration file
     * if this filename is not null.
     */
    private String configFile;
    
    /**
     * Factory will create a pipeline from this input stream if it is not null. Useful for instances where
     * a pipeline configuration is being read from inside a jarfile.
     */
    private InputStream configStream;
        
    /** 
     * A factory created by this constructor will create a pipeline from the specified
     * Digester creation file.
     */
    public DigesterPipelineFactory(String configFile) {
        this.init();
        this.configFile = configFile;
    }
    
    
    /**
     * A factory created by this constructor will create a pipeline from the specified
     * input stream. Useful for instances where a pipeline configuration is being read from inside a jarfile.
     */
    public DigesterPipelineFactory(InputStream configStream) {
        this.init();
        this.configStream = configStream;
    }
    
    
    /**
     * Adds the base RuleSets to the digester configuration.
     */
    private void init() {
        //PipelineRuleSet needs a reference to {@link org.apache.commons.digester.RuleSet RuleSet}s
        //used to parse the configuration file in case configuration is split up between multiple
        //files.
        ruleSets.add(new PipelineRuleSet(ruleSets));        
    }
    
    
    /** Creates a new pipeline */
    public Pipeline createPipeline() throws PipelineCreationException {
        try {
            if (this.configFile != null) {
                Digester digester = new Digester();
                this.initDigester(digester);
                
                File conf = new File(configFile);
                return (Pipeline) digester.parse(conf);
            }
            else if (this.configStream != null) {
                Digester digester = new Digester();
                this.initDigester(digester);
                
                return (Pipeline) digester.parse(configStream);
            }
            else {
                throw new IllegalStateException("No configuration file or stream found.");
            }
        }
        catch (Exception e) {
            throw new PipelineCreationException(e.getMessage(), e);
        }
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
     * No-op implementation - all configuration information exists in the XML file.
     */
    public void configure(java.util.Map<String,?> context) {
    }
    
    
    /**
     * The simplest possible main method that creates a pipeline from a configuration file,
     * then runs the pipeline processing from start to finish.
     *
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        try {
            DigesterPipelineFactory factory = new DigesterPipelineFactory(argv[0]);
            Pipeline pipeline = factory.createPipeline();
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
