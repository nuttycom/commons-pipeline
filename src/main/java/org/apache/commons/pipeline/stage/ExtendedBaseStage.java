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

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.math.stat.descriptive.SynchronizedDescriptiveStatistics;
import org.apache.commons.pipeline.Feeder;
import org.apache.commons.pipeline.Stage;
import org.apache.commons.pipeline.StageContext;
import org.apache.commons.pipeline.StageException;

/**
 * Base class for pipeline stages. Keeps track of performance statistics and allows
 * for collection and adjustment via JMX (optional)
 *
 * Cannot extend BaseStage because it marks the emit methods as final.
 *
 * @author mzsanford
 */
public abstract class ExtendedBaseStage implements Stage, ExtendedBaseStageMBean {
    /**  Minimum percentage of blocking before we report per-branch stats. */
    private static final float BRANCH_BLOCK_THRESHOLD = 1.0f;
    /** Default size of the moving-window average statistics */
    private static final int DEFAULT_DESCRIPTIVE_STATS_WINDOW_SIZE = 100;
    /** Default queue name when reporting statistics */
    private static final String DEFAULT_QUEUE_NAME = "[DefaultQueue]";
    /** Default number of objects after which a status message is logged */
    private static final int DEFAULT_STATUS_INTERVAL = 1000;
    protected final Log log = LogFactory.getLog( getClass() );

    protected StageContext stageContext;
    private Feeder downstreamFeeder;
    private String stageName;
    private final AtomicLong objectsReceived = new AtomicLong(0);
    private final AtomicLong unhandledExceptions = new AtomicLong(0);
    private final AtomicLong totalServiceTime = new AtomicLong(0);
    private final AtomicLong totalEmitTime = new AtomicLong(0);
    private final AtomicLong totalEmits = new AtomicLong(0);
    private final Map<String, AtomicLong> emitTimeByBranch = new HashMap<String, AtomicLong>();
    private int currentStatWindowSize = DEFAULT_DESCRIPTIVE_STATS_WINDOW_SIZE;
    private SynchronizedDescriptiveStatistics serviceTimeStatistics;
    private long statusInterval = DEFAULT_STATUS_INTERVAL;
    private Integer statusBatchSize = 1;
    private boolean collectBranchStats = false;
    private boolean preProcessed = false; // prevent recursion.
    private boolean postProcessed = false; // prevent recursion.
    private boolean jmxEnabled = true;

    /**
     * Class name for status message. Needed because java.util.logging only
     * reports the base class name.
     */
    private final String className = getClass().getSimpleName();

    /**
     * ThreadLocal sum of time spent waiting on blocked queues during the current process call.
     */
    protected static ThreadLocal<AtomicLong> emitTotal = new ThreadLocal<AtomicLong>() {
        protected synchronized AtomicLong initialValue() {
            return new AtomicLong();
        }
    };

    /**
     * ThreadLocal sum of time spent waiting on blocked queues during the current process call by queue name.
     */
    protected static ThreadLocal<Map<String, AtomicLong>> threadLocalEmitBranchTime = new ThreadLocal<Map<String, AtomicLong>>() {
        protected synchronized Map<String, AtomicLong> initialValue() {
            return new HashMap<String, AtomicLong>();
        }
    };

    /**
     * ThreadLocal count of emit calls during the current process call.
     */
    protected static ThreadLocal<AtomicInteger> emitCount = new ThreadLocal<AtomicInteger>() {
        protected synchronized AtomicInteger initialValue() {
            return new AtomicInteger();
        }
    };

    /**
     * ThreadLocal formatter since they are not thread safe.
     */
    protected static ThreadLocal<NumberFormat> floatFormatter = new ThreadLocal<NumberFormat>() {
        protected synchronized NumberFormat initialValue() {
            return new DecimalFormat("##0.000");
        }
    };

    public ExtendedBaseStage() {
        // Empty constructor provided for future use.
    }

    public void init( StageContext context ) {
        this.stageContext = context;
        if (jmxEnabled) {
            enableJMX(context);
        }
    }

