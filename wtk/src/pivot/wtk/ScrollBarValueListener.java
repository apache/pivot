/*
 * Copyright (c) 2008 VMware, Inc.
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
package pivot.wtk;

/**
 * Defines event listener methods that pertain to scroll bar values.
 * Developers register for such events by adding themselves to a scroll bar's
 * list of "scroll bar value listeners" (see
 * {@link ScrollBar#getScrollBarValueListeners()}).
 *
 * @author tvolkert
 */
public interface ScrollBarValueListener {
    /**
     * Called when a scroll bar's value has changed.
     *
     * @param scrollBar
     * @param previousValue
     */
    public void valueChanged(ScrollBar scrollBar, int previousValue);
}
