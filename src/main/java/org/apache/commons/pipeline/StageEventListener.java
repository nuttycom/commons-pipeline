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

package org.apache.commons.pipeline;

import java.util.EventListener;
import java.util.EventObject;

/**
 * Listener interface for {@link StageEvent}s
 */
public interface StageEventListener extends EventListener {
    
    /**
     * Notify this listener of a {@link StageEvent}
     */
    public abstract void notify(EventObject ev);
}
