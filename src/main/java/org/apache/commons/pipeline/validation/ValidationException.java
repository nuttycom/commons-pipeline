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
 */

package org.apache.commons.pipeline.validation;

import java.util.List;

/**
 * This exception is used to indicate that one or more validation errors
 * have occurred during an operation.
 * @author kjn
 */
public class ValidationException extends java.lang.Exception {
    private List<ValidationFailure> errors;
    
    /**
     * Creates a new instance of <code>ValidationException</code> without detail message.
     * @param errors the list of errors that caused the exception
     */
    public ValidationException(List<ValidationFailure> errors) {
        this.errors = errors;
    }
    
    
    /**
     * Constructs an instance of <code>ValidationException</code> with the specified detail message.
     * @param errors The list of errors that caused the exception
     * @param msg the detail message.
     */
    public ValidationException(String msg, List<ValidationFailure> errors) {
        super(msg);
        this.errors = errors;
    }
    
    /**
     * Returns the list of errors that precipitated this validation exception.
     * @return the list of errors that precipitated this validation exception.
     */
    public List<ValidationFailure> getValidationErrors() {
        return this.errors;
    }
}
