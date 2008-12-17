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

package org.apache.commons.pipeline;

/**
 * Exception wrapper class for exceptions that occur while processing a stage.
 */
public class StageException extends java.lang.Exception {
    /**
     *
     */
    private static final long serialVersionUID = -7427836310660170668L;
    
    //Stage within which the error occurred
    private Stage source;    
    
    /**
     * Creates a new instance of <code>StageException</code> without detail message.
     * @param source the stage that was the source of the exception
     */
    public StageException(Stage source) {
        this.source = source;
    }
    
    
    /**
     * Constructs an instance of <code>StageException</code> with the specified detail message.
     * @param source the stage that was the source of the exception
     * @param msg the detail message.
     */
    public StageException(Stage source, String msg) {
        super(msg);
        this.source = source;
    }
    
    
    /**
     * Constructs an instance of <code>StageException</code> with the specified detail message and cause
     * @param source the stage where the error occurred
     * @param msg the detail message.
     * @param cause Throwable that caused this exception.
     */
    public StageException(Stage source, Throwable cause) {
        super(cause);
        this.source = source;
    }    
    
    
    /**
     * Constructs an instance of <code>StageException</code> with the specified detail message and cause
     * @param source the stage where the error occurred
     * @param msg the detail message.
     * @param cause Throwable that caused this exception.
     */
    public StageException(Stage source, String msg, Throwable cause) {
        super(msg, cause);
        this.source = source;
    }    
    
    
    /**
     * Returns a reference to the Stage object where the exception occurred.
     * @return a reference to the Stage object where the exception occurred.
     */
    public Stage getSource() {
        return this.source;
    }
}
