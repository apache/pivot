/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.test;

public class IterationTest {
    public static void main(String[] args) {
        long t0 = System.currentTimeMillis();

        final int COUNT = 100000000;

        int i = 0;
        while (i < COUNT) {
            i++;
        }

        long t1 = System.currentTimeMillis();

        System.out.println(Math.log10(COUNT) + " " + (t1 - t0) + "; " + Math.log10(COUNT) / Math.log10(2));
    }
}
