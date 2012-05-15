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
package org.apache.pivot.wtk;

/**
 * Scroll bar listener interface.
 */
public interface ScrollBarListener {
    /**
     * Scroll bar listener adapter.
     */
    public static class Adapter implements ScrollBarListener {
        @Override
        public void orientationChanged(ScrollBar scrollBar, Orientation previousOrientation) {
            // empty block
        }

        @Override
        public void scopeChanged(ScrollBar scrollBar, int previousStart, int previousEnd,
            int previousExtent) {
            // empty block
        }

        @Override
        public void unitIncrementChanged(ScrollBar scrollBar, int previousUnitIncrement) {
            // empty block
        }

        @Override
        public void blockIncrementChanged(ScrollBar scrollBar, int previousBlockIncrement) {
            // empty block
        }
    }

    /**
     * Called when a scroll bar's orientation has changed.
     *
     * @param scrollBar
     * @param previousOrientation
     */
    public void orientationChanged(ScrollBar scrollBar, Orientation previousOrientation);

    /**
     * Called when a scroll bar's scope has changed.
     *
     * @param scrollBar
     * @param previousStart
     * @param previousEnd
     * @param previousExtent
     */
    public void scopeChanged(ScrollBar scrollBar, int previousStart, int previousEnd,
        int previousExtent);

    /**
     * Called when a scroll bar's unit increment has changed.
     *
     * @param scrollBar
     * @param previousUnitIncrement
     */
    public void unitIncrementChanged(ScrollBar scrollBar, int previousUnitIncrement);

    /**
     * Called when a scroll bar's block increment has changed.
     *
     * @param scrollBar
     * @param previousBlockIncrement
     */
    public void blockIncrementChanged(ScrollBar scrollBar, int previousBlockIncrement);
}
