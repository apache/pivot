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
package org.apache.pivot.wtk.skin;

import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.CardPaneListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.effects.FadeDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Easing;
import org.apache.pivot.wtk.effects.easing.Quartic;


/**
 * Card pane skin.
 *
 * @author gbrown
 */
public class CardPaneSkin extends ContainerSkin implements CardPaneListener {
    /**
     * Defines the supported selection change effects.
     *
     * @author gbrown
     */
    public enum SelectionChangeEffect {
        CROSSFADE,
        HORIZONTAL_SLIDE,
        VERTICAL_SLIDE
    }

    /**
     * Abstract base class for selection change transitions.
     *
     * @author gbrown
     */
    public abstract class SelectionChangeTransition extends Transition {
        public final Component fromCard;
        public final Component toCard;

        public SelectionChangeTransition(int from, int to) {
            super(SELECTION_CHANGE_DURATION, SELECTION_CHANGE_RATE, false);

            CardPane cardPane = (CardPane)getComponent();
            fromCard = (from == -1) ? null : cardPane.get(from);
            toCard = (to == -1) ? null : cardPane.get(to);
        }
    }

    /**
     * Class that performs selection change transitions.
     *
     * @author gbrown
     */
    public class CrossfadeTransition extends SelectionChangeTransition {
        private FadeDecorator fadeOutDecorator = new FadeDecorator();
        private FadeDecorator fadeInDecorator = new FadeDecorator();

        public CrossfadeTransition(int from, int to) {
            super(from, to);
        }

        @Override
        public void start(TransitionListener transitionListener) {
            if (fromCard != null) {
                fromCard.getDecorators().add(fadeOutDecorator);
            }

            if (toCard != null) {
                toCard.getDecorators().add(fadeInDecorator);
                toCard.setVisible(true);
            }

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            super.stop();

            if (fromCard != null) {
                fromCard.getDecorators().remove(fadeOutDecorator);
                fromCard.setVisible(false);
            }

            if (toCard != null) {
                toCard.getDecorators().remove(fadeInDecorator);
            }
        }

        @Override
        protected void update() {
            float percentComplete = getPercentComplete();

            fadeOutDecorator.setOpacity(1.0f - percentComplete);
            fadeInDecorator.setOpacity(percentComplete);

            if (sizeToSelection) {
                invalidateComponent();
            } else {
                repaintComponent();
            }
        }
    }

    /**
     * Class that performs slide selection change transitions.
     *
     * @author gbrown
     */
    public class SlideTransition extends SelectionChangeTransition {
        private int direction;
        private Easing slideEasing = new Quartic();

        public SlideTransition(int from, int to) {
            super(from, to);

            direction = Integer.signum(from - to);
        }

        @Override
        public void start(TransitionListener transitionListener) {
            toCard.setVisible(true);

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            fromCard.setVisible(false);

            super.stop();
        }

        @Override
        protected void update() {
            int width = getWidth();
            int height = getHeight();

            float percentComplete = slideEasing.easeOut(getElapsedTime(), 0, 1, getDuration());

            int dx = (int)(width * percentComplete) * direction;
            int dy = (int)(height * percentComplete) * direction;

            if (selectionChangeEffect == SelectionChangeEffect.HORIZONTAL_SLIDE) {
                fromCard.setLocation(dx, 0);
                toCard.setLocation(-width * direction + dx, 0);
            } else {
                fromCard.setLocation(0, dy);
                toCard.setLocation(0, -height * direction + dy);
            }
        }
    }

    private boolean sizeToSelection = false;
    private SelectionChangeEffect selectionChangeEffect = null;

    private SelectionChangeTransition selectionChangeTransition = null;

    public static final int SELECTION_CHANGE_DURATION = 250;
    public static final int SELECTION_CHANGE_RATE = 30;

    @Override
    public void install(Component component) {
        super.install(component);

        CardPane cardPane = (CardPane)component;
        cardPane.getCardPaneListeners().add(this);
    }

