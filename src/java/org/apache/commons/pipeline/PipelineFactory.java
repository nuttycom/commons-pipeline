/*
 * PipelineFactory.java
 *
 * Created on October 4, 2004, 1:22 PM
 */

package org.apache.commons.pipeline;

import java.util.Map;

/**
 * Simple factory interface for creating pipelines.
 *
 * @author  kjn
 */
public interface PipelineFactory {
    /** Returns a Pipeline created by the factory. */
    public Pipeline createPipeline() throws PipelineCreationException;
    
    /** Configure the factory */
    public void configure(Map<String,?> context);
}
