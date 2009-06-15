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
package org.apache.pivot.wtk.media.drawing;

import org.apache.pivot.util.ListenerList;
import org.apache.pivot.wtk.ApplicationContext;


/**
 * Shape representing the root of a shape hierarchy.
 *
 * @author gbrown
 */
public class Canvas extends Group {
    private class ValidateCallback implements Runnable {
        public void run() {
            validate();
            validateCallback = null;
        }
    }

    private class CanvasListenerList extends ListenerList<CanvasListener>
        implements CanvasListener {
        public void regionUpdated(Canvas canvas, int x, int y, int width, int height) {
            for (CanvasListener listener : this) {
                listener.regionUpdated(canvas, x, y, width, height);
            }
        }
    }

    private ValidateCallback validateCallback = null;

    @Override
    protected void setParent(Group parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setOrigin(int x, int y) {
        throw new UnsupportedOperationException();
    }

    @Override
    public TransformSequence getTransforms() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void invalidate() {
        if (validateCallback == null) {
            validateCallback = new ValidateCallback();
            ApplicationContext.queueCallback(validateCallback);
        }

        super.invalidate();
    }

    @Override
    protected void update(int x, int y, int width, int height) {
        super.update(x, y, width, height);
        canvasListeners.regionUpdated(this, x, y, width, height);
    }

    private CanvasListenerList canvasListeners = new CanvasListenerList();

    public ListenerList<CanvasListener> getCanvasListeners() {
        return canvasListeners;
    }
}
