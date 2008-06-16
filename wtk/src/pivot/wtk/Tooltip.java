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
package pivot.wtk;

import pivot.util.ListenerList;
import pivot.wtk.skin.terra.TooltipSkin;

public class Tooltip extends Window {
    private class TooltipListenerList extends ListenerList<TooltipListener>
        implements TooltipListener {
        public void tooltipTextChanged(Tooltip tooltip, String previousTooltipText) {
            for (TooltipListener listener : this) {
                listener.tooltipTextChanged(tooltip, previousTooltipText);
            }
        }
    }

    private String tooltipText = null;

    private TooltipListenerList tooltipListeners = new TooltipListenerList();

    public Tooltip(String tooltipText) {
        if (getClass() == Tooltip.class) {
            setSkinClass(TooltipSkin.class);
        }

        setTooltipText(tooltipText);
    }

    public String getTooltipText() {
        return tooltipText;
    }

    public void setTooltipText(String tooltipText) {
        String previousTooltipText = this.tooltipText;

        if (previousTooltipText != tooltipText) {
            this.tooltipText = tooltipText;
            tooltipListeners.tooltipTextChanged(this, previousTooltipText);
        }
    }

    /**
     * @return
     * <tt>true</tt>; by default, tooltips are auxilliary windows.
     */
    @Override
    public boolean isAuxilliary() {
        return true;
    }

    public ListenerList<TooltipListener> getTooltipListeners() {
        return tooltipListeners;
    }
}
