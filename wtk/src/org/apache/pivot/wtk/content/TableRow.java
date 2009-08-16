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
package org.apache.pivot.wtk.content;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;

/**
 * Default table row implementation.
 */
public class TableRow implements Dictionary<String, Object> {
    private HashMap<String, Object> cells = new HashMap<String, Object>();

    public Object get(String key) {
        return cells.get(key);
    }

    public Object put(String key, Object value) {
        return cells.put(key, value);
    }

    public Object remove(String key) {
        return cells.remove(key);
    }

    public boolean containsKey(String key) {
        return cells.containsKey(key);
    }

    public boolean isEmpty() {
        return cells.isEmpty();
    }
}
