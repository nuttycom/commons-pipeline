/*
 * StageRunner.java
 *
 * Created on October 6, 2004, 4:30 PM
 */

package org.apache.commons.pipeline;

import org.apache.commons.pipeline.Stage;


/**
 *
 * @author  kjn
 */
public interface StageDriver {
    /**
     * Creates and starts new worker thread(s) to process items in the queue.
     * Implementations of this method must call {@link StageMonitor#driverStarting()}
     * on the specified stage's monitor.
     */
    public void start(Stage stage) throws IllegalThreadStateException;
    
    
    /**
     * This method waits for the queue to empty and any processor thread(s) to exit
     * cleanly and then calls release() to release any resources acquired during processing, if possible.
     * Implementations of this method must call {@link StageMonitor#driverStopped()}
     * on the specified stage's monitor upon completion.
     */
    public void finish(Stage stage) throws InterruptedException;    
}
