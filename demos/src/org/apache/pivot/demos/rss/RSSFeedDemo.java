/*
 * Copyright (c) 2009 VMware, Inc.
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
package org.apache.pivot.demos.rss;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.pivot.collections.Map;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.web.GetQuery;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtkx.WTKXSerializer;
import org.apache.pivot.xml.Element;
import org.apache.pivot.xml.XMLSerializer;

public class RSSFeedDemo implements Application {
    private Window window = null;
    private ListView feedListView = null;
    private CardPane cardPane = null;
    private Label statusLabel = null;

    @Override
    public void startup(Display display, Map<String, String> properties)
        throws Exception {
        WTKXSerializer wtkxSerializer = new WTKXSerializer();
        window = (Window)wtkxSerializer.readObject(this, "rss_feed_demo.wtkx");
        feedListView = (ListView)wtkxSerializer.get("feedListView");
        cardPane = (CardPane)wtkxSerializer.get("cardPane");
        statusLabel = (Label)wtkxSerializer.get("statusLabel");

        feedListView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            private int index = -1;

            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                if (count == 1) {
                    index = feedListView.getItemAt(y);
                } else if (count == 2
                    && feedListView.getItemAt(y) == index) {
                    Element itemElement = (Element)feedListView.getListData().get(index);

                    String link = XMLSerializer.getText(itemElement, "link");
                    Desktop desktop = Desktop.getDesktop();

                    try {
                        desktop.browse(new URL(link).toURI());
                    } catch(MalformedURLException exception) {
                        throw new RuntimeException(exception);
                    } catch(URISyntaxException exception) {
                        throw new RuntimeException(exception);
                    } catch(IOException exception) {
                        System.out.println("Unable to open " + link + " in default browser.");
                    }
                }

                return false;
            }
        });

        GetQuery getQuery = new GetQuery("feeds.dzone.com", "/javalobby/frontpage");
        getQuery.setSerializer(new XMLSerializer());
        getQuery.getParameters().put("format", "xml");

        getQuery.execute(new TaskListener<Object>() {
            @Override
            public void taskExecuted(Task<Object> task) {
                Element root = (Element)task.getResult();
                feedListView.setListData(XMLSerializer.getElements(root, "channel/item"));
                cardPane.setSelectedIndex(1);
            }

            @Override
            public void executeFailed(Task<Object> task) {
                statusLabel.setText(task.getFault().toString());
            }
        });

        window.setMaximized(true);
        window.open(display);
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(RSSFeedDemo.class, args);
    }
}
