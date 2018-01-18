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
package org.apache.pivot.demos.memorygame;

import java.util.Random;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

public class MemGame implements Application, ButtonPressListener {
    private static final String IMG_BASE_FOLDER = "/org/apache/pivot/demos/memorygame/img/";

    private static Random random = new Random();

    private BXMLSerializer bxmlSerializer;
    private String defaultImage = IMG_BASE_FOLDER + "default.gif";
    private boolean firstClick = true;
    private boolean right = true;
    private PushButton buttonOne;
    private PushButton buttonTwo;
    private Button clickedButtonOne;
    private Button clickedButtonTwo;
    private int count;

    private Window window = null;
    private String[] images18;
    private String[] images36;
    private PushButton[] buttons;

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        bxmlSerializer = new BXMLSerializer();
        window = (Window) bxmlSerializer.readObject(getClass().getResource("memgame.bxml"));

        buttons = new PushButton[36];
        for (int aux = 0; aux < 36; aux++) {
            buttons[aux] = (PushButton) bxmlSerializer.getNamespace().get(String.valueOf(aux + 1));
            buttons[aux].getButtonPressListeners().add(this);
        }

        prepareGame();

        window.open(display);
        window.setWidth(420);
        window.setHeight(420);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }
        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(MemGame.class, args);
    }

    private void prepareGame() {
        prepareImagesArray();

        for (int aux = 0; aux < 36; aux++) {
            buttons[aux].setButtonData(new MemGameButtonData(defaultImage, images36[aux]));
            buttons[aux].setEnabled(true);
        }
    }

    private void prepareImagesArray() {
        int positionNumArray, x, y;
        this.images18 = new String[18];

        for (x = 0; x < 18; x++) {
            this.images18[x] = IMG_BASE_FOLDER + (x + 1) + ".gif";
        }

        this.images36 = new String[36];

        for (x = 0; x < 2; x++) {
            for (y = 0; y < 18; y++) {
                do {
                    positionNumArray = random.nextInt(36);
                } while (this.images36[positionNumArray] != null);
                this.images36[positionNumArray] = images18[y];
            }
        }
    }

    @Override
    public void buttonPressed(Button button) {
        if (firstClick) {
            if (!right) {
                buttonOne = (PushButton) clickedButtonOne;
                buttonTwo = (PushButton) clickedButtonTwo;

                ((MemGameButtonData) buttonOne.getButtonData()).setDefaultURL();
                ((MemGameButtonData) buttonTwo.getButtonData()).setDefaultURL();

                window.repaint();
            }

            clickedButtonOne = button;

            buttonOne = (PushButton) clickedButtonOne;
            ((MemGameButtonData) buttonOne.getButtonData()).setButtonURL();

            firstClick = !firstClick;
        } else {
            clickedButtonTwo = button;
            buttonTwo = (PushButton) clickedButtonTwo;

            if (clickedButtonTwo == clickedButtonOne) {
                right = false;
                Alert.alert(MessageType.WARNING, "Not permited action!", window);
            } else {
                ((MemGameButtonData) buttonTwo.getButtonData()).setButtonURL();

                if (((MemGameButtonData) buttonOne.getButtonData()).getButtonURL().equals(
                    ((MemGameButtonData) buttonTwo.getButtonData()).getButtonURL())) {
                    right = true;
                    ++count;
                    buttonOne.setEnabled(false);
                    buttonTwo.setEnabled(false);
                } else {
                    right = false;
                }

                firstClick = !firstClick;

                if (count == 18) {
                    prepareGame();
                    Alert.alert(MessageType.INFO, "Congratulations! You got a new challenge!",
                        window);
                    count = 0;
                }
            }
        }
    }

}
