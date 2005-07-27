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
 * Created on July 19, 2005, 9:05 AM
 * 
 * $Log: RunPredefinedStaticMethodStage.java,v $
 * Revision 1.2  2005/07/25 22:04:54  kjn
 * Corrected Apache licensing, documentation.
 *
 */

package org.apache.commons.pipeline.stage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.pipeline.BaseStage;
import org.apache.commons.pipeline.StageException;

/**
 * Runs a static method with the object being processed. The returned object 
 * will be exqueued on the main pipeline if it is not null. If the returned
 * object is null, this stage will attempt to place the original object on the 
 * branch specified by {@link #setNullResultBranchTag(String)}.
 *
 * @author Travis Stevens, National Geophysical Data Center, NOAA
 */
public class RunPredefinedStaticMethodStage extends BaseStage {
    
    // Branch upon which the original objects will be enqueued if the defined
    // Method returned a null result.
    private String nullResultBranchTag = "nullResult";

    // Method used to process objects in the queue
    private Method method;
    
    /**
     * Creates a new instance of RunPredefinedStaticMethodStage
     */
    public RunPredefinedStaticMethodStage(Method method) {
        super();
        this.method = method;
    }
    
    /** 
     * Returns the Method object for the method that will be used to process
     * objects in the queue.
     */
    public Method getMethod(){
        return this.method;
    }
    
    /** 
     * Convenience method to create the new stage with String description of className, methodName and argumentType
     *
     * @param className The fully qualified class name, such as "java.lang.String" of the class in which the method resides
     * @param methodName The name of the method
     * @param argumentType The argument type of the method (Sorry, this doesn't support multiple argument methods)
     */
    public static RunPredefinedStaticMethodStage newInstance(String className, String methodName, String argumentType) throws ClassNotFoundException, NoSuchMethodException {
        Class clazz = RunPredefinedStaticMethodStage.class.getClassLoader().loadClass(className);
        Class argumentClass = RunPredefinedStaticMethodStage.class.getClassLoader().loadClass(argumentType);
        return new RunPredefinedStaticMethodStage(clazz.getMethod(methodName, argumentClass));
    }
    
    /** 
     * <p>Calls the defined static method and exqueues the returned object if it is 
     * not null, otherwise placing the original object on the branch specified
     * by the nullResultBranchTag property.</p>
     *
     * @param obj The object to be processed.
     */
    public void process(Object obj) throws StageException {
        try {
            Object returnObj = this.method.invoke(null, obj);
            if (returnObj != null){
                this.exqueue(returnObj);
            } else {
                this.exqueue(nullResultBranchTag, obj);
            }
        } catch (IllegalAccessException e){
            throw new StageException("Illegal Access",e);
        } catch (InvocationTargetException e){
            throw new StageException("Invocation",e);
        }       
    }

    /**
     * Getter for property nullResultBranchTag. The default value is "nullResult".
     * @return Value of property nullResultBranchTag.
     */
    public String getNullResultBranchTag() {
        return this.nullResultBranchTag;
    }

    /**
     * Setter for property nullResultBranchTag.
     * @param nullResultBranchTag New value of property nullResultBranchTag.
     */
    public void setNullResultBranchTag(String nullResultBranchTag) {
        this.nullResultBranchTag = nullResultBranchTag;
    }
}
