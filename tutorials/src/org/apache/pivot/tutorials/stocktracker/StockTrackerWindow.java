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

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BeanAdapter;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.List.ItemIterator;
import org.apache.pivot.collections.Map;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.json.JSON;
import org.apache.pivot.serialization.CSVSerializer;
import org.apache.pivot.util.Resources;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewRowListener;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.TableViewRowComparator;

/**
 * Main Stock Tracker window.
 */
public class StockTrackerWindow extends Window implements Bindable {
    @BXML
    private TableView stocksTableView = null;
    @BXML
    private TextInput symbolTextInput = null;
    @BXML
    private Button addSymbolButton = null;
    @BXML
    private Button removeSymbolsButton = null;
    @BXML
    private BoxPane detailPane = null;
    @BXML
    private Label lastUpdateLabel = null;
    @BXML
    private Button yahooFinanceButton = null;

    private ArrayList<String> symbols;
    private GetQuery getQuery = null;

    // Action invoked to add a new symbol
    private Action addSymbolAction = new Action(false) {
        @Override
        public void perform(Component source) {
            String symbol = symbolTextInput.getText().toUpperCase();
            if (symbols.indexOf(symbol) == -1) {
                symbols.add(symbol);

                @SuppressWarnings("unchecked")
                List<StockQuote> tableData = (List<StockQuote>) stocksTableView.getTableData();
                StockQuote stockQuote = new StockQuote();
                stockQuote.setSymbol(symbol);
                int index = tableData.add(stockQuote);

                stocksTableView.setSelectedIndex(index);
            }

            symbolTextInput.setText("");
            refreshTable();
        }
    };

    // Action invoke to remove selected symbols
    private Action removeSymbolsAction = new Action(false) {
        @Override
        public void perform(Component source) {
            int selectedIndex = stocksTableView.getFirstSelectedIndex();
            ArrayList<Span> spanList = new ArrayList<>(stocksTableView.getSelectedRanges());

            // remove spans in reverse order to prevent
            // IndexOutOfBoundsException
            ItemIterator<Span> it = spanList.iterator();
            it.toEnd();
            while (it.hasPrevious()) {
                Span span = it.previous();
                stocksTableView.getTableData().remove(span.start, (int) span.getLength());
                symbols.remove(span.start, (int) span.getLength());
            }

            if (selectedIndex >= symbols.getLength()) {
                selectedIndex = symbols.getLength() - 1;
            }

            stocksTableView.setSelectedIndex(selectedIndex);

            if (selectedIndex == -1) {
                refreshDetail();
                symbolTextInput.requestFocus();
            }
        }
    };

    // Action invoked to refresh the symbol table view
    private Action refreshTableAction = new Action() {
        @Override
        public void perform(Component source) {
            refreshTable();
        }
    };

    public static final String SERVICE_HOSTNAME = "download.finance.yahoo.com";
    public static final String SERVICE_PATH = "/d/quotes.csv";
    public static final long REFRESH_INTERVAL = 15000;
    public static final String YAHOO_FINANCE_HOME = "http://finance.yahoo.com";

    public StockTrackerWindow() {
        // Create the symbol list
        symbols = new ArrayList<>();

        // Set a comparator on the symbol list so the entries are sorted
        symbols.setComparator(new Comparator<String>() {
            @Override
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        // Insert some initial symbols
        symbols.add("EBAY");
        symbols.add("AAPL");
        symbols.add("MSFT");
        symbols.add("AMZN");
        symbols.add("GOOG");
        symbols.add("ORCL");
        symbols.add("IBM");

        // Add action mapping to refresh the symbol table view
        Keyboard.Modifier commandModifier = Platform.getCommandModifier();
        Keyboard.KeyStroke refreshKeystroke = new Keyboard.KeyStroke(Keyboard.KeyCode.R,
            commandModifier.getMask());
        getActionMappings().add(new ActionMapping(refreshKeystroke, refreshTableAction));
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        // Add stocks table view event handlers
        stocksTableView.getTableViewRowListeners().add(new TableViewRowListener() {
            @Override
            public void rowsSorted(TableView tableView) {
                List<?> tableData = stocksTableView.getTableData();
                if (tableData.getLength() > 0) {
                    stocksTableView.setSelectedIndex(0);
                }
            }
        });

        stocksTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
                @Override
                public void selectedRangesChanged(TableView tableView,
                    Sequence<Span> previousSelectedRanges) {
                    int firstSelectedIndex = stocksTableView.getFirstSelectedIndex();
                    removeSymbolsAction.setEnabled(firstSelectedIndex != -1);

                    refreshDetail();
                }
            });

        stocksTableView.getTableViewSortListeners().add(new TableViewSortListener() {
            @Override
            public void sortChanged(TableView tableView) {
                @SuppressWarnings("unchecked")
                List<Object> tableData = (List<Object>) tableView.getTableData();
                tableData.setComparator(new TableViewRowComparator(tableView));
            }
        });

        stocksTableView.getComponentKeyListeners().add(new ComponentKeyListener() {
            @Override
            public boolean keyPressed(Component component, int keyCode,
                Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE || keyCode == Keyboard.KeyCode.BACKSPACE) {
                    removeSymbolsAction.perform(component);
                } else if (keyCode == Keyboard.KeyCode.A
                    && Keyboard.isPressed(Platform.getCommandModifier())) {
                    stocksTableView.selectAll();
                }

                return false;
            }
        });

