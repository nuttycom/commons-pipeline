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

package org.apache.commons.pipeline.stage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.apache.commons.pipeline.BaseStage;
import org.apache.commons.pipeline.StageException;

/**
 * <p>Provide this Stage with a class and a static method name and it will dynamically
 * look up the appropriate method to call based on the object type.  If the
 * object type is an array, it will assume that the method that needs to be called
 * contains the method signature as described by the objects in the array.
 * The object returned from the method call will be exqueued.</p>
 *
 * <p>The resulting object will be exqueued on the main pipeline if it is not null.  If
 * it is null, we will try to place the original object on the branch specified
 * by the nullResultBranchTag property. The default for this value is "nullResult".</p>
 */
public class DynamicLookupStaticMethodStage extends BaseStage {
    
    // Branch upon which the original objects will be enqueued if the defined
    // Method returned a null result.
    private String nullResultBranchTag = "nullResult";
    
    // Name of the method to call to process the object.
    private String methodName;
    
    // Class containing the method.
    private Class clazz;
    
    /**
     * Creates a new instance of DynamicLookupStaticMethodStage
     *
     * @param clazz The class that defines the static method that will be used to
     * process objects.
     * @param methodName The name of the method. This method may be overloaded.
     */
    public DynamicLookupStaticMethodStage(Class clazz, String methodName) {
        super();
        if (clazz == null){
            throw new IllegalArgumentException("Argument 'clazz' can not be null.");
        }
        if (methodName == null){
            throw new IllegalArgumentException("Argument 'methodName' can not be null.");
        }
        this.clazz = clazz;
        this.methodName = methodName;
    }
    
    /**
     * Creates a new DynamicLookupStaticMethodStage for the specified static
     * method.
     *
     * @param className The fully qualified class name of the class in which the
     * static method that will be used to process objects is defined.
     * @param methodName The name of the method. This method may be overloaded.
     */
    public static DynamicLookupStaticMethodStage newInstance(String className, String methodName) throws ClassNotFoundException {
        Class clazz = DynamicLookupStaticMethodStage.class.getClassLoader().loadClass(className);
        return new DynamicLookupStaticMethodStage(clazz, methodName);
    }
    
    /** Returns the name of the method we are using */
    public String getMethodName(){
        return this.methodName;
    }
    
    /** Returns the class we are using */
    public Class getMethodClass(){
        return this.clazz;
    }
    
    /**
     * <p>Finds the appropriate method overloading for the method specified
     * by {@link #getMethodName() methodName}, calls it to process the object, and exqueues
     * any returned object. If the returned object is null, the original object
     * is enqueued on the branch specified by the nullResultBranchTag property.</p>
     *
     * @param obj The object to process.
     */
    public void process(Object obj) throws StageException {
        try {
            Method method = null;
            if (obj.getClass().isArray()){
                Object[] objs = (Object[]) obj;
                Class[] classes = new Class[objs.length];
                for (int i = 0; i < objs.length; i++){
                    classes[i] = objs[i].getClass();
                }
                method = this.clazz.getMethod(methodName, classes);
            } else {
                method = this.clazz.getMethod(methodName, obj.getClass());
            }
            
            Object returnObj = method.invoke(null, obj);
            if (returnObj != null){
                this.exqueue(returnObj);
            } else {
                this.exqueue("nullResult", obj);
            }
        } catch (NoSuchMethodException e){
            throw new StageException("No method",e);
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
