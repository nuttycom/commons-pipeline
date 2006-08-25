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

package org.apache.commons.pipeline.testFramework;

import org.apache.commons.pipeline.*;
import org.apache.commons.pipeline.validation.ConsumedTypes;
import org.apache.commons.pipeline.validation.ProducesConsumed;

@ConsumedTypes(Object.class)
@ProducesConsumed
public class FaultingTestStage extends TestStage {
    private int counter = 0;
    
    public FaultingTestStage(int index) {
        super(index);
    }
    
    public void process(Object obj) throws StageException {
        if (++counter % 2 == 0) throw new StageException("Planned fault in stage " + super.getIndex() + ".");
        super.process(obj);
    }
}