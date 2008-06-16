/*
 * Copyright (c) 2008 VMware, Inc.
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
package pivot.wtk;

import pivot.collections.Dictionary;

public interface Renderer extends Visual {
    public static class PropertyDictionary
        implements Dictionary<String, Object> {
        public Object get(String key) {
            return null;
        }

        public Object put(String key, Object value) {
            return null;
        }

        public Object remove(String key) {
            return null;
        }

        public boolean containsKey(String key) {
            return false;
        }

        public boolean isEmpty() {
            return true;
        }

        /**
         * Verifies that a property value is of the correct type.
         *
         * @param key
         * @param value
         * @param type
         * @param nullable
         *
         * @throws IllegalArgumentException
         * If the type of <tt>value</tt> does not match the given type.
         */
        protected static final void validatePropertyType(String key, Object value,
            Class<?> type, boolean nullable) {
            if (value == null) {
                if (!nullable) {
                    throw new IllegalArgumentException(key + " must not be null.");
                }
            }
            else {
                if (!type.isInstance(value)) {
                    throw new IllegalArgumentException(key + " must be an instance of " + type);
                }
            }
        }
    }

    public Component.StyleDictionary getStyles();
    public PropertyDictionary getProperties();
}
