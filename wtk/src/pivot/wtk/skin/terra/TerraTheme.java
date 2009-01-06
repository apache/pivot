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
package pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;
import java.net.URL;

import pivot.collections.List;
import pivot.serialization.JSONSerializer;
import pivot.util.Resources;
import pivot.wtk.Accordion;
import pivot.wtk.Alert;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Border;
import pivot.wtk.Calendar;
import pivot.wtk.CalendarButton;
import pivot.wtk.Checkbox;
import pivot.wtk.Dialog;
import pivot.wtk.Expander;
import pivot.wtk.FlowPane;
import pivot.wtk.Form;
import pivot.wtk.Frame;
import pivot.wtk.Label;
import pivot.wtk.LinkButton;
import pivot.wtk.ListButton;
import pivot.wtk.ListView;
import pivot.wtk.MenuBar;
import pivot.wtk.MenuButton;
import pivot.wtk.MenuPopup;
import pivot.wtk.Menu;
import pivot.wtk.MessageType;
import pivot.wtk.Meter;
import pivot.wtk.Palette;
import pivot.wtk.Panorama;
import pivot.wtk.Prompt;
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Rollup;
import pivot.wtk.ScrollBar;
import pivot.wtk.ScrollPane;
import pivot.wtk.Separator;
import pivot.wtk.Sheet;
import pivot.wtk.Slider;
import pivot.wtk.Spinner;
import pivot.wtk.SplitPane;
import pivot.wtk.TablePane;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TableView;
import pivot.wtk.TabPane;
import pivot.wtk.TextArea;
import pivot.wtk.TextInput;
import pivot.wtk.Theme;
import pivot.wtk.Tooltip;
import pivot.wtk.TreeView;
import pivot.wtk.media.Image;

/**
 * Terra theme.
 *
 * @author gbrown
 * @author tvolkert
 */
public final class TerraTheme extends Theme {
    private String scheme;

    private Resources resources = null;

    private Font font = null;
    private Color[] colors = null;

    public TerraTheme() {
        this("default");
    }

    public TerraTheme(String scheme) {
        if (scheme == null) {
            throw new IllegalArgumentException("scheme is null.");
        }

        this.scheme = scheme;

        componentSkinMap.put(Accordion.class, TerraAccordionSkin.class);
        componentSkinMap.put(Alert.class, TerraAlertSkin.class);
        componentSkinMap.put(Border.class, TerraBorderSkin.class);
        componentSkinMap.put(Checkbox.class, TerraCheckboxSkin.class);
        componentSkinMap.put(Calendar.class, TerraCalendarSkin.class);
        componentSkinMap.put(CalendarButton.class, TerraCalendarButtonSkin.class);
        componentSkinMap.put(Dialog.class, TerraDialogSkin.class);
        componentSkinMap.put(Expander.class, TerraExpanderSkin.class);
        componentSkinMap.put(Form.class, TerraFormSkin.class);
        componentSkinMap.put(FlowPane.class, TerraFlowPaneSkin.class);
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
        componentSkinMap.put(TextArea.class, TerraTextAreaSkin.class);
        componentSkinMap.put(TextInput.class, TerraTextInputSkin.class);
        componentSkinMap.put(Tooltip.class, TerraTooltipSkin.class);
        componentSkinMap.put(TreeView.class, TerraTreeViewSkin.class);

        componentSkinMap.put(TerraCalendarSkin.DateButton.class, TerraCalendarSkin.DateButtonSkin.class);
        componentSkinMap.put(TerraExpanderSkin.ShadeButton.class, TerraExpanderSkin.ShadeButtonSkin.class);
        componentSkinMap.put(TerraFrameSkin.FrameButton.class, TerraFrameSkin.FrameButtonSkin.class);
        componentSkinMap.put(TerraRollupSkin.RollupButton.class, TerraRollupSkin.RollupButtonSkin.class);
        componentSkinMap.put(TerraScrollBarSkin.ScrollButton.class, TerraScrollBarSkin.ScrollButtonSkin.class);
        componentSkinMap.put(TerraScrollBarSkin.ScrollHandle.class, TerraScrollBarSkin.ScrollHandleSkin.class);
        componentSkinMap.put(TerraSliderSkin.Thumb.class, TerraSliderSkin.ThumbSkin.class);
        componentSkinMap.put(TerraSpinnerSkin.SpinButton.class, TerraSpinnerSkin.SpinButtonSkin.class);
        componentSkinMap.put(TerraSpinnerSkin.SpinnerContent.class, TerraSpinnerSkin.SpinnerContentSkin.class);
        componentSkinMap.put(TerraSplitPaneSkin.Splitter.class, TerraSplitPaneSkin.SplitterSkin.class);
        componentSkinMap.put(TerraSplitPaneSkin.SplitterShadow.class, TerraSplitPaneSkin.SplitterShadowSkin.class);
        componentSkinMap.put(TerraTabPaneSkin.TabButton.class, TerraTabPaneSkin.TabButtonSkin.class);
    }

    @SuppressWarnings("unchecked")
    public void install() {
        try {
            String baseName = getClass().getName() + "_" + scheme;
            resources = new Resources(baseName);
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }

        font = Font.decode(JSONSerializer.getString(resources, "font"));

        List<String> colorCodes = (List<String>)JSONSerializer.getList(resources, "colors");
        colors = new Color[colorCodes.getLength() * 3];

        for (int i = 0, n = colorCodes.getLength(); i < n; i++) {
            int baseIndex = i * 3 + 1;
            Color baseColor = Color.decode(colorCodes.get(i));

            colors[baseIndex] = baseColor;
            colors[baseIndex - 1] = darken(baseColor);
            colors[baseIndex + 1] = brighten(baseColor);
        }
    }

    public void uninstall() {
        resources = null;
    }

    public Font getFont() {
        return font;
    }

    public Color getColor(int index) {
        return colors[index];
    }

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
                messageIcon = Image.load(location);
                ApplicationContext.getResourceCache().put(location, messageIcon);
            }
        }

        return messageIcon;
    }

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
                smallMessageIcon = Image.load(location);
                ApplicationContext.getResourceCache().put(location, smallMessageIcon);
            }
        }

        return smallMessageIcon;
    }

    public Resources getResources() {
        return resources;
    }

    public static Color brighten(Color color) {
        return adjustBrightness(color, 0.1f);
    }

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
