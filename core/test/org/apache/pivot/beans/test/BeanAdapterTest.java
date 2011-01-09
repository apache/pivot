package org.apache.pivot.beans.test;

import org.apache.pivot.beans.BeanAdapter;
import org.junit.Test;

import static org.junit.Assert.*;

public class BeanAdapterTest {
    @Test
    public void basicTest() {
        SampleBean sampleBean = new SampleBean();
        BeanAdapter beanAdapter = new BeanAdapter(sampleBean);

        assertEquals(sampleBean.getA(), beanAdapter.get("a"));
        assertEquals(sampleBean.getB(), beanAdapter.get("b"));
        assertTrue(beanAdapter.isReadOnly("b"));

        for (String property : beanAdapter.keySet()) {
            assertFalse(beanAdapter.isReadOnly(property));
        }

        // Test explicit setter
        beanAdapter.put("a", "100");
        assertEquals(beanAdapter.get("a"), -100);

        // Test type coercion
        beanAdapter.put("a", 10.9);
        assertEquals(beanAdapter.get("a"), 10);

        beanAdapter.put("testEnum", "abcDef");
        beanAdapter.put("testEnum", "ABC_DEF");
    }
}
