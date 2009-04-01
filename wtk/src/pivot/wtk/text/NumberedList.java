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
package pivot.wtk.text;

/**
 * Element representing a numbered list.
 *
 * @author gbrown
 */
public class NumberedList extends List {
    /**
     * List numbering styles.
     *
     * @author gbrown
     */
    public enum Style {
        DECIMAL,
        LOWER_ALPHA,
        UPPER_ALPHA,
        LOWER_ROMAN,
        UPPER_ROMAN
    }

    private Style style = Style.DECIMAL;

    public NumberedList() {
        super();
    }

    public NumberedList(NumberedList numberedList, boolean recursive) {
        super(numberedList, recursive);
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Style style) {
        if (style == null) {
            throw new IllegalArgumentException("style is null.");
        }

        this.style = style;

        // TODO Fire event
    }

    @Override
    public Node duplicate(boolean recursive) {
        return new NumberedList(this, recursive);
    }
}
