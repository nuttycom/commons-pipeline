/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */ 

package org.apache.commons.pipeline.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.digester.AbstractObjectCreationFactory;
import org.apache.commons.digester.CallMethodRule;
import org.apache.commons.digester.Digester;
import org.apache.commons.digester.ObjectCreationFactory;
import org.apache.commons.digester.Rule;
import org.apache.commons.digester.RuleSet;
import org.apache.commons.digester.RuleSetBase;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.StageDriverFactory;
import org.xml.sax.Attributes;

/**
 * This is a Digester RuleSet that provides rules for parsing a pipeline
 * XML file.
 *
 * The rules defined by this object are used for parsing the following tags:
 * <ul>
 *     <li>
 *         <B><code>&lt;pipeline&gt;&lt;/pipeline&gt;</code></B><br/>
 *         The root element of the XML configuration file for a pipeline. This tag
 *         supports two optional attributes that are for use only when configuring
 *         branch pipelines, <code>key</code> and <code>configURI</code>. These
 *         attributes are described more fully below in the <code>&lt;branch&gt;</code>
 *         documentation.
 *     </li>
 *     <li>
 *         <B><code>&lt;driverFactory className="<em>MyDriverFactory</em>" id="<em>my_id</em>" ... /&gt;</code></B><br/>
 *         This tag is used to create and configure a {@link StageDriverFactory} that
 *         will be used to create {@link StageDriver} instances for stages
 *         in the parent pipeline. Each {@link StageDriverFactory} is identified by a unique
 *         string identifier that is used by the <code>&lt;stage&gt;</code> tag
 *         to identify the factory used to create the driver for the stage.
 *         The class of the factory (which must supply a no-argument constructor)
 *         is specified by the <code>className</code> attribute, and all other
 *         additional attributes are used by Digester to configure the associated properties
 *         (using standard Java bean naming conventions) of the driver instance created.
 *     </li>
 *     <li>
 *         <B><code>&lt;stage className="<em>MyStageClass</em>" driverFactoryId="<i>name</i>" ... &gt;&lt;/stage&gt;</code></B><br/>
 *         A single stage is created, configured, and added to the parent pipeline using
 *         this tag. Stages created in this manner are added to the pipeline in the order
 *         that they occur in the configuration file. The class of the stage (which must
 *         provide a no-argument constructor) is specified by the <em>className</em> attribute.
 *         Each stage should be associated with a previously declared driver factory
 *         by use of the <code>driverFactoryId</code> attribute. All other attributes are
 *         used by Digester to set bean properties on the newly created Stage object.
 *     </li>
 *     <li>
 *         <B><code>&lt;feed/&gt;</code></B><br/>
 *         Enqueue an object onto the first stage in the pipeline. Note that this
 *         must come <em>after</em> the first stage declaration in the configuration file,
 *         otherwise the queue for the first stage does not exist yet and the fed
 *         values will be discarded.
 *         <p/>
 *         There are two types of usage available, provided by the following subtags:
 *         <ul>
 *             <li>
 *                 <B><code>&lt;value&gt;my_value&lt;/value&gt;</code></B><br/>
 *                 Feed the string value of the body of this tag to the first stage in the
 *                 pipeline.
 *             </li>
 *             <li>
 *                 <B><code>&lt;object className="MyClass" ... /&gt;</code></B><br/>
 *                 This tag uses the standard Digester ObjectCreateRule to create an
 *                 arbitrary object of the specified class (which must provide a
 *                 no-argument constructor) to the first stage in the pipeline.
 *                 All attributes other than <code>className</codee> are used by
 *                 Digester to set bean properties on the newly created object.
 *             </li>
 *         </ul>
 *     </li>
 *     <li>
 *         <B><code>&lt;branch/&gt;</code></B><br/>
 *         Add a branch to a pipeline. The contents of this tag should
 *         be one or more <code>&lt;pipeline/&gt;</code> declarations. Branch
 *         pipelines added in this fashion must be configured with an attribute
 *         named <code>key</code> that holds the name by which the branch pipeline
 *         will be referred to.
 *         <p/>
 *         Branch pipelines may be configured either inline in the main 
 *         configuration file or in a separate file referred to by the
 *         <code>configURI</code> pipeline attribute.
 *     </li>
 * </ul>
 */
public class PipelineRuleSet extends RuleSetBase {
    private static Class[] addBranchTypes = { String.class, Pipeline.class };
    private static Class[] setEnvTypes = { String.class, Object.class };
    private List<RuleSet> nestedRuleSets;
    
    /**
     * Creates a new instance of the rule set used by Digester to configure a pipeline.
     */
    public PipelineRuleSet() {
    }
    
