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
package org.apache.pivot.wtk.media;

import java.awt.Paint;

import org.apache.pivot.wtk.media.drawing.Canvas;


/**
 * Drawing listener interface.
 *
 */
public interface DrawingListener {
    /**
     * Called when a drawing's canvas has changed.
     *
     * @param drawing
     * @param previousCanvas
     */
    public void canvasChanged(Drawing drawing, Canvas previousCanvas);

    /**
     * Called when a drawing's background has changed.
     *
     * @param drawing
     * @param previousBackground
     */
    public void backgroundChanged(Drawing drawing, Paint previousBackground);
}
