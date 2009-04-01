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
package pivot.wtk.test;

import pivot.collections.Dictionary;
import pivot.io.Folder;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.FlowPane;
import pivot.wtk.Frame;
import pivot.wtk.TextInput;
import pivot.wtk.TreeView;
import pivot.wtkx.WTKXSerializer;

public class FileBrowserTest implements Application {
    private Frame frame1 = null;
    private Frame frame2 = null;
    private Frame frame3 = null;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        frame1 = new Frame((Component)wtkxSerializer.readObject(getClass().getResource("file_browser_test.wtkx")));

        TreeView folderTreeView = (TreeView)wtkxSerializer.getObjectByName("folderTreeView");

        String pathname = "/";
        folderTreeView.setTreeData(new Folder(pathname));

        frame1.setTitle("File Browser Test");
        frame1.setPreferredSize(480, 640);
        frame1.open(display);

        FlowPane flowPane2 = new FlowPane();
        flowPane2.add(new TextInput());
        frame2 = new Frame(flowPane2);
        frame2.setTitle("Frame 2");
        frame2.setPreferredSize(240, 80);
        frame2.open(display);

        FlowPane flowPane3 = new FlowPane();
        flowPane3.add(new TextInput());
        frame3 = new Frame(flowPane3);
        frame3.setTitle("Frame 3");
        frame3.setPreferredSize(240, 80);
        frame3.open(display);
    }

    public boolean shutdown(boolean optional) {
        frame1.close();
        frame2.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
