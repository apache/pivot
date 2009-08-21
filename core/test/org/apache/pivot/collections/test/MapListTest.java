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
package org.apache.pivot.collections.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Iterator;

import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.MapList;
import org.apache.pivot.collections.MapListListener;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Map.Pair;
import org.junit.Before;
import org.junit.Test;

public class MapListTest {

    private TestMapListListener listener;

    @Before
    public void createListener() {
        listener = new TestMapListListener();
    }

    @Test
    public void basicTest() {
        MapList<String, Integer> mapList = new MapList<String, Integer>();
        assertMapList(mapList);

        mapList = new MapList<String, Integer>(new HashMap<String, Integer>());
        assertMapList(mapList);

        Map<String, Integer> source = mapList.getSource();
        mapList.getMapListListeners().add(listener);
        mapList.setSource(null);
        assertEquals(1, listener.calls);
        assertEquals(mapList, listener.mapList);
        assertEquals(source, listener.previousSource);
        assertMapList(mapList);

        TestComparator comparator = new TestComparator();
        mapList.setComparator(comparator);
        assertEquals(comparator, mapList.getComparator());
    }

    private void assertMapList(MapList<String, Integer> mapList) {
        assertNull(mapList.getComparator());
        assertNotNull(mapList.getListListeners());
        assertNotNull(mapList.getMapListListeners());
        assertEquals(0, mapList.getLength());

        Pair<String, Integer> pair = new Pair<String, Integer>("a", 1);

        assertEquals(-1, mapList.indexOf(pair));

        mapList.add(pair);
        assertEquals(1, mapList.getLength());

        Map<String, Integer> newSource = new HashMap<String, Integer>();
        newSource.put("b", 2);
        newSource.put("c", 3);
        mapList.setSource(newSource);

        assertEquals(newSource, mapList.getSource());
        assertEquals(2, mapList.getLength());

        int iteratorCount = 0;
        Iterator<Pair<String, Integer>> iter = mapList.iterator();
        while (iter.hasNext()) {
            pair = iter.next();
            if (mapList.indexOf(pair) != iteratorCount) {
                fail("Unexpected pair " + pair + " in iterator");
            }
            iteratorCount++;
        }
        assertEquals(2, iteratorCount);

        Pair<String, Integer> newCPair = new Pair<String, Integer>("c", 33);
        mapList.update(1, newCPair);
        assertEquals(newCPair, mapList.get(1));

        Pair<String, Integer> aaPair = new Pair<String, Integer>("aa", 11);
        mapList.insert(aaPair, 1);
        assertEquals(3, mapList.getLength());
        assertEquals(aaPair, mapList.get(1));
        assertEquals(newCPair, mapList.get(2));

        Sequence<Pair<String, Integer>> sequence = mapList.remove(0, 1);
        assertNotNull(sequence);
        pair = sequence.get(0);
        assertNotNull(pair);
        assertEquals("b", pair.key);
        assertEquals(2, (int)pair.value);

        assertEquals(2, mapList.getLength());

        newSource.put("aa", 77);
        pair = mapList.get(1);
        assertEquals(pair.key, "aa");
        assertEquals((int)pair.value, 77);

        newSource.remove("aa");
        assertEquals(1, mapList.getLength());

        mapList.clear();
        assertEquals(0, mapList.getLength());

        pair = new Pair<String, Integer>("d", 4);
        mapList.add(pair);
        assertEquals(1, mapList.getLength());
        assertEquals(pair, mapList.get(0));

        assertEquals(0, mapList.remove(pair));
        assertEquals(0, mapList.getLength());

        newSource.put("z", 24);
        assertEquals(1, mapList.getLength());

        newSource.put("z", 24);
        assertEquals(1, mapList.getLength());

        newSource.clear();
        assertEquals(0, mapList.getLength());
    }

    private static class TestMapListListener implements MapListListener<String, Integer> {
        private MapList<String, Integer> mapList;
        private Map<String, Integer> previousSource;
        private int calls;

        @Override
        public void sourceChanged(MapList<String, Integer> mapList,
            Map<String, Integer> previousSource) {
            this.mapList = mapList;
            this.previousSource = previousSource;
            this.calls++;
        }
    }

    private static class TestComparator implements Comparator<Pair<String, Integer>>, Serializable {
        private static final long serialVersionUID = 0;

        @Override
        public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
            return o1.key.compareTo(o2.key);
        }
    }

}
