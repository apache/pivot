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
package org.apache.pivot.demos.clock;

import java.awt.Graphics2D;
import java.util.Calendar;

import org.apache.pivot.beans.BeanSerializer;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.media.ImageListener;
import org.apache.pivot.wtk.media.Movie;
import org.apache.pivot.wtk.media.drawing.Shape;


/**
 * A concrete movie that animates a clock.
 */
public class Clock extends Movie {
    private Calendar calendar = Calendar.getInstance();
    private BeanSerializer beanSerializer;
    private Image image = null;

    public Clock() {
        beanSerializer = new BeanSerializer();
        try {
            image = (Image)beanSerializer.readObject(getClass().getResource("clock.bxml"));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

        image.getImageListeners().add(new ImageListener() {
            @Override
            public void sizeChanged(Image image, int previousWidth, int previousHeight) {
                movieListeners.sizeChanged(Clock.this, previousWidth, previousHeight);
            }

            @Override
            public void baselineChanged(Image image, int previousBaseline) {
                movieListeners.baselineChanged(Clock.this, previousBaseline);
            }

            @Override
            public void regionUpdated(Image image, int x, int y, int width, int height) {
                movieListeners.regionUpdated(Clock.this, x, y, width, height);
            }
        });

        setLooping(true);
        setFrameRate(1);
    }

    @Override
    public void setCurrentFrame(int currentFrame) {
        Shape.Rotate secondsRotation = (Shape.Rotate)beanSerializer.get("secondsRotation");
        Shape.Rotate minutesRotation = (Shape.Rotate)beanSerializer.get("minutesRotation");
        Shape.Rotate hoursRotation = (Shape.Rotate)beanSerializer.get("hoursRotation");

        calendar.setTimeInMillis(System.currentTimeMillis());

        int seconds = calendar.get(Calendar.SECOND);
        int minutes = calendar.get(Calendar.MINUTE);
        int hours = calendar.get(Calendar.HOUR);

        secondsRotation.setAngle(seconds * 6);
        minutesRotation.setAngle(minutes * 6 + seconds * (6d / 60));
        hoursRotation.setAngle(hours * 30 + minutes * (6d / 12));

        super.setCurrentFrame(currentFrame);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public int getBaseline() {
        return image.getBaseline();
    }

    @Override
    public void paint(Graphics2D graphics) {
        image.paint(graphics);
    }

    @Override
    public int getTotalFrames() {
        return 60;
    }
}
