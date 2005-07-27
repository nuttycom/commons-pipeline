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
 * Created on December 9, 2003, 4:24 PM
 *
 * $Log: StageException.java,v $
 * Revision 1.3  2005/07/25 22:04:54  kjn
 * Corrected Apache licensing, documentation.
 *
 */

package org.apache.commons.pipeline;

/**
 * Exception wrapper class for exceptions that occur while processing a stage.
 *  
 * @author <a href="mailto:Kris.Nuttycombe@noaa.gov">Kris Nuttycombe</a>, National Geophysical Data Center, NOAA
 */
public class StageException extends java.lang.Exception {
    //Stage within which the error occurred
    private Stage source;
    
    /**
     * Creates a new instance of <code>StageException</code> without detail message.
     */
    public StageException() {
    }
    
    
    /**
     * Constructs an instance of <code>StageException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public StageException(String msg) {
        super(msg);
    }
    
    /**
     * Constructs an instance of <code>StageException</code> with the specified cause.
     * @param msg the detail message.
     */
    public StageException(Throwable thr) {
        super(thr);
    }
    
    
    
    /**
     * Constructs an instance of <code>StageException</code> with the specified detail message and cause
     * @param msg the detail message.
     * @param cause Throwable that caused this exception.
     */
    public StageException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
    
    /**
     * Creates a new instance of <code>StageException</code> without detail message.
     */
    public StageException(Stage source) {
        this.source = source;
    }
    
    
    /**
     * Constructs an instance of <code>StageException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public StageException(Stage source, String msg) {
        super(msg);
        this.source = source;
    }
    
    
    /**
     * Constructs an instance of <code>StageException</code> with the specified detail message and cause
     * @param msg the detail message.
     * @param cause Throwable that caused this exception.
     */
    public StageException(Stage source, String msg, Throwable cause) {
        super(msg, cause);
        this.source = source;
    }    
    
    
    /**
     * Returns a reference to the Stage object where the exception occurred.
     */
    public Stage getSource() {
        return this.source;
    }
}
