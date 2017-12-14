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
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Orientation;

/**
 * A viewport header that displays a ruler along either the top or left side.
 */
public class Ruler extends Component {

    private Orientation orientation;

    private RulerListener.Listeners rulerListeners = new RulerListener.Listeners();

    public Ruler() {
        this(Orientation.HORIZONTAL);
    }

    public Ruler(Orientation orientation) {
        setOrientation(orientation);

        installSkin(Ruler.class);
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

    public ListenerList<RulerListener> getRulerListeners() {
        return rulerListeners;
    }
}
