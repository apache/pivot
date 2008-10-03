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

import pivot.serialization.JSONSerializer;
import pivot.util.Resources;
import pivot.wtk.Alert;
import pivot.wtk.Calendar;
import pivot.wtk.Checkbox;
import pivot.wtk.Dialog;
import pivot.wtk.Expander;
import pivot.wtk.Form;
import pivot.wtk.Frame;
import pivot.wtk.LinkButton;
import pivot.wtk.ListButton;
import pivot.wtk.ListView;
import pivot.wtk.MenuBar;
import pivot.wtk.MenuButton;
import pivot.wtk.MenuPopup;
import pivot.wtk.Menu;
import pivot.wtk.Meter;
import pivot.wtk.Palette;
import pivot.wtk.Panorama;
import pivot.wtk.Prompt;
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Rollup;
import pivot.wtk.ScrollBar;
import pivot.wtk.ScrollPane;
import pivot.wtk.Sheet;
import pivot.wtk.Spinner;
import pivot.wtk.SplitPane;
import pivot.wtk.TableViewHeader;
import pivot.wtk.TableView;
import pivot.wtk.TabPane;
import pivot.wtk.TextInput;
import pivot.wtk.Theme;
import pivot.wtk.Tooltip;
import pivot.wtk.TreeView;

/**
 * Terra theme.
 *
 * @author gbrown
 * @author tvolkert
 */
public final class TerraTheme extends Theme {
    private String scheme;

    private Resources resources = null;

    public TerraTheme() {
        this("default");
    }

    public TerraTheme(String scheme) {
        if (scheme == null) {
            throw new IllegalArgumentException("scheme is null.");
        }

        this.scheme = scheme;

        componentSkinMap.put(Alert.class, TerraAlertSkin.class);
        componentSkinMap.put(Checkbox.class, TerraCheckboxSkin.class);
        componentSkinMap.put(Calendar.class, TerraCalendarSkin.class);
        componentSkinMap.put(Dialog.class, TerraDialogSkin.class);
        componentSkinMap.put(Expander.class, TerraExpanderSkin.class);
        componentSkinMap.put(Form.class, TerraFormSkin.class);
        componentSkinMap.put(Frame.class, TerraFrameSkin.class);
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
        componentSkinMap.put(ScrollPane.Corner.class, TerraScrollPaneCornerSkin.class);
        componentSkinMap.put(Sheet.class, TerraSheetSkin.class);
        componentSkinMap.put(Spinner.class, TerraSpinnerSkin.class);
        componentSkinMap.put(SplitPane.class, TerraSplitPaneSkin.class);
        componentSkinMap.put(TableViewHeader.class, TerraTableViewHeaderSkin.class);
        componentSkinMap.put(TableView.class, TerraTableViewSkin.class);
        componentSkinMap.put(TabPane.class, TerraTabPaneSkin.class);
        componentSkinMap.put(TextInput.class, TerraTextInputSkin.class);
        componentSkinMap.put(Tooltip.class, TerraTooltipSkin.class);
        componentSkinMap.put(TreeView.class, TerraTreeViewSkin.class);

        componentSkinMap.put(TerraFrameSkin.FrameButton.class, TerraFrameSkin.FrameButtonSkin.class);
        componentSkinMap.put(TerraExpanderSkin.ShadeButton.class, TerraExpanderSkin.ShadeButtonSkin.class);
        componentSkinMap.put(TerraRollupSkin.RollupButton.class, TerraRollupSkin.RollupButtonSkin.class);
        componentSkinMap.put(TerraScrollBarSkin.ScrollButton.class, TerraScrollBarSkin.ScrollButtonSkin.class);
        componentSkinMap.put(TerraScrollBarSkin.ScrollHandle.class, TerraScrollBarSkin.ScrollHandleSkin.class);
        componentSkinMap.put(TerraSpinnerSkin.SpinButton.class, TerraSpinnerSkin.SpinButtonSkin.class);
        componentSkinMap.put(TerraSpinnerSkin.SpinnerContent.class, TerraSpinnerSkin.SpinnerContentSkin.class);
        componentSkinMap.put(TerraSplitPaneSkin.Splitter.class, TerraSplitPaneSkin.SplitterSkin.class);
        componentSkinMap.put(TerraSplitPaneSkin.SplitterShadow.class, TerraSplitPaneSkin.SplitterShadowSkin.class);
        componentSkinMap.put(TerraTabPaneSkin.TabButton.class, TerraTabPaneSkin.TabButtonSkin.class);
    }

    public void install() {
        try {
            String baseName = getClass().getName() + "_" + scheme;
            resources = new Resources(baseName);
        } catch(Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    public void uninstall() {
        resources = null;
    }

    public Font getFont() {
        // TODO Cache the decoded font?
        return Font.decode(JSONSerializer.getString(resources, "font"));
    }

    public Font getSmallFont() {
        // TODO Cache the decoded font?
        return Font.decode(JSONSerializer.getString(resources, "smallFont"));
    }

    public Color getColor(int index) {
        // TODO Cache the decoded colors?
        return Color.decode(JSONSerializer.getString(resources, "colors[" + index + "]"));
    }

    public Resources getResources() {
        return resources;
    }
}
