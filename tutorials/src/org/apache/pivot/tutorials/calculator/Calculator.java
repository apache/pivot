/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSEPERCENT_SCALE.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.tutorials.calculator;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Window;

/**
 * Calculator demonstration program that mimics the look and feel
 * of the iOS calculator app (pre iOS-11 that is).
 */
public final class Calculator implements Application {
    private BXMLSerializer serializer;

    @BXML private Window mainWindow;
    @BXML private PushButton settingsButton;
    @BXML private Label resultText;
    @BXML private PushButton clearButton;
    @BXML private PushButton changeSignButton;
    @BXML private PushButton percentButton;
    @BXML private PushButton divideButton;
    @BXML private PushButton multiplyButton;
    @BXML private PushButton minusButton;
    @BXML private PushButton plusButton;
    @BXML private PushButton equalsButton;
    @BXML private PushButton zeroButton;
    @BXML private PushButton oneButton;
    @BXML private PushButton twoButton;
    @BXML private PushButton threeButton;
    @BXML private PushButton fourButton;
    @BXML private PushButton fiveButton;
    @BXML private PushButton sixButton;
    @BXML private PushButton sevenButton;
    @BXML private PushButton eightButton;
    @BXML private PushButton nineButton;
    @BXML private PushButton dotButton;

    private static final MathContext MC = MathContext.DECIMAL64;
    private static final int PERCENT_SCALE = -2;

    private static StringBuilder resultBuffer = new StringBuilder("0");
    private static BigDecimal result = BigDecimal.ZERO;
    private static boolean seenDecimalPoint = false;
    private static boolean clearingAll = false;
    private static BigDecimal accumulator = BigDecimal.ZERO;
    private static Operator currentOperator = null;
    private static PushButton currentOperatorButton = null;
    private static boolean justSeenOperator = false;

    /** Instance variable so non-static components can be accessed from static methods. */
    private static Calculator instance;

    @Override
    public void startup(final Display display, final Map<String, String> properties) {
        try {
            serializer.readObject(Calculator.class, "calculator.bxml");
            serializer.bind(this);

            mainWindow.getComponentKeyListeners().add(new ComponentKeyListener() {
                @Override
                public boolean keyTyped(final Component comp, final char ch) {
                    // Some keys don't give us virtual key mappings, so we need to
                    // listen here for the relevant keys typed
                    switch (ch) {
                      case '*':
                        changeOperator(Operator.MULTIPLY);
                        return true;
                      case '+':
                        changeOperator(Operator.ADD);
                        return true;
                      case '%':
                        ACTION.PERCENT.perform(comp);
                        return true;
                      case '':
                        ACTION.BACKSPACE.perform(comp);
                        return true;
                      default:
                          return false;
                    }
                }
            });

            // Hook up the operators with their buttons
            Operator.ADD.setButton(plusButton);
            Operator.SUBTRACT.setButton(minusButton);
            Operator.MULTIPLY.setButton(multiplyButton);
            Operator.DIVIDE.setButton(divideButton);
            Operator.EQUALS.setButton(equalsButton);

            mainWindow.open(display);
            mainWindow.requestFocus();
        } catch (IOException | SerializationException ex) {
        }
    }

    private static void updateResult() {
        // TODO: pay attention to scaling, representation
        resultBuffer = new StringBuilder(result.toString());
        instance.resultText.setText(resultBuffer.toString());
    }

    private static void setClearAll(final boolean all) {
        instance.clearButton.setButtonData(all ? "AC" : "C");
        clearingAll = all;
    }

    private static void setOperatorButton(final boolean on) {
        if (currentOperatorButton != null) {
            currentOperatorButton.setStyleName(on ? "buttonBorderHighlight" : "buttonBorderNormal");
        }
    }

    private static void digit(final char digit) {
        setClearAll(false);
        setOperatorButton(false);
        justSeenOperator = false;

        switch (digit) {
          case '0':
          case '1':
          case '2':
          case '3':
          case '4':
          case '5':
          case '6':
          case '7':
          case '8':
          case '9':
            resultBuffer.append(digit);
            break;
          case '.':
            if (!seenDecimalPoint) {
                seenDecimalPoint = true;
                resultBuffer.append(digit);
                instance.resultText.setText(resultBuffer.toString());
                return;
            }
            break;
          default:
            break;
        }
        result = new BigDecimal(resultBuffer.toString());
        updateResult();
    }

    private enum Operator {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        EQUALS;

        private PushButton button;

        public void setButton(final PushButton button) {
            this.button = button;
        }

        public PushButton getButton() {
            return this.button;
        }
    }

