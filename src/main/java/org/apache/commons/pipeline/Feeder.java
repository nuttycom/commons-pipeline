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

package org.apache.commons.pipeline;


/**
 * This interface represents a data channel into which objects can be fed.
 * Feeders act as intermediaries between stages in a pipeline and the drivers
 * for subsequent stages. Each {@link StageDriver} implementation will
 * ordinarily provide a custom Feeder implementation that integrates receiving
 * objects with its internal stage processing workflow.
 *
 *
 */
public interface Feeder {
    /**
     * This Feeder implementation provides a standard, no-op sink for objects.
     * It is useful for situations like the terminus of a pipeline, where
     * there is nothing to be done with a generated object.
     */
    public static final Feeder VOID = new Feeder() {
        public void feed(Object obj) { }
    };
    
    /**
     * Feeds the specified object to an underlying receiver.
     * @param obj The object being fed to the receiver.
     */
    public void feed(Object obj);
}
