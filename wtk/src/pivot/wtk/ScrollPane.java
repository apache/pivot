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

/**
 *
 * @author gbrown
 * @author tvolkert
 */
@ComponentInfo(icon="ScrollPane.png")
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

    /**
     * Component class representing the components that will get placed in the
     * corners of a <tt>ScrollPane</tt>. Skins will instantiate these
     * components as needed when unfilled corners are introduced by a row
     * header or column header.
     *
     * @author tvolkert
     */
    public static class Corner extends Component {
        public static enum Placement {
            TOP_LEFT,
            BOTTOM_LEFT,
            BOTTOM_RIGHT,
            TOP_RIGHT;
        }

        private Placement placement;

        public Corner(Placement placement) {
            if (placement == null) {
                throw new IllegalArgumentException("Placement is null.");
            }

            this.placement = placement;

            installSkin(Corner.class);
        }

        public Placement getPlacement() {
            return placement;
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

        installSkin(ScrollPane.class);
    }

    public ScrollBarPolicy getHorizontalScrollBarPolicy() {
        return horizontalScrollBarPolicy;
    }

    public void setHorizontalScrollBarPolicy(ScrollBarPolicy horizontalScrollBarPolicy) {
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

    public void setHorizontalScrollBarPolicy(String horizontalScrollBarPolicy) {
        if (horizontalScrollBarPolicy == null) {
            throw new IllegalArgumentException("horizontalScrollBarPolicy is null.");
        }

        setHorizontalScrollBarPolicy(ScrollBarPolicy.decode(horizontalScrollBarPolicy));
    }

    public ScrollBarPolicy getVerticalScrollBarPolicy() {
        return verticalScrollBarPolicy;
    }

    public void setVerticalScrollBarPolicy(ScrollBarPolicy verticalScrollBarPolicy) {
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

    public void setVerticalScrollBarPolicy(String verticalScrollBarPolicy) {
        if (verticalScrollBarPolicy == null) {
            throw new IllegalArgumentException("verticalScrollBarPolicy is null.");
        }

        setVerticalScrollBarPolicy(ScrollBarPolicy.decode(verticalScrollBarPolicy));
    }

    public Component getRowHeader() {
        return rowHeader;
    }

    public void setRowHeader(Component rowHeader) {
        Component previousRowHeader = this.rowHeader;

        if (rowHeader != previousRowHeader) {
            if (rowHeader != null) {
                if (rowHeader.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                int insertionIndex = 0;

                if (getView() != null) {
                    insertionIndex++;
                }

                // Add the component
                insert(rowHeader, insertionIndex);
            }

            // Set the component as the new row header component (note that we
            // set the new component before removing the old one so two
            // row header change events don't get fired)
            this.rowHeader = rowHeader;

            // Remove any previous rowHeader component
            if (previousRowHeader != null) {
                remove(previousRowHeader);
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
            if (columnHeader != null) {
                if (columnHeader.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                int insertionIndex = 0;

                if (getView() != null) {
                    insertionIndex++;
                }

                // Add the component
                insert(columnHeader, insertionIndex);
            }

            // Set the component as the new column header component (note that
            // we set the new component before removing the old one so two
            // column header change events don't get fired)
            this.columnHeader = columnHeader;

            // Remove any previous columnHeader component
            if (previousColumnHeader != null) {
                remove(previousColumnHeader);
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
            if (corner != null) {
                if (corner.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                int insertionIndex = 0;

                if (getView() != null) {
                    insertionIndex++;
                }

                if (rowHeader != null) {
                    insertionIndex++;
                }

                if (columnHeader != null) {
                    insertionIndex++;
                }

                // Add the component
                insert(corner, insertionIndex);
            }

            // Set the component as the new corner component (note that we
            // set the new component before removing the old one so two
            // corner change events don't get fired)
            this.corner = corner;

            // Remove any previous corner component
            if (previousCorner != null) {
                remove(previousCorner);
            }

            scrollPaneListeners.cornerChanged(this, previousCorner);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == rowHeader
                || component == columnHeader
                || component == corner) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<ScrollPaneListener> getScrollPaneListeners() {
        return scrollPaneListeners;
    }
}
