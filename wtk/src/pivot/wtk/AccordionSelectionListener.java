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

import org.apache.pivot.util.Vote;

/**
 * Accordion selection listener interface.
 *
 * @author gbrown
 */
public interface AccordionSelectionListener {
    /**
     * Accordion selection listener adapter.
     *
     * @author tvolkert
     */
    public static class Adapter implements AccordionSelectionListener {
        public Vote previewSelectedIndexChange(Accordion accordion, int selectedIndex) {
            return Vote.APPROVE;
        }

        public void selectedIndexChangeVetoed(Accordion accordion, Vote reason) {
        }

        public void selectedIndexChanged(Accordion accordion, int previousSelectedIndex) {
        }
    }

    /**
     * Called to preview a selected index change.
     *
     * @param accordion
     * @param selectedIndex
     */
    public Vote previewSelectedIndexChange(Accordion accordion, int selectedIndex);

    /**
     * Called when a selected index change has been vetoed.
     *
     * @param accordion
     * @param reason
     */
    public void selectedIndexChangeVetoed(Accordion accordion, Vote reason);

    /**
     * Called when an accordion's selected index has changed.
     *
     * @param accordion
     * @param previousSelectedIndex
     */
    public void selectedIndexChanged(Accordion accordion, int previousSelectedIndex);
}
