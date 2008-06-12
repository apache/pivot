/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import pivot.collections.List;
import pivot.serialization.CSVSerializer;
import pivot.web.GetQuery;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.TableView;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class StockTracker implements Application {
    private Window window = null;

    @SuppressWarnings("unchecked")
    public void startup() throws Exception {
        ApplicationContext.getInstance().setTitle("Stock Tracker Demo");

        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/stocktracker/stocktracker.wtkx");

        TableView stocksTableView = (TableView)componentLoader.getComponent("stocksTableView");

        GetQuery getQuery = new GetQuery("download.finance.yahoo.com", "/d/quotes.csv");
        getQuery.getArguments().put("s", "AAPL,AMZN,EBAY,GOOG,MSFT,VMW");
        getQuery.getArguments().put("f", "sl1c1");

        CSVSerializer quoteSerializer = new CSVSerializer();
        quoteSerializer.getKeys().add(StockQuote.SYMBOL_KEY);
        quoteSerializer.getKeys().add(StockQuote.VALUE_KEY);
        quoteSerializer.getKeys().add(StockQuote.CHANGE_KEY);

        quoteSerializer.setItemClass(StockQuote.class);
        getQuery.setSerializer(quoteSerializer);

        List<Object> quotes = (List<Object>)getQuery.execute();
        stocksTableView.setTableData(quotes);

        window = new Window();
        window.setContent(content);
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE,
            Boolean.TRUE);
        window.open();
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }
}
