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

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.commons.pipeline.StageDriver;

/**
 * Many {@link StageDriver} implementations require for one or more queues
 * to be created. This interface provides a consistent API for factories used
 * to create such queues and supplies a couple of default implementations.
 */
public interface QueueFactory<T> {
    /**
     * Create a new queue.
     */
    public Queue<T> createQueue();
    
    public static abstract class AbstractQueueFactory<T> {
        /**
         * Holds value of property initialContents.
         */
        protected Collection<? extends T> initialContents;
        
        /**
         * Getter for property initialContents.
         * @return Value of property initialContents.
         */
        public Collection<? extends T> getInitialContents() {
            return this.initialContents;
        }
        
        /**
         * Setter for property initialContents.
         * @param initialContents New value of property initialContents.
         */
        public void setInitialContents(Collection<? extends T> initialContents) {
            this.initialContents = initialContents;
        }
    }
    
    public static class LinkedListFactory<T> extends AbstractQueueFactory<T> implements QueueFactory<T> {
        public LinkedList<T> createQueue() {
            if (this.initialContents == null || this.initialContents.isEmpty()) {
                return new LinkedList<T>();
            } else {
                return new LinkedList<T>(this.initialContents);
            }
        }
    }
    
    public static class ConcurrentLinkedQueueFactory<T> extends AbstractQueueFactory<T> implements QueueFactory<T> {
        public ConcurrentLinkedQueue<T> createQueue() {
            if (this.initialContents == null || this.initialContents.isEmpty()) {
                return new ConcurrentLinkedQueue<T>();
            } else {
                return new ConcurrentLinkedQueue<T>(this.initialContents);
            }
        }
    }
    
    public static class PriorityQueueFactory<T> extends AbstractQueueFactory<T> implements QueueFactory<T> {
        public PriorityQueue<T> createQueue() {
            if (comparator == null) {
                if (this.initialContents == null || this.initialContents.isEmpty()) {
                    return new PriorityQueue<T>(initialCapacity);
                } else {
                    return new PriorityQueue<T>(this.initialContents);
                }
            } else {
                PriorityQueue<T> queue = new PriorityQueue<T>(initialCapacity, comparator);
                queue.addAll(this.initialContents);
                return queue;
            }
        }
        
        /**
         * Holds value of property initialCapacity. Default value is the same
         * as that for java.util.concurrent.PriorityQueue.
         */
        private int initialCapacity = 11;
        
        /**
         * Getter for property initialCapacity.
         * @return Value of property initialCapacity.
         */
        public int getInitialCapacity() {
            return this.initialCapacity;
        }
        
        /**
         * Setter for property initialCapacity.
         * @param initialCapacity New value of property initialCapacity.
         */
        public void setInitialCapacity(int initialCapacity) {
            this.initialCapacity = initialCapacity;
        }
        
        /**
         * Holds value of property comparator.
         */
        private Comparator<? super T> comparator;
        
        /**
         * Getter for property comparator.
         * @return Value of property comparator.
         */
        public Comparator<? super T> getComparator() {
            return this.comparator;
        }
        
        /**
         * Setter for property comparator.
         * @param comparator New value of property comparator.
         */
        public void setComparator(Comparator<? super T> comparator) {
            this.comparator = comparator;
        }
    }
}
