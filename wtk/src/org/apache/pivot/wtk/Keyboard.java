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
package org.apache.pivot.wtk;

import java.awt.event.KeyEvent;
import java.lang.reflect.Field;
import java.util.Locale;

/**
 * Class representing the system keyboard.
 */
public final class Keyboard {
    /**
     * Enumeration representing keyboard modifiers.
     */
    public enum Modifier {
        SHIFT,
        CTRL,
        ALT,
        META;

        public int getMask() {
            return 1 << ordinal();
        }
    }

    /**
     * Enumeration representing key locations.
     */
    public enum KeyLocation {
        STANDARD,
        LEFT,
        RIGHT,
        KEYPAD
    }

    /**
     * Represents a keystroke, a combination of a keycode and modifier flags.
     */
    public static final class KeyStroke {
        private int keyCode = KeyCode.UNDEFINED;
        private int modifiersLocal = 0x00;

        public static final String COMMAND_ABBREVIATION = "CMD";

        public KeyStroke(int keyCode, int modifiers) {
            this.keyCode = keyCode;
            this.modifiersLocal = modifiers;
        }

        public int getKeyCode() {
            return keyCode;
        }

        public int getModifiers() {
            return modifiersLocal;
        }

        @Override
        public boolean equals(Object object) {
            boolean equals = false;

            if (object instanceof KeyStroke) {
                KeyStroke keyStroke = (KeyStroke)object;
                equals = (this.keyCode == keyStroke.keyCode
                    && this.modifiersLocal == keyStroke.modifiersLocal);
            }

            return equals;
        }

        @Override
        public int hashCode() {
            // NOTE Key codes are currently defined as 16-bit values, so
            // shifting by 4 bits to append the modifiers should be OK.
            // However, if Sun changes the key code values in the future,
            // this may no longer be safe.
            int hashCode = keyCode << 4 | modifiersLocal;
            return hashCode;
        }

        @Override
        public String toString() {
            int awtModifiers = 0x00;

            if (((modifiersLocal & Modifier.META.getMask()) > 0)) {
                awtModifiers |= KeyEvent.META_DOWN_MASK;
            }

            if (((modifiersLocal & Modifier.CTRL.getMask()) > 0)) {
                awtModifiers |= KeyEvent.CTRL_DOWN_MASK;
            }

            if (((modifiersLocal & Modifier.ALT.getMask()) > 0)) {
                awtModifiers |= KeyEvent.ALT_DOWN_MASK;
            }

            if (((modifiersLocal & Modifier.SHIFT.getMask()) > 0)) {
                awtModifiers |= KeyEvent.SHIFT_DOWN_MASK;
            }

            if (awtModifiers != 0x00) {
                return KeyEvent.getModifiersExText(awtModifiers) + Platform.getKeyStrokeModifierSeparator()
                    + KeyEvent.getKeyText(keyCode);
            }

            return KeyEvent.getKeyText(keyCode);
        }

        public static KeyStroke decode(String value) {
            if (value == null) {
                throw new IllegalArgumentException("value is null.");
            }

            int keyCode = KeyCode.UNDEFINED;
            int modifiersLocal = 0x00;

            String[] keys = value.split("-");
            for (int i = 0, n = keys.length; i < n; i++) {
                if (i < n - 1) {
                    // Modifier
                    String modifierAbbreviation = keys[i].toUpperCase(Locale.ENGLISH);

                    Modifier modifier;
                    if (modifierAbbreviation.equals(COMMAND_ABBREVIATION)) {
                        modifier = Platform.getCommandModifier();
                    } else {
                        modifier = Modifier.valueOf(modifierAbbreviation);
                    }

                    modifiersLocal |= modifier.getMask();
                } else {
                    // Keycode
                    try {
                        Field keyCodeField = KeyCode.class.getField(keys[i].toUpperCase(Locale.ENGLISH));
                        keyCode = (Integer)keyCodeField.get(null);
                    } catch(Exception exception) {
                        throw new IllegalArgumentException(exception);
                    }
                }
            }

            return new KeyStroke(keyCode, modifiersLocal);
        }
    }

    /**
     * Contains a set of key code constants that are common to all locales.
     */
    public static final class KeyCode {
        public static final int A = KeyEvent.VK_A;
        public static final int B = KeyEvent.VK_B;
        public static final int C = KeyEvent.VK_C;
        public static final int D = KeyEvent.VK_D;
        public static final int E = KeyEvent.VK_E;
        public static final int F = KeyEvent.VK_F;
        public static final int G = KeyEvent.VK_G;
        public static final int H = KeyEvent.VK_H;
        public static final int I = KeyEvent.VK_I;
        public static final int J = KeyEvent.VK_J;
        public static final int K = KeyEvent.VK_K;
        public static final int L = KeyEvent.VK_L;
        public static final int M = KeyEvent.VK_M;
        public static final int N = KeyEvent.VK_N;
        public static final int O = KeyEvent.VK_O;
        public static final int P = KeyEvent.VK_P;
        public static final int Q = KeyEvent.VK_Q;
        public static final int R = KeyEvent.VK_R;
        public static final int S = KeyEvent.VK_S;
        public static final int T = KeyEvent.VK_T;
        public static final int U = KeyEvent.VK_U;
        public static final int V = KeyEvent.VK_V;
        public static final int W = KeyEvent.VK_W;
        public static final int X = KeyEvent.VK_X;
        public static final int Y = KeyEvent.VK_Y;
        public static final int Z = KeyEvent.VK_Z;

