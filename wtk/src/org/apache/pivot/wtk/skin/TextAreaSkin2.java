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
package org.apache.pivot.wtk.skin;

import java.awt.Graphics2D;

import org.apache.pivot.wtk.Bounds;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.TextArea2;
import org.apache.pivot.wtk.TextAreaListener2;
import org.apache.pivot.wtk.TextAreaContentListener2;
import org.apache.pivot.wtk.TextAreaSelectionListener2;

/**
 * Text area skin.
 */
public class TextAreaSkin2 extends ComponentSkin implements TextArea2.Skin,
    TextAreaListener2, TextAreaContentListener2, TextAreaSelectionListener2 {
    @Override
    public void install(Component component) {
        super.install(component);

        // TODO
    }

    @Override
    public int getPreferredWidth(int height) {
        // TODO
        return 0;
    }

    @Override
    public int getPreferredHeight(int width) {
        // TODO
        return 0;
    }

    @Override
    public Dimensions getPreferredSize() {
        // TODO
        return null;
    }

    @Override
    public void layout() {
        // TODO
    }

    @Override
    public void paint(Graphics2D graphics) {
        // TODO
    }

    public int getInsertionPoint(int x, int y) {
        // TODO
        return -1;
    }

    public int getNextInsertionPoint(int x, int from, FocusTraversalDirection direction) {
        // TODO
        return -1;
    }

    public int getRowIndex(int offset) {
        // TODO
        return -1;
    }

    public int getRowCount() {
        // TODO
        return 0;
    }

    public Bounds getCharacterBounds(int offset) {
        // TODO
        return null;
    }

    @Override
    public void maximumLengthChanged(TextArea2 textArea, int previousMaximumLength) {
        // TODO
    }

    @Override
    public void editableChanged(TextArea2 textArea) {
        // TODO
    }

    @Override
    public void textInserted(TextArea2 textArea, int index, int count) {
        // TODO
    }

    @Override
    public void textRemoved(TextArea2 textArea, int index, int count) {
        // TODO
    }

    @Override
    public void textChanged(TextArea2 textArea) {
        // TODO
    }

    @Override
    public void selectionChanged(TextArea2 textArea, int previousSelectionStart,
        int previousSelectionLength) {
        // TODO
    }
}
