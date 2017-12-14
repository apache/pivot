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
 * Enumeration defining the possible border configurations for a Ruler.
 */
public enum Borders {
    /** No borders at all. */
    NONE,
    /** All four borders. */
    ALL,
    /** Only the left side. */
    LEFT,
    /** Only the right side. */
    RIGHT,
    /** Only the top. */
    TOP,
    /** Only the bottom. */
    BOTTOM,
    /** Both the left and right sides. */
    LEFT_RIGHT,
    /** Both the top and bottom. */
    TOP_BOTTOM,
    /** The left side and the top. */
    LEFT_TOP,
    /** The left side and the bottom. */
    LEFT_BOTTOM,
    /** The right side and the top. */
    RIGHT_TOP,
    /** The right side and the bottom. */
    RIGHT_BOTTOM,
    /** All sides except the right side. */
    NOT_RIGHT,
    /** All sides except the bottom. */
    NOT_BOTTOM,
    /** All sides except the left side. */
    NOT_LEFT,
    /** All sides except the top. */
    NOT_TOP
}
