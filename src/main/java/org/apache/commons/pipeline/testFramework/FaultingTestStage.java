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

package org.apache.commons.pipeline.testFramework;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pipeline.*;
import org.apache.commons.pipeline.validation.ConsumedTypes;
import org.apache.commons.pipeline.validation.ProducesConsumed;

/**
 * This stage will generate {@link StageException}s for every other object this
 * stage processes. By design, the even numbered objects will cause a <CODE>StageException</CODE>
 * to be thrown (counting the first object as 1).
 */
@ConsumedTypes(Object.class)
@ProducesConsumed
public class FaultingTestStage extends TestStage {
    private Log log = LogFactory.getLog(FaultingTestStage.class);
    private int counter = 0;
    
    public FaultingTestStage(int index) {
        super(index);
    }
    
    public void process(Object obj) throws StageException {
        if (++counter % 2 == 0) {
            log.error("Planned fault in stage " + this + ".");
            throw new StageException(this, "Planned fault in stage " + super.getIndex() + ".");
        }
        
        super.process(obj);
    }
}