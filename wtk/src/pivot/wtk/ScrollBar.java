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

import pivot.util.ListenerList;

/**
 * Component that allows a user to select one of a range of values. Most often
 * used by scroll panes.
 *
 * @author gbrown
 */
public class ScrollBar extends Container {
    private static class ScrollBarListenerList extends ListenerList<ScrollBarListener>
        implements ScrollBarListener {
        public void orientationChanged(ScrollBar scrollBar,
            Orientation previousOrientation) {
            for (ScrollBarListener listener : this) {
                listener.orientationChanged(scrollBar, previousOrientation);
            }
        }

        public void scopeChanged(ScrollBar scrollBar, int previousRangeStart,
           int previousRangeEnd, int previousExtent) {
            for (ScrollBarListener listener : this) {
                listener.scopeChanged(scrollBar, previousRangeStart,
                    previousRangeEnd, previousExtent);
            }
        }

        public void unitIncrementChanged(ScrollBar scrollBar, int previousUnitIncrement) {
            for (ScrollBarListener listener : this) {
                listener.unitIncrementChanged(scrollBar, previousUnitIncrement);
            }
        }

        public void blockIncrementChanged(ScrollBar scrollBar,
            int previousBlockIncrement) {
            for (ScrollBarListener listener : this) {
                listener.blockIncrementChanged(scrollBar, previousBlockIncrement);
            }
        }
    }

    private static class ScrollBarValueListenerList extends ListenerList<ScrollBarValueListener>
        implements ScrollBarValueListener {
        public void valueChanged(ScrollBar scrollBar, int previousValue) {
            for (ScrollBarValueListener listener : this) {
                listener.valueChanged(scrollBar, previousValue);
            }
        }
    }

    private Orientation orientation;
    private int rangeStart = 0;
    private int rangeEnd = 100;
    private int extent = 100;
    private int value = 0;
    private int unitIncrement = 1;
    private int blockIncrement = 1;

    private ScrollBarListenerList scrollBarListeners = new ScrollBarListenerList();
    private ScrollBarValueListenerList scrollBarValueListeners =
        new ScrollBarValueListenerList();

    public ScrollBar(Orientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("orientation is null");
        }

        this.orientation = orientation;

        installSkin(ScrollBar.class);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        if (orientation == null) {
            throw new IllegalArgumentException("orientation is null");
        }

        Orientation previousOrientation = this.orientation;

        if (orientation != previousOrientation) {
            this.orientation = orientation;
            scrollBarListeners.orientationChanged(this, previousOrientation);
        }
    }

    public int getRangeStart() {
        return rangeStart;
    }

    public int getRangeEnd() {
        return rangeEnd;
    }

    public Span getRange() {
        return new Span(rangeStart, rangeEnd);
    }

    public int getExtent() {
        return extent;
    }

    public void setScope(int rangeStart, int rangeEnd, int extent) {
        int previousRangeStart = this.rangeStart;
        int previousRangeEnd = this.rangeEnd;
        int previousExtent = this.extent;

        if (rangeStart != previousRangeStart
            || rangeEnd != previousRangeEnd
            || extent != previousExtent) {
            if (rangeStart > value) {
                throw new IllegalArgumentException("rangeStart is greater than value");
            }

            if (extent < 0) {
                throw new IllegalArgumentException("extent is negative");
            }

            if (rangeEnd < value + extent) {
                throw new IllegalArgumentException("rangeEnd is less than value+extent");
            }

            this.rangeStart = rangeStart;
            this.rangeEnd = rangeEnd;
            this.extent = extent;

            scrollBarListeners.scopeChanged(this, previousRangeStart,
                previousRangeEnd, previousExtent);
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int previousValue = this.value;

        if (value != previousValue) {
            if (value < rangeStart) {
                throw new IllegalArgumentException("value is less than rangeStart");
            }

            if (value + extent > rangeEnd) {
                throw new IllegalArgumentException("value+extent is greater than rangeEnd");
            }

            this.value = value;

            scrollBarValueListeners.valueChanged(this, previousValue);
        }
    }

    public int getUnitIncrement() {
        return unitIncrement;
    }

    public void setUnitIncrement(int unitIncrement) {
        if (unitIncrement < 0) {
            throw new IllegalArgumentException("unitIncrement is negative");
        }

        int previousUnitIncrement = this.unitIncrement;

        if (unitIncrement != previousUnitIncrement) {
            this.unitIncrement = unitIncrement;
            scrollBarListeners.unitIncrementChanged(this, previousUnitIncrement);
        }
    }

    public int getBlockIncrement() {
        return blockIncrement;
    }

    public void setBlockIncrement(int blockIncrement) {
        if (blockIncrement < 0) {
            throw new IllegalArgumentException("blockIncrement is negative");
        }

        int previousBlockIncrement = this.blockIncrement;

        if (blockIncrement != previousBlockIncrement) {
            this.blockIncrement = blockIncrement;
            scrollBarListeners.blockIncrementChanged(this, previousBlockIncrement);
        }
    }

    public ListenerList<ScrollBarListener> getScrollBarListeners() {
        return scrollBarListeners;
    }

    public ListenerList<ScrollBarValueListener> getScrollBarValueListeners() {
        return scrollBarValueListeners;
    }
}
