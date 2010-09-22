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

package org.apache.commons.pipeline.config;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.digester.Digester;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.PipelineCreationException;
import org.xml.sax.SAXException;


/**
 * This factory is designed to simplify creating a pipeline using Digester.
 * @see  PipelineRuleSet for additional information on the format of the
 * XML configuration file.
 */
public class DigesterPipelineFactory implements org.apache.commons.pipeline.PipelineFactory {
    
    /** Digester rule sets used to configure the Digester instance. */
    private List<RuleSet> ruleSets = new ArrayList<RuleSet>();
    
    /**
     * Factory will create a pipeline from the specified Digester configuration file
     * if this filename is not null.
     */
    private URL confURL;
    
    /**
     * A factory created by this constructor will create a pipeline from the specified
     * XML configuration file.
     * @param configFile the XML file containing pipeline configuration information
     */
    public DigesterPipelineFactory(URL confURL) {
        if (confURL == null) throw new IllegalArgumentException("Configuration file URL may not be null.");
        this.confURL = confURL;
    
        //PipelineRuleSet needs a reference to {@link org.apache.commons.digester.RuleSet RuleSet}s
        //used to parse the configuration file in case configuration is split up between multiple
        //files.
        ruleSets.add(new PipelineRuleSet(ruleSets));        
    }
    
    /**
     * Creates a new pipeline based upon the configuration of this factory instance.
     * @throws org.apache.commons.pipeline.PipelineCreationException Thrown if an error is encountered parsing the configuration file.
     * @return The newly created pipeline instance
     */
    public Pipeline createPipeline() throws PipelineCreationException {
        try {
                Digester digester = new Digester();
                this.initDigester(digester);
                
            InputStream in = confURL.openStream();
            try {
                return (Pipeline) digester.parse(in);
            } finally {
                in.close();
            }
        } catch (IOException e) {
            throw new PipelineCreationException("An IOException occurred reading the configuration file: " + e.getMessage(), e);
        } catch (SAXException e) {
            throw new PipelineCreationException("A formatting error exists in the configuration file: " + e.getMessage(), e);
        }
    }
    
    /**
     * Initialize a Digester instance with the rule sets provided to this factory.
     * @param digester The digester instance to be initialized
     */
    public void initDigester(Digester digester) {
        for (Iterator iter = ruleSets.iterator(); iter.hasNext();) {
            digester.addRuleSet((RuleSet) iter.next());
        }
    }
    
    /**
     * Adds a RuleSet to the list of rules available to Digester for parsing
     * the configuration file.
     * @param ruleSet The rule set to be added to the Digester
     */
    public void addRuleSet(RuleSet ruleSet) {
        this.ruleSets.add(ruleSet);
    }
    
    /**
     * The simplest possible main method that creates a pipeline from a configuration file,
     * then runs the pipeline processing from start to finish.
     *
     * When run from the command line, the only argument to this method should be
     * the path to the configuration file.
     *
     * @param argv the command line arguments
     */
    public static void main(String[] argv) {
        try {
            File configFile = new File(argv[0]);
            
            DigesterPipelineFactory factory = new DigesterPipelineFactory(configFile.toURL());
            Pipeline pipeline = factory.createPipeline();
            for (int i = 1; i < argv.length; i++) {
                pipeline.getSourceFeeder().feed(argv[i]);
            }
            
            System.out.println("Pipeline created, about to begin processing...");
            
            pipeline.start();
            pipeline.finish();
            
            System.out.println("Pipeline successfully finished processing. See logs for details.");
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
}
