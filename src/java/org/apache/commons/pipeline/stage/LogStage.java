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

package org.apache.commons.pipeline.impl;

import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.BaseStage;
import org.apache.log4j.Logger;
import java.util.Queue;


/**
 * A do-nothing implementation of Stage with Log4j logging. Useful for debugging purposes.
 *
 * @author Kris Nuttycombe, National Geophysical Data Center
 * @version $Revision$
 */
public class LogStage extends BaseStage {
    private Logger log = Logger.getLogger(this.getClass());
    
    /**
     * Creates a new LogStage.
     */
    public LogStage() {
    }
    
    /**
     * Creates a new LogStage with the specified {@link StageQueue}.
     */
    public LogStage(Queue<Object> queue) {
        super(queue);
    }
    
    /**
     * Logs the point at which preprocessing runs.
     */
    public void preprocess() throws StageException {
        log.info("Stage " + this.getClass().getName() + " preprocessing.");
    }
    
    /**
     * Logs the current state of an object on the queue and passes the
     * object unchanged to the next stage in the pipeline.
     */
    public void process(Object obj) throws StageException {
        log.info("Processing object " + obj);
        this.exqueue(obj);
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
     * Default toString implementation.
     */
    public String toString() {
        return this.getClass().getName();
    }
}
