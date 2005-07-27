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
 *
 * Created on July 19, 2005, 4:34 PM
 *
 * $Log: AbstractStageMonitor.java,v $
 * Revision 1.3  2005/07/25 22:04:54  kjn
 * Corrected Apache licensing, documentation.
 *
 * Revision 1.2  2005/07/22 23:21:35  kjn
 * Added stage parameter to constructor to simplify callbacks for those monitors
 * that need to access the Stage.
 */

package org.apache.commons.pipeline.driver;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageMonitor;
import org.apache.commons.pipeline.StageMonitor.State;

/**
 * Provades an abstract {@link StageMonitor} base class that implements methods 
 * that do not need to be synchronized.
 *
 * @author Travis Stevens, National Geophysical Data Center, NOAA
 */
public abstract class AbstractStageMonitor implements StageMonitor {
    private static final Log log = LogFactory.getLog(AbstractStageMonitor.class);
    
    protected Stage stage;
    protected List<Throwable> errors = new ArrayList<Throwable>();
    protected State state = State.STOPPED;
    
    /**
     * Creates a new instance of AbstractStageMonitor
     */
    public AbstractStageMonitor(Stage stage) {
        this.stage = stage;
    }
    
    /**
     *
     */
    public final void preprocessFailed(Throwable fault) {
        this.state = State.ERROR;
        log.fatal("Error in preprocessing caused abort.", fault);
        this.errors.add(fault);
    }
    
    /**
     * Monitors handler failures.
     *
     * @param data the data that was being processed as the fault occurred
     * @param fault the faulting exception
     */
    public final void processingFailed( Object data, Throwable fault) {
        log.error("Processing error on data object " + data, fault);
        this.errors.add(fault);
    }
    
    /**
     *
     */
    public final void postprocessFailed(Throwable fault) {
        this.state = State.ERROR;
        log.fatal("Error in postprocessing caused abort.", fault);
        this.errors.add(fault);
    }
    
    /**
     * Monitors driver thread interruption failures.
     *
     * @param fault the faulting exception
     */
    public final void driverFailed( InterruptedException fault ) {
        this.state = State.ERROR;
        this.errors.add(fault);
    }
}
