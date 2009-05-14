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
package pivot.tutorials.stocktracker;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import pivot.collections.ArrayList;
import pivot.collections.Dictionary;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.serialization.CSVSerializer;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskListener;
import pivot.web.GetQuery;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Container;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.Form;
import pivot.wtk.Keyboard;
import pivot.wtk.Label;
import pivot.wtk.MessageType;
import pivot.wtk.Span;
import pivot.wtk.TableView;
import pivot.wtk.TableViewSelectionListener;
import pivot.wtk.TaskAdapter;
import pivot.wtk.TextInput;
import pivot.wtk.TextInputTextListener;
import pivot.wtk.Window;
import pivot.wtk.text.TextNode;
import pivot.wtkx.Bindable;

public class StockTracker extends Bindable implements Application {
    private ArrayList<String> symbols = new ArrayList<String>();

    @Load(resourceName="stocktracker.wtkx") private Window window;

    @Bind(fieldName="window") private TableView stocksTableView;
    @Bind(fieldName="window") private TextInput symbolTextInput;
    @Bind(fieldName="window") private Button addSymbolButton;
    @Bind(fieldName="window") private Button removeSymbolsButton;
    @Bind(fieldName="window") private Label lastUpdateLabel;
    @Bind(fieldName="window") private Button yahooFinanceButton;

    @Bind(fieldName="window", id="detail.rootPane")
    private Container detailRootPane;

    @Bind(fieldName="window", id="detail.changeLabel")
    private Label detailChangeLabel;

    private GetQuery getQuery = null;

    public static final String LANGUAGE_PROPERTY_NAME = "language";
    public static final String SERVICE_HOSTNAME = "download.finance.yahoo.com";
    public static final String SERVICE_PATH = "/d/quotes.csv";
    public static final long REFRESH_INTERVAL = 15000;
    public static final String YAHOO_FINANCE_HOME = "http://finance.yahoo.com";

