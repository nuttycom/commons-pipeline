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
 * Created on October 4, 2004, 1:35 PM
 *
 * $Log: PipelineCreationException.java,v $
 * Revision 1.2  2005/07/25 22:04:54  kjn
 * Corrected Apache licensing, documentation.
 *
 */

package org.apache.commons.pipeline;

/**
 * This is a wrapper exception for use by {@link PipelineFactory}s.
 *
 * @author <a href="mailto:Kris.Nuttycombe@noaa.gov">Kris Nuttycombe</a>, National Geophysical Data Center, NOAA
 */
public class PipelineCreationException extends java.lang.Exception {
    
    /**
     * Creates a new instance of <code>PipelineCreationException</code> without detail message.
     */
    public PipelineCreationException() {
    }    
    
    /**
     * Constructs an instance of <code>PipelineCreationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PipelineCreationException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>PipelineCreationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PipelineCreationException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    /**
     * Constructs an instance of <code>PipelineCreationException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public PipelineCreationException(Throwable cause) {
        super(cause);
    }    
}
