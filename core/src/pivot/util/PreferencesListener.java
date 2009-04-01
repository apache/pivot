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
package pivot.util;

/**
 * Preferences listener interface.
 *
 * @author gbrown
 */
public interface PreferencesListener {
    /**
     * Called when a preference has been added.
     *
     * @param preferences
     * The source of the event.
     *
     * @param key
     * The key of the value that was added.
     */
    public void valueAdded(Preferences preferences, String key);

    /**
     * Called when a preference value has been updated.
     *
     * @param preferences
     * The source of the event.
     *
     * @param key
     * The key whose value was updated.
     *
     * @param previousValue
     * The value that was previously associated with the key.
     */
    public void valueUpdated(Preferences preferences, String key, Object previousValue);

    /**
     * Called when a preference value has been removed.
     *
     * @param preferences
     * The source of the event.
     *
     * @param key
     * The key of the value that was removed.
     *
     * @param value
     * The value that was removed.
     */
    public void valueRemoved(Preferences preferences, String key, Object value);
}
