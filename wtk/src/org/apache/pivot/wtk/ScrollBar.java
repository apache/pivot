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
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * Component that allows a user to select one of a range of values. Most often
 * used by scroll panes.
 */
public class ScrollBar extends Container {
    /**
     * Class representing a scroll bar's scope (that is, the start, end and extent values).
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
            Utils.checkNull(scope, "scope");

            if (!scope.containsKey(START_KEY)) {
                throw new IllegalArgumentException(START_KEY + " is required.");
            }

            if (!scope.containsKey(END_KEY)) {
                throw new IllegalArgumentException(END_KEY + " is required.");
            }

            if (!scope.containsKey(EXTENT_KEY)) {
                throw new IllegalArgumentException(EXTENT_KEY + " is required.");
            }

            start = scope.getInt(START_KEY);
            end = scope.getInt(END_KEY);
            extent = scope.getInt(EXTENT_KEY);
        }

        public Scope(Sequence<?> scope) {
            Utils.checkNull(scope, "scope");

            start = ((Number) scope.get(0)).intValue();
            end = ((Number) scope.get(1)).intValue();
            extent = ((Number) scope.get(2)).intValue();
        }

        @Override
        public String toString() {
            return ("{start: " + start + ", end: " + end + ", extent: " + extent + "}");
        }

        public static Scope decode(String value) {
            Utils.checkNullOrEmpty(value, "scope");

            Scope scope;
            if (value.startsWith("{")) {
                try {
                    scope = new Scope(JSONSerializer.parseMap(value));
                } catch (SerializationException exception) {
                    throw new IllegalArgumentException(exception);
                }
            } else if (value.startsWith("[")) {
                try {
                    scope = new Scope(JSONSerializer.parseList(value));
                } catch (SerializationException exception) {
                    throw new IllegalArgumentException(exception);
                }
            } else {
                String[] parts = value.split("\\s*[,;]\\s*");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Invalid format for ScrollBar.Scope: " + value);
                }
                try {
                    scope = new Scope(
                        Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                } catch (NumberFormatException ex) {
                    throw new IllegalArgumentException(ex);
                }
            }

            return scope;
        }
    }

    private Orientation orientation;
    private int start = 0;
    private int end = 100;
    private int extent = 1;
    private int value = 0;
    private int unitIncrement = 1;
    private int blockIncrement = 1;

    private ScrollBarListener.Listeners scrollBarListeners = new ScrollBarListener.Listeners();
    private ScrollBarValueListener.Listeners scrollBarValueListeners = new ScrollBarValueListener.Listeners();

    public ScrollBar() {
        this(Orientation.HORIZONTAL);
    }

    public ScrollBar(Orientation orientation) {
        Utils.checkNull(orientation, "orientation");

        this.orientation = orientation;

        installSkin(ScrollBar.class);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        Utils.checkNull(orientation, "orientation");

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
        Utils.checkNull(range, "range");

        setRange(range.start, range.end);
    }

    public final void setRange(Dictionary<String, ?> range) {
        setRange(new Span(range));
    }

    public final void setRange(Sequence<?> range) {
        setRange(new Span(range));
    }

    public final void setRange(String range) {
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

        if (start != previousStart || end != previousEnd || extent != previousExtent) {
            if (start > value) {
                throw new IllegalArgumentException(
                    "start (" + start + ") is greater than value (" + value + ")");
            }

            Utils.checkNonNegative(extent, "extent");

            if (end < value + extent) {
                throw new IllegalArgumentException(
                    "end (" + end + ") is less than value (" + value + ") + extent (" + extent + ")");
            }

            this.start = start;
            this.end = end;
            this.extent = extent;

            scrollBarListeners.scopeChanged(this, previousStart, previousEnd, previousExtent);
        }
    }

    public final void setScope(Scope scope) {
        Utils.checkNull(scope, "scope");

        setScope(scope.start, scope.end, scope.extent);
    }

    public final void setScope(Dictionary<String, ?> scope) {
        setScope(new Scope(scope));
    }

    public final void setScope(Sequence<?> scope) {
        setScope(new Scope(scope));
    }

    public final void setScope(String scope) {
        setScope(Scope.decode(scope));
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int previousValue = this.value;

        if (value != previousValue) {
            if (value < start) {
                throw new IllegalArgumentException(
                    "value (" + value + ") is less than start (" + start + ")");
            }

            if (value + extent > end) {
                throw new IllegalArgumentException(
                    "value (" + value + ") + extent (" + extent + ") is greater than end (" + end + ")");
            }

            this.value = value;

            scrollBarValueListeners.valueChanged(this, previousValue);
        }
    }

    public int getUnitIncrement() {
        return unitIncrement;
    }

    public void setUnitIncrement(int unitIncrement) {
        Utils.checkNonNegative(unitIncrement, "unitIncrement");

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
        Utils.checkNonNegative(blockIncrement, "blockIncrement");

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
