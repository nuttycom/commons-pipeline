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

package org.apache.commons.pipeline.driver.control;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.StageException;
import org.apache.commons.pipeline.testFramework.TestStage;


class CPUBoundTestStage extends TestStage {
    private Log log = LogFactory.getLog(CPUBoundTestStage.class);
    private long consume;
    
    public CPUBoundTestStage(int id, long consume) {
        super( id );
        this.consume = consume;
    }
    
    public void process(Object obj) throws StageException {
        super.process( obj );
        long startTime = System.currentTimeMillis();
        double val = PrioritizableStageDriverTestUtils.consumeNCubed( consume );
        log.debug( "CPU stage took " + (System.currentTimeMillis() - startTime) + " ms to produce value " + val);
    }
}
