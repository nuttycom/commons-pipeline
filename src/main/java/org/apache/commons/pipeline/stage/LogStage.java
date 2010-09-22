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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.StageException;


/**
 * A do-nothing implementation of Stage that simply logs the state of processing. 
 * and each object seen by its {@link #process(Object)} method.
 * Useful for debugging purposes. 
 */
public class LogStage extends BaseStage {
    private Log log = LogFactory.getLog(LogStage.class);
    
    /**
     * Creates a new LogStage.
     */
    public LogStage() {
    }
    
    /**
     * Logs the point at which preprocessing runs.
     */
    public void preprocess() throws StageException {
        super.preprocess();
        log.info("Stage " + this.getClass().getName() + " preprocessing.");
    }
    
    /**
     * Logs the current state of an object on the queue and passes the
     * object unchanged to the next stage in the pipeline.
     */
    public void process(Object obj) throws StageException {
        log.info("Processing object " + obj);
        this.emit(obj);
    }
        
    /**
     * Logs tht point at which postprocessing runs
     */
    public void postprocess() throws StageException {
        log.info("Stage " + this.getClass().getName() + " postprocessing.");
    }
    
    /**
     * Logs the point at which stage resources are released.
     */
    public void release() {
        log.info("Stage " + this.getClass().getName() + " released.");
    }

    /**
     * Sets the logger.
     */
    public synchronized void setLog(Log log) {
        this.log = log;
    }
    
    /**
     * Sets the logger based upon the log name.
     */
    public synchronized void setLog(String logName) {
        this.log = LogFactory.getLog(logName);
    }
    
    /**
     * Sets the logger based upon the specified class.
     */
    public synchronized void setLog(Class clazz) {
        this.log = LogFactory.getLog(clazz);
    }
}