    public StockTracker() {
        symbols.setComparator(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        symbols.add("EBAY");
        symbols.add("AAPL");
        symbols.add("MSFT");
        symbols.add("AMZN");
        symbols.add("GOOG");
        symbols.add("VMW");
        symbols.add("JAVA");
    }

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        // Set the locale
        String language = properties.get(LANGUAGE_PROPERTY_NAME);
        if (language != null) {
            Locale.setDefault(new Locale(language));
        }

        // Bind to the WTKX source
        bind();

        // Wire up event handlers
        stocksTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                refreshDetail();
            }
        });

        stocksTableView.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE) {
                    removeSelectedSymbols();
                }

                return false;
            }
        });

        symbolTextInput.getTextInputTextListeners().add(new TextInputTextListener() {
            public void textChanged(TextInput textInput) {
                TextNode textNode = textInput.getTextNode();
                addSymbolButton.setEnabled(textNode.getCharacterCount() > 0);
            }
        });

        symbolTextInput.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    addSymbol();
                }

                return false;
            }
        });

        addSymbolButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                addSymbol();
            }
        });

        removeSymbolsButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                removeSelectedSymbols();
            }
        });

        yahooFinanceButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                try {
                    ApplicationContext.open(new URL(YAHOO_FINANCE_HOME));
                } catch(MalformedURLException exception) {
                }
            }
        });

        window.open(display);

        refreshTable();

        ApplicationContext.scheduleRecurringCallback(new Runnable() {
            public void run() {
                refreshTable();
            }
        }, REFRESH_INTERVAL);

        symbolTextInput.requestFocus();
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    @SuppressWarnings("unchecked")
    private void refreshTable() {
        getQuery = new GetQuery(SERVICE_HOSTNAME, SERVICE_PATH);

        StringBuilder symbolsArgumentBuilder = new StringBuilder();
        for (int i = 0, n = symbols.getLength(); i < n; i++) {
            if (i > 0) {
                symbolsArgumentBuilder.append(",");
            }

            symbolsArgumentBuilder.append(symbols.get(i));
        }

        // Format:
        // s - symbol
        // n - company name
        // l1 - most recent value
        // o - opening value
        // h - high value
        // g - low value
        // c1 - change percentage
        // v - volume
        String symbolsArgument = symbolsArgumentBuilder.toString();
        getQuery.getArguments().put("s", symbolsArgument);
        getQuery.getArguments().put("f", "snl1ohgc1v");

        CSVSerializer quoteSerializer = new CSVSerializer();
        quoteSerializer.getKeys().add("symbol");
        quoteSerializer.getKeys().add("companyName");
        quoteSerializer.getKeys().add("value");
        quoteSerializer.getKeys().add("openingValue");
        quoteSerializer.getKeys().add("highValue");
        quoteSerializer.getKeys().add("lowValue");
        quoteSerializer.getKeys().add("change");
        quoteSerializer.getKeys().add("volume");

        quoteSerializer.setItemClass(StockQuote.class);
        getQuery.setSerializer(quoteSerializer);

        getQuery.execute(new TaskAdapter<Object>(new TaskListener<Object>() {
            public void taskExecuted(Task<Object> task) {
                if (task == getQuery) {
                    Sequence<Span> selectedRanges = stocksTableView.getSelectedRanges();

                    List<StockQuote> quotes = (List<StockQuote>)task.getResult();
                    stocksTableView.setTableData(quotes);

                    if (selectedRanges.getLength() > 0) {
                        stocksTableView.setSelectedRanges(selectedRanges);
                    } else {
                        if (quotes.getLength() > 0) {
                            stocksTableView.setSelectedIndex(0);
                        }
                    }

                    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                        DateFormat.MEDIUM, Locale.getDefault());
                    lastUpdateLabel.setText(dateFormat.format(new Date()));

                    getQuery = null;
                }
            }

            public void executeFailed(Task<Object> task) {
                if (task == getQuery) {
                    System.out.println(task.getFault());
                    getQuery = null;
                }
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private void refreshDetail() {
        int firstSelectedIndex = stocksTableView.getFirstSelectedIndex();
        removeSymbolsButton.setEnabled(firstSelectedIndex != -1);

        StockQuote stockQuote = null;
        Form.setFlag(detailChangeLabel, (Form.Flag)null);

        if (firstSelectedIndex != -1) {
            int lastSelectedIndex = stocksTableView.getLastSelectedIndex();

            if (firstSelectedIndex == lastSelectedIndex) {
                List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
                stockQuote = tableData.get(firstSelectedIndex);

                if (stockQuote.getChange() < 0) {
                    Form.setFlag(detailChangeLabel, new Form.Flag(MessageType.ERROR));
                }
            }
        } else {
            stockQuote = new StockQuote();
        }

        StockQuoteView stockQuoteView = new StockQuoteView(stockQuote);
        detailRootPane.load(stockQuoteView);
    }

    @SuppressWarnings("unchecked")
    private void addSymbol() {
        String symbol = symbolTextInput.getText().toUpperCase();
        if (symbols.indexOf(symbol) == -1) {
            int index = symbols.add(symbol);

            List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
            StockQuote stockQuote = new StockQuote();
            stockQuote.setSymbol(symbol);
            tableData.insert(stockQuote, index);

            stocksTableView.setSelectedIndex(index);
        }

        symbolTextInput.setText("");
        refreshTable();
    }

    private void removeSelectedSymbols() {
        int selectedIndex = stocksTableView.getFirstSelectedIndex();
        int selectionLength = stocksTableView.getLastSelectedIndex() - selectedIndex + 1;
        stocksTableView.getTableData().remove(selectedIndex, selectionLength);
        symbols.remove(selectedIndex, selectionLength);

        if (selectedIndex >= symbols.getLength()) {
            selectedIndex = symbols.getLength() - 1;
        }

        stocksTableView.setSelectedIndex(selectedIndex);

        if (selectedIndex == -1) {
            refreshDetail();
        }
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(StockTracker.class, args);
    }
}
