/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.util;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;

import pivot.collections.Dictionary;
import pivot.serialization.SerializationException;

/**
 * Provides access to application preference data. Preferences are modeled as
 * JSON data and are stored using the JNLP Persistence API. One top-level JSON
 * object exists per URL. The dictionary methods allow a caller to access and
 * modify this data.
 * <p>
 * Keys may be dot-delimited, allowing callers to get/set nested values.
 *
 * @author gbrown
 */
public class Preferences implements Dictionary<String, Object>, Iterable<String> {
    private static class PreferencesListenerList extends ListenerList<PreferencesListener>
        implements PreferencesListener {
        public void valueAdded(Preferences preferences, String key) {
            for (PreferencesListener listener : this) {
                listener.valueAdded(preferences, key);
            }
        }

        public void valueUpdated(Preferences preferences, String key, Object previousValue) {
            for (PreferencesListener listener : this) {
                listener.valueUpdated(preferences, key, previousValue);
            }
        }

        public void valueRemoved(Preferences preferences, String key, Object value) {
            for (PreferencesListener listener : this) {
                listener.valueRemoved(preferences, key, value);
            }
        }
    }

    private PreferencesListenerList preferencesListeners = new PreferencesListenerList();

    /**
     * Creates a preferences object that provides access to the preferences for
     * a given URL.
     *
     * @param url
     * @param defaults
     */
    public Preferences(URL url, Dictionary<String, Object> defaults)
        throws IOException, SerializationException {
        // TODO
    }

    public Object get(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object put(String key, Object value) {
        // TODO Auto-generated method stub
        return null;
    }

    public Object remove(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean containsKey(String key) {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    public void save() throws IOException, SerializationException {
        // TODO
    }

    public Iterator<String> iterator() {
        // TODO
        return null;
    }

    public ListenerList<PreferencesListener> getPreferencesListeners() {
        return preferencesListeners;
    }

    public void setPreferencesListener(PreferencesListener listener) {
        preferencesListeners.add(listener);
    }
}
