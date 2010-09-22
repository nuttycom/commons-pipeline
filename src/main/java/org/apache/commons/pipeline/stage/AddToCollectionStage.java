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

import java.util.Collection;
import java.util.Collections;

import org.apache.commons.pipeline.validation.ConsumedTypes;
import org.apache.commons.pipeline.validation.ProducesConsumed;

/**
 * This is a simple stage in the pipeline which will add each processed object 
 * to the specified collection. 
 *
 * For the purposes of validation, this stage is considered to be able to consume
 * objects of any class although the process() method may throw a ClassCastException
 * if a processed object cannot be added to the collection.
 */
@ConsumedTypes(Object.class)
@ProducesConsumed
public class AddToCollectionStage<T> extends BaseStage {
    
    /**
     * Holds value of property collection.
     */
    private Collection<T> collection;

    /** 
     * Creates a new instance of AddToCollectionStage.  This constructor
     * will synchronized the collection by default.
     */
    public AddToCollectionStage(Collection<T> collection) {
        this(collection, true);
    }
    
    /** 
     * Creates a new instance of AddToCollectionStage.
     * @param collection The collection in which to add objects to
     * @param synchronized A flag value that determines whether or not accesses
     * to the underlying collection are synchronized.
     */
    public AddToCollectionStage(Collection<T> collection, boolean synchronize) {
        if (collection == null){
            throw new IllegalArgumentException("Argument 'collection' can not be null.");
        }
        
        this.collection = synchronize ? Collections.synchronizedCollection(collection) : collection;
    }
    
    /** 
     * Adds the object to the underlying collection.
     *
     * @throws ClassCastException if the object is not of a suitable type to be added
     * to the collection.
     */
    public void process(Object obj) throws org.apache.commons.pipeline.StageException {
        this.collection.add((T) obj);
        this.emit(obj);
    }
    
    /**
     * Returns the collection to which elements have been added during
     * processing.
     */
    public Collection<T> getCollection() {
        return this.collection;
    }    
}
