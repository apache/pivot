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

import java.io.Serializable;

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Utils;

/**
 * Class representing the insets of an object, also called "padding"
 * (or in some classes, "margin").
 */
public final class Insets implements Serializable {
    private static final long serialVersionUID = -8528862892185591370L;

    public final int top;
    public final int left;
    public final int bottom;
    public final int right;

    public static final String TOP_KEY = "top";
    public static final String LEFT_KEY = "left";
    public static final String BOTTOM_KEY = "bottom";
    public static final String RIGHT_KEY = "right";

    /**
     * Insets whose top, left, bottom, and right values are all zero.
     */
    public static final Insets NONE = new Insets(0);

    public Insets(int inset) {
        this.top = inset;
        this.left = inset;
        this.bottom = inset;
        this.right = inset;
    }

    public Insets(int top, int left, int bottom, int right) {
        this.top = top;
        this.left = left;
        this.bottom = bottom;
        this.right = right;
    }

    public Insets(Insets insets) {
        Utils.checkNull(insets, "padding/margin");

        this.top = insets.top;
        this.left = insets.left;
        this.bottom = insets.bottom;
        this.right = insets.right;
    }

    public Insets(Dictionary<String, ?> insets) {
        Utils.checkNull(insets, "padding/margin");

        top = insets.getIntValue(TOP_KEY);
        left = insets.getIntValue(LEFT_KEY);
        bottom = insets.getIntValue(BOTTOM_KEY);
        right = insets.getIntValue(RIGHT_KEY);

    }

    @Override
    public boolean equals(Object object) {
        boolean equals = false;

        if (object instanceof Insets) {
            Insets insets = (Insets) object;
            equals = (top == insets.top && left == insets.left &&
                      bottom == insets.bottom && right == insets.right);
        }

        return equals;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + top;
        result = prime * result + left;
        result = prime * result + bottom;
        result = prime * result + right;
        return result;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [" + top + ", " + left + ", " + bottom + ", " + right + "]";
    }

    /**
     * Decode a possible Insets value, which can be in one of the
     * following forms:
     * <ul>
     * <li><pre>{ "top": nnn, "left": nnn, "bottom": nnn, "right": nnn }</pre>
     * <li><pre>[ top, left, bottom, right ]</pre>
     * <li>nnnn
     * </ul>
     *
     * @param value The string value of the Insets to decode.
     * @return The parsed <tt>Insets</tt> value.
     * @throws IllegalArgumentException if the input is not in one of these
     * formats.
     * @see #Insets(Dictionary)
     * @see #Insets(int, int, int, int)
     * @see #Insets(int)
     */
    public static Insets decode(String value) {
        Utils.checkNullOrEmpty(value, "padding/margin");

        Insets insets;
        if (value.startsWith("{")) {
            try {
                insets = new Insets(JSONSerializer.parseMap(value));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else if (value.startsWith("[")) {
            try {
                @SuppressWarnings("unchecked")
                List<Integer> values = (List<Integer>)JSONSerializer.parseList(value);
                insets = new Insets(values.get(0), values.get(1), values.get(2), values.get(3));
            } catch (SerializationException exception) {
                throw new IllegalArgumentException(exception);
            }
        } else {
            try {
                insets = new Insets(Integer.parseInt(value));
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(nfe);
            }
        }

        return insets;
    }

}