        public static final int N0 = KeyEvent.VK_0;
        public static final int N1 = KeyEvent.VK_1;
        public static final int N2 = KeyEvent.VK_2;
        public static final int N3 = KeyEvent.VK_3;
        public static final int N4 = KeyEvent.VK_4;
        public static final int N5 = KeyEvent.VK_5;
        public static final int N6 = KeyEvent.VK_6;
        public static final int N7 = KeyEvent.VK_7;
        public static final int N8 = KeyEvent.VK_8;
        public static final int N9 = KeyEvent.VK_9;

        public static final int TAB = KeyEvent.VK_TAB;
        public static final int SPACE = KeyEvent.VK_SPACE;
        public static final int ENTER = KeyEvent.VK_ENTER;
        public static final int ESCAPE = KeyEvent.VK_ESCAPE;
        public static final int BACKSPACE = KeyEvent.VK_BACK_SPACE;
        public static final int DELETE = KeyEvent.VK_DELETE;
        public static final int INSERT = KeyEvent.VK_INSERT;

        public static final int UP = KeyEvent.VK_UP;
        public static final int DOWN = KeyEvent.VK_DOWN;
        public static final int LEFT = KeyEvent.VK_LEFT;
        public static final int RIGHT = KeyEvent.VK_RIGHT;

        public static final int PAGE_UP = KeyEvent.VK_PAGE_UP;
        public static final int PAGE_DOWN = KeyEvent.VK_PAGE_DOWN;

        public static final int HOME = KeyEvent.VK_HOME;
        public static final int END = KeyEvent.VK_END;

        public static final int KEYPAD_0 = KeyEvent.VK_NUMPAD0;
        public static final int KEYPAD_1 = KeyEvent.VK_NUMPAD1;
        public static final int KEYPAD_2 = KeyEvent.VK_NUMPAD2;
        public static final int KEYPAD_3 = KeyEvent.VK_NUMPAD3;
        public static final int KEYPAD_4 = KeyEvent.VK_NUMPAD4;
        public static final int KEYPAD_5 = KeyEvent.VK_NUMPAD5;
        public static final int KEYPAD_6 = KeyEvent.VK_NUMPAD6;
        public static final int KEYPAD_7 = KeyEvent.VK_NUMPAD7;
        public static final int KEYPAD_8 = KeyEvent.VK_NUMPAD8;
        public static final int KEYPAD_9 = KeyEvent.VK_NUMPAD9;
        public static final int KEYPAD_UP = KeyEvent.VK_KP_UP;
        public static final int KEYPAD_DOWN = KeyEvent.VK_KP_DOWN;
        public static final int KEYPAD_LEFT = KeyEvent.VK_KP_LEFT;
        public static final int KEYPAD_RIGHT = KeyEvent.VK_KP_RIGHT;

        public static final int PLUS = KeyEvent.VK_PLUS;
        public static final int MINUS = KeyEvent.VK_MINUS;
        public static final int EQUALS = KeyEvent.VK_EQUALS;

        public static final int ADD = KeyEvent.VK_ADD;
        public static final int SUBTRACT = KeyEvent.VK_SUBTRACT;
        public static final int MULTIPLY = KeyEvent.VK_MULTIPLY;
        public static final int DIVIDE = KeyEvent.VK_DIVIDE;

        public static final int F1 = KeyEvent.VK_F1;
        public static final int F2 = KeyEvent.VK_F2;
        public static final int F3 = KeyEvent.VK_F3;
        public static final int F4 = KeyEvent.VK_F4;
        public static final int F5 = KeyEvent.VK_F5;
        public static final int F6 = KeyEvent.VK_F6;
        public static final int F7 = KeyEvent.VK_F7;
        public static final int F8 = KeyEvent.VK_F8;
        public static final int F9 = KeyEvent.VK_F9;
        public static final int F10 = KeyEvent.VK_F10;
        public static final int F11 = KeyEvent.VK_F11;
        public static final int F12 = KeyEvent.VK_F12;

        public static final int UNDEFINED = KeyEvent.VK_UNDEFINED;
    }

    private static int modifiers = 0;

    /**
     * Returns a bitfield representing the keyboard modifiers that are
     * currently pressed.
     */
    public static int getModifiers() {
        return modifiers;
    }

    protected static void setModifiers(int modifiers) {
        Keyboard.modifiers = modifiers;
    }

    /**
     * Tests the pressed state of a modifier.
     *
     * @param modifier
     *
     * @return
     * <tt>true</tt> if the modifier is pressed; <tt>false</tt>, otherwise.
     */
    public static boolean isPressed(Modifier modifier) {
        return (modifiers & modifier.getMask()) > 0;
    }

    /**
     * Returns the current drop action.
     *
     * @return
     * The drop action corresponding to the currently pressed modifier keys,
     * or <tt>null</tt> if no modifiers are pressed.
     */
    public static DropAction getDropAction() {
        // TODO Return an appropriate action for OS:
        // Windows: no modifier - move; control - copy; control-shift - link
        // Mac OS X: no modifier - move; option - copy; option-command - link

        DropAction dropAction = null;

        if (isPressed(Modifier.CTRL)
            && isPressed(Modifier.SHIFT)) {
            dropAction = DropAction.LINK;
        } else if (isPressed(Modifier.CTRL)) {
            dropAction = DropAction.COPY;
        } else {
            dropAction = DropAction.MOVE;
        }

        return dropAction;
    }
}
