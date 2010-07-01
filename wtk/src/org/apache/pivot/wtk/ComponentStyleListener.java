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
 * Component style listener interface.
 */
public interface ComponentStyleListener {
    /**
     * Component style listener adapter.
     */
    public static class Adapter implements ComponentStyleListener {
        public void styleUpdated(Component component, String styleKey, Object previousValue) {
        }

        public void styleClassNameAdded(Component component, String styleClassName) {
        }

        public void styleClassNameRemoved(Component component, String styleClassName) {
        }
    }

    /**
     * Called when a component style has been updated.
     *
     * @param component
     * @param styleKey
     * @param previousValue
     */
    public void styleUpdated(Component component, String styleKey, Object previousValue);

    /**
     * Called when a style class name has been added.
     *
     * @param component
     * @param styleClassName
     */
    public void styleClassNameAdded(Component component, String styleClassName);

    /**
     * Called when a style class name has been removed.
     *
     * @param component
     * @param styleClassName
     */
    public void styleClassNameRemoved(Component component, String styleClassName);
}
