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
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;

import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.serialization.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Accordion;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Calendar;
import org.apache.pivot.wtk.CalendarButton;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Dialog;
import org.apache.pivot.wtk.Expander;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FileBrowserSheet;
import org.apache.pivot.wtk.Form;
import org.apache.pivot.wtk.Frame;
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
import org.apache.pivot.wtk.TabPane;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewHeader;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.Tooltip;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.media.Image;


/**
 * Terra theme. The default color palette is shown below:
 * <p>
 *   <a name="palette"/>
 *   <img src="doc-files/palette.png" border="0"/>
 *   <br/>
 *   <font color="#000000" size="-1" face="arial,helvetica,sanserif">
 *     <i>The default color palette</i>
 *   </font>
 * </p>
 */
public final class TerraTheme extends Theme {
    private Font font = null;
    private Color[] colors = null;

    /**
     * Creates a new theme using the default font and color palette.
     */
    public TerraTheme() {
        this(TerraTheme.class.getResource("TerraTheme_default.json"));
    }

    /**
     * Constructs a theme, pulling the font and color palette from a JSON file
     * at the specified location. The JSON file should represent a <tt>Map</tt>
     * containing the following properties:
     * <p>
     * <table border="1" cellpadding="5">
     *   <tr>
     *     <th nowrap="nowrap">Property:</th>
     *     <th nowrap="nowrap">Type:</th>
     *     <th nowrap="nowrap">Description:</th>
     *   </tr>
     *   <tr valign="top">
     *     <td><tt>font</tt></td>
     *     <td><tt>String</tt></td>
     *     <td>
     *       The default theme font; must be understandable by
     *       <tt>java.awt.Font.decode()</tt>.
     *     </td>
     *   </tr>
     *   <tr valign="top">
     *     <td><tt>colors</tt></td>
     *     <td><tt>List&lt;String&gt;</tt></td>
     *     <td>
     *       This list should contain 8 colors in a form understandable by
     *       <tt>java.awt.Color.decode()</tt>. This list represents the theme's
     *       "base color palette", from which the full color palette is
     *       derived. Each of these 8 colors will be expanded to comprise 3
     *       colors in the final palette: a darker version, the color itself,
     *       and a lighter version. Thus, the final color palette will contain
     *       24 colors. For instance, in the <a href="#palette">default color
     *       palette</a> the "base palette" colors are the colors in the middle
     *       column.
     *     </td>
     *   </tr>
     * </table>
     *
     * @param location
     * The location of the JSON file that defines the theme's font and colors.
     */
    public TerraTheme(URL location) {
        if (location == null) {
            throw new IllegalArgumentException("location is null.");
        }

        componentSkinMap.put(Accordion.class, TerraAccordionSkin.class);
        componentSkinMap.put(ActivityIndicator.class, TerraActivityIndicatorSkin.class);
        componentSkinMap.put(Alert.class, TerraAlertSkin.class);
        componentSkinMap.put(Border.class, TerraBorderSkin.class);
        componentSkinMap.put(Checkbox.class, TerraCheckboxSkin.class);
        componentSkinMap.put(Calendar.class, TerraCalendarSkin.class);
        componentSkinMap.put(CalendarButton.class, TerraCalendarButtonSkin.class);
        componentSkinMap.put(Dialog.class, TerraDialogSkin.class);
        componentSkinMap.put(Expander.class, TerraExpanderSkin.class);
        componentSkinMap.put(FileBrowser.class, TerraFileBrowserSkin.class);
        componentSkinMap.put(FileBrowserSheet.class, TerraFileBrowserSheetSkin.class);
        componentSkinMap.put(Form.class, TerraFormSkin.class);
        componentSkinMap.put(BoxPane.class, TerraBoxPaneSkin.class);
        componentSkinMap.put(Frame.class, TerraFrameSkin.class);
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
        componentSkinMap.put(TablePane.class, TerraTablePaneSkin.class);
        componentSkinMap.put(TableViewHeader.class, TerraTableViewHeaderSkin.class);
        componentSkinMap.put(TableView.class, TerraTableViewSkin.class);
        componentSkinMap.put(TabPane.class, TerraTabPaneSkin.class);
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

        loadScheme(location);
    }