    @Override
    public void uninstall() {
        CardPane cardPane = (CardPane)getComponent();
        cardPane.getCardPaneListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        if (sizeToSelection
            || height == -1) {
            Dimensions preferredSize = getPreferredSize();
            preferredWidth = preferredSize.width;
        } else {
            CardPane cardPane = (CardPane)getComponent();
            for (Component card : cardPane) {
                preferredWidth = Math.max(preferredWidth, card.getPreferredWidth(height));
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        if (sizeToSelection
            || width == -1) {
            Dimensions preferredSize = getPreferredSize();
            preferredHeight = preferredSize.height;
        } else {
            CardPane cardPane = (CardPane)getComponent();
            for (Component card : cardPane) {
                preferredHeight = Math.max(preferredHeight, card.getPreferredHeight(width));
            }
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        Dimensions preferredSize;

        CardPane cardPane = (CardPane)getComponent();

        if (sizeToSelection) {
            if (selectionChangeTransition == null) {
                Component selectedCard = cardPane.getSelectedCard();

                if (selectedCard == null) {
                    preferredSize = new Dimensions(0, 0);
                } else {
                    preferredSize = selectedCard.getPreferredSize();
                }
            } else {
                float percentComplete = selectionChangeTransition.getPercentComplete();

                int previousWidth;
                int previousHeight;
                if (selectionChangeTransition.fromCard == null) {
                    previousWidth = 0;
                    previousHeight = 0;
                } else {
                    Dimensions fromSize = selectionChangeTransition.fromCard.getPreferredSize();
                    previousWidth = fromSize.width;
                    previousHeight = fromSize.height;
                }

                int width;
                int height;
                if (selectionChangeTransition.toCard == null) {
                    width = 0;
                    height = 0;
                } else {
                    Dimensions toSize = selectionChangeTransition.toCard.getPreferredSize();
                    width = toSize.width;
                    height = toSize.height;
                }

                int preferredWidth = previousWidth + (int)((width - previousWidth) * percentComplete);
                int preferredHeight = previousHeight + (int)((height - previousHeight) * percentComplete);

                preferredSize = new Dimensions(preferredWidth, preferredHeight);
            }
        } else {
            int preferredWidth = 0;
            int preferredHeight = 0;

            for (Component card : cardPane) {
                Dimensions cardSize = card.getPreferredSize();

                preferredWidth = Math.max(cardSize.width, preferredWidth);
                preferredHeight = Math.max(cardSize.height, preferredHeight);
            }

            preferredSize = new Dimensions(preferredWidth, preferredHeight);
        }

        return preferredSize;
    }

    public void layout() {
        CardPane cardPane = (CardPane)getComponent();
        int width = getWidth();
        int height = getHeight();

        for (Component card : cardPane) {
            card.setLocation(0, 0);
            card.setSize(width, height);
        }
    }

    public boolean getSizeToSelection() {
        return sizeToSelection;
    }

    public void setSizeToSelection(boolean sizeToSelection) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        this.sizeToSelection = sizeToSelection;
        invalidateComponent();
    }

    public SelectionChangeEffect getSelectionChangeEffect() {
        return selectionChangeEffect;
    }

    public void setSelectionChangeEffect(SelectionChangeEffect selectionChangeEffect) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        this.selectionChangeEffect = selectionChangeEffect;
    }

    public void setSelectionChangeEffect(String selectionChangeEffect) {
        if (selectionChangeEffect == null) {
            throw new IllegalArgumentException();
        }

        setSelectionChangeEffect(SelectionChangeEffect.valueOf(selectionChangeEffect.toUpperCase()));
    }

    @Override
    public void componentInserted(Container container, int index) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        super.componentInserted(container, index);

        CardPane cardPane = (CardPane)container;
        Component card = cardPane.get(index);
        card.setVisible(false);

        if (cardPane.getLength() == 1) {
            cardPane.setSelectedIndex(0);
        }

        invalidateComponent();
    }

    @Override
    public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.end();
        }

        super.componentsRemoved(container, index, removed);

        for (int i = 0, n = removed.getLength(); i < n; i++){
            Component card = removed.get(i);
            card.setVisible(true);
        }

        invalidateComponent();
    }

    public Vote previewSelectedIndexChange(CardPane cardPane, int selectedIndex) {
        Vote vote;

        if (cardPane.isShowing()
            && selectionChangeEffect != null
            && selectionChangeTransition == null) {
            int previousSelectedIndex = cardPane.getSelectedIndex();

            switch(selectionChangeEffect) {
                case CROSSFADE: {
                    selectionChangeTransition = new CrossfadeTransition(previousSelectedIndex, selectedIndex);
                    break;
                }

                case HORIZONTAL_SLIDE:
                case VERTICAL_SLIDE: {
                    if (previousSelectedIndex != -1
                        && selectedIndex != -1) {
                        selectionChangeTransition = new SlideTransition(previousSelectedIndex, selectedIndex);
                    }
                    break;
                }
            }

            if (selectionChangeTransition != null) {
                selectionChangeTransition.start(new TransitionListener() {
                    public void transitionCompleted(Transition transition) {
                        CardPane cardPane = (CardPane)getComponent();

                        SelectionChangeTransition selectionChangeTransition =
                            (SelectionChangeTransition)transition;

                        int selectedIndex = cardPane.indexOf(selectionChangeTransition.toCard);
                        cardPane.setSelectedIndex(selectedIndex);
                        CardPaneSkin.this.selectionChangeTransition = null;
                    }
                });
            }
        }

        if (selectionChangeTransition == null
            || !selectionChangeTransition.isRunning()) {
            vote = Vote.APPROVE;
        } else {
            vote = Vote.DEFER;
        }

        return vote;
    }

    public void selectedIndexChangeVetoed(CardPane cardPane, Vote reason) {
        if (reason == Vote.DENY
            && selectionChangeTransition != null) {
            // NOTE We stop, rather than end, the transition so the completion
            // event isn't fired; if the event fires, the listener will set
            // the selection state
            selectionChangeTransition.stop();
            selectionChangeTransition = null;

            if (sizeToSelection) {
                invalidateComponent();
            }
        }
    }

    public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
        int selectedIndex = cardPane.getSelectedIndex();
        if (selectedIndex != -1) {
            Component selectedCard = cardPane.get(selectedIndex);
            selectedCard.setVisible(true);
        }

        if (previousSelectedIndex != -1) {
            Component previousSelectedCard = cardPane.get(previousSelectedIndex);
            previousSelectedCard.setVisible(false);
        }

        if (selectedIndex == -1
            || previousSelectedIndex == -1
            || sizeToSelection) {
            invalidateComponent();
        }
    }
}
