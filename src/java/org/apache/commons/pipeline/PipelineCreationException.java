/*
 * PipelineCreationException.java
 *
 * Created on October 4, 2004, 1:35 PM
 */

package org.apache.commons.pipeline;

/**
 *
 * @author  kjn
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
    
}
