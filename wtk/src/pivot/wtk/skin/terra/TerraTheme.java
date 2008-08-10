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

import pivot.wtk.Alert;
import pivot.wtk.Checkbox;
import pivot.wtk.DatePicker;
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
import pivot.wtk.PushButton;
import pivot.wtk.RadioButton;
import pivot.wtk.Rollup;
import pivot.wtk.ScrollBar;
import pivot.wtk.ScrollPane;
import pivot.wtk.Separator;
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
    public TerraTheme() {
        componentSkinMap.put(Alert.class, AlertSkin.class);
        componentSkinMap.put(Checkbox.class, CheckboxSkin.class);
        componentSkinMap.put(DatePicker.class, DatePickerSkin.class);
        componentSkinMap.put(Dialog.class, DialogSkin.class);
        componentSkinMap.put(Expander.class, ExpanderSkin.class);
        componentSkinMap.put(Form.class, FormSkin.class);
        componentSkinMap.put(Frame.class, FrameSkin.class);
        componentSkinMap.put(LinkButton.class, LinkButtonSkin.class);
        componentSkinMap.put(ListButton.class, ListButtonSkin.class);
        componentSkinMap.put(ListView.class, ListViewSkin.class);
        componentSkinMap.put(MenuBar.class, MenuBarSkin.class);
        componentSkinMap.put(MenuButton.class, MenuButtonSkin.class);
        componentSkinMap.put(MenuPopup.class, MenuPopupSkin.class);
        componentSkinMap.put(Menu.class, MenuSkin.class);
        componentSkinMap.put(Meter.class, MeterSkin.class);
        componentSkinMap.put(PushButton.class, PushButtonSkin.class);
        componentSkinMap.put(RadioButton.class, RadioButtonSkin.class);
        componentSkinMap.put(Rollup.class, RollupSkin.class);
        componentSkinMap.put(ScrollBar.class, ScrollBarSkin.class);
        componentSkinMap.put(ScrollPane.Corner.class, ScrollPaneCornerSkin.class);
        componentSkinMap.put(Separator.class, SpacerSkin.class);
        componentSkinMap.put(Spinner.class, SpinnerSkin.class);
        componentSkinMap.put(SplitPane.class, SplitPaneSkin.class);
        componentSkinMap.put(TableViewHeader.class, TableViewHeaderSkin.class);
        componentSkinMap.put(TableView.class, TableViewSkin.class);
        componentSkinMap.put(TabPane.class, TabPaneSkin.class);
        componentSkinMap.put(TextInput.class, TextInputSkin.class);
        componentSkinMap.put(Tooltip.class, TooltipSkin.class);
        componentSkinMap.put(TreeView.class, TreeViewSkin.class);

        componentSkinMap.put(AbstractFrameSkin.FrameButton.class, AbstractFrameSkin.FrameButtonSkin.class);
        componentSkinMap.put(ExpanderSkin.ShadeButton.class, ExpanderSkin.ShadeButtonSkin.class);
        componentSkinMap.put(RollupSkin.RollupButton.class, RollupSkin.RollupButtonSkin.class);
        componentSkinMap.put(ScrollBarSkin.ScrollButton.class, ScrollBarSkin.ScrollButtonSkin.class);
        componentSkinMap.put(ScrollBarSkin.ScrollHandle.class, ScrollBarSkin.ScrollHandleSkin.class);
        componentSkinMap.put(SpinnerSkin.SpinButton.class, SpinnerSkin.SpinButtonSkin.class);
        componentSkinMap.put(SpinnerSkin.SpinnerContent.class, SpinnerSkin.SpinnerContentSkin.class);
        componentSkinMap.put(SplitPaneSkin.Splitter.class, SplitPaneSkin.SplitterSkin.class);
        componentSkinMap.put(SplitPaneSkin.SplitterShadow.class, SplitPaneSkin.SplitterShadowSkin.class);
        componentSkinMap.put(TabPaneSkin.TabButton.class, TabPaneSkin.TabButtonSkin.class);
    }

    public void install() {
        // No-op
    }

    public void uninstall() {
        // No-op
    }
}
