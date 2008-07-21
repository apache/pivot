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
package pivot.wtkx;

import java.io.StringReader;

import org.w3c.dom.Element;

import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Component;
import pivot.wtk.TableView;
import pivot.wtk.TableViewHeader;

class TableViewHeaderLoader extends Loader {
    public static final String TABLE_VIEW_HEADER_TAG = "TableViewHeader";
    public static final String TABLE_VIEW_ID_ATTRIBUTE = "tableViewID";

    public static final String DATA_RENDERER_ATTRIBUTE = "dataRenderer";
    public static final String DATA_RENDERER_STYLES_ATTRIBUTE = "dataRendererStyles";
    public static final String DATA_RENDERER_PROPERTIES_ATTRIBUTE = "dataRendererProperties";

    @Override
    @SuppressWarnings("unchecked")
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        if (!element.hasAttribute(TABLE_VIEW_ID_ATTRIBUTE)) {
            throw new LoadException("Table view header must specify a table view ID.");
        }

        String tableViewID = element.getAttribute(TABLE_VIEW_ID_ATTRIBUTE);
        TableView tableView = (TableView)rootLoader.getComponent(tableViewID);
        TableViewHeader tableViewHeader = new TableViewHeader(tableView);

        if (element.hasAttribute(DATA_RENDERER_ATTRIBUTE)) {
            String dataRendererAttribute = element.getAttribute(DATA_RENDERER_ATTRIBUTE);

            try {
                Class<?> dataRendererClass = Class.forName(dataRendererAttribute);
                tableViewHeader.setDataRenderer((TableViewHeader.DataRenderer)dataRendererClass.newInstance());
            } catch(Exception exception) {
                throw new LoadException("Unable to install data renderer.", exception);
            }
        }

        TableViewHeader.DataRenderer dataRenderer = tableViewHeader.getDataRenderer();

        if (element.hasAttribute(DATA_RENDERER_STYLES_ATTRIBUTE)) {
            String dataRendererStylesAttribute = element.getAttribute(DATA_RENDERER_STYLES_ATTRIBUTE);

            StringReader dataRendererStylesReader = null;
            try {
                dataRendererStylesReader = new StringReader(dataRendererStylesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                Map<String, Object> styles =
                    (Map<String, Object>)jsonSerializer.readObject(dataRendererStylesReader);

                for (String key : styles) {
                    dataRenderer.getStyles().put(key, styles.get(key));
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to apply data renderer styles.", exception);
            } finally {
                if (dataRendererStylesReader != null) {
                    dataRendererStylesReader.close();
                }
            }
        }

        if (element.hasAttribute(DATA_RENDERER_PROPERTIES_ATTRIBUTE)) {
            String dataRendererPropertiesAttribute =
                element.getAttribute(DATA_RENDERER_PROPERTIES_ATTRIBUTE);

            StringReader dataRendererPropertiesReader = null;
            try {
                dataRendererPropertiesReader = new StringReader(dataRendererPropertiesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                Map<String, Object> properties =
                    (Map<String, Object>)jsonSerializer.readObject(dataRendererPropertiesReader);

                for (String key : properties) {
                    dataRenderer.put(key, properties.get(key));
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to apply data renderer properties.", exception);
            } finally {
                if (dataRendererPropertiesReader != null) {
                    dataRendererPropertiesReader.close();
                }
            }
        }

        return tableViewHeader;
    }

}
