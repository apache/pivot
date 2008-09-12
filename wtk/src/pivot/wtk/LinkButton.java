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

import pivot.wtk.content.LinkButtonDataRenderer;

/**
 * <p>Button component that resembles an HTML hyperlink.</p>
 *
 * @author gbrown
 */
@ComponentInfo(icon="LinkButton.png")
public class LinkButton extends Button {
    public LinkButton() {
        this(null);
    }

    public LinkButton(Object buttonData) {
        super(buttonData);

        setDataRenderer(new LinkButtonDataRenderer());
        installSkin(LinkButton.class);
    }

    @Override
    public void setToggleButton(boolean toggleButton) {
        throw new UnsupportedOperationException("Link buttons cannot be toggle buttons.");
    }
}