    @SuppressWarnings("unchecked")
    private final void enableJMX(StageContext context) {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        if (mbs != null) {
            // Try to build a unique JMX name.
            StringBuilder sb = new StringBuilder("org.apache.commons.pipeline:");
            sb.append("class=").append(className);
            if (stageName != null) {
                sb.append(",name=").append(stageName);
            }

            try {
                ObjectName name = new ObjectName(sb.toString());
                if (mbs.isRegistered(name)) {
                    log.info("JMX Overlap. Multiple instances of '" + name + "'. Only one will be registered.");
                } else {
                    Class mbeanInterface = ExtendedBaseStageMBean.class;
                    try {
                        // Find out if the stage has a more specific MBean. Reflection can be slow
                        // but this operation is pretty fast. Not to mention it's only done at startup.
                        Class[] interfaces = getClass().getInterfaces();
                        for (int i=0 ; i < interfaces.length; i++) {
                            Class current = interfaces[i];
                            // Only use interfaces that extend ExtendedBaseStageMBean to maintain a minimum
                            // amount of functionality.
                            if (current != ExtendedBaseStageMBean.class
                                && ExtendedBaseStageMBean.class.isAssignableFrom(current)) {
                                mbeanInterface = current;
                                break;
                            }
                        }
                    } catch (Exception e) {
                        // In the event of security or cast exceptions, default back to base.
                        log.info("Reflection error while checking for JMX interfaces.");
                        // Reset in the off chance it got hosed.
                        mbeanInterface = ExtendedBaseStageMBean.class;
                    }

                    StandardMBean mbean = new StandardMBean(this,
                                                            mbeanInterface);
                    mbs.registerMBean(mbean, name);
                    log.info("JMX MBean registered: " + name.toString() + " (" + mbeanInterface.getSimpleName() + ")");
                }
            } catch (Exception e) {
                log.warn("Failed to register with JMX server",e);
            }
        }
    }

    /**
     * Called when a stage has been created but before the first object is
     * sent to the stage for processing. Subclasses
     * should use the innerPreprocess method, which is called by this method.
     *
     * @see org.apache.commons.pipeline.Stage#preprocess()
     */
    public final void preprocess() throws StageException {
        if ( !preProcessed ) {
            serviceTimeStatistics = new SynchronizedDescriptiveStatistics();
            serviceTimeStatistics.setWindowSize(currentStatWindowSize);
            innerPreprocess();
        }
        preProcessed = true;
    }

    public final void process( Object obj ) throws StageException {
        objectsReceived.incrementAndGet();
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            this.innerProcess( obj );
        } catch (Exception e) {
            // Hate to catch Exception, but don't want anything killing off the thread
            // and hanging the pipeline.
            log.error("Uncaught exception in " + className + ": " + e.getMessage(), e);
            unhandledExceptions.incrementAndGet();
        }
        stopWatch.stop();

        long totalTime = stopWatch.getTime();
        totalServiceTime.addAndGet(totalTime);

        // I hate to synchronize anything in the base class, but this
        // call should be very fast and is not thread safe.
        serviceTimeStatistics.addValue(totalTime);

        // Use ThreadLocals so that the stats only reflect process
        // calls that have completed. Otherwise the emit times can
        // exceed the process times.
        totalEmits.addAndGet(emitCount.get().intValue());
        totalEmitTime.addAndGet(emitTotal.get().longValue());
        emitCount.remove();
        emitTotal.remove();

        if (collectBranchStats) {
            for (Map.Entry<String, AtomicLong> entry : threadLocalEmitBranchTime.get().entrySet()) {
                if (emitTimeByBranch.containsKey(entry.getKey())) {
                    emitTimeByBranch.get(entry.getKey()).addAndGet(entry.getValue().longValue());
                } else {
                    // Race condition. containsKey could return false and another thread could insert
                    // here. We can synchronize here and we will very rarely hit this block. Only the first
                    // time each queue is accessed.
                    synchronized (emitTimeByBranch) {
                        // Double check for the race condition.
                        if (emitTimeByBranch.containsKey(entry.getKey())) {
                            emitTimeByBranch.get(entry.getKey()).addAndGet(entry.getValue().longValue());
                        } else {
                            emitTimeByBranch.put(entry.getKey(), new AtomicLong(entry.getValue().longValue()));
                        }
                    }
                }
            }
            threadLocalEmitBranchTime.remove();
        }

