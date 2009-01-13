/*
 * Copyright (c) 2009 VMware, Inc.
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
package pivot.wtk.skin.text;

import pivot.wtk.Component;
import pivot.wtk.Dimensions;
import pivot.wtk.skin.TextAreaSkin;
import pivot.wtk.text.Node;

/**
 * Document view.
 *
 * @author gbrown
 */
public class DocumentView extends ElementView {
    private TextAreaSkin textAreaSkin;

    public DocumentView(TextAreaSkin textAreaSkin) {
        this.textAreaSkin = textAreaSkin;
    }

    @Override
    public void attach(Node node) {
        super.attach(node);

        // TODO?
    }

    @Override
    public void detach() {
        // TODO?

        super.detach();
    }

    public int getPreferredWidth(int height) {
        // TODO Auto-generated method stub
        return 0;
    }

    public int getPreferredHeight(int width) {
        // TODO Auto-generated method stub
        return 0;
    }

    public Dimensions getPreferredSize() {
        // TODO Auto-generated method stub
        return null;
    }

    public void layout() {
        // TODO
    }

    @Override
    public void invalidate() {
        super.invalidate();

        Component textArea = textAreaSkin.getComponent();
        textArea.invalidate();
    }

    @Override
    public NodeView breakAt(int x) {
        return this;
    }
}
