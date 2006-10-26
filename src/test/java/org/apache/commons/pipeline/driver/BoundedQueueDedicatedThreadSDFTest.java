/*
 * FiniteDedicatedThreadStageDriverFactoryTest.java
 * JUnit based test
 *
 * Created on August 11, 2006, 12:45 PM
 */

package org.apache.commons.pipeline.driver;

import java.util.concurrent.LinkedBlockingQueue;
import junit.framework.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.AbstractLoggingTestCase;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageDriver;
import org.apache.commons.pipeline.testFramework.TestStage;
import org.apache.commons.pipeline.testFramework.TestStageContext;

/**
 * Test the FiniteDedicatedThreadStageDriverFactory behavior. Primarily focus
 * on the Feeder queue capacity.
 * <P>
 * Testing of accessor methods has been omitted as per normal unit testing
 * conventions.
 *
 */
public class BoundedQueueDedicatedThreadSDFTest extends AbstractLoggingTestCase {
    private TestStage stage;
    private TestStageContext context;
    private Log log;
    
    /**
     * Construct a named test.
     * @param testName is an identifying name for this instance
     */
    public BoundedQueueDedicatedThreadSDFTest(String testName) {
        super(testName);
        this.log = LogFactory.getLog(BoundedQueueDedicatedThreadSDFTest.class);
    }

    /**
     * Allocate a stage and context for testing the DedicatedThreadStageDriverFactory
     * @throws java.lang.Exception if there are errors preparing the test environment
     */
    protected void setUp() throws Exception {     
        this.stage = new TestStage(0);
        this.context = new TestStageContext();
    }

    /**
     * Release memory allocated by the setUp method.
     * @throws java.lang.Exception if there are errors releasing resources
     */
    protected void tearDown() throws Exception {
        this.stage = null;
        this.context = null;
    }

    /**
     * Test suite is convenience method to run all the tests.
     * @return a composite object of the tests to be run
     */
    public static Test suite() {
        TestSuite suite = new TestSuite(BoundedQueueDedicatedThreadSDFTest.class);
        
        return suite;
    }

    /**
     * Test of createStageDriver method, of class 
     * org.apache.commons.pipeline.driver.FiniteDedicatedThreadStageDriverFactory.
     */
    public void testCreateStageDriver() {
        log.debug("createStageDriver ----------------------------------------");
        
        BoundedQueueDedicatedThreadSDF instance = new BoundedQueueDedicatedThreadSDF();
        
        // Check defaults
        int expectedQueueSizeDefault = 100;
        int queueSizeResult = instance.getQueueSize();
        assertEquals(expectedQueueSizeDefault, queueSizeResult);
        
        long expectedTimeoutDefault = 500;
        long timeoutResult = instance.getTimeout();
        assertEquals(expectedTimeoutDefault, timeoutResult);
        
        FaultTolerance expectedFaultToleranceDefault = FaultTolerance.NONE;
        FaultTolerance faultToleranceResult = instance.getFaultTolerance();
        assertEquals(expectedFaultToleranceDefault, faultToleranceResult);
        
        // Test for StageDriver creation with defaults.
        DedicatedThreadStageDriver result = (DedicatedThreadStageDriver) instance.createStageDriver(stage, context);
        assertNotNull(result);
        
        int newQueueSize = result.getQueueSize();
        assertEquals(expectedQueueSizeDefault, newQueueSize);
        
        long newTimeout = result.getTimeout();
        assertEquals(expectedTimeoutDefault, newTimeout);
        
        FaultTolerance newFaultTolerance = result.getFaultTolerance();
        assertEquals(expectedFaultToleranceDefault, newFaultTolerance);
        
        // Test for StageDriver creation with custom values.
        int expectedQueueSizeCustom = 25;
        long expectedTimeoutCustom = 3000;
        FaultTolerance expectedFaultToleranceCustom = FaultTolerance.CHECKED;
        
        // Load the custom values into the factory before calling createStageDriver.
        instance.setFaultTolerance(expectedFaultToleranceCustom);
        instance.setQueueSize(expectedQueueSizeCustom);
        instance.setTimeout(expectedTimeoutCustom);
        
        result = (DedicatedThreadStageDriver) instance.createStageDriver(stage, context);
        assertNotNull(result);
        
        newQueueSize = result.getQueueSize();
        assertEquals(expectedQueueSizeCustom, newQueueSize);
        
        newTimeout = result.getTimeout();
        assertEquals(expectedTimeoutCustom, newTimeout);
        
        newFaultTolerance = result.getFaultTolerance();
        assertEquals(expectedFaultToleranceCustom, newFaultTolerance);
    } // testCreateStageDriver()
    
}