    @SuppressWarnings("unchecked")
    public void loadScheme(URL location) {
        try {
            InputStream inputStream = location.openStream();

            try {
                JSONSerializer serializer = new JSONSerializer();
                Map<String, ?> properties = (Map<String, ?>)serializer.readObject(inputStream);

                font = Font.decode((String)properties.get("font"));

                List<String> colorCodes = (List<String>)properties.get("colors");
                colors = new Color[colorCodes.getLength() * 3];

                for (int i = 0, n = colorCodes.getLength(); i < n; i++) {
                    int baseIndex = i * 3 + 1;
                    Color baseColor = Color.decode(colorCodes.get(i));

                    colors[baseIndex] = baseColor;
                    colors[baseIndex - 1] = darken(baseColor);
                    colors[baseIndex + 1] = brighten(baseColor);
                }
            } finally {
                inputStream.close();
            }
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } catch (SerializationException ex) {
            throw new RuntimeException(ex);
        }
    }

    protected void install() {
    }

    protected void uninstall() {
    }

    /**
     * Gets the theme's font.
     */
    public Font getFont() {
        return font;
    }

    /**
     * Gets the color found at the specified index in the theme's color
     * palette.
     *
     * @param index
     * A color palette index, from 0 to 23.
     */
    public Color getColor(int index) {
        return colors[index];
    }

    /**
     * Gets the image that this theme uses to represent messages of the
     * specified type.
     */
    public Image getMessageIcon(MessageType messageType) {
        String messageIconName;

        switch (messageType) {
            case ERROR: {
                messageIconName = "message_type-error-32x32.png";
                break;
            }

            case WARNING: {
                messageIconName = "message_type-warning-32x32.png";
                break;
            }

            case QUESTION: {
                messageIconName = "message_type-question-32x32.png";
                break;
            }

            case INFO: {
                messageIconName = "message_type-info-32x32.png";
                break;
            }

            case APPLICATION: {
                messageIconName = null;
                break;
            }

            default: {
                throw new IllegalArgumentException();
            }
        }

        Image messageIcon = null;

        if (messageIconName != null) {
            URL location = getClass().getResource(messageIconName);
            messageIcon = (Image)ApplicationContext.getResourceCache().get(location);

            if (messageIcon == null) {
                try {
                    messageIcon = Image.load(location);
                } catch (TaskExecutionException exception) {
                    throw new RuntimeException(exception);
                }

                ApplicationContext.getResourceCache().put(location, messageIcon);
            }
        }

        return messageIcon;
    }

    /**
     * Gets the "small" image that this theme uses to represent messages of the
     * specified type.
     */
    public Image getSmallMessageIcon(MessageType messageType) {
        String smallMessageIconName;

        switch (messageType) {
            case ERROR: {
                smallMessageIconName = "message_type-error-16x16.png";
                break;
            }

            case WARNING: {
                smallMessageIconName = "message_type-warning-16x16.png";
                break;
            }

            case QUESTION: {
                smallMessageIconName = "message_type-question-16x16.png";
                break;
            }

            case INFO: {
                smallMessageIconName = "message_type-info-16x16.png";
                break;
            }

            case APPLICATION: {
                smallMessageIconName = null;
                break;
            }

            default: {
                throw new IllegalArgumentException();
            }
        }

        Image smallMessageIcon = null;

        if (smallMessageIconName != null) {
            URL location = getClass().getResource(smallMessageIconName);
            smallMessageIcon = (Image)ApplicationContext.getResourceCache().get(location);

            if (smallMessageIcon == null) {
                try {
                    smallMessageIcon = Image.load(location);
                } catch (TaskExecutionException exception) {
                    throw new RuntimeException(exception);
                }

                ApplicationContext.getResourceCache().put(location, smallMessageIcon);
            }
        }

        return smallMessageIcon;
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
        return new Color((color.getAlpha() << 24) | (rgb & 0xffffff));
    }
}