    /**
     * Creates a new pipeline rule set with the specified collection of additional
     * rule sets that may be used for recursive creation of branch pipelines.
     * @param nestedRuleSets A list of other RuleSet instances that are being used in conjunction with the
     * PipelineRuleSet. In the case that branch pipelines are referred to by URI, these
     * rule sets will be added to a new Digester instance (along with a PipelineRuleSet
     * instance) that is used to parse the branch configuration file.
     */
    public PipelineRuleSet(List<RuleSet> nestedRuleSets) {
        this.nestedRuleSets = nestedRuleSets;
    }
    
    /**
     * Adds the rule instances for pipeline, stage, and enqueue
     * tasks to the Digester instance supplied.
     * @param digester The Digester instance to which the rules should be added.
     */
    public void addRuleInstances(Digester digester) {
        ObjectCreationFactory pipelineFactory = new PipelineFactory();
        ObjectCreationFactory driverFactoryFactory = new StageDriverFactoryFactory();
        
        //rules to create pipeline
        digester.addFactoryCreate("pipeline", pipelineFactory);
        digester.addSetProperties("pipeline");
        digester.addRule("pipeline", new PipelineDriverFactoriesRule());
        
        //these rules are used to add branches to the main pipeline
        digester.addFactoryCreate("*/branch/pipeline", pipelineFactory);
        digester.addRule("*/branch/pipeline", new CallMethodRule(1, "addBranch", 2, addBranchTypes));
        digester.addCallParam("*/branch/pipeline", 0, "key");
        digester.addCallParam("*/branch/pipeline", 1, 0);
        digester.addRule("*/branch/pipeline", new PipelineDriverFactoriesRule());
        
        //rules for adding values to the global pipeline environment
        digester.addObjectCreate("*/pipeline/env/object", "java.lang.Object", "className");
        digester.addSetProperties("*/pipeline/env/object");
        digester.addRule("*/pipeline/env/object", new CallMethodRule(1, "setEnv", 2, setEnvTypes));
        digester.addCallParam("*/pipeline/env/object", 0, "key");
        digester.addCallParam("*/pipeline/env/object", 1, 0);
        
        digester.addRule("*/pipeline/env/value", new CallMethodRule(0, "setEnv", 2, setEnvTypes));
        digester.addCallParam("*/pipeline/env/value", 0, "key");
        digester.addCallParam("*/pipeline/env/value", 1);
        
        //this rule is intended to be used to add a StageEventListener to the pipeline.
        digester.addObjectCreate("*/pipeline/listener", "org.apache.commons.pipeline.StageEventListener", "className");
        digester.addSetProperties("*/pipeline/listener");
        digester.addSetNext("*/pipeline/listener", "registerListener", "org.apache.commons.pipeline.StageEventListener");
        
        //this rule is intended to be used to add a StageDriverFactory to the pipeline.
        digester.addFactoryCreate("*/pipeline/driverFactory", driverFactoryFactory);
        digester.addSetProperties("*/pipeline/driverFactory");
        
        digester.addObjectCreate("*/driverFactory", "org.apache.commons.pipeline.StageDriverFactory", "className");
        digester.addSetProperties("*/driverFactory");
        digester.addSetNext("*/driverFactory", "setWrappedSDF", "org.apache.commons.pipeline.StageDriverFactory");
        
        //rules for adding lifecycle jobs
        digester.addObjectCreate("*/pipeline/lifecycleJob", "org.apache.commons.pipeline.PipelineLifecycleJob", "className");
        digester.addSetProperties("*/pipeline/lifecycleJob");
        digester.addSetNext("*/pipeline/lifecycleJob", "addLifecycleJob", "org.apache.commons.pipeline.PipelineLifecycleJob");
        
        //rules for setting an object property on the next-to-top object on the stack
        //similar to setNestedPropertiesRule
        digester.addObjectCreate("*/property", "java.lang.Object", "className");
        digester.addSetProperties("*/property");
        digester.addRule("*/property", new SetNestedPropertyObjectRule());
        
        //this rule is intended to be used to add a stage to a pipeline
        digester.addObjectCreate("*/pipeline/stage", "org.apache.commons.pipeline.BaseStage", "className");
        digester.addSetProperties("*/pipeline/stage");
        digester.addRule("*/pipeline/stage", new PipelineAddStageRule());
        
        //rule for feeding a string value to the source feeder
        digester.addRule("*/pipeline/feed/value", new PipelineFeedValueRule());
        
        //rules for enqueueing an object
        digester.addObjectCreate("*/pipeline/feed/object", "java.lang.Object", "className");
        digester.addSetProperties("*/pipeline/feed/object");
        digester.addRule("*/pipeline/feed/object", new PipelineFeedObjectRule());
    }
    
