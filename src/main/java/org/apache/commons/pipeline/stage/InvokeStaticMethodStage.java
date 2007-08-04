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
 * Runs a static method with the object (or array) being processed. The returned object 
 * will be exqueued on the main pipeline if it is not null. If the returned
 * object is null, this stage will attempt to place the original object on the 
 * branch specified by {@link #setNullResultBranchTag(String)}.
 */
public class InvokeStaticMethodStage extends BaseStage {
    
    // Branch upon which the original objects will be enqueued if the defined
    // Method returned a null result.
    private String nullResultBranchKey;

    // Method used to process objects in the queue
    private Method method;
    
    /**
     * Creates a new instance of InvokeStaticMethodStage
     */
    public InvokeStaticMethodStage(Method method) {
        super();
        this.method = method;
    }
    
    /** 
     * Convenience method to create the new stage with String description of className, methodName and argumentType
     *
     * @param className The fully qualified class name, such as "java.lang.String" of the class in which the method resides
     * @param methodName The name of the method
     * @param argumentType The argument type of the method (Sorry, this doesn't support multiple argument methods)
     */
    public InvokeStaticMethodStage(String className, String methodName, String... argumentTypeNames) throws ClassNotFoundException, NoSuchMethodException {
        Class clazz = InvokeStaticMethodStage.class.getClassLoader().loadClass(className);
        Class[] argTypes = new Class[argumentTypeNames.length];
        for (int i = 0; i < argumentTypeNames.length; i++) {
            argTypes[i] = Class.forName(argumentTypeNames[i]);
        }
        
        this.method = clazz.getMethod(methodName, argTypes);
    }
    
    /** 
     * Returns the Method object for the method that will be used to process
     * objects in the queue.
     */
    public Method getMethod(){
        return this.method;
    }
    
    /** 
     * <p>Calls the defined static method and exqueues the returned object if it is 
     * not null, otherwise placing the original object on the branch specified
     * by the nullResultBranchKey property if nullResultBranchKey is not null.</p>
     *
     * @param obj The object to be processed.
     */
    public void process(Object obj) throws StageException {
        try {
            Object result = this.method.invoke(null, obj);
            if (result != null){
                this.emit(result);
            } else if (nullResultBranchKey != null) {
                this.context.getBranchFeeder(nullResultBranchKey).feed(obj);
            }
        } catch (IllegalAccessException e){
            throw new StageException(this, e);
        } catch (InvocationTargetException e){
            throw new StageException(this, e);
        }       
    }

    /**
     * Getter for property nullResultBranchKey.
     *
     * @return Value of property nullResultBranchKey.
     */
    public String getNullResultBranchKey() {
        return this.nullResultBranchKey;
    }

    /**
     * Setter for property nullResultBranchKey. If set to null (default) 
     * then objects generating null results are simply dropped from the stream.
     *
     * @param nullResultBranchKey New value of property nullResultBranchKey.
     */
    public void setNullResultBranchKey(String nullResultBranchKey) {
        this.nullResultBranchKey = nullResultBranchKey;
    }
}
