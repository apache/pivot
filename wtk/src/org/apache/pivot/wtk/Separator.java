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

/**
 * Component representing a horizontal divider.
 */
public class Separator extends Component {
    private String heading = null;

    private SeparatorListener.Listeners separatorListeners = new SeparatorListener.Listeners();

    public Separator() {
        this(null);
    }

    public Separator(String heading) {
        setHeading(heading);
        installSkin(Separator.class);
    }

    /**
     * Returns the separator's heading.
     *
     * @return The separator's heading, or {@code null} if no heading is set.
     */
    public String getHeading() {
        return heading;
    }

    /**
     * Sets the separator's heading.
     *
     * @param heading The new heading, or {@code null} for no heading.
     */
    public void setHeading(String heading) {
        String previousHeading = this.heading;

        if (previousHeading != heading) {
            this.heading = heading;
            separatorListeners.headingChanged(this, previousHeading);
        }
    }

    public ListenerList<SeparatorListener> getSeparatorListeners() {
        return separatorListeners;
    }
}
