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
package org.apache.pivot.tutorials.bxmlexplorer;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.SheetCloseListener;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.TextInputSelectionListener;
import org.apache.pivot.wtk.Window;
import org.xml.sax.SAXException;

public class BXMLExplorerWindow extends Window implements Bindable {
    @BXML
    private FileBrowserSheet fileBrowserSheet;
    @BXML
    private TabPane tabPane = null;
    @BXML
    private PushButton closeButton = null;
    @BXML
    private ScrollPane paletteTreeViewScrollPane;
    @BXML
    private TabPane paletteTabPane;
    @BXML
    private SplitPane splitPane;
    @BXML
    private Menu.Section fileMenuSection;
    @BXML
    private Menu.Item fileNewMenuItem;

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

    public BXMLExplorerWindow() {
        Action.getNamedActions().put("fileNew", new Action() {
            @Override
            public void perform(Component source) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                bxmlSerializer.getNamespace().put("menuHandler", menuHandler);

                Component tab;
                try {
                    tab = (BXMLExplorerDocument) bxmlSerializer.readObject(
                        BXMLExplorerWindow.class, "bxml_explorer_document.bxml");
                } catch (IOException exception) {
                    throw new RuntimeException(exception);
                } catch (SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                tabPane.getTabs().add(tab);
                TabPane.setTabData(tab, "New File " + tabPane.getTabs().getLength());
                tabPane.setSelectedIndex(tabPane.getTabs().getLength() - 1);
                closeButton.setEnabled(true);
            }
        });

        Action.getNamedActions().put("fileOpen", new Action() {
            @Override
            public void perform(Component source) {
                fileBrowserSheet.open(BXMLExplorerWindow.this, new SheetCloseListener() {
                    @Override
                    public void sheetClosed(Sheet sheet) {
                        if (!sheet.getResult()) {
                            return;
                        }
                        File f = fileBrowserSheet.getSelectedFile();

                        // if we have already loaded the file, select the
                        // appropriate tab and return
                        int idx = 0;
                        for (Component comp : tabPane.getTabs()) {
                            if (f.equals(((BXMLExplorerDocument) comp).getLoadedFile())) {
                                tabPane.setSelectedIndex(idx);
                                return;
                            }
                            idx++;
                        }

                        BXMLSerializer bxmlSerializer = new BXMLSerializer();
                        bxmlSerializer.getNamespace().put("menuHandler", menuHandler);

                        Component tab;
                        try {
                            BXMLExplorerDocument explorerDoc = (BXMLExplorerDocument) bxmlSerializer.readObject(
                                BXMLExplorerWindow.class, "bxml_explorer_document.bxml");
                            explorerDoc.load(f);
                            tab = explorerDoc;
                        } catch (RuntimeException exception) {
                            exception.printStackTrace();
                            BXMLExplorer.displayLoadException(exception, BXMLExplorerWindow.this);
                            return;
                        } catch (IOException exception) {
                            exception.printStackTrace();
                            BXMLExplorer.displayLoadException(exception, BXMLExplorerWindow.this);
                            return;
                        } catch (SerializationException exception) {
                            exception.printStackTrace();
                            BXMLExplorer.displayLoadException(exception, BXMLExplorerWindow.this);
                            return;
                        } catch (ParserConfigurationException exception) {
                            exception.printStackTrace();
                            BXMLExplorer.displayLoadException(exception, BXMLExplorerWindow.this);
                            return;
                        } catch (SAXException exception) {
                            exception.printStackTrace();
                            BXMLExplorer.displayLoadException(exception, BXMLExplorerWindow.this);
                            return;
                        }

                        tabPane.getTabs().add(tab);
                        TabPane.setTabData(tab, f.getName());
                        tabPane.setSelectedIndex(tabPane.getTabs().getLength() - 1);
                        closeButton.setEnabled(true);
                    }
                });

            }
        });

        Action.getNamedActions().put("cut", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput) BXMLExplorerWindow.this.getFocusDescendant();
                textInput.cut();
            }
        });

        Action.getNamedActions().put("copy", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput) BXMLExplorerWindow.this.getFocusDescendant();
                textInput.copy();
            }
        });

        Action.getNamedActions().put("paste", new Action(false) {
            @Override
            public void perform(Component source) {
                TextInput textInput = (TextInput) BXMLExplorerWindow.this.getFocusDescendant();
                textInput.paste();
            }
        });

    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        // hide until we support editing and saving BXML files
        if (!BXMLExplorer.ENABLE_EDITING) {
            paletteTabPane.getTabs().remove(paletteTreeViewScrollPane);
            splitPane.setSplitRatio(0);
            fileMenuSection.remove(fileNewMenuItem);
        }

        fileBrowserSheet.setDisabledFileFilter(new Filter<File>() {
            @Override
            public boolean include(File item) {
                return !(item.isDirectory() || item.getName().endsWith(".bxml"));
            }
        });
        closeButton.setEnabled(false);
        closeButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                int x = tabPane.getSelectedIndex();
                tabPane.getTabs().remove(x, 1);
                if (tabPane.getTabs().getLength() > 0) {
                    x = Math.max(x - 1, 0);
                    tabPane.setSelectedIndex(x);
                }
                closeButton.setEnabled(tabPane.getTabs().getLength() > 0);
            }
        });
    }
}
