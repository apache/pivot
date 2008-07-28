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

import pivot.collections.Sequence;
import pivot.util.ListenerList;


public class CardPane extends Container {
    private class CardPaneListenerList extends ListenerList<CardPaneListener>
        implements CardPaneListener {
        public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
            for (CardPaneListener listener : this) {
                listener.selectedIndexChanged(cardPane, previousSelectedIndex);
            }
        }
    }

    private int selectedIndex = -1;
    private CardPaneListenerList cardPaneListeners = new CardPaneListenerList();

    public CardPane() {
        installSkin(CardPane.class);
    }

    /**
     * Returns the currently selected card index.
     *
     * @return
     * The selected card index, or <tt>-1</tt> if no card is selected.
     */
    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex < -1
            || selectedIndex > getComponents().getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
            this.selectedIndex = selectedIndex;
            cardPaneListeners.selectedIndexChanged(this, previousSelectedIndex);
        }
    }

    @Override
    protected void insertComponent(Component component, int index) {
        super.insertComponent(component, index);

        // If the selected component's index changed as a result of
        // this insertion, update it
        if (selectedIndex >= index) {
            setSelectedIndex(selectedIndex + 1);
        }
    }

    @Override
    protected Sequence<Component> removeComponents(int index, int count) {
        Sequence<Component> removed = super.removeComponents(index, count);

        // If the selected component was removed, clear the selection
        if (selectedIndex >= index
            && selectedIndex < index + count) {
            setSelectedIndex(-1);
        }

        return removed;
    }

    public ListenerList<CardPaneListener> getCardPaneListeners() {
        return cardPaneListeners;
    }
}
