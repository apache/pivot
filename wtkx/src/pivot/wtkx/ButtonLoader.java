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

import pivot.collections.HashMap;
import pivot.collections.Map;
import pivot.serialization.JSONSerializer;
import pivot.wtk.Button;
import pivot.wtk.Component;

abstract class ButtonLoader extends Loader {
    public static final String BUTTON_DATA_ATTRIBUTE = "buttonData";
    public static final String TOGGLE_BUTTON_ATTRIBUTE = "toggleButton";
    public static final String TRI_STATE_ATTRIBUTE = "triState";
    public static final String GROUP_ATTRIBUTE = "group";
    public static final String STATE_ATTRIBUTE = "state";
    public static final String SELECTED_ATTRIBUTE = "selected";
    public static final String DATA_RENDERER_ATTRIBUTE = "dataRenderer";
    public static final String DATA_RENDERER_STYLES_ATTRIBUTE = "dataRendererStyles";
    public static final String DATA_RENDERER_PROPERTIES_ATTRIBUTE = "dataRendererProperties";
    public static final String SELECTED_KEY_ATTRIBUTE = "selectedKey";
    public static final String STATE_KEY_ATTRIBUTE = "stateKey";

    private String groupName = null;
    private static HashMap<String, Button.Group> groups = new HashMap<String, Button.Group>();

    protected abstract Button createButton();

    @Override
    @SuppressWarnings("unchecked")
    protected Component load(Element element, ComponentLoader rootLoader)
        throws LoadException {
        Button button = createButton();

        if (element.hasAttribute(BUTTON_DATA_ATTRIBUTE)) {
            String buttonDataAttribute = element.getAttribute(BUTTON_DATA_ATTRIBUTE);

            StringReader buttonDataReader = null;
            try {
                buttonDataReader = new StringReader(buttonDataAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                button.setButtonData(rootLoader.resolve(jsonSerializer.readObject(buttonDataReader)));
            } catch(Exception exception) {
                throw new LoadException("Unable to set button data.", exception);
            } finally {
                if (buttonDataReader != null) {
                    buttonDataReader.close();
                }
            }
        }

        if (element.hasAttribute(TOGGLE_BUTTON_ATTRIBUTE)) {
            boolean toggleButton = Boolean.parseBoolean(element.getAttribute(TOGGLE_BUTTON_ATTRIBUTE));
            button.setToggleButton(toggleButton);
        }

        if (element.hasAttribute(TRI_STATE_ATTRIBUTE)) {
            boolean triState = Boolean.parseBoolean(element.getAttribute(TRI_STATE_ATTRIBUTE));
            button.setTriState(triState);
        }

        if (element.hasAttribute(GROUP_ATTRIBUTE)) {
            groupName = element.getAttribute(GROUP_ATTRIBUTE);

            String absoluteNamespace = rootLoader.getQualifiedNamepsace();
            if (absoluteNamespace != null) {
                groupName = absoluteNamespace + "$" + groupName;
            }

            Button.Group group = groups.get(groupName);
            if (group == null) {
                group = new Button.Group();
                groups.put(groupName, group);
            }

            button.setGroup(group);
        }

        if (element.hasAttribute(DATA_RENDERER_ATTRIBUTE)) {
            String dataRendererAttribute = element.getAttribute(DATA_RENDERER_ATTRIBUTE);

            try {
                Class<?> dataRendererClass = Class.forName(dataRendererAttribute);
                button.setDataRenderer((Button.DataRenderer)dataRendererClass.newInstance());
            } catch(Exception exception) {
                throw new LoadException("Unable to install data renderer.", exception);
            }
        }

        Button.DataRenderer dataRenderer = button.getDataRenderer();

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

            StringReader itemRendererPropertiesReader = null;
            try {
                itemRendererPropertiesReader = new StringReader(dataRendererPropertiesAttribute);
                JSONSerializer jsonSerializer = new JSONSerializer();
                Map<String, Object> properties =
                    (Map<String, Object>)jsonSerializer.readObject(itemRendererPropertiesReader);

                for (String key : properties) {
                    dataRenderer.getProperties().put(key, properties.get(key));
                }
            } catch(Exception exception) {
                throw new LoadException("Unable to apply item renderer properties.", exception);
            } finally {
                if (itemRendererPropertiesReader != null) {
                    itemRendererPropertiesReader.close();
                }
            }
        }

        if (element.hasAttribute(STATE_ATTRIBUTE)) {
            String stateAttribute = element.getAttribute(STATE_ATTRIBUTE);
            button.setState(Button.State.decode(stateAttribute));
        } else {
            if (element.hasAttribute(SELECTED_ATTRIBUTE)) {
                boolean selected = Boolean.parseBoolean(element.getAttribute(SELECTED_ATTRIBUTE));
                button.setSelected(selected);
            }
        }

        if (element.hasAttribute(SELECTED_KEY_ATTRIBUTE)) {
            String selectedKey = element.getAttribute(SELECTED_KEY_ATTRIBUTE);
            button.setSelectedKey(selectedKey);
        }

        if (element.hasAttribute(STATE_KEY_ATTRIBUTE)) {
            String stateKey = element.getAttribute(STATE_KEY_ATTRIBUTE);
            button.setStateKey(stateKey);
        }

        return button;
    }

    @Override
    public void finalize()
        throws Throwable {
        try {
            if (groupName != null) {
                if (groups.containsKey(groupName)) {
                    System.out.println("Removing " + groupName);
                    groups.remove(groupName);
                }
            }
        } finally {
            super.finalize();
        }
    }
}
