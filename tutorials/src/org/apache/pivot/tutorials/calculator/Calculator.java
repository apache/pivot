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
package org.apache.pivot.tutorials.calculator;

import java.io.*;
import java.math.*;
import org.apache.pivot.beans.*;
import org.apache.pivot.collections.*;
import org.apache.pivot.serialization.*;
import org.apache.pivot.wtk.*;

public class Calculator
    extends Application.Adapter
{
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

    private static MathContext MC = MathContext.DECIMAL64;

    private static StringBuilder resultBuffer = new StringBuilder("0");
    private static BigDecimal result = BigDecimal.ZERO;
    private static boolean seenDecimalPoint = false;
    private static BigDecimal accumulator = BigDecimal.ZERO;
    private static Operator currentOperator = null;

    private static Calculator instance;

    @Override
    public void startup(Display display, Map<String, String> properties) {
        try {
            serializer.readObject(Calculator.class, "calculator.bxml");
            serializer.bind(this);

            mainWindow.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
                @Override
                public boolean keyTyped(Component comp, char ch) {
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
                      case '\u0008':
                      case '\u007F':
                        ACTION.BACKSPACE.perform(comp);
                        return true;
                      default:
                          return false;
                    }
                }
            });

            mainWindow.open(display);
            mainWindow.requestFocus();
        }
        catch (IOException | SerializationException ex) {
        }
    }

    private static void updateResult() {
        // TODO: pay attention to scaling, representation
        resultBuffer = new StringBuilder(result.toString());
        instance.resultText.setText(resultBuffer.toString());
    }

    private static void digit(char digit) {
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
        }
        result = new BigDecimal(resultBuffer.toString());
        updateResult();
    }

    private enum Operator
    {
        ADD,
        SUBTRACT,
        MULTIPLY,
        DIVIDE,
        EQUALS
    }

    private static void changeOperator(Operator newOperator) {
        if (currentOperator == null) {
            currentOperator = newOperator;
            accumulator = result;
            resultBuffer = new StringBuilder();
            seenDecimalPoint = false;
            // TODO: highlight (somehow) the selected operator
        }
        else {
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
            }
            if (newOperator == Operator.EQUALS) {
                currentOperator = null;
                accumulator = BigDecimal.ZERO;
            }
            else {
                currentOperator = newOperator;
                accumulator = result;
            }
            seenDecimalPoint = false;
            updateResult();
            resultBuffer = new StringBuilder();
        }
    }

    private interface ActionDoer
    {
        void perform(Component source);
    }

    private enum ACTION implements ActionDoer
    {
        DOT {
            @Override
            public void perform(Component source) {
                digit('.');
            }
        },
        ZERO {
            @Override
            public void perform(Component source) {
                digit('0');
            }
        },
        ONE {
            @Override
            public void perform(Component source) {
                digit('1');
            }
        },
        TWO {
            @Override
            public void perform(Component source) {
                digit('2');
            }
        },
        THREE {
            @Override
            public void perform(Component source) {
                digit('3');
            }
        },
        FOUR {
            @Override
            public void perform(Component source) {
                digit('4');
            }
        },
        FIVE {
            @Override
            public void perform(Component source) {
                digit('5');
            }
        },
        SIX {
            @Override
            public void perform(Component source) {
                digit('6');
            }
        },
        SEVEN {
            @Override
            public void perform(Component source) {
                digit('7');
            }
        },
        EIGHT {
            @Override
            public void perform(Component source) {
                digit('8');
            }
        },
        NINE {
            @Override
            public void perform(Component source) {
                digit('9');
            }
        },
        ADD {
            @Override
            public void perform(Component source) {
                changeOperator(Operator.ADD);
            }
        },
        SUBTRACT {
            @Override
            public void perform(Component source) {
                changeOperator(Operator.SUBTRACT);
            }
        },
        MULTIPLY {
            @Override
            public void perform(Component source) {
                changeOperator(Operator.MULTIPLY);
            }
        },
        DIVIDE {
            @Override
            public void perform(Component source) {
                changeOperator(Operator.DIVIDE);
            }
        },
        EQUALS {
            @Override
            public void perform(Component source) {
                changeOperator(Operator.EQUALS);
            }
        },
        NEGATE {
            @Override
            public void perform(Component source) {
                result = result.negate(MC);
                updateResult();
            }
        },
        PERCENT {
            @Override
            public void perform(Component source) {
                if (currentOperator == null ||
                    currentOperator == Operator.MULTIPLY ||
                    currentOperator == Operator.DIVIDE ||
                    accumulator == null) {
                    result = result.scaleByPowerOfTen(-2);
                } else {
                    result = accumulator.multiply(result, MC).scaleByPowerOfTen(-2);
                }
                updateResult();
            }
        },
        BACKSPACE {
            @Override
            public void perform(Component source) {
                if (resultBuffer.length() > 0 && !resultBuffer.toString().equals("0")) {
                    char ch = resultBuffer.charAt(resultBuffer.length() - 1);
                    if (ch == '.') {
                        seenDecimalPoint = false;
                    }
                    resultBuffer.deleteCharAt(resultBuffer.length() - 1);
                    instance.resultText.setText(resultBuffer.toString());
                }
            }
        },
        CLEAR {
            @Override
            public void perform(Component source) {
                result = BigDecimal.ZERO;
                seenDecimalPoint = false;
                accumulator = BigDecimal.ZERO;
                currentOperator = null;
                updateResult();
            }
        },
        QUIT {
            @Override
            public void perform(Component source) {
                DesktopApplicationContext.exit();
            }
        }
    }

    private class CalculatorAction extends Action
    {
        private ACTION action;

        public CalculatorAction(ACTION action) {
            this.action = action;
            Action.getNamedActions().put(action.toString(), this);
        }

        @Override
        public void perform(Component source) {
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

    public static void main(String[] args) {
        DesktopApplicationContext.main(Calculator.class, args);
    }

}

