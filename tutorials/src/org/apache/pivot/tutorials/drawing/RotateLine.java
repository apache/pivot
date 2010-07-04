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
package org.apache.pivot.tutorials.drawing;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.media.drawing.Shape;

public class RotateLine extends Window implements Bindable {
    private Shape.Rotate rotation = null;

    @Override
    public void initialize(Dictionary<String, Object> namespace, URL location, Resources resources) {
        rotation = (Shape.Rotate)namespace.get("rotation");

        ApplicationContext.scheduleRecurringCallback(new Runnable() {
            @Override
            public void run() {
                int angle = (int)rotation.getAngle();
                angle = (angle + 6) % 360;
                rotation.setAngle(angle);
            }
        }, 1000);
    }
}
