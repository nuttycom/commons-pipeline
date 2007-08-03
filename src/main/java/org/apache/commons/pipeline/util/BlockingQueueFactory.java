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

import java.util.Comparator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.apache.commons.pipeline.StageDriver;

/**
 * Many {@link StageDriver} implementations require for one or more queues
 * to be created. This interface provides a consistent API for factories used
 * to create such queues and supplies a couple of default implementations.
 */
public interface BlockingQueueFactory<T> extends QueueFactory<T> {
    public BlockingQueue<T> createQueue();
    
    public static class ArrayBlockingQueueFactory<T> extends AbstractQueueFactory<T> implements BlockingQueueFactory<T> {
        public ArrayBlockingQueue<T> createQueue() {
            if (this.initialContents == null || this.initialContents.isEmpty()) {
                return new ArrayBlockingQueue<T>(this.capacity, this.fair);
            } else {
                if (this.initialContents.size() > this.capacity) {
                    throw new IllegalStateException("The number of elements in the initial contents of the queue to be created exceeds its capacity.");
                } else {
                    ArrayBlockingQueue<T> queue = new ArrayBlockingQueue<T>(this.capacity, this.fair);
                    queue.addAll(this.initialContents);
                    return queue;
                }
            }
        }
        
        /**
         * Holds value of property capacity.
         */
        private int capacity = Integer.MAX_VALUE;
        
        /**
         * Getter for property capacity.
         * @return Value of property capacity.
         */
        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
        
        /**
         * Setter for property capacity.
         * @param capacity New value of property capacity.
         */
        public int getCapacity() {
            return this.capacity;
        }
        
        /**
         * Holds value of property fair.
         */
        private boolean fair = false;
        
        /**
         * Getter for property fair.
         * @return Value of property fair.
         */
        public boolean isFair() {
            return this.fair;
        }
        
        /**
         * Setter for property fair.
         * @param fair New value of property fair.
         */
        public void setFair(boolean fair) {
            this.fair = fair;
        }
    }
    
    public static class DelayQueueFactoryL<T extends Delayed> extends AbstractQueueFactory<T>  implements BlockingQueueFactory<T> {
        public DelayQueue<T> createQueue() {
            if (this.initialContents == null || this.initialContents.isEmpty()) {
                return new DelayQueue<T>();
            } else {
                return new DelayQueue<T>(this.initialContents);
            }
        }
    }
    
    public static class LinkedBlockingQueueFactory<T> extends AbstractQueueFactory<T>  implements BlockingQueueFactory<T> {
        
        public LinkedBlockingQueue<T> createQueue() {
            if (this.initialContents == null || this.initialContents.isEmpty()) {
                return new LinkedBlockingQueue<T>(capacity);
            } else {
                if (this.initialContents.size() > this.capacity) {
                    throw new IllegalStateException("The number of elements in the initial contents of the queue to be created exceeds its capacity.");
                } else {
                    LinkedBlockingQueue<T> queue = new LinkedBlockingQueue<T>(capacity);
                    queue.addAll(this.initialContents);
                    return queue;
                }
            }
        }
        
        /**
         * Holds value of property capacity.
         */
        private int capacity = Integer.MAX_VALUE;
        
        /**
         * Getter for property capacity.
         * @return Value of property capacity.
         */
        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }
        
        /**
         * Setter for property capacity.
         * @param capacity New value of property capacity.
         */
        public int getCapacity() {
            return this.capacity;
        }
    }
    
    public static class PriorityBlockingQueueFactory<T> extends AbstractQueueFactory<T> implements BlockingQueueFactory<T> {
        public PriorityBlockingQueue<T> createQueue() {
            if (comparator == null) {
                if (this.initialContents == null || this.initialContents.isEmpty()) {
                    return new PriorityBlockingQueue<T>(initialCapacity);
                } else {
                    return new PriorityBlockingQueue<T>(this.initialContents);
                }
            } else {
                PriorityBlockingQueue<T> queue = new PriorityBlockingQueue<T>(initialCapacity, comparator);
                if ( !(this.initialContents == null || this.initialContents.isEmpty()) ) {
                    queue.addAll(this.initialContents);
                }
                return queue;
            }
        }
        
        /**
         * Holds value of property initialCapacity. Default value is the same
         * as that for java.util.concurrent.PriorityBlockingQueue.
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
    
    public static class SynchronousQueueFactory<T> implements BlockingQueueFactory<T> {
        public SynchronousQueue<T> createQueue() {
            return new SynchronousQueue<T>(this.fair);
        }
        
        /**
         * Holds value of property fair.
         */
        private boolean fair = false;
        
        /**
         * Getter for property fair.
         * @return Value of property fair.
         */
        public boolean isFair() {
            return this.fair;
        }
        
        /**
         * Setter for property fair.
         * @param fair New value of property fair.
         */
        public void setFair(boolean fair) {
            this.fair = fair;
        }
    }
}
