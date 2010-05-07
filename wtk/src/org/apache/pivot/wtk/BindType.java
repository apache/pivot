/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.wtk;

/**
 * Defines a bind type.
 */
public enum BindType {
    /**
     * The binding will only be processed for {@link Component#load(Object)}
     * operations.
     */
    LOAD,

    /**
     * The binding will only be processed for {@link Component#store(Object)}
     * operations.
     */
    STORE,

    /**
     * The binding will be process for both {@link Component#load(Object)}
     * and {@link Component#store(Object)} operations.
     */
    BOTH
}
