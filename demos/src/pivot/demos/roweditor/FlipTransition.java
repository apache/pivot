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
package pivot.demos.roweditor;

import pivot.wtk.CardPane;
import pivot.wtk.effects.ScaleDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;

/**
 *
 *
 * @author tvolkert
 */
public class FlipTransition extends Transition {
    private CardPane cardPane;
    private double beginTheta;
    private double endTheta;
    private double currentTheta;

    private ScaleDecorator scaleDecorator = new ScaleDecorator();

    public FlipTransition(int duration, CardPane cardPane, double beginTheta, double endTheta) {
        super(duration, 30, false);

        if (beginTheta < 0 || beginTheta > Math.PI) {
            throw new IllegalArgumentException("beginTheta must be between 0 and PI.");
        }

        if (endTheta < 0 || endTheta > Math.PI) {
            throw new IllegalArgumentException("endTheta must be between 0 and PI.");
        }

        this.cardPane = cardPane;
        this.beginTheta = beginTheta;
        this.endTheta = endTheta;
    }

    public void setBeginTheta(double beginTheta) {
        if (isRunning()) {
            throw new IllegalStateException("Transition is currently running.");
        }

        this.beginTheta = beginTheta;
    }

    public void setEndTheta(double endTheta) {
        if (isRunning()) {
            throw new IllegalStateException("Transition is currently running.");
        }

        this.endTheta = endTheta;
    }

    public double getCurrentTheta() {
        return currentTheta;
    }

    @Override
    public void start(TransitionListener transitionListener) {
        currentTheta = beginTheta;
        cardPane.getDecorators().add(scaleDecorator);

        super.start(transitionListener);
    }

    @Override
    public void stop() {
        cardPane.getDecorators().remove(scaleDecorator);

        super.stop();
    }

    @Override
    protected void update() {
        float percentComplete = getPercentComplete();

        if (percentComplete < 1f) {
            currentTheta = beginTheta + ((endTheta - beginTheta) * percentComplete);

            float scaleY = (float)Math.abs(Math.cos(currentTheta));
            scaleDecorator.setScaleY(Math.max(scaleY, 0.01f));

            cardPane.setSelectedIndex(currentTheta < Math.PI / 2 ? 0 : 1);
            cardPane.repaint();
        }
    }
}
