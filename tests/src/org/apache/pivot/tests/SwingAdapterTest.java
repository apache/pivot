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

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Window;

public class SwingAdapterTest extends Window implements Bindable {
    static {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (UnsupportedLookAndFeelException exception) {
            System.err.println(exception);
        } catch (ClassNotFoundException exception) {
            System.err.println(exception);
        } catch (InstantiationException exception) {
            System.err.println(exception);
        } catch (IllegalAccessException exception) {
            System.err.println(exception);
        }
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        JButton swingButton = (JButton)namespace.get("swingButton");

        swingButton.addMouseListener(new MouseListener() {
            @Override
            public void mouseExited(MouseEvent event) {
                System.out.println(event);
            }

            @Override
            public void mouseEntered(MouseEvent event) {
                System.out.println(event);
            }

            @Override
            public void mousePressed(MouseEvent event) {
                System.out.println(event);
            }

            @Override
            public void mouseReleased(MouseEvent event) {
                System.out.println(event);
            }

            @Override
            public void mouseClicked(MouseEvent event) {
                System.out.println(event);
            }
        });

        swingButton.addMouseMotionListener(new MouseMotionListener() {
            @Override
            public void mouseMoved(MouseEvent event) {
                System.out.println(event);
            }

            @Override
            public void mouseDragged(MouseEvent event) {
                System.out.println(event);
            }
        });

        swingButton.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent event) {
                System.out.println(event);
            }
        });

        swingButton.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent event) {
                System.out.println(event);
            }

            @Override
            public void keyReleased(KeyEvent event) {
                System.out.println(event);
            }

            @Override
            public void keyPressed(KeyEvent event) {
                System.out.println(event);
            }
        });
    }
}
