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

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Utils;

/**
 * A column or row heading component that display a line number or
 * character column ruler suitable for use with scrolling text controls
 * ({@link TextArea} or {@link TextPane}).
 */
public class NumberRuler extends Component {
    private Orientation orientation = Orientation.VERTICAL;
    /** Maximum number of digits expected in the numbering. */
    private int textSize = 5;

    private NumberRulerListener.Listeners rulerListeners = new NumberRulerListener.Listeners();

    public NumberRuler() {
        installSkin(NumberRuler.class);
    }

    public Orientation getOrientation() {
        return orientation;
    }

    public void setOrientation(Orientation orientation) {
        Utils.checkNull(orientation, "orientation");

        if (this.orientation != orientation) {
            this.orientation = orientation;
            rulerListeners.orientationChanged(this);
        }
    }

    public int getTextSize() {
        return textSize;
    }

    public void setTextSize(String size) {
        Utils.checkNullOrEmpty(size, "size");

        int newSize = Integer.parseInt(size);
        if (newSize <= 0 || newSize > 20) {
            throw new IllegalArgumentException("Text size must be positive and less or equal to 20.");
        }

        if (newSize != textSize) {
            int previousSize = this.textSize;
            this.textSize = newSize;
            rulerListeners.textSizeChanged(this, previousSize);
        }
    }

    public ListenerList<NumberRulerListener> getRulerListeners() {
        return rulerListeners;
    }

}