        // Add symbol text input event handlers
        symbolTextInput.getTextInputContentListeners().add(new TextInputContentListener() {
            @Override
            public void textChanged(TextInput textInput) {
                addSymbolAction.setEnabled(textInput.getCharacterCount() > 0);
            }
        });

        symbolTextInput.getComponentKeyListeners().add(new ComponentKeyListener() {
            @Override
            public boolean keyPressed(Component component, int keyCode,
                Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    if (addSymbolAction.isEnabled()) {
                        addSymbolAction.perform(component);
                    }
                }

                return false;
            }
        });

        // Assign actions to add and remove symbol buttons
        addSymbolButton.setAction(addSymbolAction);
        removeSymbolsButton.setAction(removeSymbolsAction);

        // Add a button press listener to open the Yahoo! Finance web page when
        // the link is clicked
        yahooFinanceButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                Desktop desktop = Desktop.getDesktop();

                try {
                    desktop.browse(new URL(YAHOO_FINANCE_HOME).toURI());
                } catch (MalformedURLException exception) {
                    throw new RuntimeException(exception);
                } catch (URISyntaxException exception) {
                    throw new RuntimeException(exception);
                } catch (IOException exception) {
                    System.out.println("Unable to open " + YAHOO_FINANCE_HOME
                        + " in default browser.");
                }
            }
        });
    }

    @Override
    public void open(Display display, Window owner) {
        super.open(display, owner);

        ApplicationContext.runAndScheduleRecurringCallback(() -> refreshTable(), REFRESH_INTERVAL);

        symbolTextInput.requestFocus();
    }

    private void refreshTable() {
        // Abort any outstanding query
        if (getQuery != null) {
            synchronized (getQuery) {
                if (getQuery.isPending()) {
                    getQuery.abort();
                }
            }
        }

        // Execute the query
        if (symbols.getLength() > 0) {
            getQuery = new GetQuery(SERVICE_HOSTNAME, SERVICE_PATH);

            StringBuilder symbolsParameterBuilder = new StringBuilder();
            for (int i = 0, n = symbols.getLength(); i < n; i++) {
                if (i > 0) {
                    symbolsParameterBuilder.append(",");
                }

                symbolsParameterBuilder.append(symbols.get(i));
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
            String symbolsParameter = symbolsParameterBuilder.toString();
            getQuery.getParameters().put("s", symbolsParameter);
            getQuery.getParameters().put("f", "snl1ohgc1v");

            CSVSerializer quoteSerializer = new CSVSerializer(StockQuote.class);
            quoteSerializer.setKeys("symbol", "companyName", "value", "openingValue", "highValue",
                "lowValue", "change", "volume");

            getQuery.setSerializer(quoteSerializer);

            getQuery.execute(new TaskAdapter<>(new TaskListener<Object>() {
                @Override
                public void taskExecuted(Task<Object> task) {
                    if (task == getQuery) {
                        @SuppressWarnings("unchecked")
                        List<Object> quotes = (List<Object>) task.getResult();

                        // Preserve any existing sort and selection
                        Sequence<?> selectedStocks = stocksTableView.getSelectedRows();

                        @SuppressWarnings("unchecked")
                        List<Object> tableData = (List<Object>) stocksTableView.getTableData();
                        Comparator<Object> comparator = tableData.getComparator();
                        quotes.setComparator(comparator);

                        stocksTableView.setTableData(quotes);

                        if (selectedStocks.getLength() > 0) {
                            // Select current indexes of selected stocks
                            for (int i = 0, n = selectedStocks.getLength(); i < n; i++) {
                                Object selectedStock = selectedStocks.get(i);

                                int index = 0;
                                for (Object stock : stocksTableView.getTableData()) {
                                    String symbol = JSON.get(stock, "symbol");
                                    String selectedSymbol = JSON.get(selectedStock, "symbol");

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
    }

    private void refreshDetail() {
        StockQuote stockQuote = null;

        int firstSelectedIndex = stocksTableView.getFirstSelectedIndex();
        if (firstSelectedIndex != -1) {
            int lastSelectedIndex = stocksTableView.getLastSelectedIndex();

            if (firstSelectedIndex == lastSelectedIndex) {
                @SuppressWarnings("unchecked")
                List<StockQuote> tableData = (List<StockQuote>) stocksTableView.getTableData();
                stockQuote = tableData.get(firstSelectedIndex);
            } else {
                stockQuote = new StockQuote();
            }
        } else {
            stockQuote = new StockQuote();
        }

        detailPane.load(new BeanAdapter(stockQuote));
    }
}
