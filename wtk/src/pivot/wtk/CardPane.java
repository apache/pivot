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
import pivot.util.Vote;

/**
 * Container that behaves like a deck of cards, only one of which may be
 * visible at a time.
 *
 * @author gbrown
 */
public class CardPane extends Container {
    private static class CardPaneListenerList extends ListenerList<CardPaneListener>
        implements CardPaneListener {
    	public Vote previewSelectedIndexChange(CardPane cardPane, int selectedIndex) {
            Vote vote = Vote.APPROVE;

            for (CardPaneListener listener : this) {
                vote = vote.tally(listener.previewSelectedIndexChange(cardPane, selectedIndex));
            }

            return vote;
    	}

    	public void selectedIndexChangeVetoed(CardPane cardPane, Vote reason) {
            for (CardPaneListener listener : this) {
                listener.selectedIndexChangeVetoed(cardPane, reason);
            }
    	}

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

    /**
     * Sets the selected card index.
     *
     * @param selectedIndex
     * The selected card index, or <tt>-1</tt> for no selection.
     */
    public void setSelectedIndex(int selectedIndex) {
        if (selectedIndex < -1
            || selectedIndex > getLength() - 1) {
            throw new IndexOutOfBoundsException();
        }

        int previousSelectedIndex = this.selectedIndex;

        if (previousSelectedIndex != selectedIndex) {
        	Vote vote = cardPaneListeners.previewSelectedIndexChange(this, selectedIndex);

        	if (vote == Vote.APPROVE) {
                this.selectedIndex = selectedIndex;
                cardPaneListeners.selectedIndexChanged(this, previousSelectedIndex);
        	} else {
        		cardPaneListeners.selectedIndexChangeVetoed(this, vote);
        	}
        }
    }

    @Override
    public void insert(Component component, int index) {
        super.insert(component, index);

        // If the selected component's index changed as a result of
        // this insertion, update it
        if (selectedIndex >= index) {
            setSelectedIndex(selectedIndex + 1);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        Sequence<Component> removed = super.remove(index, count);

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
