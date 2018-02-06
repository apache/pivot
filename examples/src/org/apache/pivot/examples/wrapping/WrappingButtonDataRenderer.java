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
package org.apache.pivot.examples.wrapping;

import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Style;

/**
 * A button data renderer that wraps the text.
 */
public class WrappingButtonDataRenderer extends Label implements Button.DataRenderer {
    public WrappingButtonDataRenderer() {
        getStyles().put(Style.wrapText, true);
    }

    @Override
    public void setSize(int width, int height) {
        super.setSize(width, height);
        validate();
    }

    @Override
    public void render(Object data, Button button, boolean highlighted) {
        setText(toString(data));
    }

    @Override
    public String toString(Object data) {
        return (data == null) ? null : data.toString();
    }
}
