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
package org.apache.pivot.tutorials.stocktracker;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewRowListener;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputTextListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TableViewRowComparator;
import org.apache.pivot.wtkx.Bindable;
import org.apache.pivot.wtkx.WTKX;

public class StockTrackerWindow extends Window implements Bindable {
    @WTKX private TableView stocksTableView = null;
    @WTKX private TextInput symbolTextInput = null;
    @WTKX private Button addSymbolButton = null;
    @WTKX private Button removeSymbolsButton = null;
    @WTKX private DetailPane detailPane = null;
    @WTKX private Label lastUpdateLabel = null;
    @WTKX private Button yahooFinanceButton = null;

    private ArrayList<String> symbols = new ArrayList<String>();
    private GetQuery getQuery = null;

    public static final String SERVICE_HOSTNAME = "download.finance.yahoo.com";
    public static final String SERVICE_PATH = "/d/quotes.csv";
    public static final long REFRESH_INTERVAL = 15000;
    public static final String YAHOO_FINANCE_HOME = "http://finance.yahoo.com";

    public StockTrackerWindow() {
        symbols.setComparator(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        symbols.add("EBAY");
        symbols.add("AAPL");
        symbols.add("MSFT");
        symbols.add("AMZN");
        symbols.add("GOOG");
        symbols.add("ORCL");
        symbols.add("IBM");
    }

    @Override
    public void initialize(Resources resources) {
        // Wire up event handlers
        stocksTableView.getTableViewRowListeners().add(new TableViewRowListener.Adapter() {
            @Override
            public void rowsSorted(TableView tableView) {
                List<?> tableData = stocksTableView.getTableData();
                if (tableData.getLength() > 0) {
                    stocksTableView.setSelectedIndex(0);
                }
            }
        });

        stocksTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener.Adapter() {
            @Override
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                refreshDetail();
            }
        });

        stocksTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView) {
                List<Object> tableData = (List<Object>)tableView.getTableData();
                tableData.setComparator(new TableViewRowComparator(tableView));
            }
        });

        stocksTableView.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE
                    || keyCode == Keyboard.KeyCode.BACKSPACE) {
                    removeSelectedSymbols();
                }

                return false;
            }
        });

        symbolTextInput.getTextInputTextListeners().add(new TextInputTextListener() {
            @Override
            public void textChanged(TextInput textInput) {
                addSymbolButton.setEnabled(textInput.getTextLength() > 0);
            }
        });

        symbolTextInput.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    addSymbol();
                }

                return false;
            }
        });

        addSymbolButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                addSymbol();
            }
        });

        removeSymbolsButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                removeSelectedSymbols();
            }
        });

        yahooFinanceButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Desktop desktop = Desktop.getDesktop();

                try {
                    desktop.browse(new URL(YAHOO_FINANCE_HOME).toURI());
                } catch(MalformedURLException exception) {
                    throw new RuntimeException(exception);
                } catch(URISyntaxException exception) {
                    throw new RuntimeException(exception);
                } catch(IOException exception) {
                    System.out.println("Unable to open "
                        + YAHOO_FINANCE_HOME + " in default browser.");
                }
            }
        });
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);

        refreshTable();

        ApplicationContext.scheduleRecurringCallback(new Runnable() {
            @Override
            public void run() {
                refreshTable();
            }
        }, REFRESH_INTERVAL);

        symbolTextInput.requestFocus();
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
        getQuery.getParameters().put("s", symbolsArgument);
        getQuery.getParameters().put("f", "snl1ohgc1v");

        CSVSerializer quoteSerializer = new CSVSerializer();
        quoteSerializer.setItemClass(StockQuote.class);

        quoteSerializer.getKeys().add("symbol");
        quoteSerializer.getKeys().add("companyName");
        quoteSerializer.getKeys().add("value");
        quoteSerializer.getKeys().add("openingValue");
        quoteSerializer.getKeys().add("highValue");
        quoteSerializer.getKeys().add("lowValue");
        quoteSerializer.getKeys().add("change");
        quoteSerializer.getKeys().add("volume");

        getQuery.setSerializer(quoteSerializer);

        getQuery.execute(new TaskAdapter<Object>(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                if (task == getQuery) {
                    List<Object> quotes = (List<Object>)task.getResult();

                    // Preserve any existing sort and selection
                    Sequence<?> selectedStocks = stocksTableView.getSelectedRows();

                    List<Object> tableData = (List<Object>)stocksTableView.getTableData();
                    Comparator<Object> comparator = tableData.getComparator();
                    quotes.setComparator(comparator);

                    stocksTableView.setTableData(quotes);

                    if (selectedStocks.getLength() > 0) {
                        // Select current indexes of selected stocks
                        for (int i = 0, n = selectedStocks.getLength(); i < n; i++) {
                            Object selectedStock = selectedStocks.get(i);

                            int index = 0;
                            for (Object stock : stocksTableView.getTableData()) {
                                String symbol = JSONSerializer.getString(stock, "symbol");
                                String selectedSymbol = JSONSerializer.getString(selectedStock, "symbol");

                                if (symbol.equals(selectedSymbol)) {
                                    stocksTableView.addSelectedIndex(index);
                                    break;
                                }

                                index++;
                            }
                        }
                    } else {
                        if (quotes.getLength() > 0) {
                            stocksTableView.setSelectedIndex(0);
                        }
                    }

                    refreshDetail();

                    DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG,
                        DateFormat.MEDIUM, Locale.getDefault());
                    lastUpdateLabel.setText(dateFormat.format(new Date()));

                    getQuery = null;
                }
            }

            @Override
            public void executeFailed(Task<Object> task) {
                if (task == getQuery) {
                    System.err.println(task.getFault());
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

        if (firstSelectedIndex != -1) {
            int lastSelectedIndex = stocksTableView.getLastSelectedIndex();

            if (firstSelectedIndex == lastSelectedIndex) {
                List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
                stockQuote = tableData.get(firstSelectedIndex);
            } else {
                stockQuote = new StockQuote();
            }
        } else {
            stockQuote = new StockQuote();
        }

        detailPane.load(stockQuote);
    }

    @SuppressWarnings("unchecked")
    private void addSymbol() {
        String symbol = symbolTextInput.getText().toUpperCase();
        if (symbols.indexOf(symbol) == -1) {
            symbols.add(symbol);

            List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
            StockQuote stockQuote = new StockQuote();
            stockQuote.setSymbol(symbol);
            int index = tableData.add(stockQuote);

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
}
