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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.junit.Test;

public class BindTest {
    @Test
    @SuppressWarnings("unchecked")
    public void testBind() throws IOException, SerializationException {
        JSONSerializer objectSerializer = new JSONSerializer();
        Object sampleObject = objectSerializer.readObject(getClass().getResourceAsStream("sample.json"));

        JSONSerializer beanSerializer = new JSONSerializer(SampleBean.class);
        SampleBean sampleBean = (SampleBean)beanSerializer.readObject(getClass().getResourceAsStream("sample.json"));

        assertEquals(sampleBean.getA(), JSON.get(sampleObject, "a"));
        assertEquals(sampleBean.getB(), JSON.get(sampleObject, "b"));
        assertEquals(sampleBean.getC(), JSON.get(sampleObject, "c"));
        assertEquals(sampleBean.getD(), JSON.get(sampleObject, "d"));
        assertEquals(sampleBean.getE(), JSON.get(sampleObject, "e"));
        assertEquals(sampleBean.getI().getA(), JSON.get(sampleObject, "i.a"));

        JSONSerializer listSerializer = new JSONSerializer(ArrayList.class);
        List<?> list = (List<?>)listSerializer.readObject(new StringReader("[1, 2, 3, 4, 5]"));
        assertEquals(list.get(0), 1);

        JSONSerializer mapSerializer = new JSONSerializer(HashMap.class);
        HashMap<String, ?> map = (HashMap<String, ?>)mapSerializer.readObject(new StringReader("{a:1, b:2, c:3}"));
        assertEquals(map.get("a"), 1);
    }
}