    /**
     * This factory is used to create a pipeline. If the "configURI" parameter
     * is specified, the pipeline is created based upon the configuration file
     * located at that URI.
     */
    private class PipelineFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) throws java.lang.Exception {
            String configURI = attributes.getValue("configURI");
            if (configURI == null) {
                return new Pipeline();
            } else {
                Digester subDigester = new Digester();
                if (nestedRuleSets != null) {
                    for (RuleSet ruleset : nestedRuleSets) {
                        subDigester.addRuleSet(ruleset);
                    }
                    
                    Pipeline pipeline = (Pipeline) subDigester.parse(configURI);
                    return pipeline;
                } else {
                    throw new IllegalStateException("Unable to parse branch configuration file: No parsing rules provided to PipelineRuleSet constructor.");
                }
            }
        }
    }
    
    /**
     * Configure the storage for the map of driver factories for the pipeline.
     */
    private class PipelineDriverFactoriesRule extends Rule {
        public void begin(String namespace, String name, Attributes attributes) throws Exception {
            digester.push("org.apache.commons.pipeline.config.DriverFactories", new HashMap<String, StageDriverFactory>());
            super.begin(namespace, name, attributes);
        }
        
        public void end(String namespace, String name) throws Exception {
            super.end(namespace, name);
            digester.pop("org.apache.commons.pipeline.config.DriverFactories");
        }
    }
    
    /**
     * Configure the storage for the map of driver factories for the pipeline.
     */
    private class SetNestedPropertyObjectRule extends Rule {
        String propName;
        
        public void begin(String namespace, String name, Attributes attributes) throws Exception {
            propName = attributes.getValue("propName");
            super.begin(namespace, name, attributes);
        }
        
        public void end(String namespace, String name) throws Exception {
            super.end(namespace, name);
            BeanUtils.setProperty(digester.peek(1), propName, digester.peek());
        }
    }
    
    /**
     * This ObjectCreationFactory creates a stage driver factory and stores
     * it in the scope of the rule set so that it can be retrieved by the stage
     * creation rule.
     */
    private class StageDriverFactoryFactory extends AbstractObjectCreationFactory {
        public Object createObject(Attributes attributes) throws Exception {
            Map<String, StageDriverFactory> driverFactories =
                    (Map<String,StageDriverFactory>) digester.peek("org.apache.commons.pipeline.config.DriverFactories");
            
            String className = attributes.getValue("className");
            String id = attributes.getValue("id");
            Class clazz = Class.forName(className);
            if (!StageDriverFactory.class.isAssignableFrom(clazz)) {
                throw new IllegalArgumentException("Class " + clazz + " does not implement StageDriverFactory.");
            } else {
                StageDriverFactory factory = (StageDriverFactory) clazz.newInstance();
                driverFactories.put(id, factory);
                return factory;
            }
        }
    }
    
    /**
     * This Rule adds a stage to the pipeline using the factory specified
     * by the driverFactoryId attribute.
     */
    private class PipelineAddStageRule extends Rule {
        public void begin(String namespace, String name, Attributes attributes) throws Exception {
            digester.push("org.apache.commons.pipeline.config.DriverFactoryIds", attributes.getValue("driverFactoryId"));
            super.begin(namespace, name, attributes);
        }
        
        public void end(String namespace, String name) throws Exception {
            super.end(namespace, name);
            String factoryId = (String) digester.pop("org.apache.commons.pipeline.config.DriverFactoryIds");
            Map<String, StageDriverFactory> driverFactories =
                    (Map<String,StageDriverFactory>) digester.peek("org.apache.commons.pipeline.config.DriverFactories");
            StageDriverFactory factory = driverFactories.get(factoryId);
            Stage stage = (Stage) digester.peek();
            Pipeline pipeline = (Pipeline) digester.peek(1);
            pipeline.addStage(stage, factory);
        }
    }
    
    /**
     * This Rule allows an object to be fed to the pipeline.
     */
    private class PipelineFeedValueRule extends Rule {
        public void body(String namespace, String name, String text) throws Exception {
            Pipeline pipeline = (Pipeline) digester.peek();
            pipeline.getSourceFeeder().feed(text);
            super.body(namespace, name, text);
        }
    }
    
    /**
     * This Rule allows an object to be fed to the pipeline.
     */
    private class PipelineFeedObjectRule extends Rule {
        public void end(String namespace, String name) throws Exception {
            super.end(namespace, name);
            Pipeline pipeline = (Pipeline) digester.peek(1);
            pipeline.getSourceFeeder().feed(digester.peek());
        }
    }
}
