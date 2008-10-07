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
 * Component representing a text heading.
 *
 * @author gbrown
 */
public class Heading extends Label {
    private static class HeadingListenerList extends ListenerList<HeadingListener>
        implements HeadingListener {
        public void levelChanged(Heading heading, int previousLevel) {
            for (HeadingListener listener : this) {
                listener.levelChanged(heading, previousLevel);
            }
        }
    }

    private int level;

    private HeadingListenerList headingListeners = new HeadingListenerList();

    public static final int MAXIMUM_LEVEL = 3;

    public Heading() {
        this(1);
    }

    public Heading(int level) {
        setLevel(level);

        installSkin(Heading.class);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level < 1
            || level > MAXIMUM_LEVEL) {
            throw new IllegalArgumentException("level must be between 1 and "
                + MAXIMUM_LEVEL + ", inclusive.");
        }

        int previousLevel = this.level;

        if (previousLevel != level) {
            this.level = level;
            headingListeners.levelChanged(this, previousLevel);
        }
    }

    public ListenerList<HeadingListener> getHeadingListeners() {
        return headingListeners;
    }
}
