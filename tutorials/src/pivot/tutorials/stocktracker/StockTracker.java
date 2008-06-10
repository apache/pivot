package pivot.tutorials.stocktracker;

import pivot.collections.List;
import pivot.serialization.CSVSerializer;
import pivot.web.GetQuery;
import pivot.wtk.Application;
import pivot.wtk.Component;
import pivot.wtk.Display;
import pivot.wtk.TableView;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class StockTracker implements Application {
    private Window window = null;

    @SuppressWarnings("unchecked")
    public void startup() throws Exception {
        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader();

        Component content =
            componentLoader.load("pivot/tutorials/stocktracker/stocktracker.wtkx");

        TableView stocksTableView = (TableView)componentLoader.getComponent("stocksTableView");

        GetQuery getQuery = new GetQuery("download.finance.yahoo.com", "/d/quotes.csv");
        getQuery.getArguments().put("s", "AAPL,EBAY,GOOG,AMZN,VMW,MSFT");
        getQuery.getArguments().put("f", "sl1c1");

        CSVSerializer quoteSerializer = new CSVSerializer();
        quoteSerializer.getKeys().add("symbol");
        quoteSerializer.getKeys().add("value");
        quoteSerializer.getKeys().add("change");
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
