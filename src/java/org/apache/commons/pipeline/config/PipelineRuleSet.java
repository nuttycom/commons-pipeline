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
 * Created on February 12, 2004, 1:48 PM
 */

package org.apache.commons.pipeline.config;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.digester.*;
import org.apache.commons.pipeline.Pipeline;
import org.apache.commons.pipeline.StageQueue;
import org.apache.commons.pipeline.impl.SingleThreadStageQueue;
import org.xml.sax.Attributes;



/**
 * <P>This is a Digester RuleSet that provides rules for parsing a process pipeline
 * XML file.</P>
 *
 * The rules defined by this object are used for parsing the following tags:
 * <ul>
 *  <li>&lt;pipeline&gt;&lt;/pipeline&gt; - The root element of the
 *  XML configuration file for a pipeline. This tag supports two attributes
 *  that are for use only when configuring branch pipelines, <code>key</code>
 *  and <code>configURI</code>. These attributes are described more fully
 *  below in the %lt;branch&gt; documentation.</li>
 *
 *  <li>&lt;stage className="<i>name</i>" queueClass="<i>name</i>" ... &gt;&lt;/stage&gt; - A single stage is
 *  created and configured using this tag. It is a child of &lt;pipeline&gt;. Stages
 *  created in this manner are added to the pipeline in the order that they
 *  occur in the configuration file. The class of the stage is specified by the
 *  <i>className</i> attribute; all other attributes are used by Digester to set bean
 *  properties on the newly created Stage object. At present, Stages configured using
 *  this tag must provide a one-argument constructor that takes a StageQueue instance. 
 *  By default, the stage will be constructed with a 
 * {@link org.apache.commons.pipeline.impl.SingleThreadStageQueue SingleThreadStageQueue}
 *  instance if the queueClass attribute is not set; otherwise the stage will be
 *  constructed with a new instance of the specified class, which should provide
 *  a no-arguments constructor.</li>
 *
 *  <li>&lt;enqueue/&gt; - Enqueue an object onto the first stage in the pipeline.</li>
 *  <li>&lt;branch/%gt; - Add a branch to a pipeline. The contents of this tag should
 *  be one or more &lt;pipeline/&gt;s. Branch pipelines added in this fashion must
 *  be configured with an attribute named <code>key</code> that holds the name by
 *  which the pipeline will be referred to by {@link org.apache.commons.pipeline.StageHandler StageHandler}s.
 *  Branch pipelines may be configured either inline in the main configuration
 *  file or in a separate file referred to by the <code>configURI</code> pipeline
 *  attribute.
 * </ul>
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision: 1.1 $
 * @todo Add support for more complicated StageQueue construction and configuration as part of the Stage 
 * tag processing.
 */
public class PipelineRuleSet extends RuleSetBase {
    private List nestedRuleSets;
    
    /** Creates a new instance of ChainRuleSet */
    public PipelineRuleSet() {
    }
    
    /** Creates a new instance of ChainRuleSet */
    public PipelineRuleSet(List nestedRuleSets) {
        this.nestedRuleSets = nestedRuleSets;
    }
    
    /**
     * Adds the rule instances for pipeline, stage, and enqueue
     * tasks to the Digester instance supplied.
     */
    public void addRuleInstances(Digester digester) {
        ObjectCreationFactory factory = new PipelineFactory();
        
        digester.addFactoryCreate("pipeline", factory);
        digester.addSetProperties("pipeline");
        
        // these rules are used to add subchains to the main pipeline
        digester.addFactoryCreate("*/branch/pipeline", factory);
        digester.addRule("*/branch/pipeline", new CallMethodRule(1, "addBranch", 2, new Class[] { String.class, Pipeline.class }));
        digester.addCallParam("*/branch/pipeline", 0, "key");
        digester.addCallParam("*/branch/pipeline", 1, 0);
        
        //this rule is intended to be used to add a pipeline element. the ChainLogger is
        //simply the default if no pipeline element class is specified
        digester.addFactoryCreate("*/pipeline/stage", StageFactory.class, "stageFactory", false);
        digester.addSetProperties("*/pipeline/stage");
        digester.addSetNext("*/pipeline/stage", "addStage", "org.apache.commons.pipeline.Pipeline$Stage");
        
        //rule for enqueuing string
        digester.addCallMethod("*/stage/enqueue/value", "enqueue", 0);
    }
    
    
    public static class StageFactory extends AbstractObjectCreationFactory {
        private static final Class[] DEFAULT_STAGE_CONSTRUCTOR_PARAMCLASSES = { StageQueue.class };
        
        public Object createObject(Attributes attributes) throws java.lang.Exception {
            String queueClassName = attributes.getValue("queueClass");
            Class queueClass = (queueClassName == null) ? SingleThreadStageQueue.class : Class.forName(queueClassName);
            
            String stageClassName = attributes.getValue("className");
            if (stageClassName == null) throw new IllegalArgumentException("className attribute may not be null for element <stage>");
            Class stageClass = Class.forName(stageClassName);
            
            Constructor constructor = stageClass.getConstructor(DEFAULT_STAGE_CONSTRUCTOR_PARAMCLASSES);
            return constructor.newInstance(new Object[] {queueClass.newInstance()});
        }       
    }
    
    
    public class PipelineFactory extends AbstractObjectCreationFactory {        
        public Object createObject(Attributes attributes) throws java.lang.Exception {
            String configURI = attributes.getValue("configURI");
            if (configURI == null) {
                return new Pipeline();
            }
            else {
                Digester subDigester = new Digester();
                if (nestedRuleSets != null) {
                    for (Iterator iter = nestedRuleSets.iterator(); iter.hasNext();) {
                        subDigester.addRuleSet((RuleSet) iter.next());
                    }
                    
                    Pipeline pipeline = (Pipeline) subDigester.parse(configURI);
                    return pipeline;
                }
                else {
                    throw new IllegalStateException("Unable to parse branch configuration file: No parsing rules provided to PipelineRuleSet constructor.");
                }
            }
        }
    }
}
