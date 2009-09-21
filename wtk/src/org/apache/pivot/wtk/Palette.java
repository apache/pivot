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
 * Window representing a "tool palette". A tool palette is an auxiliary window
 * whose visibility is tied to its owner's active state.
 */
public class Palette extends Window {
    public Palette() {
        this(null, null);
    }

    public Palette(String title) {
        this(title, null);
    }

    public Palette(Component content) {
        this(null, content);
    }

    public Palette(String title, Component content) {
        super(content);

        setTitle(title);
        installSkin(Palette.class);
    }

    @Override
    public void open(Display display, Window owner) {
        if (owner == null) {
            throw new IllegalStateException("Palettes must have an owner.");
        }

        super.open(display, owner);
    }
}
