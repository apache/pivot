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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Expander;
import org.apache.pivot.wtk.ExpanderListener;

/**
 * Abstract base class for expander skins.
 */
public abstract class ExpanderSkin extends ContainerSkin
    implements ExpanderListener {
    @Override
    public void install(Component component) {
        super.install(component);

        Expander expander = (Expander)component;
        expander.getExpanderListeners().add(this);
    }

    // ExpanderListener methods

    @Override
    public void titleChanged(Expander expander, String previousTitle) {
        // No-op
    }

    @Override
    public void collapsibleChanged(Expander expander) {
        // No-op
    }

    @Override
    public Vote previewExpandedChange(Expander expander) {
        // No-op
        return Vote.APPROVE;
    }

    @Override
    public void expandedChangeVetoed(Expander expander, Vote reason) {
        // No-op
    }

    @Override
    public void expandedChanged(Expander expander) {
        // No-op
    }

    @Override
    public void contentChanged(Expander expander, Component previousContent) {
        // No-op
    }
}
