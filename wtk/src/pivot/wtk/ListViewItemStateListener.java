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
package pivot.wtk;

/**
 * List view item state listener interface.
 *
 * @author gbrown
 * @author tvolkert
 */
public interface ListViewItemStateListener {
    /**
     * List view item state listener adapter.
     *
     * @author tvolkert
     */
    public static class Adapter implements ListViewItemStateListener {
        public void itemDisabledChanged(ListView listView, int index) {
        }

        public void itemCheckedChanged(ListView listView, int index) {
        }
    }

    /**
     * Called when an item's disabled state has changed.
     *
     * @param listView
     * @param index
     */
    public void itemDisabledChanged(ListView listView, int index);

    /**
     * Called when an item's checked state has changed.
     *
     * @param listView
     * @param index
     */
    public void itemCheckedChanged(ListView listView, int index);
}
