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
        VERTICAL_SLIDE,
        HORIZONTAL_FLIP,
        VERTICAL_FLIP,
        ZOOM
    }

    /**
     * Abstract base class for selection change transitions.
     *
     * @author gbrown
     */
    public abstract class SelectionChangeTransition extends Transition {
        public final int from;
        public final int to;
        public final Component fromCard;
        public final Component toCard;

        public SelectionChangeTransition(int from, int to) {
            this(from, to, SELECTION_CHANGE_DURATION);
        }

        public SelectionChangeTransition(int from, int to, int duration) {
            super(duration, SELECTION_CHANGE_RATE, false);

            this.from = from;
            this.to = to;

            CardPane cardPane = (CardPane)getComponent();
            fromCard = (from == -1) ? null : cardPane.get(from);
            toCard = (to == -1) ? null : cardPane.get(to);
        }
    }

    /**
     * Class that performs cross-fade selection change transitions.
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
                fromCard.setLocation(padding.left + dx, padding.top);
                toCard.setLocation(padding.left - (width * direction) + dx, padding.top);
            } else {
                fromCard.setLocation(padding.left, padding.top + dy);
                toCard.setLocation(padding.left, padding.top - (height * direction) + dy);
            }
        }
    }

    /**
     * Class that performs flip selection change transitions.
     *
     * @author tvolkert
     */
    public class FlipTransition extends SelectionChangeTransition {
        private Orientation orientation;
        private double theta;
        private ScaleDecorator scaleDecorator = new ScaleDecorator();

        public FlipTransition(Orientation orientation, int from, int to) {
            super(from, to, 350);
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
                    scaleDecorator.setScale(Math.max(scale, 0.01f), 1.0f);
                } else {
                    scaleDecorator.setScale(1.0f, Math.max(scale, 0.01f));
                }

                fromCard.setVisible(theta < Math.PI / 2);
                toCard.setVisible(theta >= Math.PI / 2);

                repaintComponent();
            }
        }
    }

    /**
     * Class that performs zoom change transitions.
     *
     * @author gbrown
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

            if (from < to) {
                fromScaleDecorator.setScale(1.0f + percentComplete);
                toScaleDecorator.setScale(percentComplete);
            } else {
                fromScaleDecorator.setScale(1.0f - percentComplete);
                toScaleDecorator.setScale(2.0f - percentComplete);
            }

            super.update();
        }
    }

    private Insets padding = new Insets(0);
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

            preferredWidth += (padding.left + padding.right);
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

            preferredHeight += (padding.top + padding.bottom);
        }

        return preferredHeight;
    }

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

    public void layout() {
        // Set the size of all components to match the size of the stack pane,
        // minus padding
        CardPane cardPane = (CardPane)getComponent();
        int width = getWidth() - (padding.left + padding.right);
        int height = getHeight() - (padding.top + padding.bottom);

        for (Component card : cardPane) {
            card.setLocation(padding.left, padding.top);
            card.setSize(width, height);
        }
    }

    public Insets getPadding() {
        return padding;
    }

    public void setPadding(Insets padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        this.padding = padding;
        invalidateComponent();
    }

    public final void setPadding(Dictionary<String, ?> padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(new Insets(padding));
    }

    public final void setPadding(int padding) {
        setPadding(new Insets(padding));
    }

    public final void setPadding(Number padding) {
        if (padding == null) {
            throw new IllegalArgumentException("padding is null.");
        }

        setPadding(padding.intValue());
    }

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
