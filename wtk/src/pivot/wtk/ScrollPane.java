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

import pivot.collections.Sequence;
import pivot.util.ListenerList;
import pivot.wtk.skin.ScrollPaneSkin;

/**
 *
 * @author gbrown
 * @author tvolkert
 */
public class ScrollPane extends Viewport {
    public enum ScrollBarPolicy {
        AUTO,
        NEVER,
        ALWAYS,
        FILL,
        FILL_TO_CAPACITY;

        public static ScrollBarPolicy decode(String value) {
            if (value == null) {
                throw new IllegalArgumentException("value is null.");
            }

            ScrollBarPolicy scrollBarPolicy = null;

            if (value.equalsIgnoreCase("auto")) {
                scrollBarPolicy = AUTO;
            } else if (value.equalsIgnoreCase("never")) {
                scrollBarPolicy = NEVER;
            } else if (value.equalsIgnoreCase("always")) {
                scrollBarPolicy = ALWAYS;
            } else if (value.equalsIgnoreCase("fill")) {
                scrollBarPolicy = FILL;
            } else if (value.equalsIgnoreCase("fillToCapacity")) {
                scrollBarPolicy = FILL_TO_CAPACITY;
            } else {
                throw new IllegalArgumentException("\"" + value
                    + "\" is not a valid scroll bar policy.");
            }

            return scrollBarPolicy;
        }
    }

    private class ScrollPaneListenerList extends ListenerList<ScrollPaneListener>
        implements ScrollPaneListener {

        public void horizontalScrollBarPolicyChanged(ScrollPane scrollPane,
            ScrollBarPolicy previousHorizontalScrollBarPolicy) {
            for (ScrollPaneListener listener : this) {
                listener.horizontalScrollBarPolicyChanged(scrollPane,
                    previousHorizontalScrollBarPolicy);
            }
        }

        public void verticalScrollBarPolicyChanged(ScrollPane scrollPane,
            ScrollBarPolicy previousVerticalScrollBarPolicy) {
            for (ScrollPaneListener listener : this) {
                listener.verticalScrollBarPolicyChanged(scrollPane,
                    previousVerticalScrollBarPolicy);
            }
        }

        public void rowHeaderChanged(ScrollPane scrollPane, Component previousRowHeader) {
            for (ScrollPaneListener listener : this) {
                listener.rowHeaderChanged(scrollPane, previousRowHeader);
            }
        }

        public void columnHeaderChanged(ScrollPane scrollPane,
            Component previousColumnHeader) {
            for (ScrollPaneListener listener : this) {
                listener.columnHeaderChanged(scrollPane, previousColumnHeader);
            }
        }

        public void cornerChanged(ScrollPane scrollPane, Component previousCorner) {
            for (ScrollPaneListener listener : this) {
                listener.cornerChanged(scrollPane, previousCorner);
            }
        }
    }

    private ScrollBarPolicy horizontalScrollBarPolicy;
    private ScrollBarPolicy verticalScrollBarPolicy;
    private Component rowHeader;
    private Component columnHeader;
    private Component corner;
    private ScrollPaneListenerList scrollPaneListeners = new ScrollPaneListenerList();

    public ScrollPane() {
        this(ScrollBarPolicy.AUTO, ScrollBarPolicy.AUTO);
    }

    public ScrollPane(ScrollBarPolicy horizontalScrollBarPolicy,
        ScrollBarPolicy verticalScrollBarPolicy) {
        super();

        if (horizontalScrollBarPolicy == null) {
            throw new IllegalArgumentException("horizontalScrollBarPolicy is null");
        }

        if (verticalScrollBarPolicy == null) {
            throw new IllegalArgumentException("verticalScrollBarPolicy is null");
        }

        this.horizontalScrollBarPolicy = horizontalScrollBarPolicy;
        this.verticalScrollBarPolicy = verticalScrollBarPolicy;

        if (getClass() == ScrollPane.class) {
            setSkinClass(ScrollPaneSkin.class);
        }
    }

    public ScrollBarPolicy getHorizontalPolicy() {
        return horizontalScrollBarPolicy;
    }

