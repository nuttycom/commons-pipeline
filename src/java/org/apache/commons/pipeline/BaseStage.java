/*
 * BaseStage.java
 *
 * Created on October 12, 2004, 11:31 AM
 */

package org.apache.commons.pipeline;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 *
 * @author  kjn
 */
public class BaseStage extends Stage {
    
    /** Creates a new instance of BaseStage using an unbounded BlockingQueue. */
    public BaseStage() {
        this(new LinkedBlockingQueue<Object>());
    }
    
    
    /** Creates a new instance of BaseStage with the specified queue. */
    public BaseStage(Queue<Object> queue) {
        super(queue);
    }
    
    /**
     * No-op implementation. This method should be overridden to provide
     * preprocessing capability for the stage.
     *
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     * @see Stage#preprocess()
     */
    public void preprocess() throws StageException {
    }
    
    /**
     * The only operation performed by this implementation of process()
     * is to exqueue the specified object, passing it to the subsequent stage.
     * This method should be overridden to provide
     * processing capability for the stage.
     *
     * @throws ClassCastException if the object is of an incorrect type
     * for the processing operation
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public void process(Object obj) throws StageException {
        this.exqueue(obj);
    }
    
    /**
     * No-op implementation. This method should be overridden to provide
     * postprocessing capability for the stage.
     *
     * @throws StageException an Exception thrown by the implementation should
     * be wrapped in a {@link StageException}
     */
    public void postprocess() throws StageException {
    }
    
    /**
     * No-op implementation. This method should be overridden to provide
     * resource release capability for the stage.
     *
     * Implementations overriding this method should clean up any lingering resources
     * that might otherwise be left allocated if an exception is thrown during
     * processing.
     * @see Stage#release()
     */
    public void release() {
    }
    
    
    
}
