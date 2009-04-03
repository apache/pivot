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

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.util.concurrent.Dispatcher;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskExecutionException;
import pivot.util.concurrent.TaskListener;

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
    /**
     * Task that loads a set of user preferences.
     *
     * @author gbrown
     */
    public static class LoadTask extends Task<Preferences> {
        private URL url;

        public LoadTask(URL url) {
            this(url, DEFAULT_DISPATCHER);
        }

        public LoadTask(URL url, Dispatcher dispatcher) {
            super(dispatcher);

            if (url == null) {
                throw new IllegalArgumentException();
            }

            this.url = url;
        }

        @Override
        public Preferences execute() throws TaskExecutionException {
            Preferences preferences = null;

            try {
                InputStream inputStream = null;

                try {
                    // TODO Load the preferences
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
            } catch(Exception exception) {
                throw new TaskExecutionException(exception);
            }

            return preferences;
        }
    }

    /**
     * Task that saves a set of user preferences.
     *
     * @author gbrown
     */
    public static class SaveTask extends Task<Void> {
        private URL url;
        private Preferences preferences;

        public SaveTask(Preferences preferences, URL url) {
            this(preferences, url, DEFAULT_DISPATCHER);
        }

        public SaveTask(Preferences preferences, URL url, Dispatcher dispatcher) {
            super(dispatcher);

            if (url == null) {
                throw new IllegalArgumentException();
            }

            this.url = url;
            this.preferences = preferences;
        }

        @Override
        public Void execute() throws TaskExecutionException {
            try {
                OutputStream outputStream = null;

                try {
                    // TODO Save the preferences
                } finally {
                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } catch(Exception exception) {
                throw new TaskExecutionException(exception);
            }

            return null;
        }
    }

    /**
     * Preferences listener list.
     *
     * @author gbrown
     */
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

    private HashMap<String, Object> preferencesMap;
    private PreferencesListenerList preferencesListeners = new PreferencesListenerList();

    private static Dispatcher DEFAULT_DISPATCHER = new Dispatcher();

    /**
     * Constructs an empty preferences object.
     */
    public Preferences() {
        this(null);
    }

    /**
     * Constructs a preferences object with a set of default values.
     *
     * @param defaults
     */
    public Preferences(Map<String, Object> defaults) {
        preferencesMap = new HashMap<String, Object>(defaults);
    }

    /**
     * Retrieves the preference value for the given key.
     *
     * @param key
     * The key whose value is to be returned.
     */
    public Object get(String key) {
        return JSONSerializer.get(preferencesMap, key);
    }

    /**
     * Sets the preference value of the given key, creating a new entry or
     * replacing the existing value.
     *
     * @param key
     * The key whose value is to be set.
     *
     * @param value
     * The value to be associated with the given key.
     */
    public Object put(String key, Object value) {
        boolean update = JSONSerializer.containsKey(preferencesMap, key);
        Object previousValue = JSONSerializer.put(preferencesMap, key, value);

        if (update) {
            preferencesListeners.valueUpdated(this, key, previousValue);
        }
        else {
            preferencesListeners.valueAdded(this, key);
        }

        return previousValue;
    }

    /**
     * Removes a key/value pair from the map.
     *
     * @param key
     * The key whose mapping is to be removed.
     *
     * @return
     * The value that was removed.
     */
    public Object remove(String key) {
        Object value = null;

        if (JSONSerializer.containsKey(preferencesMap, key)) {
            value = JSONSerializer.remove(preferencesMap, key);
            preferencesListeners.valueRemoved(this, key, value);
        }

        return value;
    }

    /**
     * Tests the existence of a preference value.
     *
     * @param key
     * The key of the value whose presence is to be tested.
     *
     * @return
     * <tt>true</tt> if the preference value exists; <tt>false</tt>, otherwise.
     */
    public boolean containsKey(String key) {
        return JSONSerializer.containsKey(preferencesMap, key);
    }

    /**
     * Tests the emptiness of the preferences collection.
     *
     * @return
     * <tt>true</tt> if the dictionary contains no keys; <tt>false</tt>,
     * otherwise.
     */
    public boolean isEmpty() {
        return preferencesMap.isEmpty();
    }

    /**
     * Returns an iterator on the preference keys.
     */
    public Iterator<String> iterator() {
        return new ImmutableIterator<String>(preferencesMap.iterator());
    }

    /**
     * Returns the preferences listener list.
     */
    public ListenerList<PreferencesListener> getPreferencesListeners() {
        return preferencesListeners;
    }

    /**
     * Loads a set of user preferences.
     *
     * @param url
     */
    public static Preferences load(URL url) {
        LoadTask loadTask = new LoadTask(url);

        Preferences preferences = null;
        try {
            preferences = loadTask.execute();
        } catch(TaskExecutionException exception) {
            throw new RuntimeException(exception);
        }

        return preferences;
    }

    /**
     * Loads a set of user preferences asynchronously.
     *
     * @param url
     * @param loadListener
     *
     * @return
     * The load task that was created.
     */
    public static Preferences.LoadTask load(URL url, TaskListener<Preferences> loadListener) {
        LoadTask loadTask = new LoadTask(url);
        loadTask.execute(loadListener);
        return loadTask;
    }

    /**
     * Saves a set of user preferences.
     *
     * @param preferences
     * @param url
     */
    public static void save(Preferences preferences, URL url) {
        SaveTask saveTask = new SaveTask(preferences, url);

        try {
            saveTask.execute();
        } catch(TaskExecutionException exception) {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Saves a set of user preferences asynchronously.
     *
     * @param preferences
     * @param url
     * @param saveListener
     *
     * @return
     * The save task that was created.
     */
    public static Preferences.SaveTask save(Preferences preferences, URL url,
        TaskListener<Void> saveListener) {
        SaveTask saveTask = new SaveTask(preferences, url);
        saveTask.execute(saveListener);
        return saveTask;
    }
}
