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

/**
 * Interface to JMX enable the ExtendedBaseStage.
 * 
 * @author mzsanford
 */
public interface ExtendedBaseStageMBean {
	/**
	 * @return build the status message. This may have an effect on stage throughput.
	 */
	public abstract String getStatusMessage();

	/**
	 * @return number of records after which status messages are logged.
	 */
	public abstract Long getStatusInterval();

	/**
	 * @param statusInterval new status interval
	 */
	public abstract void setStatusInterval(Long statusInterval);

	/**
	 * @return Size of batches processes by this stage (used to adjust throughput statistics)
	 */
	public Integer getStatusBatchSize();

	/**
	 * @param statusBatchSize Size of batches processes by this stage (used to adjust throughput statistics)
	 */
	public void setStatusBatchSize(Integer statusBatchSize);
	
	/**
	 * @return number of objects received
	 */
	public abstract long getObjectsReceived();

	/**
	 * @return total number of milliseconds spent processing
	 */
	public abstract long getTotalServiceTime();

	/**
	 * @return total number of milliseconds spent blocked on downstream queues
	 */
	public abstract long getTotalEmitTime();

	/**
	 * @return total number of emits to downstream queues
	 */
	public abstract long getTotalEmits();

	/**
	 * @return true is this stage is collecting branch stats, false otherwise.
	 */
	public abstract Boolean getCollectBranchStats();

	/**
	 * Branch stats are disabled by default because they are slow. Turning this on
	 * can have a noticeable effect on stage throughput.
	 * 
	 * @param collectBranchStats true if this stage should start collecting branch stats,
	 *  false otherwise.
	 */
	public abstract void setCollectBranchStats(Boolean collectBranchStats);
	
	/**
	 * Get the current average service time. This works by looking only at the last
	 * X objects processed, where X is defined and reported by the getCurrentStatWindowSize
	 *  and  setCurrentStatWindowSize methods.
	 *
	 * @return average time to process in milliseconds
	 */
	public double getCurrentServiceTimeAverage();
	
	/**
	 * Get the size of the service time collection window
	 */
	public Integer getCurrentStatWindowSize();
	
	/**
	 * Set the size of the service time collection window
	 */
	public void setCurrentStatWindowSize(Integer newStatWindowSize);

}
