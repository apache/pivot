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

import java.util.Iterator;

import pivot.collections.Dictionary;
import pivot.collections.HashMap;

/**
 * Utility class for introspecting a MIME type string.
 *
 * @author gbrown
 */
public class MIMEType implements Dictionary<String, String>, Iterable<String> {
    private String baseType;
    private HashMap<String, String> parameters = new HashMap<String, String>();

    public MIMEType(String baseType) {
        this.baseType = baseType;
    }

    /**
     * Returns the base type of this MIME type (i.e. the type string minus
     * parameter values).
     */
    public String getBaseType() {
        return baseType;
    }

    public String get(String key) {
        return parameters.get(key);
    }

    public String put(String key, String value) {
        return parameters.put(key, value);
    }

    public String remove(String key) {
        return parameters.remove(key);
    }

    public boolean containsKey(String key) {
        return parameters.containsKey(key);
    }

    public boolean isEmpty() {
        return parameters.isEmpty();
    }

    public Iterator<String> iterator() {
        return new ImmutableIterator<String>(parameters.iterator());
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(baseType);
        if (!parameters.isEmpty()) {
            for (String parameter : parameters) {
                stringBuilder.append("; " + parameter + "=" + parameters.get(parameter));
            }
        }

        return stringBuilder.toString();
    }

    public static MIMEType decode(String value) {
        if (value == null) {
            throw new IllegalArgumentException("value is null.");
        }

        MIMEType mimeType;

        int i = value.indexOf(";");
        if (i == -1) {
            mimeType = new MIMEType(value);
        } else {
            mimeType = new MIMEType(value.substring(0, i));

            int n = value.length();
            do {
                // Get the index of the assignment delimiter for
                // this parameter
                int j = value.indexOf("=", i);
                if (j == -1) {
                    throw new IllegalArgumentException("Parameter list is invalid.");
                }

                String parameterKey = value.substring(i + 1, j).trim();
                if (parameterKey.length() == 0) {
                    throw new IllegalArgumentException("Missing parameter name.");
                }

                // Get the index of the next parameter delimiter
                i = value.indexOf(";", j);
                if (i == -1) {
                    i = n;
                }

                String parameterValue = value.substring(j + 1, i).trim();
                if (parameterValue.length() == 0) {
                    throw new IllegalArgumentException("Missing parameter value.");
                }

                // Add the parameter to the dictionary
                mimeType.put(parameterKey, parameterValue);
            } while (i < n);
        }

        return mimeType;
    }
}
