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
 * Component layout listener interface.
 *
 * @author gbrown
 */
public interface ComponentLayoutListener {
    /**
     * Called when a component's preferred size has changed.
     *
     * @param component
     * @param previousPreferredWidth
     * @param previousPreferredHeight
     */
    public void preferredSizeChanged(Component component,
        int previousPreferredWidth, int previousPreferredHeight);

    /**
     * Called when a component's displayable flag has changed.
     *
     * @param component
     */
    public void displayableChanged(Component component);
}
