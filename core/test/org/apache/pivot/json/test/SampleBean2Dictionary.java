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
package org.apache.pivot.json.test;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;

public class SampleBean2Dictionary implements Dictionary<String, SampleBean2> {
    private HashMap<String, SampleBean2> values = new HashMap<>();

    @Override
    public SampleBean2 get(String key) {
        return values.get(key);
    }

    @Override
    public SampleBean2 put(String key, SampleBean2 value) {
        return values.put(key, value);
    }

    @Override
    public SampleBean2 remove(String key) {
        return values.remove(key);
    }

    @Override
    public boolean containsKey(String key) {
        return values.containsKey(key);
    }
}
