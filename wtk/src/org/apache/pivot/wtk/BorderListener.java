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
 * Border listener interface.
 *
 *
 */
public interface BorderListener {
    /**
     * Border listener adapter.
     */
    public static class Adapter implements BorderListener {
        @Override
        public void titleChanged(Border border, String previousTitle) {
            // empty block
        }

        @Override
        public void contentChanged(Border border, Component previousContent) {
            // empty block
        }
    }

    /**
     * Called when a border's title has changed.
     *
     * @param border
     * @param previousTitle
     */
    public void titleChanged(Border border, String previousTitle);

    /**
     * Called when a border's content component has changed.
     *
     * @param border
     * @param previousContent
     */
    public void contentChanged(Border border, Component previousContent);
}
