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
package pivot.wtk.effects;

import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quadratic;

/**
 * Slide transition class.
 *
 * @author gbrown
 */
public class SlideTransition extends Transition {
    private Component component;
    private int x0;
    private int x1;
    private int y0;
    private int y1;
    private boolean reverse;

    private int x;
    private int y;

    private Easing easing = new Quadratic();
    private TranslationDecorator translationDecorator = new TranslationDecorator(true);

    public SlideTransition(Component component, int x0, int x1, int y0, int y1,
        boolean reverse, int duration, int rate) {
        super(duration, rate, false);
        this.component = component;
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
        this.reverse = reverse;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    @Override
    public void start(TransitionListener transitionListener) {
        component.getDecorators().add(translationDecorator);
        super.start(transitionListener);
    }

    @Override
    public void stop() {
        super.stop();
        component.getDecorators().remove(translationDecorator);
    }

    @Override
    protected void update() {
        int elapsedTime = getElapsedTime();
        int duration = getDuration();

        x = (int)(reverse ? easing.easeIn(elapsedTime, x0, x1 - x0, duration)
            : easing.easeOut(elapsedTime, x0, x1 - x0, duration));

        y = (int)(reverse ? easing.easeOut(elapsedTime, y0, y1 - y0, duration)
            : easing.easeOut(elapsedTime, y0, y1 - y0, duration));

        translationDecorator.setOffset(x, y);

        Container parent = component.getParent();
        if (parent != null) {
            parent.repaint(component.getDecoratedBounds());
        }
    }
}
