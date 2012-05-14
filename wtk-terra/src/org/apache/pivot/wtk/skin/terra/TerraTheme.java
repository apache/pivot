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
import java.util.Locale;

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
import org.apache.pivot.wtk.Component;
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
import org.apache.pivot.wtk.Panel;
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
import org.apache.pivot.wtk.TextPane;
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
    private int numberOfPaletteColors = 0;
    private HashMap<MessageType, Image> messageIcons = null;
    private HashMap<MessageType, Image> smallMessageIcons = null;

    private static float colorMultiplier = 0.1f;

    public static final String LOCATION_PROPERTY = "location";
    public static final String COMMAND_BUTTON_STYLE = "commandButton";

    @SuppressWarnings("unchecked")
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
        componentSkinMap.put(Panel.class, TerraPanelSkin.class);
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
        componentSkinMap.put(TextPane.class, TerraTextPaneSkin.class);
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

        String packageName = getClass().getPackage().getName();

        // Load the color scheme
        String location = null;
        try {
            String locationKey = getClass().getPackage().getName() + "." + LOCATION_PROPERTY;
            location = System.getProperty(locationKey);
        } catch (SecurityException exception) {
            // No-op
        }


        URL locationURL;
        if (location == null) {
            locationURL = getClass().getResource("TerraTheme_default.json");
        } else {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

            if (location.startsWith("/")) {
                locationURL = classLoader.getResource(location.substring(1));
            } else {
                locationURL = classLoader.getResource(packageName.replace('.', '/') + "/" + location);
            }
        }

        if (locationURL == null) {
            throw new RuntimeException("Unable to locate color scheme resource \"" + location + "\".");
        }

        load(locationURL);

        // Install named styles
        try {
            InputStream inputStream = getClass().getResourceAsStream("terra_theme_styles.json");

            try {
                JSONSerializer serializer = new JSONSerializer();
                Map<String, ?> terraThemeStyles = (Map<String, ?>)serializer.readObject(inputStream);

                for (String name : terraThemeStyles) {
                    Component.getNamedStyles().put(packageName + "." + name, (Map<String, ?>)terraThemeStyles.get(name));
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @SuppressWarnings("unchecked")
    private void load(URL location) {
        if (location == null) {
            throw new IllegalArgumentException("location URL is null");
        }

        try {
            InputStream inputStream = location.openStream();

            try {
                JSONSerializer serializer = new JSONSerializer();
                Map<String, ?> properties = (Map<String, ?>)serializer.readObject(inputStream);

                font = Font.decode((String)properties.get("font"));

                List<String> colorCodes = (List<String>)properties.get("colors");
                numberOfPaletteColors = colorCodes.getLength();
                int numberOfColors = numberOfPaletteColors * 3;
                colors = new ArrayList<Color>(numberOfColors);

                colorMultiplier = ((Double)properties.get("colorMultiplier")).floatValue();

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
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void loadMessageIcons(Map<String, String> messageIconNames,
        HashMap<MessageType, Image> messageIconsLocal) {
        for (String messageIconType : messageIconNames) {
            String messageIconName = messageIconNames.get(messageIconType);

            Image messageIcon;
            try {
                messageIcon = Image.load(getClass().getResource(messageIconName));
            } catch (TaskExecutionException exception) {
                throw new RuntimeException(exception);
            }

            messageIconsLocal.put(MessageType.valueOf(messageIconType.toUpperCase(Locale.ENGLISH)), messageIcon);
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
        if (font == null) {
            throw new IllegalArgumentException();
        }

        this.font = font;
    }

    /**
     * Gets a value from the theme's color palette.
     *
     * @param index
     */
    public Color getColor(int index) {
        return colors.get(index);
    }

    /**
     * Sets a value in the theme's color palette.
     *
     * @param index
     * @param color
     */
    public void setColor(int index, Color color) {
        if (color == null) {
            throw new IllegalArgumentException();
        }

        colors.update(index, color);
    }

    /**
     * Gets a base color from the theme's color palette.
     *
     * @param index
     */
    public Color getBaseColor(int index) {
        return colors.get(index * 3 + 1);
    }

    /**
     * Sets a base color in the theme's color palette.
     *
     * @param index
     * @param baseColor
     */
    public void setBaseColor(int index, Color baseColor) {
        if (baseColor == null) {
            throw new IllegalArgumentException();
        }

        int offset = index * 3;
        colors.update(offset, darken(baseColor));
        colors.update(offset + 1, baseColor);
        colors.update(offset + 2, brighten(baseColor));
    }

    /**
     * Gets the number of Palette Colors
     * @return the number
     */
    // @Override  // TODO: re-enable this override for PIVOT-689
    public int getNumberOfPaletteColors() {
        return numberOfPaletteColors;
    }

    /**
     * Gets the total number of Colors
     * @return the number
     */
    // @Override  // TODO: re-enable this override for PIVOT-689
    public int getNumberOfColors() {
        return colors == null ? 0 : colors.getLength();
    }

    /**
     * Gets the image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType
     */
    public Image getMessageIcon(MessageType messageType) {
        return messageIcons.get(messageType);
    }

    /**
     * Sets the image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType
     * @param messageIcon
     */
    public void setMessageIcon(MessageType messageType, Image messageIcon) {
        if (messageType == null
            || messageIcon == null) {
            throw new IllegalArgumentException();
        }

        messageIcons.put(messageType, messageIcon);
    }

    /**
     * Gets the small image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType
     */
    public Image getSmallMessageIcon(MessageType messageType) {
        return smallMessageIcons.get(messageType);
    }

    /**
     * Sets the small image that this theme uses to represent messages of the
     * specified type.
     *
     * @param messageType
     * @param smallMessageIcon
     */
    public void setSmallMessageIcon(MessageType messageType, Image smallMessageIcon) {
        if (messageType == null
            || smallMessageIcon == null) {
            throw new IllegalArgumentException();
        }

        smallMessageIcons.put(messageType, smallMessageIcon);
    }

    /**
     * Returns a brighter version of the specified color. Specifically, it
     * increases the brightness (in the HSB color model) by the
     * <tt>colorMultiplier</tt> factor already set.
     */
    public static Color brighten(Color color) {
        return adjustBrightness(color, colorMultiplier);
    }

    /**
     * Returns a darker version of the specified color. Specifically, it
     * decreases the brightness (in the HSB color model) by the
     * <tt>colorMultiplier</tt> factor already set.
     */
    public static Color darken(Color color) {
        return adjustBrightness(color, (colorMultiplier * -1.0f));
    }

    /**
     * Returns a brighter version of the specified color. Specifically, it
     * increases the brightness (in the HSB color model) by the given
     * <tt>adjustment</tt> factor (usually in the range ]0 .. 1[).
     */
    public static Color brighten(Color color, float adjustment) {
        return adjustBrightness(color, adjustment);
    }

    /**
     * Returns a darker version of the specified color. Specifically, it
     * decreases the brightness (in the HSB color model) by the given
     * <tt>adjustment</tt> factor (usually in the range ]0 .. 1[).
     */
    public static Color darken(Color color, float adjustment) {
        return adjustBrightness(color, (adjustment * -1.0f));
    }

    private static Color adjustBrightness(Color color, float adjustment) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[2] = Math.min(Math.max(hsb[2] + adjustment, 0f), 1f);
        int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
        return new Color((color.getAlpha() << 24) | (rgb & 0xffffff), true);
    }

}
