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

package org.apache.commons.pipeline.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.commons.pipeline.Stage;

/**
 * This marker annotation indicates that the {@link Stage} produces the same type
 * of object that is consumed (or any subtype thereof). This is used to allow
 * correct resolution of stage compatibility between different parts of a
 * pipeline when there are intervening generic pass-through stages such
 * as loggers, which can consume any type of object and produce the
 * same object unchanged.
 *
 * This annotation is used in lieu of the {@link Production} annotaion
 * and overrides it if both are present.
 *
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ProducesConsumed { }
