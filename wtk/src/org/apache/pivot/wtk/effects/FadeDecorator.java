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
package org.apache.pivot.wtk.effects;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;

import org.apache.pivot.wtk.Component;

/**
 * Decorator that applies an opacity to a component.
 */
public class FadeDecorator implements Decorator {
    private float opacity;

    public FadeDecorator() {
        this(0.5f);
    }

    public FadeDecorator(float opacity) {
        this.opacity = opacity;
    }

    public float getOpacity() {
        return opacity;
    }

    public void setOpacity(float opacity) {
        if (opacity < 0f || opacity > 1f) {
            throw new IllegalArgumentException(
                "opacity must be a value between 0.0 and 1.0, inclusive.");
        }

        this.opacity = opacity;
    }

    @Override
    public Graphics2D prepare(Component component, Graphics2D graphics) {
        graphics.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
        return graphics;
    }

}
