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
 * Form attribute listener interface.
 */
public interface FormAttributeListener {
    /**
     * Form attribute listener adapter.
     */
    public class Adapter implements FormAttributeListener {
        @Override
        public void labelChanged(Form form, Component field, String previousLabel) {
            // empty block
        }

        @Override
        public void requiredChanged(Form form, Component field) {
            // empty block
        }

        @Override
        public void flagChanged(Form form, Component field, Form.Flag previousFlag) {
            // empty block
        }
    }

    /**
     * Called when a fields's label attribute has changed.
     *
     * @param form
     * @param field
     * @param previousLabel
     */
    public void labelChanged(Form form, Component field, String previousLabel);

    /**
     * Called when a fields's required attribute has changed.
     *
     * @param form
     * @param field
     */
    public void requiredChanged(Form form, Component field);

    /**
     * Called when a field's flag attribute has changed.
     *
     * @param form
     * @param field
     * @param previousFlag
     */
    public void flagChanged(Form form, Component field, Form.Flag previousFlag);
}
