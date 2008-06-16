/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.wtk;

import pivot.wtk.Keyboard;

public interface ButtonListener {
    public void buttonDataChanged(Button button, Object previousButtonData);
    public void dataRendererChanged(Button button, Button.DataRenderer previousDataRenderer);
    public void actionTriggerChanged(Button button, Keyboard.KeyStroke previousActionTrigger);
    public void toggleButtonChanged(Button button);
    public void triStateChanged(Button button);
    public void groupChanged(Button button, Button.Group previousGroup);
    public void selectedKeyChanged(Button button, String previousSelectedKey);
    public void stateKeyChanged(Button button, String previousStateKey);
}
