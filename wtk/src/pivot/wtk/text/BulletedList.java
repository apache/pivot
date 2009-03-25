/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.text;

/**
 * Element representing a bulleted list.
 *
 * @author gbrown
 */
public class BulletedList extends List {
    /**
     * List bullet styles.
     *
     * @author gbrown
     */
    public enum Style {
        CIRCLE,
        CIRCLE_OUTLINE,
        SQUARE,
        SQUARE_OUTLINE
    }

    private Style style = Style.CIRCLE;

    public BulletedList() {
        super();
    }

    public BulletedList(BulletedList bulletedList, boolean recursive) {
        super(bulletedList, recursive);
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
        return new BulletedList(this, recursive);
    }
}
