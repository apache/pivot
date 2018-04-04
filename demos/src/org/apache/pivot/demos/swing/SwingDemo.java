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
package org.apache.pivot.demos.swing;

import java.beans.PropertyVetoException;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Window;

public class SwingDemo extends ApplicationContext {
    public static class HostApplet extends JApplet {
        private static final long serialVersionUID = 0;

        @Override
        public void init() {
            setContentPane(desktop);
        }

        @Override
        public void start() {
            createFrames();
        }

        @Override
        public void stop() {
            desktop.removeAll();
        }
    }

    @SuppressWarnings("unused")
    private static final long serialVersionUID = 0;

    private static JDesktopPane desktop = new JDesktopPane();

    static {
        // Start the callback timer
        createTimer();
    }

    public static void main(String[] args) {
        final JFrame jFrame = new JFrame("Pivot/Swing Demo");

        jFrame.setContentPane(desktop);
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(1024, 768);
        jFrame.setVisible(true);

        createFrames();
    }

    private static void createFrames() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                createSwingFrame();
                createPivotFrame();
            }
        });
    }

    private static void createSwingFrame() {
        // Create the internal frame that will contain the Swing components
        JInternalFrame internalFrame = new JInternalFrame("Swing Components");
        desktop.add(internalFrame);

        Box box = Box.createVerticalBox();
        box.setBorder(new EmptyBorder(8, 8, 8, 8));

        box.add(new JLabel("Hello from Swing!"));
        box.add(Box.createVerticalStrut(8));

        box.add(new JButton("JButton"));
        box.add(Box.createVerticalStrut(8));

        JCheckBox jCheckBox = new JCheckBox("JCheckBox");
        jCheckBox.setSelected(true);
        box.add(jCheckBox);
        box.add(Box.createVerticalStrut(8));

        ButtonGroup buttonGroup = new ButtonGroup();

        JRadioButton jRadioButton1 = new JRadioButton("JRadioButton 1", true);
        buttonGroup.add(jRadioButton1);
        box.add(jRadioButton1);

        JRadioButton jRadioButton2 = new JRadioButton("JRadioButton 2", true);
        buttonGroup.add(jRadioButton2);
        box.add(jRadioButton2);

        JRadioButton jRadioButton3 = new JRadioButton("JRadioButton 3", true);
        buttonGroup.add(jRadioButton3);
        box.add(jRadioButton3);
        box.add(Box.createVerticalStrut(8));

        JProgressBar jProgressBar = new JProgressBar();
        jProgressBar.setIndeterminate(true);
        box.add(jProgressBar);

        internalFrame.add(new JScrollPane(box));

        // Open and select the internal frame
        internalFrame.setLocation(50, 50);
        internalFrame.setSize(480, 360);
        internalFrame.setVisible(true);
        internalFrame.setResizable(true);
    }

    private static void createPivotFrame() {
        // Create the internal frame that will contain the Pivot components
        JInternalFrame internalFrame = new JInternalFrame("Pivot Components");
        desktop.add(internalFrame);

        // Create the display host
        ApplicationContext.DisplayHost displayHost = new ApplicationContext.DisplayHost();
        internalFrame.add(displayHost);

        // Add the display to the display list
        displays.add(displayHost.getDisplay());

        // Load the Pivot window
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        Window window;
        try {
            window = (Window) bxmlSerializer.readObject(SwingDemo.class.getResource("pivot_window.bxml"));
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        // Open the Pivot window on the display
        window.open(displayHost.getDisplay());

        // Open and select the internal frame
        internalFrame.setLocation(240, 100);
        internalFrame.setSize(480, 360);
        internalFrame.setVisible(true);
        internalFrame.setResizable(true);

        try {
            internalFrame.setSelected(true);
        } catch (PropertyVetoException exception) {
            throw new RuntimeException(exception);
        }
    }
}