        if ( objectsReceived.longValue() % statusInterval == 0 ) {
            logStatus();
        }
    }

    /**
     * Convenience method to feed the specified object to the next stage downstream.
     */
    public final void emit( Object obj ) {
        if ( log.isDebugEnabled() ) {
            log.debug( this.getClass() + " is emitting object " + obj );
        }
        if ( this.downstreamFeeder == null ) {
            synchronized (this) {
                // Lazy init the default feeder.
                this.downstreamFeeder = stageContext.getDownstreamFeeder( this );
            }
        }
        feed( DEFAULT_QUEUE_NAME, downstreamFeeder, obj );
    }

    /**
     * Convenience method to feed the specified object to the first stage of the specified branch.
     */
    public final void emit( String branch, Object obj ) {
        Feeder feeder = this.stageContext.getBranchFeeder( branch );
        feed( branch, feeder, obj );
    }

    private void feed(String name, Feeder feeder, Object obj ) {
        if ( feeder == null ) {
            // The pipeline code should never allow this to happen.
            String objectType = ( obj != null ? obj.getClass().getSimpleName() : "null" );
            log.error( "Ignoring attempt to emit " + objectType +
                       " object to invalid feeder" );
            return;
        }
        StopWatch emitWatch = new StopWatch();
        emitWatch.start();

        // Pass the emitted object to the next stage (default or branch)
        feeder.feed( obj );

        emitWatch.stop();

        // Use ThreadLocal variables so the emit totals do not
        // go up until the process call completes.
        emitTotal.get().addAndGet( emitWatch.getTime() );
        emitCount.get().incrementAndGet();

        if (collectBranchStats) {
            if (! threadLocalEmitBranchTime.get().containsKey(name)) {
                AtomicLong currentTotal = new AtomicLong(emitWatch.getTime());
                threadLocalEmitBranchTime.get().put(name, currentTotal);
            } else {
                threadLocalEmitBranchTime.get().get(name).addAndGet(emitWatch.getTime());
            }
        }
    }

    /**
     * Called when a stage has completed all processing. Subclasses
     * should use the innerPostprocess method, which is called by this method.
     *
     * @see org.apache.commons.pipeline.Stage#postprocess()
     */
    public final void postprocess() throws StageException {
        if ( !postProcessed ) {
            logStatus();
            innerPostprocess();
        }
        postProcessed = true;
    }

    public void release() {
        // No-op implementation to fulfill interface
    }

    public abstract void innerProcess( Object obj )
      throws StageException;

    public void innerPreprocess() throws StageException {
        // No-op default implementation.
    }

    public void innerPostprocess() throws StageException {
        // No-op default implementation.
    }

    /**
     * Class-specific status message. Null or empty status' will be ignored.
     */
    public abstract String status();

    public void logStatus() {
        String logMessage = getStatusMessage();
        log.info(logMessage);
    }

    /**
     * @return Log message including both base stage and class specific stats.
     */
    public String getStatusMessage() {
        StringBuilder sb = new StringBuilder( 512 );
        NumberFormat formatter = floatFormatter.get();

        float serviceTime = ( totalServiceTime.floatValue() / 1000.0f );
        float emitTime = ( totalEmitTime.floatValue() / 1000.0f );
        float netServiceTime = ( serviceTime - emitTime );

        float emitPercentage = 0.0f;
        float emitFloat = totalEmits.floatValue();
        float recvFloat = objectsReceived.floatValue();
        if (recvFloat > 0) {
            emitPercentage = (emitFloat / recvFloat)*100.0f;
        }

        sb.append( "\n\t === " ).append( className ).append( " Generic Stats === " );

        if (statusBatchSize > 1) {
            sb.append("\n\tStatus Batch Size (all /obj and /sec include this): ").append(statusBatchSize);
        }

        sb.append( "\n\tTotal objects received:" ).append( objectsReceived );
        sb.append( "\n\tTotal unhandled exceptions:" ).append( unhandledExceptions );
        sb.append( "\n\tTotal objects emitted:" ).append( totalEmits );
        if (emitFloat > 0) {
            sb.append(" (").append(formatter.format(emitPercentage)).append("%)");
        }
        sb.append( "\n\tTotal gross processing time (sec):" )
          .append( formatter.format( serviceTime ) );
        sb.append( "\n\tTotal emit blocked time (sec):" )
          .append( formatter.format( emitTime ) );
        sb.append( "\n\tTotal net processing time (sec):" )
          .append( formatter.format( netServiceTime ) );

        float avgServiceTime = 0;
        float avgEmitTime = 0;
        float avgNetServiceTime = 0;
        if ( objectsReceived.longValue() > 0 ) {
            avgServiceTime = ( serviceTime / objectsReceived.floatValue()/statusBatchSize );
            avgEmitTime = ( emitTime / objectsReceived.floatValue()/statusBatchSize );
            avgNetServiceTime = ( netServiceTime / objectsReceived.floatValue()/statusBatchSize );
        }

        sb.append( "\n\tAverage gross processing time (sec/obj):" )
          .append( formatter.format( avgServiceTime ) );
        sb.append( "\n\tAverage emit blocked time (sec/obj):" )
          .append( formatter.format( avgEmitTime ) );
        sb.append( "\n\tAverage net processing time (sec/obj):" )
          .append( formatter.format( avgNetServiceTime ) );

        if (serviceTimeStatistics != null) {
            long count = serviceTimeStatistics.getN();
            if (count > 0) {
                double avgMillis = getCurrentServiceTimeAverage()/(float)statusBatchSize;
                sb.append( "\n\tAverage gross processing time in last ")
                  .append(count)
                  .append(" (sec/obj):" )
                  .append( formatter.format( avgMillis/1000 ) );
            }
        }

        float grossThroughput = 0;
        if ( avgServiceTime > 0 ) {
            grossThroughput = ( 1.0f / avgServiceTime );
        }
        float netThroughput = 0;
        if ( avgNetServiceTime > 0 ) {
            netThroughput = ( 1.0f / avgNetServiceTime );
        }

        sb.append( "\n\tGross throughput (obj/sec):" )
          .append( formatter.format( grossThroughput ) );
        sb.append( "\n\tNet throughput (obj/sec):" )
          .append( formatter.format( netThroughput ) );

        float percWorking = 0;
        float percBlocking = 0;
        if ( serviceTime > 0 ) {
            percWorking = ( netServiceTime / serviceTime ) * 100;
            percBlocking = ( emitTime / serviceTime ) * 100;
        }

        sb.append( "\n\t% time working:" ).append( formatter.format( percWorking ) );
        sb.append( "\n\t% time blocking:" ).append( formatter.format( percBlocking ) );

        // No need to output for a non-branching stage or if there was very little
        // blocking (as defined in the constant)
        if (collectBranchStats && emitTimeByBranch.size() > 1 && percBlocking >= BRANCH_BLOCK_THRESHOLD) {
            try {
                for (Map.Entry<String, AtomicLong> entry : emitTimeByBranch.entrySet()) {
                    float branchBlockSec = (entry.getValue().floatValue()/1000.0f);
                    float branchBlockPerc = (branchBlockSec/emitTime) * 100;
                    sb.append("\n\t\t% branch ").append(entry.getKey()).append(":").append(formatter.format(branchBlockPerc));
                }
            } catch (RuntimeException e) {
                // Synchronizing would be slow, ConcurrentMod is possible but unlikely since the map is
                // only modified the first time a stage is emitted to so just catch and
                // log it. No need to stop all processing over a reporting failure.
                sb.append("\n\t\tproblem getting per-branch stats: ").append(e.getMessage());
            }
        }

        String stageSpecificStatus = this.status();
        if ( stageSpecificStatus != null && stageSpecificStatus.length() > 0 ) {
            sb.append( "\n\t === " )
              .append( className )
              .append( " Specific Stats === " );
            sb.append( stageSpecificStatus );
        }

        return sb.toString();
    }

    protected String formatTotalTimeStat( String name, AtomicLong totalTime ) {
        return formatTotalTimeStat( name, totalTime.longValue() );
    }

    protected String formatTotalTimeStat( String name, long totalTime ) {
        if ( name == null || totalTime < 0 ) {
            return "";
        }
        NumberFormat formatter = floatFormatter.get();
        StringBuilder sb = new StringBuilder();
        // Total processing time minus calls to emit.
        float totalSec = totalTime / 1000.0f;
        float average = 0;
        if ( getObjectsReceived() > 0 ) {
            average = totalSec / getObjectsReceived() / (float)statusBatchSize;
        }

        if ( log.isDebugEnabled() ) {
            sb.append( "\n\tTotal " )
              .append( name )
              .append( " processing time (sec):" )
              .append( formatter.format( totalSec ) );
        }

        sb.append( "\n\tAverage " )
          .append( name )
          .append( " processing time (sec/obj):" )
          .append( formatter.format( average ) );

        if ( log.isDebugEnabled() && average > 0 ) {
            float throughput = ( 1.0f ) / average * (float)statusBatchSize;
            sb.append( "\n\tThroughput for " )
              .append( name )
              .append( " (obj/sec):" )
              .append( formatter.format( throughput ) );
        }

        return sb.toString();
    }

    protected String formatCounterStat( String name, AtomicInteger count ) {
        return formatCounterStat(name, count.get());
    }

    protected String formatCounterStat( String name, AtomicLong count ) {
        return formatCounterStat(name, count.get());
    }

    protected String formatCounterStat( String name, long count ) {
        if ( name == null || count < 0 || getObjectsReceived() <= 0) {
            return "";
        }
        NumberFormat formatter = floatFormatter.get();
        StringBuilder sb = new StringBuilder();

        float perc = ((float)count*(float)statusBatchSize/(float)getObjectsReceived())*100.0f;

        sb.append( "\n\tNumber of " )
          .append( name )
          .append( " (" )
          .append( formatter.format(perc) )
          .append( "%) :")
          .append( count );

        return sb.toString();
    }

    /**
     * @see org.apache.commons.pipeline.ExtendedBaseStageMBean#getStatusInterval()
     */
    public Long getStatusInterval() {
        return Long.valueOf(statusInterval);
    }

    /**
     * @see org.apache.commons.pipeline.ExtendedBaseStageMBean#setStatusInterval(long)
     */
    public void setStatusInterval( Long statusInterval ) {
        this.statusInterval = statusInterval;
    }

    public Integer getStatusBatchSize() {
        return statusBatchSize;
    }

    public void setStatusBatchSize(Integer statusBatchSize) {
        this.statusBatchSize = statusBatchSize;
    }

    /**
     * @see org.apache.commons.pipeline.ExtendedBaseStageMBean#getObjectsReceived()
     */
    public long getObjectsReceived() {
        return objectsReceived.longValue();
    }

    /**
     * @see org.apache.commons.pipeline.ExtendedBaseStageMBean#getTotalServiceTime()
     */
    public long getTotalServiceTime() {
        return totalServiceTime.longValue();
    }

    /**
     * @see org.apache.commons.pipeline.ExtendedBaseStageMBean#getTotalEmitTime()
     */
    public long getTotalEmitTime() {
        return totalEmitTime.longValue();
    }

    /**
     * @see org.apache.commons.pipeline.ExtendedBaseStageMBean#getTotalEmits()
     */
    public long getTotalEmits() {
        return totalEmits.longValue();
    }

    /**
     * @see org.apache.commons.pipeline.ExtendedBaseStageMBean#getCollectBranchStats()
     */
    public Boolean getCollectBranchStats() {
        return collectBranchStats;
    }

    /**
     * @see org.apache.commons.pipeline.ExtendedBaseStageMBean#setCollectBranchStats(Boolean)
     */
    public void setCollectBranchStats(Boolean collectBranchStats) {
        this.collectBranchStats = collectBranchStats;
    }

    public Integer getCurrentStatWindowSize() {
        return Integer.valueOf(currentStatWindowSize);
    }

    public void setCurrentStatWindowSize(Integer newStatWindowSize) {
        if (serviceTimeStatistics != null
                && newStatWindowSize != this.currentStatWindowSize) {
            synchronized (serviceTimeStatistics) {
                serviceTimeStatistics.setWindowSize(newStatWindowSize);
            }
        }
        this.currentStatWindowSize = newStatWindowSize;
    }

    public String getStageName() {
        return stageName;
    }

    public void setStageName(String name) {
        this.stageName = name;
    }

    public boolean isJmxEnabled() {
        return jmxEnabled;
    }

    public void setJmxEnabled(boolean jmxEnabled) {
        this.jmxEnabled = jmxEnabled;
    }

    /**
     * Returns a moving average of the service time. This does not yet take into account time spent
     * calling emit, nor does it return minimum, maximum or other statistical information at this time.
     *
     * @return Average time to process the last <code>currentStatWindowSize</code> objects.
     */
    public double getCurrentServiceTimeAverage() {
        double avg = -1.0d;

        // Hate to synchronize in the base class, but this should be very quick.
        avg = serviceTimeStatistics.getMean();

        return avg;
    }
}
