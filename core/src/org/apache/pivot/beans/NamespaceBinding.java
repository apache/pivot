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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.MapListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSON;

/**
 * Represents a binding relationship between a source property and a target
 * property within a namespace.
 */
public class NamespaceBinding {
    private Map<String, Object> namespace;

    private String sourcePath;
    private Object source;
    private Map<String, Object> sourceMap;
    private BeanMonitor sourceMonitor;
    private String sourceKey;

    private String targetPath;
    private Object target;
    private Dictionary<String, Object> targetDictionary;
    private String targetKey;

    private MapListener<String, Object> sourceMapListener = new MapListener.Adapter<String, Object>() {
        @Override
        public void valueUpdated(Map<String, Object> map, String key, Object previousValue) {
            if (key.equals(sourceKey)) {
                targetDictionary.put(targetKey, sourceMap.get(sourceKey));
            }
        }
    };

    private PropertyChangeListener sourcePropertyChangeListener = new PropertyChangeListener() {
        @Override
        public void propertyChanged(Object bean, String propertyName) {
            if (propertyName.equals(sourceKey)) {
                targetDictionary.put(targetKey, sourceMap.get(sourceKey));
            }
        }
    };

    @SuppressWarnings("unchecked")
    public NamespaceBinding(Map<String, Object> namespace, String sourcePath, String targetPath) {
        if (namespace == null) {
            throw new IllegalArgumentException();
        }

        if (sourcePath == null) {
            throw new IllegalArgumentException();
        }

        if (targetPath == null) {
            throw new IllegalArgumentException();
        }

        // Set the namespace
        this.namespace = namespace;

        // Set the source properties
        this.sourcePath = sourcePath;

        Sequence<String> sourceKeys = JSON.parse(sourcePath);
        int sourceKeyCount = sourceKeys.getLength();

        source = JSON.get(namespace, sourceKeys, sourceKeyCount - 1);

        if (source instanceof Map<?, ?>) {
            sourceMap = (Map<String, Object>)source;
            sourceMonitor = null;
        } else {
            sourceMap = new BeanAdapter(source);
            sourceMonitor = new BeanMonitor(source);
        }

        sourceKey = sourceKeys.get(sourceKeyCount - 1);

        if (!sourceMap.containsKey(sourceKey)) {
            throw new IllegalArgumentException("Source property \"" + sourcePath
                + "\" does not exist.");
        }

        if (sourceMonitor != null
            && !sourceMonitor.isNotifying(sourceKey)) {
            throw new IllegalArgumentException("\"" + sourceKey + "\" is not a notifying property.");
        }

        // Set the target properties
        this.targetPath = targetPath;

        Sequence<String> targetKeys = JSON.parse(targetPath);
        int targetKeyCount = targetKeys.getLength();

        target = JSON.get(namespace, targetKeys, targetKeyCount - 1);

        if (target instanceof Dictionary<?, ?>) {
            targetDictionary = (Dictionary<String, Object>)target;
        } else {
            targetDictionary = new BeanAdapter(target);
        }

        targetKey = targetKeys.get(targetKeyCount - 1);

        if (!targetDictionary.containsKey(targetKey)) {
            throw new IllegalArgumentException("Target property \"" + targetPath
                + "\" does not exist.");
        }

        // Perform the initial set from source to target
        targetDictionary.put(targetKey, sourceMap.get(sourceKey));
    }

    /**
     * Returns the namespace.
     */
    public Map<String, Object> getNamespace() {
        return namespace;
    }

    /**
     * Returns the path to the source property.
     */
    public String getSourcePath() {
        return sourcePath;
    }

    /**
     * Returns the source object.
     */
    public Object getSource() {
        return source;
    }

    /**
     * Returns the name of the source property.
     */
    public String getSourceKey() {
        return sourceKey;
    }

    /**
     * Returns the path to the target property.
     */
    public String getTargetPath() {
        return targetPath;
    }

    /**
     * Returns the target object.
     */
    public Object getTarget() {
        return target;
    }

    /**
     * Returns the name of the target property.
     */
    public String getTargetKey() {
        return targetKey;
    }

    /**
     * Binds the source property to the target property.
     */
    public void bind() {
        if (source instanceof Map<?, ?>) {
            sourceMap.getMapListeners().add(sourceMapListener);
        } else {
            sourceMonitor.getPropertyChangeListeners().add(sourcePropertyChangeListener);
        }
    }

    /**
     * Unbinds the source property from the target property.
     */
    public void unbind() {
        if (source instanceof Map<?, ?>) {
            sourceMap.getMapListeners().remove(sourceMapListener);
        } else {
            sourceMonitor.getPropertyChangeListeners().remove(sourcePropertyChangeListener);
        }
    }

    @Override
    public boolean equals(Object o) {
        boolean equals = false;

        if (o instanceof NamespaceBinding) {
            NamespaceBinding namespaceBinding = (NamespaceBinding)o;

            equals = (source == namespaceBinding.source
                && sourceKey.equals(namespaceBinding.sourceKey)
                && target == namespaceBinding.target
                && targetKey.equals(namespaceBinding.targetKey));
        }

        return equals;
    }

    @Override
    public int hashCode() {
        return (source.hashCode() * sourceKey.hashCode()
            + target.hashCode() * targetKey.hashCode());
    }
}
