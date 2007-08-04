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
 * <p>
 * Provide this Stage with a class and a static method name and it will
 * dynamically look up the appropriate method to call based on the object type.
 * If the object type is an array, it will assume that the method that needs to
 * be called contains the method signature as described by the objects in the
 * array. The object returned from the method call will be exqueued.
 * </p>
 *
 * <p>
 * The resulting object will be exqueued on the main pipeline if it is not null.
 * If it is null, we will try to place the original object on the branch
 * specified by the nullResultBranchKey property. The default for this value is
 * "nullResult".
 * </p>
 */
public class DynamicLookupStaticMethodStage extends BaseStage {
    
    // Branch upon which the original objects will be enqueued if the defined
    // Method returned a null result.
    private String nullResultBranchKey;
    
    // Name of the method to call to process the object.
    private String methodName;
    
    // Class containing the method.
    private Class clazz;
    
    /**
     * Creates a new instance of DynamicLookupStaticMethodStage
     *
     * @param clazz
     *            The class that defines the static method that will be used to
     *            process objects.
     * @param methodName
     *            The name of the method. This method may be overloaded.
     */
    public DynamicLookupStaticMethodStage(Class clazz, String methodName) {
        if (clazz == null) throw new IllegalArgumentException("Argument 'clazz' can not be null.");
        if (methodName == null) throw new IllegalArgumentException("Argument 'methodName' can not be null.");
        
        this.clazz = clazz;
        this.methodName = methodName;
    }
    
    /**
     * Creates a new DynamicLookupStaticMethodStage for the specified class and
     * static method.
     *
     * @param className
     *            The fully qualified class name of the class in which the
     *            static method that will be used to process objects is defined.
     * @param methodName
     *            The name of the method. This method may be overloaded.
     * @throws ClassNotFoundException
     *             if the specified class cannot be loaded.
     */
    public DynamicLookupStaticMethodStage(String className, String methodName) throws ClassNotFoundException {
        this(Thread.currentThread().getContextClassLoader().loadClass(className), methodName);
    }
    
    /**
     * <p>
     * Finds the appropriate method overloading for the method specified by
     * {@link #getMethodName() methodName}, calls it to process the object, and
     * exqueues any returned object. If the returned object is null, the
     * original object is enqueued on the branch specified by the
     * nullResultBranchKey property.
     * </p>
     *
     * @param obj
     *            The object to process.
     */
    public void process(Object obj) throws StageException {
        Class[] argTypes;
        if (obj.getClass().isArray()) {
            Object[] objs = (Object[]) obj;
            argTypes = new Class[objs.length];
            for (int i = 0; i < objs.length; i++) {
                argTypes[i] = objs[i].getClass();
            }
        } else {
            argTypes = new Class[] {obj.getClass()};
        }
        
        try {
            Method method = this.clazz.getMethod(methodName, argTypes);
            
            // due to the way that varargs work, we need to ensure that we get
            // the correct compile-time overloading of method.invoke()
            Object result = obj.getClass().isArray() ? method.invoke(null, (Object[]) obj) : method.invoke(null, obj);
            if (result != null){
                this.emit(result);
            } else if (this.nullResultBranchKey != null) {
                this.context.getBranchFeeder(this.nullResultBranchKey).feed(obj);
            }
        } catch (NoSuchMethodException e){
            StringBuilder message = new StringBuilder("No acceptable method " + methodName + " found with argument(s) of type: [ ");
            for (Class<?> clazz : argTypes) message.append(clazz.getName()).append(" ");
            message.append("]");
            
            throw new StageException(this, message.toString() ,e);
        } catch (IllegalAccessException e){
            throw new StageException(this, e);
        } catch (InvocationTargetException e){
            throw new StageException(this, e);
        }
    }
    
    /** Returns the name of the method to be executed. */
    public String getMethodName(){
        return this.methodName;
    }
    
    /** Returns the class containing the method to be executed */
    public Class getMethodClass(){
        return this.clazz;
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
     * Setter for property nullResultBranchKey.
     *
     * @param nullResultBranchKey
     *            New value of property nullResultBranchKey.
     */
    public void setNullResultBranchKey(String nullResultBranchKey) {
        this.nullResultBranchKey = nullResultBranchKey;
    }
    
}
