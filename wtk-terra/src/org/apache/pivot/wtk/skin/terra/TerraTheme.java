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
package org.apache.pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Calendar;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.ColorChooser;
import org.apache.pivot.wtk.ColorChooserButton;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Expander;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Frame;
import org.apache.pivot.wtk.GridPane;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.LinkButton;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuBar;
import org.apache.pivot.wtk.MenuButton;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Meter;
import org.apache.pivot.wtk.Palette;
import org.apache.pivot.wtk.Panorama;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.ScrollBar;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.Separator;
import org.apache.pivot.wtk.Sheet;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SplitPane;
import org.apache.pivot.wtk.SuggestionPopup;
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TextArea;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.media.Image;

/**
 * Terra theme.
 */
public final class TerraTheme extends Theme {
    private Font font = null;
    private ArrayList<Color> colors = null;
    private HashMap<MessageType, Image> messageIcons = null;
    private HashMap<MessageType, Image> smallMessageIcons = null;

    public static final String LOCATION_PROPERTY = "org.apache.pivot.wtk.skin.terra.location";

    public TerraTheme() {
        componentSkinMap.put(Accordion.class, TerraAccordionSkin.class);
        componentSkinMap.put(ActivityIndicator.class, TerraActivityIndicatorSkin.class);
        componentSkinMap.put(Alert.class, TerraAlertSkin.class);
        componentSkinMap.put(Border.class, TerraBorderSkin.class);
        componentSkinMap.put(Checkbox.class, TerraCheckboxSkin.class);
        componentSkinMap.put(Calendar.class, TerraCalendarSkin.class);
        componentSkinMap.put(CalendarButton.class, TerraCalendarButtonSkin.class);
        componentSkinMap.put(ColorChooser.class, TerraColorChooserSkin.class);
        componentSkinMap.put(ColorChooserButton.class, TerraColorChooserButtonSkin.class);
        componentSkinMap.put(Dialog.class, TerraDialogSkin.class);
        componentSkinMap.put(Expander.class, TerraExpanderSkin.class);
        componentSkinMap.put(FileBrowser.class, TerraFileBrowserSkin.class);
        componentSkinMap.put(FileBrowserSheet.class, TerraFileBrowserSheetSkin.class);
        componentSkinMap.put(Form.class, TerraFormSkin.class);
        componentSkinMap.put(BoxPane.class, TerraBoxPaneSkin.class);
        componentSkinMap.put(Frame.class, TerraFrameSkin.class);
        componentSkinMap.put(GridPane.class, TerraGridPaneSkin.class);
        componentSkinMap.put(Label.class, TerraLabelSkin.class);
        componentSkinMap.put(LinkButton.class, TerraLinkButtonSkin.class);
        componentSkinMap.put(ListButton.class, TerraListButtonSkin.class);
        componentSkinMap.put(ListView.class, TerraListViewSkin.class);
        componentSkinMap.put(Menu.class, TerraMenuSkin.class);
        componentSkinMap.put(Menu.Item.class, TerraMenuItemSkin.class);
        componentSkinMap.put(MenuBar.class, TerraMenuBarSkin.class);
        componentSkinMap.put(MenuBar.Item.class, TerraMenuBarItemSkin.class);
        componentSkinMap.put(MenuButton.class, TerraMenuButtonSkin.class);
        componentSkinMap.put(MenuPopup.class, TerraMenuPopupSkin.class);
        componentSkinMap.put(Meter.class, TerraMeterSkin.class);
        componentSkinMap.put(Palette.class, TerraPaletteSkin.class);
        componentSkinMap.put(Panorama.class, TerraPanoramaSkin.class);
        componentSkinMap.put(Prompt.class, TerraPromptSkin.class);
        componentSkinMap.put(PushButton.class, TerraPushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, TerraRadioButtonSkin.class);
        componentSkinMap.put(Rollup.class, TerraRollupSkin.class);
        componentSkinMap.put(ScrollBar.class, TerraScrollBarSkin.class);
        componentSkinMap.put(ScrollPane.class, TerraScrollPaneSkin.class);
        componentSkinMap.put(ScrollPane.Corner.class, TerraScrollPaneCornerSkin.class);
        componentSkinMap.put(Separator.class, TerraSeparatorSkin.class);
        componentSkinMap.put(Sheet.class, TerraSheetSkin.class);
        componentSkinMap.put(Slider.class, TerraSliderSkin.class);
        componentSkinMap.put(Spinner.class, TerraSpinnerSkin.class);
        componentSkinMap.put(SplitPane.class, TerraSplitPaneSkin.class);
        componentSkinMap.put(SuggestionPopup.class, TerraSuggestionPopupSkin.class);
        componentSkinMap.put(TablePane.class, TerraTablePaneSkin.class);
        componentSkinMap.put(TableViewHeader.class, TerraTableViewHeaderSkin.class);
        componentSkinMap.put(TableView.class, TerraTableViewSkin.class);
        componentSkinMap.put(TabPane.class, TerraTabPaneSkin.class);
        componentSkinMap.put(TextArea.class, TerraTextAreaSkin.class);
        componentSkinMap.put(TextInput.class, TerraTextInputSkin.class);
        componentSkinMap.put(Tooltip.class, TerraTooltipSkin.class);
        componentSkinMap.put(TreeView.class, TerraTreeViewSkin.class);

        componentSkinMap.put(TerraCalendarSkin.DateButton.class, TerraCalendarSkin.DateButtonSkin.class);
        componentSkinMap.put(TerraExpanderSkin.ShadeButton.class, TerraExpanderSkin.ShadeButtonSkin.class);
        componentSkinMap.put(TerraFrameSkin.FrameButton.class, TerraFrameSkin.FrameButtonSkin.class);
        componentSkinMap.put(TerraRollupSkin.RollupButton.class, TerraRollupSkin.RollupButtonSkin.class);
        componentSkinMap.put(TerraScrollBarSkin.ScrollButton.class, TerraScrollBarSkin.ScrollButtonSkin.class);
        componentSkinMap.put(TerraScrollBarSkin.Handle.class, TerraScrollBarSkin.HandleSkin.class);
        componentSkinMap.put(TerraSliderSkin.Thumb.class, TerraSliderSkin.ThumbSkin.class);
        componentSkinMap.put(TerraSpinnerSkin.SpinButton.class, TerraSpinnerSkin.SpinButtonSkin.class);
        componentSkinMap.put(TerraSpinnerSkin.SpinnerContent.class, TerraSpinnerSkin.SpinnerContentSkin.class);
        componentSkinMap.put(TerraSplitPaneSkin.Splitter.class, TerraSplitPaneSkin.SplitterSkin.class);
        componentSkinMap.put(TerraSplitPaneSkin.SplitterShadow.class, TerraSplitPaneSkin.SplitterShadowSkin.class);
        componentSkinMap.put(TerraTabPaneSkin.TabButton.class, TerraTabPaneSkin.TabButtonSkin.class);

        String location = null;

        try {
            location = System.getProperty(LOCATION_PROPERTY);
        } catch (SecurityException exception) {
            // No-op
        }

        if (location == null) {
            load(getClass().getResource("TerraTheme_default.json"));
        } else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            load(classLoader.getResource(location.substring(1)));
        }
    }

    @SuppressWarnings("unchecked")
    private void load(URL location) {
        try {
            InputStream inputStream = location.openStream();

            try {
                JSONSerializer serializer = new JSONSerializer();
                Map<String, ?> properties = (Map<String, ?>)serializer.readObject(inputStream);

                font = Font.decode((String)properties.get("font"));

                List<String> colorCodes = (List<String>)properties.get("colors");
                colors = new ArrayList<Color>(colorCodes.getLength() * 3);

                for (String colorCode : colorCodes) {
                    Color baseColor = Color.decode(colorCode);
                    colors.add(darken(baseColor));
                    colors.add(baseColor);
                    colors.add(brighten(baseColor));
                }

                Map<String, String> messageIconNames =
                    (Map<String, String>)properties.get("messageIcons");
                messageIcons = new HashMap<MessageType, Image>();
                loadMessageIcons(messageIconNames, messageIcons);

                Map<String, String> smallMessageIconNames =
                    (Map<String, String>)properties.get("smallMessageIcons");
                smallMessageIcons = new HashMap<MessageType, Image>();
                loadMessageIcons(smallMessageIconNames, smallMessageIcons);
            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (SerializationException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void loadMessageIcons(Map<String, String> messageIconNames,
        HashMap<MessageType, Image> messageIcons) {
        for (String messageIconType : messageIconNames) {
            String messageIconName = messageIconNames.get(messageIconType);

            Image messageIcon;
            try {
                messageIcon = Image.load(getClass().getResource(messageIconName));
            } catch (TaskExecutionException exception) {
                throw new RuntimeException(exception);
            }

            messageIcons.put(MessageType.valueOf(messageIconType.toUpperCase()), messageIcon);
        }
    }

    /**
     * Gets the theme's font.
     */
    @Override
    public Font getFont() {
        return font;
    }

    /**
     * Sets the theme's font.
     */
    @Override
    public void setFont(Font font) {
        this.font = font;
    }

    /**
     * Gets the color found at the specified index in the theme's color
     * palette.
     *
     * @param index
     * A color palette index, from 0 to 23.
     */
    public Color getColor(int index) {
        return colors.get(index);
    }

    /**
     * Gets the image that this theme uses to represent messages of the
     * specified type.
     */
    public Image getMessageIcon(MessageType messageType) {
        return messageIcons.get(messageType);
    }

    /**
     * Gets the "small" image that this theme uses to represent messages of the
     * specified type.
     */
    public Image getSmallMessageIcon(MessageType messageType) {
        return smallMessageIcons.get(messageType);
    }

    /**
     * Returns a brighter version of the specified color. Specifically, it
     * increases the brightness (in the HSB color model) by <tt>0.1</tt>.
     */
    public static Color brighten(Color color) {
        return adjustBrightness(color, 0.1f);
    }

    /**
     * Returns a darker version of the specified color. Specifically, it
     * decreases the brightness (in the HSB color model) by <tt>0.1</tt>.
     */
    public static Color darken(Color color) {
        return adjustBrightness(color, -0.1f);
    }

    private static Color adjustBrightness(Color color, float adjustment) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[2] = Math.min(Math.max(hsb[2] + adjustment, 0f), 1f);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color((color.getAlpha() << 24) | (rgb & 0xffffff), true);
    }
}
