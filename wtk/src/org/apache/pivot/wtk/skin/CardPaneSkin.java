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

import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.util.Vote;
import org.apache.pivot.wtk.CardPane;
import org.apache.pivot.wtk.CardPaneListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Orientation;
import org.apache.pivot.wtk.effects.FadeDecorator;
import org.apache.pivot.wtk.effects.ScaleDecorator;
import org.apache.pivot.wtk.effects.Transition;
import org.apache.pivot.wtk.effects.TransitionListener;
import org.apache.pivot.wtk.effects.easing.Easing;
import org.apache.pivot.wtk.effects.easing.Quartic;

/**
 * Card pane skin.
 */
public class CardPaneSkin extends ContainerSkin implements CardPaneListener {
    /**
     * Defines the supported selection change effects.
     */
    public enum SelectionChangeEffect {
        CROSSFADE,
        HORIZONTAL_SLIDE,
        VERTICAL_SLIDE,
        HORIZONTAL_FLIP,
        VERTICAL_FLIP,
        ZOOM
    }

    /**
     * Abstract base class for selection change transitions.
     */
    public abstract class SelectionChangeTransition extends Transition {
        public final int from;
        public final int to;
        public final Component fromCard;
        public final Component toCard;
        public final int direction;

        public SelectionChangeTransition(int from, int to) {
            super(selectionChangeDuration, selectionChangeRate, false);

            this.from = from;
            this.to = to;

            CardPane cardPane = (CardPane)getComponent();
            fromCard = (from == -1) ? null : cardPane.get(from);
            toCard = (to == -1) ? null : cardPane.get(to);

            int length = cardPane.getLength();
            if (circular
                && length >= 3) {
                if (from == length - 1
                    && to == 0) {
                    direction = 1;
                } else if (from == 0
                    && to == length - 1) {
                    direction = -1;
                } else {
                    direction = Integer.signum(to - from);
                }
            } else {
                direction = Integer.signum(to - from);
            }
        }
    }

    /**
     * Class that performs cross-fade selection change transitions.
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
     */
    public class SlideTransition extends SelectionChangeTransition {
        private Easing slideEasing = new Quartic();

        public SlideTransition(int from, int to) {
            super(from, to);
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

            int dx = (int)(width * percentComplete) * -direction;
            int dy = (int)(height * percentComplete) * -direction;

            if (selectionChangeEffect == SelectionChangeEffect.HORIZONTAL_SLIDE) {
                fromCard.setLocation(padding.left + dx, padding.top);
                toCard.setLocation(padding.left - (width * -direction) + dx, padding.top);
            } else {
                fromCard.setLocation(padding.left, padding.top + dy);
                toCard.setLocation(padding.left, padding.top - (height * -direction) + dy);
            }
        }
    }

    /**
     * Class that performs flip selection change transitions.
     */
    public class FlipTransition extends SelectionChangeTransition {
        private Orientation orientation;
        private double theta;
        private ScaleDecorator scaleDecorator = new ScaleDecorator();

        public FlipTransition(Orientation orientation, int from, int to) {
            super(from, to);
            this.orientation = orientation;
        }

        @Override
        public void start(TransitionListener transitionListener) {
            theta = 0;
            getComponent().getDecorators().add(scaleDecorator);

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            getComponent().getDecorators().remove(scaleDecorator);

            super.stop();
        }

        @Override
        protected void update() {
            float percentComplete = getPercentComplete();

            if (percentComplete < 1f) {
                theta = Math.PI * percentComplete;

                float scale = (float)Math.abs(Math.cos(theta));

                if (orientation == Orientation.HORIZONTAL) {
                    scaleDecorator.setScale(scale, 1.0f);
                } else {
                    scaleDecorator.setScale(1.0f, scale);
                }

                fromCard.setVisible(theta < Math.PI / 2);
                toCard.setVisible(theta >= Math.PI / 2);

                repaintComponent();
            }
        }
    }

    /**
     * Class that performs zoom change transitions.
     */
    public class ZoomTransition extends CrossfadeTransition {
        private ScaleDecorator fromScaleDecorator = new ScaleDecorator();
        private ScaleDecorator toScaleDecorator = new ScaleDecorator();

        public ZoomTransition(int from, int to) {
            super(from, to);
        }

        @Override
        public void start(TransitionListener transitionListener) {
            if (fromCard != null) {
                fromCard.getDecorators().add(fromScaleDecorator);
            }

            if (toCard != null) {
                toCard.getDecorators().add(toScaleDecorator);
                toCard.setVisible(true);
            }

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            super.stop();

            if (fromCard != null) {
                fromCard.getDecorators().remove(fromScaleDecorator);
                fromCard.setVisible(false);
            }

            if (toCard != null) {
                toCard.getDecorators().remove(toScaleDecorator);
            }
        }

        @Override
        protected void update() {
            float percentComplete = getPercentComplete();

            if (direction == 1) {
                fromScaleDecorator.setScale(1.0f + percentComplete);
                toScaleDecorator.setScale(percentComplete);
            } else {
                fromScaleDecorator.setScale(1.0f - percentComplete);
                toScaleDecorator.setScale(2.0f - percentComplete);
            }

            super.update();
        }
    }

