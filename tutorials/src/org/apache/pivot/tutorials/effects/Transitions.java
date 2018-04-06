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
package org.apache.pivot.tutorials.effects;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;

public class Transitions extends Window implements Bindable {
    private PushButton button1 = null;
    private PushButton button2 = null;
    private PushButton button3 = null;
    private PushButton button4 = null;

    private CollapseTransition collapseTransition = null;

    public static final int TRANSITION_DURATION = 250;
    public static final int TRANSITION_RATE = 30;

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        button1 = (PushButton) namespace.get("button1");
        button2 = (PushButton) namespace.get("button2");
        button3 = (PushButton) namespace.get("button3");
        button4 = (PushButton) namespace.get("button4");

        ButtonPressListener buttonPressListener = new ButtonPressListener() {
            @Override
            public void buttonPressed(final Button button) {
                if (collapseTransition == null) {
                    collapseTransition = new CollapseTransition(button, TRANSITION_DURATION,
                        TRANSITION_RATE);

                    TransitionListener transitionListener = new TransitionListener() {
                        @Override
                        public void transitionCompleted(Transition transition) {
                            CollapseTransition collapseTransitionLocal = (CollapseTransition) transition;

                            if (!transition.isReversed()) {
                                Component component = collapseTransitionLocal.getComponent();
                                component.getParent().remove(component);
                            }

                            Transitions.this.collapseTransition = null;
                        }
                    };

                    collapseTransition.start(transitionListener);
                } else {
                    collapseTransition.reverse();

                    if (collapseTransition.getComponent() != button) {
                        collapseTransition.end();
                    }
                }
            }
        };

        button1.getButtonPressListeners().add(buttonPressListener);
        button2.getButtonPressListeners().add(buttonPressListener);
        button3.getButtonPressListeners().add(buttonPressListener);
        button4.getButtonPressListeners().add(buttonPressListener);
    }
}