    public void setHorizontalPolicy(ScrollBarPolicy horizontalScrollBarPolicy) {
        if (horizontalScrollBarPolicy == null) {
            throw new IllegalArgumentException("horizontalScrollBarPolicy is null");
        }

        ScrollBarPolicy previousHorizontalScrollBarPolicy = this.horizontalScrollBarPolicy;

        if (horizontalScrollBarPolicy != previousHorizontalScrollBarPolicy) {
            this.horizontalScrollBarPolicy = horizontalScrollBarPolicy;
            scrollPaneListeners.horizontalScrollBarPolicyChanged(this,
                previousHorizontalScrollBarPolicy);
        }
    }

    public ScrollBarPolicy getVerticalPolicy() {
        return verticalScrollBarPolicy;
    }

    public void setVerticalPolicy(ScrollBarPolicy verticalScrollBarPolicy) {
        if (verticalScrollBarPolicy == null) {
            throw new IllegalArgumentException("verticalScrollBarPolicy is null");
        }

        ScrollBarPolicy previousVerticalScrollBarPolicy = this.verticalScrollBarPolicy;

        if (verticalScrollBarPolicy != previousVerticalScrollBarPolicy) {
            this.verticalScrollBarPolicy = verticalScrollBarPolicy;
            scrollPaneListeners.verticalScrollBarPolicyChanged(this,
                previousVerticalScrollBarPolicy);
        }
    }

    public Component getRowHeader() {
        return rowHeader;
    }

    public void setRowHeader(Component rowHeader) {
        Component previousRowHeader = this.rowHeader;

        if (rowHeader != previousRowHeader) {
            Container.ComponentSequence components = getComponents();

            if (rowHeader != null) {
                if (rowHeader.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                // Add the component
                components.add(rowHeader);
            }

            // Set the component as the new row header component (note that we
            // set the new component before removing the old one so two
            // row header change events don't get fired)
            this.rowHeader = rowHeader;

            // Remove any previous rowHeader component
            if (previousRowHeader != null) {
                components.remove(previousRowHeader);
            }

            scrollPaneListeners.rowHeaderChanged(this, previousRowHeader);
        }
    }

    public Component getColumnHeader() {
        return columnHeader;
    }

    public void setColumnHeader(Component columnHeader) {
        Component previousColumnHeader = this.columnHeader;

        if (columnHeader != previousColumnHeader) {
            Container.ComponentSequence components = getComponents();

            if (columnHeader != null) {
                if (columnHeader.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                // Add the component
                components.add(columnHeader);
            }

            // Set the component as the new column header component (note that
            // we set the new component before removing the old one so two
            // column header change events don't get fired)
            this.columnHeader = columnHeader;

            // Remove any previous columnHeader component
            if (previousColumnHeader != null) {
                components.remove(previousColumnHeader);
            }

            scrollPaneListeners.columnHeaderChanged(this, previousColumnHeader);
        }
    }

    public Component getCorner() {
        return corner;
    }

    public void setCorner(Component corner) {
        Component previousCorner = this.corner;

        if (corner != this.corner) {
            Container.ComponentSequence components = getComponents();

            if (corner != null) {
                if (corner.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                // Add the component
                components.add(corner);
            }

            // Set the component as the new corner component (note that we
            // set the new component before removing the old one so two
            // corner change events don't get fired)
            this.corner = corner;

            // Remove any previous corner component
            if (previousCorner != null) {
                components.remove(previousCorner);
            }

            scrollPaneListeners.cornerChanged(this, previousCorner);
        }
    }

    @Override
    protected Sequence<Component> removeComponents(int index, int count) {
        // Call the base method to remove the components
        Sequence<Component> removed = super.removeComponents(index, count);

        // Ensure that the appropriate instance variable is cleared if the
        // component being removed maps to the row header, etc.
        for (int i = 0, n = removed.getLength(); i < n; i++) {
            Component component = removed.get(i);

            if (component == rowHeader) {
                rowHeader = null;
                scrollPaneListeners.rowHeaderChanged(this, component);
            } else if (component == columnHeader) {
                columnHeader = null;
                scrollPaneListeners.columnHeaderChanged(this, component);
            } else if (component == corner) {
                corner = null;
                scrollPaneListeners.cornerChanged(this, component);
            }
        }

        return removed;
    }

    public ListenerList<ScrollPaneListener> getScrollPaneListeners() {
        return scrollPaneListeners;
    }
}
