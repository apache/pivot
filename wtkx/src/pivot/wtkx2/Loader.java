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
package pivot.wtkx2;

import java.net.MalformedURLException;
import java.net.URL;
import pivot.collections.Dictionary;
import pivot.collections.HashMap;
import pivot.wtkx.LoadException;

public class Loader {
    private Loader parent = null;
    private String namespace = null;

    private URL baseURL = null;

    private HashMap<String, Object> values = new HashMap<String, Object>();
    private HashMap<String, Loader> loaders = new HashMap<String, Loader>();

    public static final String URL_PREFIX = "@";
    public static final String RESOURCE_KEY_PREFIX = "%";
    public static final String ID_PREFIX = "#";

    public static final String ID_ATTRIBUTE = "wtkx:id";
    public static final String INCLUDE_TAG = "wtkx:include";

    public Loader() {
        this(null, null);
    }

    private Loader(Loader parent, String namespace) {
        this.parent = parent;
        this.namespace = namespace;
    }

    public Loader getParent() {
        return parent;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getQualifiedNamepsace() {
        String qualifiedNamespace = this.namespace;

        if (qualifiedNamespace != null) {
            Loader ancestor = parent;

            while (ancestor != null) {
                if (ancestor.namespace != null) {
                    qualifiedNamespace = ancestor.namespace + "." + qualifiedNamespace;
                }

                ancestor = ancestor.parent;
            }
        }

        return qualifiedNamespace;
    }

    public URL getResource(String name) {
        URL location = null;

        try {
            if (name.startsWith("/")) {
                ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                location = classLoader.getResource(name.substring(1));
            } else {
                location = new URL(baseURL, name);
            }
        } catch(MalformedURLException exception) {
            // No-op
        }

        return location;
    }

    public Object load(String resourceName)
        throws LoadException {
        return load(resourceName, null);
    }

    public Object load(String resourceName, Dictionary<String, ?> resources)
        throws LoadException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resourceURL = classLoader.getResource(resourceName);

        if (resourceURL == null) {
            throw new LoadException("Could not find resource named \""
                + resourceName + "\".");
        }

        return load(resourceURL, resources);
    }

    private Object load(URL resourceURL, Dictionary<String, ?> resources)
        throws LoadException {
        // TODO
        return null;
    }

    /**
     * Retrieves a named loader.
     *
     * @param name
     * The name of the loader, relative to this loader. The loader's name is
     * the concatentation of its parent namespaces with its namespace,
     * separated by periods (e.g. "foo.bar.baz").
     *
     * @return
     * The named loader, or <tt>null</tt> if a loader with the given name does
     * not exist.
     */
    public Loader getLoader(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Loader loader = this;
        String[] namespacePath = name.split("\\.");

        int i = 0;
        int n = namespacePath.length - 1;
        while (i < n
            && loader != null) {
            String namespace = namespacePath[i++];
            loader = loader.loaders.get(namespace);
        }

        return loader;
    }

    /**
     * Retrieves a named value.
     *
     * @param name
     * The name of the value, relative to this loader. The values's name is
     * the concatentation of its parent namespaces and its ID, separated by
     * periods (e.g. "foo.bar.baz").
     *
     * @return
     * The named value, or <tt>null</tt> if a value with the given name does
     * not exist.
     */
    public Object getValue(String name) {
        if (name == null) {
            throw new IllegalArgumentException("name is null.");
        }

        Object value = null;
        Loader loader = this;
        String[] namespacePath = name.split("\\.");

        int i = 0;
        int n = namespacePath.length - 1;
        while (i < n
            && loader != null) {
            String namespace = namespacePath[i++];
            loader = loader.loaders.get(namespace);
        }

        if (loader != null) {
            value = loader.values.get(namespacePath[i]);
        }

        return value;
    }
}