    private Insets padding = Insets.NONE;
    private boolean sizeToSelection = false;
    private SelectionChangeEffect selectionChangeEffect = null;
    private int selectionChangeDuration = DEFAULT_SELECTION_CHANGE_DURATION;
    private int selectionChangeRate = DEFAULT_SELECTION_CHANGE_RATE;
    private boolean circular = false;

    private SelectionChangeTransition selectionChangeTransition = null;

    private static final int DEFAULT_SELECTION_CHANGE_DURATION = 250;
    private static final int DEFAULT_SELECTION_CHANGE_RATE = 30;

    @Override
    public void install(Component component) {
        super.install(component);

        CardPane cardPane = (CardPane)component;
        cardPane.getCardPaneListeners().add(this);
    }

    @Override
    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        CardPane cardPane = (CardPane)getComponent();

        if (sizeToSelection) {
            if (selectionChangeTransition == null) {
                Component selectedCard = cardPane.getSelectedCard();

                if (selectedCard != null) {
                    preferredWidth = selectedCard.getPreferredWidth(height);
                }
            } else {
                float percentComplete = selectionChangeTransition.getPercentComplete();

                int previousWidth;
                if (selectionChangeTransition.fromCard == null) {
                    previousWidth = 0;
                } else {
                    previousWidth = selectionChangeTransition.fromCard.getPreferredWidth(height);
                }

                int width;
                if (selectionChangeTransition.toCard == null) {
                    width = 0;
                } else {
                    width = selectionChangeTransition.toCard.getPreferredWidth(height);
                }

                preferredWidth = previousWidth + (int)((width - previousWidth) * percentComplete);
            }
        } else {
            for (Component card : cardPane) {
                preferredWidth = Math.max(preferredWidth, card.getPreferredWidth(height));
            }

            preferredWidth += (padding.left + padding.right);
        }

