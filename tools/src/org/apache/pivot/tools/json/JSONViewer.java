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
package org.apache.pivot.tools.json;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence.Tree.Path;
import org.apache.pivot.io.FileList;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Clipboard;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.effects.OverlayDecorator;
import org.apache.pivot.wtkx.WTKXSerializer;

public class JSONViewer implements Application {
    private Window window = null;
    private TreeView treeView = null;
    private OverlayDecorator promptDecorator = new OverlayDecorator();

    public static final String APPLICATION_KEY = "application";
    public static final String WINDOW_TITLE = "JSON Viewer";

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        wtkxSerializer.put(APPLICATION_KEY, this);

        window = (Window)wtkxSerializer.readObject(this, "json_viewer.wtkx");
        treeView = (TreeView)wtkxSerializer.get("treeView");

        Label prompt = new Label("Drag or paste JSON here");
        prompt.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        prompt.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);
        promptDecorator.setOverlay(prompt);
        treeView.getDecorators().add(promptDecorator);

        window.setTitle(WINDOW_TITLE);
        window.open(display);
        window.requestFocus();
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public void paste() {
        Manifest clipboardContent = Clipboard.getContent();

        if (clipboardContent != null
            && clipboardContent.containsText()) {
            String json = null;
            JSONSerializer jsonSerializer = new JSONSerializer();
            try {
                json = clipboardContent.getText();
                setValue(jsonSerializer.readObject(new StringReader(json)));
            } catch (IOException exception) {
                Prompt.prompt(exception.getMessage(), window);
            } catch(SerializationException exception) {
                String message = "Serialization exception at line "
                    + jsonSerializer.getLineNumber() + ": "
                    + "\"" + exception.getMessage() + "\"";
                Prompt.prompt(message, window);
            }

            window.setTitle(WINDOW_TITLE);
        }
    }

    public DropAction drop(Manifest dragContent) {
        DropAction dropAction = null;

        try {
            FileList fileList = dragContent.getFileList();
            if (fileList.getLength() == 1) {
                File file = fileList.get(0);

                JSONSerializer jsonSerializer = new JSONSerializer();
                FileInputStream fileInputStream = null;
                try {
                    try {
                        fileInputStream = new FileInputStream(file);
                        setValue(jsonSerializer.readObject(fileInputStream));
                    } finally {
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                    }
                } catch (IOException exception) {
                    Prompt.prompt(exception.getMessage(), window);
                } catch (SerializationException exception) {
                    String message = "Serialization exception at line "
                        + jsonSerializer.getLineNumber() + " in " + file + ": "
                        + "\"" + exception.getMessage() + "\"";
                    Prompt.prompt(message, window);
                }

                window.setTitle(WINDOW_TITLE + " - " + file.getName());

                dropAction = DropAction.COPY;
            } else {
                Prompt.prompt("Multiple files not supported.", window);
            }
        } catch(IOException exception) {
            Prompt.prompt(exception.getMessage(), window);
        }

        return dropAction;
    }

    private void setValue(Object value) {
        assert (value instanceof Map<?, ?>
            || value instanceof List<?>);
        // Remove prompt decorator
        if (promptDecorator != null) {
            treeView.getDecorators().remove(promptDecorator);
            promptDecorator = null;
        }

        TreeBranch treeData = new TreeBranch();
        treeData.add(build(value));
        treeView.setTreeData(treeData);
        treeView.expandBranch(new Path(0));
    }

    @SuppressWarnings("unchecked")
    private static TreeNode build(Object value) {
        TreeNode treeNode;

        if (value instanceof Map<?, ?>) {
            TreeBranch treeBranch = new TreeBranch("{}");

            Map<String, Object> map = (Map<String, Object>)value;
            for (String key : map) {
                TreeNode valueNode = build(map.get(key));

                String text = valueNode.getText();
                if (text == null) {
                    valueNode.setText(key);
                } else {
                    valueNode.setText(key + " : " + text);
                }

                treeBranch.add(valueNode);
            }

            treeNode = treeBranch;
        } else if (value instanceof List<?>) {
            TreeBranch treeBranch = new TreeBranch("[]");

            List<Object> list = (List<Object>)value;
            for (int i = 0, n = list.getLength(); i < n; i++) {
                TreeNode itemNode = build(list.get(i));

                String text = itemNode.getText();
                if (text == null) {
                    itemNode.setText("[" + i + "]");
                } else {
                    itemNode.setText("[" + i + "] " + text);
                }

                treeBranch.add(itemNode);
            }

            treeNode = treeBranch;
        } else if (value instanceof String) {
            treeNode = new TreeNode("\"" + value.toString() + "\"");
        } else if (value instanceof Number) {
            treeNode = new TreeNode(value.toString());
        } else if (value instanceof Boolean) {
            treeNode = new TreeNode(value.toString());
        } else {
            treeNode = new TreeNode("null");
        }

        return treeNode;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(JSONViewer.class, args);
    }
}
