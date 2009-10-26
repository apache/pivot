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
package org.apache.pivot.beans;

/**
 * Bean dictionary listener interface.
 */
public interface BeanDictionaryListener {
    /**
     * Bean dictionary listener adapter.
     */
    public static class Adapter implements BeanDictionaryListener {
        @Override
        public void beanChanged(BeanDictionary beanDictionary, Object previousBean) {
        }

        @Override
        public void propertyChanged(BeanDictionary beanDictionary, String propertyName) {
        }
    }

    /**
     * Called when a bean dictionary's bean has changed.
     *
     * @param beanDictionary
     * @param previousBean
     */
    public void beanChanged(BeanDictionary beanDictionary, Object previousBean);

    /**
     * Called when a property of the bean dictionary's bean has changed.
     *
     * @param beanDictionary
     * @param propertyName
     */
    public void propertyChanged(BeanDictionary beanDictionary, String propertyName);
}
