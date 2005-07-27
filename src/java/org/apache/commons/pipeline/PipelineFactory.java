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
 * Created on October 4, 2004, 1:22 PM
 *
 * $Log: PipelineFactory.java,v $
 * Revision 1.2  2005/07/25 22:04:54  kjn
 * Corrected Apache licensing, documentation.
 *
 */

package org.apache.commons.pipeline;

import java.util.Map;

/**
 * Simple factory interface for creating pipelines.
 *
 * @author <a href="mailto:Kris.Nuttycombe@noaa.gov">Kris Nuttycombe</a>, National Geophysical Data Center, NOAA
 */
public interface PipelineFactory {
    /** Returns a Pipeline created by the factory. */
    public Pipeline createPipeline() throws PipelineCreationException;
    
    /** Configure the factory */
    public void configure(Map<String,?> context);
}
