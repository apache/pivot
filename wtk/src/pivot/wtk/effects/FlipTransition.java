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

import pivot.wtk.CardPane;

/**
 * Transition that appears to "flip" a single card over. It does this by using
 * a <tt>CardPane</tt> with two cards in it; at the halfway point of the
 * transition, it changes the selected card to give the illusion that the
 * visual element has flipped over.
 * <p>
 * This class uses theta values from zero to <tt>&#960;</tt> to represent the
 * flip progress. Zero represents the first card being fully visible,
 * <tt>&#960;</tt> represents the second card being fully visible, and
 * <tt>&#960;/2</tt> represents the halfway point. This class allows the caller
 * to specify the begin and end theta to enable partial flips. Partial flips
 * are useful when reversing a flip that is currently in progress.
 * <p>
 * When in progress, this transition assumes that the card pane has at least
 * two cards and will only use the card pane's irst two cards. If the card
 * pane has less than two cards when the transition is in progress, a runtime
 * exception will be thrown.
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

    public double getBeginTheta() {
        return beginTheta;
    }

    public void setBeginTheta(double beginTheta) {
        if (isRunning()) {
            throw new IllegalStateException("Transition is currently running.");
        }

        this.beginTheta = beginTheta;
    }

    public double getEndTheta() {
        return endTheta;
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
