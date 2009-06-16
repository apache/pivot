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
package org.apache.pivot.tutorials.transition;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtkx.WTKX;
import org.apache.pivot.wtkx.WTKXSerializer;

public class Transitions implements Application {
    private Window window = null;

    @WTKX private Button button1;
    @WTKX private Button button2;
    @WTKX private Button button3;
    @WTKX private Button button4;

    private CollapseTransition collapseTransition = null;

    public static int TRANSITION_DURATION = 2000;
    public static int TRANSITION_RATE = 30;

    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "transitions.wtkx");
        wtkxSerializer.bind(this, Transitions.class);

        ButtonPressListener buttonPressListener = new ButtonPressListener() {
            public void buttonPressed(final Button button) {
                if (collapseTransition == null) {
                    collapseTransition = new CollapseTransition(button, TRANSITION_DURATION, TRANSITION_RATE);

                    TransitionListener transitionListener = new TransitionListener() {
                        public void transitionCompleted(Transition transition) {
                            CollapseTransition collapseTransition = (CollapseTransition)transition;

                            if (!transition.isReversed()) {
                                Component component = collapseTransition.getComponent();
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

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(Transitions.class, args);
    }
}
