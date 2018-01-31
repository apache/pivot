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
 * Frame listener interface.
 */
public interface FrameListener {
    /**
     * Frame listeners.
     */
    public static class Listeners extends ListenerList<FrameListener> implements FrameListener {
        @Override
        public void menuBarChanged(Frame frame, MenuBar previousMenuBar) {
            forEach(listener -> listener.menuBarChanged(frame, previousMenuBar));
        }
    }

    /**
     * Called when a frame's menu bar has changed.
     *
     * @param frame           The frame that has changed.
     * @param previousMenuBar The previous menu bar for this frame.
     */
    public void menuBarChanged(Frame frame, MenuBar previousMenuBar);
}
