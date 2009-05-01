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
package pivot.wtk.test;

import java.awt.Color;
import java.awt.Graphics2D;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.MovieView;
import pivot.wtk.Window;
import pivot.wtk.media.Drawing;
import pivot.wtk.media.Movie;
import pivot.wtk.media.drawing.Line;
import pivot.wtk.media.drawing.Shape;

public class MovieViewTest implements Application {
    private Window window;

    private Movie movie = new Movie() {
        private int angle = 6;
        private Drawing drawing = new Drawing();
            private Shape.Rotate rotateTransform = new Shape.Rotate(0, 320, 240);

        {
            setLooping(true);
            setFrameRate(1);

            drawing.setSize(640, 480);

            Line line = new Line();
            line.setX1(220);
            line.setY1(240);
            line.setX2(220);
            line.setY2(40);
            /*
            line.setOrigin(320, 240);
            line.setX1(0);
            line.setY1(0);
            line.setX2(0);
            line.setY2(-200);
            */

            line.setStroke(Color.BLACK);
            line.setStrokeThickness(5);
            line.getTransforms().add(rotateTransform);
            drawing.getCanvas().add(line);
        }

        public void setCurrentFrame(int currentFrame) {
            if (currentFrame == 0) {
                System.out.println(currentFrame);
            }
            rotateTransform.setAngle(currentFrame * angle);

            movieListeners.regionUpdated(this, 0, 0, getWidth(), getHeight());

            super.setCurrentFrame(currentFrame);
        }

        public int getWidth() {
            return drawing.getWidth();
        }

        public int getHeight() {
            return drawing.getHeight();
        }

        public void paint(Graphics2D graphics) {
            drawing.paint(graphics);
        }

        public int getTotalFrames() {
            return (360 / angle);
        }
    };

    public static void main(String[] args) {
        DesktopApplicationContext.main(MovieViewTest.class, args);
    }

    public void startup(Display display, Dictionary<String, String> properties) {
        window = new Window();
        window.setMaximized(true);
        window.setContent(new MovieView(movie));
        window.open(display);
        movie.play();
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
