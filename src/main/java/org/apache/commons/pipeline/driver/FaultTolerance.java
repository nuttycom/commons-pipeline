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

package org.apache.commons.pipeline.driver;

/**
 * Stores the failure tolerance flag for a worker thread. If faultTolerance
 * is set to CHECKED, {@link org.apache.commons.pipeline.StageException StageException}s thrown by
 * the {@link org.apache.commons.pipeline.Stage#process(Object)} method will not interrupt queue
 * processing, but will simply be logged with a severity of ERROR.
 * If faultTolerance is set to ALL, runtime exceptions will also be
 * logged and otherwise ignored.
 *
 *
 */
public enum FaultTolerance {
    /**
     * {@link org.apache.commons.pipeline.StageException StageException}s thrown by
     * the {@link org.apache.commons.pipeline.Stage#process(Object)} method will interrupt queue
     * processing and will be logged with a severity of ERROR.
     */
    NONE, 
    /**
     * {@link org.apache.commons.pipeline.StageException StageException}s thrown by
     * the {@link org.apache.commons.pipeline.Stage#process(Object)} method will not interrupt queue
     * processing, but will simply be logged with a severity of ERROR.
     */
    CHECKED, 
    /**
     * If faultTolerance is set to ALL, runtime exceptions will be
     * logged and otherwise ignored.
     */
    ALL;
}
