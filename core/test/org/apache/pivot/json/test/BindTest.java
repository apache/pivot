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

import org.apache.pivot.json.JSON;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.junit.Test;

public class BindTest {
    @Test
    public void testBind() throws IOException, SerializationException {
        JSONSerializer jsonSerializer = new JSONSerializer();
        Object sampleObject = jsonSerializer.readObject(getClass().getResourceAsStream("sample.json"));
        SampleBean sampleBean = JSON.bind(sampleObject, SampleBean.class);

        assertEquals(sampleBean.getA(), JSON.get(sampleObject, "a"));
        assertEquals(sampleBean.getB(), JSON.get(sampleObject, "b"));
        assertEquals(sampleBean.getC(), JSON.get(sampleObject, "c"));
        assertEquals(sampleBean.getD(), JSON.get(sampleObject, "d"));
        assertEquals(sampleBean.getE(), JSON.get(sampleObject, "e"));
        assertEquals(sampleBean.getI().getA(), JSON.get(sampleObject, "i.a"));
        assertEquals(sampleBean.getJ()[0], JSON.get(sampleObject, "j[0]"));
    }
}
