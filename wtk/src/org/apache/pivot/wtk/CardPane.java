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
package org.apache.pivot.wtk;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.ListenerList;
import org.apache.pivot.util.Vote;


/**
 * Container that behaves like a deck of cards, only one of which may be
 * visible at a time.
 */
public class CardPane extends Container {
    private static class CardPaneListenerList extends WTKListenerList<CardPaneListener>
        implements CardPaneListener {
        @Override
        public Vote previewSelectedIndexChange(CardPane cardPane, int selectedIndex) {
            Vote vote = Vote.APPROVE;

            for (CardPaneListener listener : this) {
                vote = vote.tally(listener.previewSelectedIndexChange(cardPane, selectedIndex));
            }

            return vote;
        }

        @Override
        public void selectedIndexChangeVetoed(CardPane cardPane, Vote reason) {
            for (CardPaneListener listener : this) {
                listener.selectedIndexChangeVetoed(cardPane, reason);
            }
        }

        @Override
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
        indexBoundsCheck("selectedIndex", selectedIndex, -1, getLength() -1);

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

    public Component getSelectedCard() {
        return (selectedIndex == -1) ? null : get(selectedIndex);
    }

    @Override
    public void insert(Component component, int index) {
        // Update the selection
        int previousSelectedIndex = selectedIndex;

        if (selectedIndex >= index) {
            selectedIndex++;
        }

        // Insert the component
        super.insert(component, index);

        // Fire selection change event, if necessary
        if (selectedIndex != previousSelectedIndex && previousSelectedIndex > -1) {
            cardPaneListeners.selectedIndexChanged(this, selectedIndex);
        }
    }

    @Override
    public Sequence<Component> remove(int index, int count) {
        // Update the selection
        int previousSelectedIndex = selectedIndex;

        if (selectedIndex >= index) {
            if (selectedIndex < index + count) {
                selectedIndex = -1;
            } else {
                selectedIndex -= count;
            }
        }

        // Remove the components
        Sequence<Component> removed = super.remove(index, count);

        // Fire selection change event, if necessary
        if (selectedIndex != previousSelectedIndex && previousSelectedIndex > -1) {
            cardPaneListeners.selectedIndexChanged(this, selectedIndex);
        }

        return removed;
    }

    public ListenerList<CardPaneListener> getCardPaneListeners() {
        return cardPaneListeners;
    }
}
