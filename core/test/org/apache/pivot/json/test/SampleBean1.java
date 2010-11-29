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
import org.apache.pivot.collections.HashMap;

public class SampleBean1 {
    private int a = 0;
    private String b = null;
    private boolean c = false;
    private ArrayList<String> d = null;
    private HashMap<String, Integer> e = null;
    private SampleBean1 i = null;
    private ArrayList<SampleBean2> k = null;

    public int getA() {
        return a;
    }

    public void setA(int a) {
        this.a = a;
    }

    public String getB() {
        return b;
    }

    public void setB(String b) {
        this.b = b;
    }

    public boolean getC() {
        return c;
    }

    public void setC(boolean c) {
        this.c = c;
    }

    public ArrayList<String> getD() {
        return d;
    }

    public void setD(ArrayList<String> d) {
        this.d = d;
    }

    public HashMap<String, Integer> getE() {
        return e;
    }

    public void setE(HashMap<String, Integer> e) {
        this.e = e;
    }

    public SampleBean1 getI() {
        return i;
    }

    public void setI(SampleBean1 i) {
        this.i = i;
    }

    public ArrayList<SampleBean2> getK() {
        return k;
    }

    public void setK(ArrayList<SampleBean2> k) {
        this.k = k;
    }
}
