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
package org.apache.pivot.tutorials.menus;

import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.TextInputSelectionListener;

public class MenuBars extends Frame implements Bindable {
    @BXML
    private FileBrowserSheet fileBrowserSheet;
    @BXML
    private TabPane tabPane = null;

    private MenuHandler menuHandler = new MenuHandler() {
        TextInputContentListener textInputTextListener = new TextInputContentListener() {
            @Override
            public void textChanged(TextInput textInput) {
                updateActionState(textInput);
            }
        };

        TextInputSelectionListener textInputSelectionListener = new TextInputSelectionListener() {
            @Override
            public void selectionChanged(TextInput textInput, int previousSelectionStart,
                int previousSelectionLength) {
                updateActionState(textInput);
            }
        };

        @Override
        public void configureMenuBar(Component component, MenuBar menuBar) {
            if (component instanceof TextInput) {
                TextInput textInput = (TextInput) component;

                updateActionState(textInput);
                Action.getNamedActions().get("paste").setEnabled(true);

                textInput.getTextInputContentListeners().add(textInputTextListener);
                textInput.getTextInputSelectionListeners().add(textInputSelectionListener);
            } else {
                Action.getNamedActions().get("cut").setEnabled(false);
                Action.getNamedActions().get("copy").setEnabled(false);
                Action.getNamedActions().get("paste").setEnabled(false);
            }
        }

        @Override
        public void cleanupMenuBar(Component component, MenuBar menuBar) {
            if (component instanceof TextInput) {
                TextInput textInput = (TextInput) component;
                textInput.getTextInputContentListeners().remove(textInputTextListener);
                textInput.getTextInputSelectionListeners().remove(textInputSelectionListener);
            }
        }

        private void updateActionState(TextInput textInput) {
            Action.getNamedActions().get("cut").setEnabled(textInput.getSelectionLength() > 0);
            Action.getNamedActions().get("copy").setEnabled(textInput.getSelectionLength() > 0);
        }
    };

    public MenuBars() {
        Action.getNamedActions().put("fileNew", new Action() {
            @Override
            public void perform(Component source) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                bxmlSerializer.getNamespace().put("menuHandler", menuHandler);

                Component tab;
                try {
                    tab = new Border((Component) bxmlSerializer.readObject(MenuBars.class,
                        "document.bxml"));
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                tabPane.getTabs().add(tab);
                TabPane.setTabData(tab, "Document " + tabPane.getTabs().getLength());
                tabPane.setSelectedIndex(tabPane.getTabs().getLength() - 1);
            }
        });

        Action.getNamedActions().put("fileOpen", new Action() {
            @Override
            public void perform(Component source) {
                fileBrowserSheet.open(MenuBars.this);
            }
        });

        Action.getNamedActions().put("cut", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput) MenuBars.this.getFocusDescendant();
                textInput.cut();
            }
        });

        Action.getNamedActions().put("copy", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput) MenuBars.this.getFocusDescendant();
                textInput.copy();
            }
        });

        Action.getNamedActions().put("paste", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput) MenuBars.this.getFocusDescendant();
                textInput.paste();
            }
        });
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        // empty block
    }
}
