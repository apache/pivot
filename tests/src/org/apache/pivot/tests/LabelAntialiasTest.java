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
package org.apache.pivot.tests;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;

import org.apache.pivot.collections.Map;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;

public class LabelAntialiasTest implements Application {
    private Window window = null;

    private Label buildLabel(double rotation) {
        Label label = new Label();

        Font font = new Font("Arial", Font.BOLD, 64);

        AffineTransform fontAT = new AffineTransform();
        // Derive a new font using a rotation transform
        fontAT.rotate(rotation * java.lang.Math.PI / 180.0d);
        Font fontDerived = font.deriveFont(fontAT);

        label.setText("Hello at " + rotation + " degree.");
        label.getStyles().put("color", Color.RED);
        label.getStyles().put("font", fontDerived);
        label.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        label.getStyles().put("verticalAlignment", VerticalAlignment.TOP);

        return label;
    }

    /**
     * Write to console some details of Desktop Hints, for Font Rendering.
     *
     * @see org.apache.pivot.wtk.Platform#initializeFontRenderContext
     */
    private void showFontDesktopHints() {
        System.out.println("Show Font Desktop Hints:");

        Toolkit toolkit = Toolkit.getDefaultToolkit();
        java.util.Map<?, ?> fontDesktopHints = (java.util.Map<?, ?>) toolkit.getDesktopProperty("awt.font.desktophints");

        System.out.println(fontDesktopHints);
    }

    /**
     * Write to console the list of Font families found in the System.
     */
    private void showFontFamilies() {
        System.out.println("Show Font Families:");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        String[] fontFamilies = ge.getAvailableFontFamilyNames();
        int fontFamiliesNumber = fontFamilies.length;
        StringBuffer fontFamilyNames = new StringBuffer(1024);
        for (int i = 0; i < fontFamiliesNumber; i++) {
            if (i > 0) {
                fontFamilyNames.append(", ");
            }
            fontFamilyNames.append(fontFamilies[i]);
        }
        System.out.println(fontFamilyNames);
    }

    @Override
    public void startup(Display display, Map<String, String> properties) {
        window = new Window();

        showFontDesktopHints();
        showFontFamilies();

        Label label = buildLabel(45);
        window.setContent(label);

        window.setTitle("Label Antialiasing Test");
        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return false;
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(LabelAntialiasTest.class, args);
    }

}
