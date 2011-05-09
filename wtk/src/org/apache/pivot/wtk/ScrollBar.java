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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;

/**
 * Component that allows a user to select one of a range of values. Most often
 * used by scroll panes.
 */
public class ScrollBar extends Container {
    /**
     * Class representing a scroll bar's scope.
     */
    public static final class Scope {
        public final int start;
        public final int end;
        public final int extent;

        public static final String START_KEY = "start";
        public static final String END_KEY = "end";
        public static final String EXTENT_KEY = "extent";

        public Scope(int start, int end, int extent) {
            this.start = start;
            this.end = end;
            this.extent = extent;
        }

        public Scope(Dictionary<String, ?> scope) {
            if (scope == null) {
                throw new IllegalArgumentException("scope is null.");
            }

            if (!scope.containsKey(START_KEY)) {
                throw new IllegalArgumentException(START_KEY + " is required.");
            }

            if (!scope.containsKey(END_KEY)) {
                throw new IllegalArgumentException(END_KEY + " is required.");
            }

            if (!scope.containsKey(EXTENT_KEY)) {
                throw new IllegalArgumentException(EXTENT_KEY + " is required.");
            }

            start = (Integer)scope.get(START_KEY);
            end = (Integer)scope.get(END_KEY);
            extent = (Integer)scope.get(EXTENT_KEY);
        }

        @Override
        public String toString() {
            return ("{start: " + start + ", end: " + end + ", extent: " + extent + "}");
        }

        public static Scope decode(String value) {
            if (value == null) {
                throw new IllegalArgumentException();
            }

            Scope scope;
            try {
                scope = new Scope(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }

            return scope;
        }
    }

    private static class ScrollBarListenerList extends WTKListenerList<ScrollBarListener>
        implements ScrollBarListener {
        @Override
        public void orientationChanged(ScrollBar scrollBar,
            Orientation previousOrientation) {
            for (ScrollBarListener listener : this) {
                listener.orientationChanged(scrollBar, previousOrientation);
            }
        }

        @Override
        public void scopeChanged(ScrollBar scrollBar, int previousStart, int previousEnd,
            int previousExtent) {
            for (ScrollBarListener listener : this) {
                listener.scopeChanged(scrollBar, previousStart, previousEnd,
                    previousExtent);
            }
        }

        @Override
        public void unitIncrementChanged(ScrollBar scrollBar, int previousUnitIncrement) {
            for (ScrollBarListener listener : this) {
                listener.unitIncrementChanged(scrollBar, previousUnitIncrement);
            }
        }

        @Override
        public void blockIncrementChanged(ScrollBar scrollBar,
            int previousBlockIncrement) {
            for (ScrollBarListener listener : this) {
                listener.blockIncrementChanged(scrollBar, previousBlockIncrement);
            }
        }
    }

    private static class ScrollBarValueListenerList extends WTKListenerList<ScrollBarValueListener>
        implements ScrollBarValueListener {
        @Override
        public void valueChanged(ScrollBar scrollBar, int previousValue) {
            for (ScrollBarValueListener listener : this) {
                listener.valueChanged(scrollBar, previousValue);
            }
        }
    }

    private Orientation orientation;
    private int start = 0;
    private int end = 100;
    private int extent = 1;
    private int value = 0;
    private int unitIncrement = 1;
    private int blockIncrement = 1;

    private ScrollBarListenerList scrollBarListeners = new ScrollBarListenerList();
    private ScrollBarValueListenerList scrollBarValueListeners =
        new ScrollBarValueListenerList();

    public ScrollBar() {
        this(Orientation.HORIZONTAL);
    }

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

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        setScope(start, end, extent);
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        setScope(start, end, extent);
    }

    public Span getRange() {
        return new Span(start, end);
    }

    public void setRange(int start, int end) {
        setScope(start, end, extent);
    }

    public final void setRange(Span range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        setRange(range.start, range.end);
    }

    public final void setRange(Dictionary<String, ?> range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        setRange(new Span(range));
    }

    public final void setRange(String range) {
        if (range == null) {
            throw new IllegalArgumentException("range is null.");
        }

        setRange(Span.decode(range));
    }

    public int getExtent() {
        return extent;
    }

    public void setExtent(int extent) {
        setScope(start, end, extent);
    }

    public Scope getScope() {
        return new Scope(start, end, extent);
    }

    public void setScope(int start, int end, int extent) {
        int previousStart = this.start;
        int previousEnd = this.end;
        int previousExtent = this.extent;

        if (start != previousStart
            || end != previousEnd
            || extent != previousExtent) {
            if (start > value) {
                throw new IllegalArgumentException(String.format
                    ("start (%d) is greater than value (%d)", start, value));
            }

            if (extent < 0) {
                throw new IllegalArgumentException(String.format
                    ("extent (%d) is negative", extent));
            }

            if (end < value + extent) {
                throw new IllegalArgumentException(String.format
                    ("end (%d) is less than value (%d) + extent (%d)", end, value, extent));
            }

            this.start = start;
            this.end = end;
            this.extent = extent;

            scrollBarListeners.scopeChanged(this, previousStart, previousEnd,
                previousExtent);
        }
    }

    public final void setScope(Scope scope) {
        if (scope == null) {
            throw new IllegalArgumentException("scope is null.");
        }

        setScope(scope.start, scope.end, scope.extent);
    }

    public final void setScope(Dictionary<String, ?> scope) {
        if (scope == null) {
            throw new IllegalArgumentException("scope is null.");
        }

        setScope(new Scope(scope));
    }

    public final void setScope(String scope) {
        if (scope == null) {
            throw new IllegalArgumentException("scope is null.");
        }

        setScope(Scope.decode(scope));
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int previousValue = this.value;

        if (value != previousValue) {
            if (value < start) {
                throw new IllegalArgumentException(String.format
                    ("value (%d) is less than start (%d)", value, start));
            }

            if (value + extent > end) {
                throw new IllegalArgumentException(String.format
                    ("value (%d) + extent (%d) is greater than end (%d)", value, extent, end));
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