        return preferredWidth;
    }

    @Override
    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        CardPane cardPane = (CardPane)getComponent();

        if (sizeToSelection) {
            if (selectionChangeTransition == null) {
                Component selectedCard = cardPane.getSelectedCard();

                if (selectedCard != null) {
                    preferredHeight = selectedCard.getPreferredHeight(width);
                }
            } else {
                float percentComplete = selectionChangeTransition.getPercentComplete();

                int previousHeight;
                if (selectionChangeTransition.fromCard == null) {
                    previousHeight = 0;
                } else {
                    previousHeight = selectionChangeTransition.fromCard.getPreferredHeight(width);
                }

                int height;
                if (selectionChangeTransition.toCard == null) {
                    height = 0;
                } else {
                    height = selectionChangeTransition.toCard.getPreferredHeight(width);
                }

                preferredHeight = previousHeight + (int)((height - previousHeight) * percentComplete);
            }
        } else {
            for (Component card : cardPane) {
                preferredHeight = Math.max(preferredHeight, card.getPreferredHeight(width));
            }

            preferredHeight += (padding.top + padding.bottom);
        }

        return preferredHeight;
    }

    @Override
    public Dimensions getPreferredSize() {
        int preferredWidth = 0;
        int preferredHeight = 0;

        CardPane cardPane = (CardPane)getComponent();

        if (sizeToSelection) {
            if (selectionChangeTransition == null) {
                Component selectedCard = cardPane.getSelectedCard();

                if (selectedCard != null) {
                    Dimensions cardSize = selectedCard.getPreferredSize();
                    preferredWidth = cardSize.width;
                    preferredHeight = cardSize.height;
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

                preferredWidth = previousWidth + (int)((width - previousWidth) * percentComplete);
                preferredHeight = previousHeight + (int)((height - previousHeight) * percentComplete);
            }
        } else {
            for (Component card : cardPane) {
                Dimensions cardSize = card.getPreferredSize();

                preferredWidth = Math.max(cardSize.width, preferredWidth);
                preferredHeight = Math.max(cardSize.height, preferredHeight);
            }
        }

        preferredWidth += (padding.left + padding.right);
        preferredHeight += (padding.top + padding.bottom);

        return new Dimensions(preferredWidth, preferredHeight);
    }

    @Override
    public int getBaseline(int width, int height) {
        int baseline = -1;

        if (sizeToSelection) {
            CardPane cardPane = (CardPane)getComponent();
            Component selectedCard = cardPane.getSelectedCard();

            if (selectedCard != null) {
                int cardWidth = Math.max(width - (padding.left + padding.right), 0);
                int cardHeight = Math.max(height - (padding.top + padding.bottom), 0);

                baseline = selectedCard.getBaseline(cardWidth, cardHeight);

                if (baseline != -1) {
                    baseline += padding.top;
                }
            }
        }

        return baseline;
    }

    @Override
    public void layout() {
        // Set the size of all components to match the size of the card pane,
        // minus padding
        CardPane cardPane = (CardPane)getComponent();
        int width = Math.max(getWidth() - (padding.left + padding.right), 0);
        int height = Math.max(getHeight() - (padding.top + padding.bottom), 0);

        for (Component card : cardPane) {
            card.setLocation(padding.left, padding.top);
            card.setSize(width, height);
        }
    }

    /**
     * Returns the amount of space between the edge of the CardPane and its content.
     */
    public Insets getPadding() {
        return padding;
    }

    /**
     * Sets the amount of space to leave between the edge of the CardPane and its content.
     */
    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    /**
     * Sets the amount of space to leave between the edge of the CardPane and its content.
     *
     * @param padding A dictionary with keys in the set {left, top, bottom, right}.
     */
    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the CardPane and its content,
     * uniformly on all four edges.
     */
    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    /**
     * Sets the amount of space to leave between the edge of the CardPane and its content,
     * uniformly on all four edges.
     */
    public void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

    /**
     * Sets the amount of space to leave between the edge of the CardPane and its content.
     *
     * @param padding A string containing an integer or a JSON dictionary with keys
     * left, top, bottom, and/or right.
     */
    public final void setPadding(String padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(Insets.decode(padding));
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

    public int getSelectionChangeDuration() {
        return selectionChangeDuration;
    }

    public void setSelectionChangeDuration(int selectionChangeDuration) {
        this.selectionChangeDuration = selectionChangeDuration;
    }

    public int getSelectionChangeRate() {
        return selectionChangeRate;
    }

    public void setSelectionChangeRate(int selectionChangeRate) {
        this.selectionChangeRate = selectionChangeRate;
    }
    /**
     * Sets the circular style, which controls the direction of certain
     * transitions (transitions for which a direction makes sense) when looping
     * from the first index of a card pane to the last, or vice versa. When
     * this style is <tt>false</tt> (the default), directional transitions will
     * always appear to move forward when transitioning from a lower card index
     * to a higher card index, and vice versa. When this style is <tt>true</tt>,
     * directional transitions will appear to move forward when transitioning
     * from the last card to the first, and backward when they transition from
     * the first card to the last.
     * <p>
     * Note: to avoid ambiguity, the circular style will be ignored if the card
     * pane has fewer than three cards.
     *
     * @return
     * <tt>true</tt> if directional transitions will be circular;
     * <tt>false</tt> otherwise
     */
    public boolean isCircular() {
        return circular;
    }

    /**
     * Sets the circular style, which controls the direction of certain
     * transitions (transitions for which a direction makes sense) when looping
     * from the first index of a card pane to the last, or vice versa. When
     * this style is <tt>false</tt> (the default), directional transitions will
     * always appear to move forward when transitioning from a lower card index
     * to a higher card index, and vice versa. When this style is <tt>true</tt>,
     * directional transitions will appear to move forward when transitioning
     * from the last card to the first, and backward when they transition from
     * the first card to the last.
     * <p>
     * Note: to avoid ambiguity, the circular style will be ignored if the card
     * pane has fewer than three cards.
     *
     * @param circular
     * <tt>true</tt> if directional transitions should be circular;
     * <tt>false</tt> otherwise
     */
    public void setCircular(boolean circular) {
        this.circular = circular;
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

    @Override
    public Vote previewSelectedIndexChange(CardPane cardPane, int selectedIndex) {
        Vote vote;

        if (cardPane.isShowing()
            && selectionChangeEffect != null
            && selectionChangeTransition == null) {
            int previousSelectedIndex = cardPane.getSelectedIndex();

            switch (selectionChangeEffect) {
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

                case HORIZONTAL_FLIP: {
                    if (previousSelectedIndex != -1
                        && selectedIndex != -1) {
                        selectionChangeTransition = new FlipTransition(Orientation.HORIZONTAL,
                            previousSelectedIndex, selectedIndex);
                    }
                    break;
                }

                case VERTICAL_FLIP: {
                    if (previousSelectedIndex != -1
                        && selectedIndex != -1) {
                        selectionChangeTransition = new FlipTransition(Orientation.VERTICAL,
                            previousSelectedIndex, selectedIndex);
                    }
                    break;
                }

                case ZOOM: {
                    if (previousSelectedIndex != -1
                        && selectedIndex != -1) {
                        selectionChangeTransition = new ZoomTransition(previousSelectedIndex, selectedIndex);
                    }
                    break;
                }
            }

            if (selectionChangeTransition != null) {
                selectionChangeTransition.start(new TransitionListener() {
                    @Override
                    public void transitionCompleted(Transition transition) {
                        CardPane cardPaneLocal = (CardPane)getComponent();

                        SelectionChangeTransition selectionChangeTransitionLocal =
                            (SelectionChangeTransition)transition;

                        int selectedIndexLocal = cardPaneLocal.indexOf(selectionChangeTransitionLocal.toCard);
                        cardPaneLocal.setSelectedIndex(selectedIndexLocal);
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

    @Override
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

    @Override
    public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
        int selectedIndex = cardPane.getSelectedIndex();

        if (selectedIndex != previousSelectedIndex) {
            // This was not an indirect selection change
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
}
