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
package pivot.util;

import pivot.collections.Dictionary;

/**
 * Utility class for introspecting a MIME type string.
 *
 * @author gbrown
 */
public class MIMEType implements Dictionary<String, String> {
    private String type;

    public MIMEType(String type) {
        this.type = type;
    }

    /**
     * Returns the base type of this MIME type (i.e. the type string minus
     * parameter values).
     */
    public String getType() {
        return type;
    }

    public String get(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    public String put(String key, String value) {
        // TODO Auto-generated method stub
        return null;
    }

    public String remove(String key) {
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

    public static MIMEType decode(String value) {
        // TODO
        return null;
    }
}
