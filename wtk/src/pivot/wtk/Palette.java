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
 * <p>Window representing a "tool palette".</p>
 *
 * @author gbrown
 */
public class Palette extends Window {
    /**
     * Creates a new palette.
     */
    public Palette() {
        this(null);
    }

    /**
     * Creates a new palette with an initial content component.
     *
     * @param content
     * The sheet's content component.
     */
    public Palette(Component content) {
        super(content);

        installSkin(Palette.class);
    }

    /**
     * @return
     * <tt>true</tt>; by default, palettes are auxilliary windows.
     */
    @Override
    public boolean isAuxilliary() {
        return true;
    }

    @Override
    public final void setOwner(Window owner) {
        if (owner == null) {
            throw new UnsupportedOperationException("A palette must have an owner.");
        }

        super.setOwner(owner);
    }
}