    private static void changeOperator(final Operator newOperator) {
        if (currentOperator == null) {
            currentOperator = newOperator;
            accumulator = result;
            resultBuffer = new StringBuilder();
            seenDecimalPoint = false;
            currentOperatorButton = currentOperator.getButton();
            setOperatorButton(true);
            justSeenOperator = true;
        } else if (justSeenOperator) {
            setOperatorButton(false);
            currentOperator = newOperator;
            currentOperatorButton = currentOperator.getButton();
            setOperatorButton(true);
        } else {
            // Perform the currentOperator function on accumulator x result
            switch (currentOperator) {
              case ADD:
                result = accumulator.add(result, MC);
                break;
              case SUBTRACT:
                result = accumulator.subtract(result, MC);
                break;
              case MULTIPLY:
                result = accumulator.multiply(result, MC);
                break;
              case DIVIDE:
                result = accumulator.divide(result, MC);
                break;
              default:
                break;
            }
            if (newOperator == Operator.EQUALS) {
                setOperatorButton(false);
                currentOperator = null;
                currentOperatorButton = null;
                justSeenOperator = false;
                accumulator = BigDecimal.ZERO;
            } else {
                currentOperator = newOperator;
                currentOperatorButton = currentOperator.getButton();
                accumulator = result;
            }
            seenDecimalPoint = false;
            updateResult();
            resultBuffer = new StringBuilder();
        }
    }

    private interface ActionDoer {
        void perform(Component source);
    }

    private enum ACTION implements ActionDoer {
        DOT {
            @Override
            public void perform(final Component source) {
                digit('.');
            }
        },
        ZERO {
            @Override
            public void perform(final Component source) {
                digit('0');
            }
        },
        ONE {
            @Override
            public void perform(final Component source) {
                digit('1');
            }
        },
        TWO {
            @Override
            public void perform(final Component source) {
                digit('2');
            }
        },
        THREE {
            @Override
            public void perform(final Component source) {
                digit('3');
            }
        },
        FOUR {
            @Override
            public void perform(final Component source) {
                digit('4');
            }
        },
        FIVE {
            @Override
            public void perform(final Component source) {
                digit('5');
            }
        },
        SIX {
            @Override
            public void perform(final Component source) {
                digit('6');
            }
        },
        SEVEN {
            @Override
            public void perform(final Component source) {
                digit('7');
            }
        },
        EIGHT {
            @Override
            public void perform(final Component source) {
                digit('8');
            }
        },
        NINE {
            @Override
            public void perform(final Component source) {
                digit('9');
            }
        },
        ADD {
            @Override
            public void perform(final Component source) {
                changeOperator(Operator.ADD);
            }
        },
        SUBTRACT {
            @Override
            public void perform(final Component source) {
                changeOperator(Operator.SUBTRACT);
            }
        },
        MULTIPLY {
            @Override
            public void perform(final Component source) {
                changeOperator(Operator.MULTIPLY);
            }
        },
        DIVIDE {
            @Override
            public void perform(final Component source) {
                changeOperator(Operator.DIVIDE);
            }
        },
        EQUALS {
            @Override
            public void perform(final Component source) {
                changeOperator(Operator.EQUALS);
            }
        },
        NEGATE {
            @Override
            public void perform(final Component source) {
                result = result.negate(MC);
                updateResult();
            }
        },
        PERCENT {
            @Override
            public void perform(final Component source) {
                if (currentOperator == null
                 || currentOperator == Operator.MULTIPLY
                 || currentOperator == Operator.DIVIDE
                 || accumulator == null) {
                    result = result.scaleByPowerOfTen(PERCENT_SCALE);
                } else {
                    result = accumulator.multiply(result, MC).scaleByPowerOfTen(PERCENT_SCALE);
                }
                updateResult();
            }
        },
        BACKSPACE {
            @Override
            public void perform(final Component source) {
                if (resultBuffer.length() > 0 && !resultBuffer.toString().equals("0")) {
                    char ch = resultBuffer.charAt(resultBuffer.length() - 1);
                    if (ch == '.') {
                        seenDecimalPoint = false;
                    }
                    if (resultBuffer.length() == 1) {
                        resultBuffer.setCharAt(0, '0');
                    } else {
                        resultBuffer.deleteCharAt(resultBuffer.length() - 1);
                    }
                    instance.resultText.setText(resultBuffer.toString());
                }
            }
        },
        CLEAR {
            @Override
            public void perform(final Component source) {
                result = BigDecimal.ZERO;
                seenDecimalPoint = false;
                justSeenOperator = false;
                if (clearingAll) {
                    accumulator = BigDecimal.ZERO;
                    setOperatorButton(false);
                    currentOperator = null;
                    currentOperatorButton = null;
                } else {
                    setClearAll(true);
                }
                updateResult();
            }
        },
        QUIT {
            @Override
            public void perform(final Component source) {
                DesktopApplicationContext.exit();
            }
        }
    }

    private class CalculatorAction extends Action {
        private ACTION action;

        public CalculatorAction(final ACTION action) {
            this.action = action;
            Action.getNamedActions().put(action.toString(), this);
        }

        @Override
        public void perform(final Component source) {
            action.perform(source);
        }
    }


    public void registerActions() {
        for (ACTION act : ACTION.values()) {
            new CalculatorAction(act);
        }
    }

    public Calculator() {
        serializer = new BXMLSerializer();
        ApplicationContext.applyStylesheet("@org/apache/pivot/tutorials/calculator/calculator_styles.json");
        registerActions();
        instance = this;
    }

    public static void main(final String[] args) {
        DesktopApplicationContext.main(Calculator.class, args);
    }

}

