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

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Sequence;

public class SampleBean2Sequence implements Sequence<SampleBean2> {
    private ArrayList<SampleBean2> items = new ArrayList<>();

    @Override
    public int add(SampleBean2 item) {
        return items.add(item);
    }

    @Override
    public void insert(SampleBean2 item, int index) {
        items.insert(item, index);
    }

    @Override
    public SampleBean2 update(int index, SampleBean2 item) {
        return items.update(index, item);
    }

    @Override
    public int remove(SampleBean2 item) {
        return items.remove(item);
    }

    @Override
    public Sequence<SampleBean2> remove(int index, int count) {
        return items.remove(index, count);
    }

    @Override
    public SampleBean2 get(int index) {
        return items.get(index);
    }

    @Override
    public int indexOf(SampleBean2 item) {
        return items.indexOf(item);
    }

    @Override
    public int getLength() {
        return items.getLength();
    }
}
