/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.    
 */ 

package org.apache.commons.pipeline.util;

/**
 * An implementation of this interface should define a strategy that will
 * allow a unique identifier to be generated for an object. This identifier
 * may be mutually comparable with identifiers generated for a different 
 * object type; it is used to permit signalling between drivers in
 * separate pipeline branches.
 *
 *
 */
public interface KeyFactory<T,K> {
    /**
     * Generates a unique identifier of type K for an object of type T.
     * @return the newly created identifier
     */ 
    public K generateKey(T source);
    
    /**
     * Trivial key factory that produces the object's hash code as a key.
     */
    public static class HashKeyFactory implements KeyFactory<Object,Integer> {
        public Integer generateKey(Object source) {
            return source.hashCode();
        }        
    }
}
