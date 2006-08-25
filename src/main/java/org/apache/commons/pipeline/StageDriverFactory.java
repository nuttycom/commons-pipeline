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

/**
 * This interface represents a factory that is used by a {@link Pipeline} to create
 * a driver for a {@link Stage} when that stage is added to the pipeline. The factory
 * pattern is used here to ensure that each stage is run by a unique driver
 * instance.
 * @author kjn
 */
public interface StageDriverFactory {
    /**
     * This method is used to create a driver that will run the specified stage
     * in the specified context.
     * @param stage The stage to be run by the newly created driver.
     * @param context The context in which the stage will be run
     * @return The newly created driver
     */
    public StageDriver createStageDriver(Stage stage, StageContext context);
}
