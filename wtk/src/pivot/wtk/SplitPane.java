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
 *
 * @author tvolkert
 */
@ComponentInfo(icon="SplitPane.png")
public class SplitPane extends Container {
    public enum Region {
        TOP_LEFT,
        BOTTOM_RIGHT
    }

    private class SplitPaneListenerList extends ListenerList<SplitPaneListener>
        implements SplitPaneListener {
        public void topLeftComponentChanged(SplitPane splitPane, Component previousTopLeftComponent) {
            for (SplitPaneListener listener : this) {
                listener.topLeftComponentChanged(splitPane, previousTopLeftComponent);
            }
        }

        public void bottomRightComponentChanged(SplitPane splitPane, Component previousBottomRightComponent) {
            for (SplitPaneListener listener : this) {
                listener.bottomRightComponentChanged(splitPane, previousBottomRightComponent);
            }
        }

        public void orientationChanged(SplitPane splitPane) {
            for (SplitPaneListener listener : this) {
                listener.orientationChanged(splitPane);
            }
        }

        public void primaryRegionChanged(SplitPane splitPane) {
            for (SplitPaneListener listener : this) {
                listener.primaryRegionChanged(splitPane);
            }
        }

        public void splitLocationChanged(SplitPane splitPane, int previousSplitLocation) {
            for (SplitPaneListener listener : this) {
                listener.splitLocationChanged(splitPane, previousSplitLocation);
            }
        }

        public void splitBoundsChanged(SplitPane splitPane, Span previousSplitBounds) {
            for (SplitPaneListener listener : this) {
                listener.splitBoundsChanged(splitPane, previousSplitBounds);
            }
        }

        public void lockedChanged(SplitPane splitPane) {
            for (SplitPaneListener listener : this) {
                listener.lockedChanged(splitPane);
            }
        }
    }

    Component topLeftComponent = null;
    Component bottomRightComponent = null;
    private Orientation orientation = null;
    private Region primaryRegion = Region.TOP_LEFT;
    private int splitLocation = 0;
    private Span splitBounds = null;
    private boolean locked = false;

    private SplitPaneListenerList splitPaneListeners = new SplitPaneListenerList();

    // TODO Define a constructor that takes 2 components as arguments

    public SplitPane() {
        this(Orientation.HORIZONTAL);
    }

    public SplitPane(Orientation orientation) {
        this.orientation = orientation;

        installSkin(SplitPane.class);
    }

    public Component getTopLeftComponent() {
        return topLeftComponent;
    }

    public void setTopLeftComponent(Component topLeftComponent) {
        if (topLeftComponent != this.topLeftComponent) {
            Container.ComponentSequence components = getComponents();

            // Set the component as the new top/left component
            Component previousTopLeftComponent = this.topLeftComponent;
            this.topLeftComponent = topLeftComponent;

            // Remove any previous content component
            if (previousTopLeftComponent != null) {
                components.remove(previousTopLeftComponent);
            }

            if (topLeftComponent != null) {
                if (topLeftComponent.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                // Add the component
                components.add(topLeftComponent);
            }

            splitPaneListeners.topLeftComponentChanged(this, previousTopLeftComponent);
        }
    }

    public Component getBottomRightComponent() {
        return bottomRightComponent;
    }

    public void setBottomRightComponent(Component bottomRightComponent) {
        if (bottomRightComponent != this.bottomRightComponent) {
            Container.ComponentSequence components = getComponents();

            // Set the component as the new bottom/right component
            Component previousBottomRightComponent = this.bottomRightComponent;
            this.bottomRightComponent = bottomRightComponent;

            // Remove any previous content component
            if (previousBottomRightComponent != null) {
                components.remove(previousBottomRightComponent);
            }

            if (bottomRightComponent != null) {
                if (bottomRightComponent.getParent() != null) {
                    throw new IllegalArgumentException("Component already has a parent.");
                }

                // Add the component
                components.add(bottomRightComponent);
            }

            splitPaneListeners.bottomRightComponentChanged(this, previousBottomRightComponent);
        }
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (this.orientation != orientation) {
            this.orientation = orientation;

            splitPaneListeners.orientationChanged(this);
        }
    }

    public Region getPrimaryRegion() {
        return primaryRegion;
    }

    public void setPrimaryRegion(Region primaryRegion) {
        if (this.primaryRegion != primaryRegion) {
            this.primaryRegion = primaryRegion;

            splitPaneListeners.primaryRegionChanged(this);
        }
    }

    public int getSplitLocation() {
        return splitLocation;
    }

    public void setSplitLocation(int splitLocation) {
        int previousSplitLocation = this.splitLocation;

        if (previousSplitLocation != splitLocation) {
            this.splitLocation = splitLocation;
            splitPaneListeners.splitLocationChanged(this, previousSplitLocation);
        }
    }

    public Span getSplitBounds() {
        return splitBounds;
    }

    public void setSplitBounds(Span splitBounds) {
        // Check if this is a no-op.
        if (this.splitBounds == null) {
           if (splitBounds == null) {
              return;
           }
        } else {
           if (this.splitBounds.equals(splitBounds)) {
              return;
           }
        }

        int start = splitBounds.getStart();
        int end = splitBounds.getEnd();

        Span previousSplitBounds = this.splitBounds;
        this.splitBounds = new Span(start, end);

        // Reposition the splitter if necessary.
        if (splitBounds != null) {
           if (splitLocation < start) {
              setSplitLocation(start);
           } else if (splitLocation > end) {
              setSplitLocation(end);
           }
        }

        splitPaneListeners.splitBoundsChanged(this, previousSplitBounds);
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

    @Override
    protected Sequence<Component> removeComponents(int index, int count) {
        ComponentSequence components = getComponents();
        for (int i = index, n = index + count; i < n; i++) {
            Component component = components.get(i);
            if (component == topLeftComponent
                || component == bottomRightComponent) {
                throw new UnsupportedOperationException();
            }
        }

        // Call the base method to remove the components
        return super.removeComponents(index, count);
    }

    public ListenerList<SplitPaneListener> getSplitPaneListeners() {
        return splitPaneListeners;
    }
}
