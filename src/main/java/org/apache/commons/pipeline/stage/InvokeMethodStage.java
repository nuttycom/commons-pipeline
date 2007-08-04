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

package org.apache.commons.pipeline.stage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.pipeline.StageException;

/**
 * Calls a method on the processed object giving it the arguments specified
 * at the time of object construction.
 *
 *
 * @version $Id: InvokeMethodStage.java 3742 2006-08-28 16:50:23Z kjn $
 */
public class InvokeMethodStage extends BaseStage {
    //private static final Log log = LogFactory.getLog(InvokeMethodStage.class);
    
    private Method method;
    private Object[] arguments;
    
    /**
     * Creates a new instance of InvokeMethodStage
     */
    public InvokeMethodStage(Method method){
        this.method = method;
        this.arguments = new Object[] { };
    }
    
    /**
     * Creates a new instance of InvokeMethodStage
     */
    public InvokeMethodStage(Method method, Object... arguments) {
        this.method = method;
        this.arguments = arguments;
    }
    
    /**
     * Creates a new instance of InvokeMethodStage from the class and method names.
     */
    public InvokeMethodStage(String className, String methodName, Object... arguments) throws ClassNotFoundException, NoSuchMethodException{
        Class<?> clazz = InvokeMethodStage.class.getClassLoader().loadClass(className);
        Class[] argTypes = new Class[arguments.length];
        for (int i = 0; i < arguments.length; i++) argTypes[i] = arguments[i].getClass();
        
        this.method = clazz.getMethod(methodName, argTypes);
        this.arguments = arguments;
    }
    
    /** Returns the method to be accessed by processing
     *@return the method
     */
    public Method getMethod(){
        return this.method;
    }
    
    /** Returns the objects being used to invoke this method
     *@return The objects being used
     */
    public Object[] getArguments(){
        return this.arguments;
    }
    
    /** Calls the specified method on the object being processed and exqueues the result
     * @param obj The object being processed.
     */
    public void process(Object obj) throws org.apache.commons.pipeline.StageException {        
        try {
            Object result = method.invoke(obj, arguments);
            this.emit(result);
        } catch (IllegalAccessException e){
            throw new StageException(this, e);
        } catch (InvocationTargetException e){
            throw new StageException(this, e);
        }
    }
}
