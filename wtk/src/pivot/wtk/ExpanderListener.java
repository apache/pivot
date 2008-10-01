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
 * Expander listener list.
 *
 * @author tvolkert
 */
public interface ExpanderListener {
    /**
     * Called when an expander's title has changed.
     *
     * @param expander
     * @param previousTitle
     */
    public void titleChanged(Expander expander, String previousTitle);

    /**
     * Called when an expander's expanded state has changed.
     *
     * @param expander
     */
    public void expandedChanged(Expander expander);

    /**
     * Called when an expander's content component has changed.
     *
     * @param expander
     * @param previousContent
     */
    public void contentChanged(Expander expander, Component previousContent);
}
