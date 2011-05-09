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

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;

/**
 * A <tt>SplitPane</tt> is a container component that splits its size up into
 * two regions, each of which is capable of holding one component.  A split
 * pane may be setup to support either horizontal or veritcal splits.  The area
 * in between the two regions is known as the <i>splitter</i> and typically
 * allows the user to adjust the partitioning between the two regions.
 * <p>
 * Since <tt>SplitPane</tt>s only support a single splitter, multiple
 * <tt>SplitPane</tt>s may be nested to support more complex layouts.  In
 * that case, one split pane will "own" the other.  The implication of this
 * is noticed when a split pane directly contains a child split pane of the same
 * orientation.  The parent pane's separator will be able to travel past that
 * of it's child, but the child's separator will be unable to pass the parent's.
 */
public class SplitPane extends Container {
    /**
     * Enumeration defining split pane regions.
     */
    public enum Region {
        TOP_LEFT,
        BOTTOM_RIGHT
    }

    /**
     * Enumeration defining split pane resizing modes.
     */
    public enum ResizeMode {
        /**
         * When resizing, maintain the ratio between the regions.
         */
        SPLIT_RATIO,

        /**
         * When resizing, preserve the size of the primary region.
         */
        PRIMARY_REGION
    }

    private static class SplitPaneListenerList extends WTKListenerList<SplitPaneListener>
        implements SplitPaneListener {
        @Override
        public void topLeftChanged(SplitPane splitPane, Component previousTopLeft) {
            for (SplitPaneListener listener : this) {
                listener.topLeftChanged(splitPane, previousTopLeft);
            }
        }

        @Override
        public void bottomRightChanged(SplitPane splitPane, Component previousBottomRight) {
            for (SplitPaneListener listener : this) {
                listener.bottomRightChanged(splitPane, previousBottomRight);
            }
        }

        @Override
        public void orientationChanged(SplitPane splitPane) {
            for (SplitPaneListener listener : this) {
                listener.orientationChanged(splitPane);
            }
        }

        @Override
        public void primaryRegionChanged(SplitPane splitPane) {
            for (SplitPaneListener listener : this) {
                listener.primaryRegionChanged(splitPane);
            }
        }

        @Override
        public void splitRatioChanged(SplitPane splitPane, float previousSplitRatio) {
            for (SplitPaneListener listener : this) {
                listener.splitRatioChanged(splitPane, previousSplitRatio);
            }
        }

        @Override
        public void lockedChanged(SplitPane splitPane) {
            for (SplitPaneListener listener : this) {
                listener.lockedChanged(splitPane);
            }
        }

        @Override
        public void resizeModeChanged(SplitPane splitPane, ResizeMode previousResizeMode) {
            for (SplitPaneListener listener : this) {
                listener.resizeModeChanged(splitPane, previousResizeMode);
            }
        }
    }

    private Component topLeft = null;
    private Component bottomRight = null;
    private Orientation orientation = null;
    private Region primaryRegion = Region.TOP_LEFT;
    private ResizeMode resizeMode = ResizeMode.SPLIT_RATIO;
    private float splitRatio = 0.5f;
    private boolean locked = false;

    private SplitPaneListenerList splitPaneListeners = new SplitPaneListenerList();

    public SplitPane() {
        this(Orientation.HORIZONTAL);
    }

    public SplitPane(Orientation orientation) {
        this(orientation, null, null);
    }

    public SplitPane(Orientation orientation, Component topLeft, Component bottomRight) {
        this.orientation = orientation;

        installSkin(SplitPane.class);

        setTopLeft(topLeft);
        setBottomRight(bottomRight);
    }

    public Component getTopLeft() {
        return topLeft;
    }

    public void setTopLeft(Component topLeft) {
        Component previousTopLeft = this.topLeft;

        if (topLeft != previousTopLeft) {
            // Set the component as the new top/left component
            this.topLeft = topLeft;

            // Remove any previous content component
            if (previousTopLeft != null) {
                remove(previousTopLeft);
            }

            if (topLeft != null) {
                if (topLeft.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                // Add the component
                add(topLeft);
            }

            splitPaneListeners.topLeftChanged(this, previousTopLeft);
        }
    }

    public Component getBottomRight() {
        return bottomRight;
    }

    public void setBottomRight(Component bottomRight) {
        Component previousBottomRight = this.bottomRight;

        if (bottomRight != previousBottomRight) {
            // Set the component as the new bottom/right component
            this.bottomRight = bottomRight;

            // Remove any previous content component
            if (previousBottomRight != null) {
                remove(previousBottomRight);
            }

            if (bottomRight != null) {
                if (bottomRight.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                // Add the component
                add(bottomRight);
            }

            splitPaneListeners.bottomRightChanged(this, previousBottomRight);
        }
    }

    public Component getTop() {
        return (orientation == Orientation.HORIZONTAL) ? null : getTopLeft();
    }

    public void setTop(Component component) {
        setTopLeft(component);
    }

    public Component getBottom() {
        return (orientation == Orientation.HORIZONTAL) ? null : getBottomRight();
    }

    public void setBottom(Component component) {
        setBottomRight(component);
    }

    public Component getLeft() {
        return (orientation == Orientation.VERTICAL) ? null : getTopLeft();
    }

    public void setLeft(Component component) {
        setTopLeft(component);
    }

    public Component getRight() {
        return (orientation == Orientation.VERTICAL) ? null : getBottomRight();
    }

    public void setRight(Component component) {
        setBottomRight(component);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("orientation is null.");
        }

        if (this.orientation != orientation) {
            this.orientation = orientation;
            splitPaneListeners.orientationChanged(this);
        }
    }

    public Region getPrimaryRegion() {
        return primaryRegion;
    }

    public void setPrimaryRegion(Region primaryRegion) {
        if (primaryRegion == null) {
            throw new IllegalArgumentException("primaryRegion is null.");
        }

        if (this.primaryRegion != primaryRegion) {
            this.primaryRegion = primaryRegion;
            splitPaneListeners.primaryRegionChanged(this);
        }
    }

    public float getSplitRatio() {
        return splitRatio;
    }

    public void setSplitRatio(float splitRatio) {
        if (splitRatio < 0
            || splitRatio > 1) {
            throw new IllegalArgumentException("splitRatio must be between 0 and 1.");
        }

        float previousSplitRatio = this.splitRatio;

        if (previousSplitRatio != splitRatio) {
            this.splitRatio = splitRatio;
            splitPaneListeners.splitRatioChanged(this, previousSplitRatio);
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        if (this.locked != locked) {
            this.locked = locked;
            splitPaneListeners.lockedChanged(this);
        }
    }

    public ResizeMode getResizeMode() {
        return resizeMode;
    }

    public void setResizeMode(ResizeMode resizeMode) {
        if (resizeMode == null) {
            throw new IllegalArgumentException("resizeMode is null.");
        }

        ResizeMode previousResizeMode = this.resizeMode;

        if (previousResizeMode != resizeMode) {
            this.resizeMode = resizeMode;
            splitPaneListeners.resizeModeChanged(this, previousResizeMode);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        for (int i = index, n = index + count; i < n; i++) {
            Component component = get(i);
            if (component == topLeft
                || component == bottomRight) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.remove(index, count);
    }

    public ListenerList<SplitPaneListener> getSplitPaneListeners() {
        return splitPaneListeners;
    }
}
