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
package pivot.wtk.skin;

import pivot.collections.Sequence;
import pivot.util.Vote;
import pivot.wtk.CardPane;
import pivot.wtk.CardPaneListener;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Dimensions;
import pivot.wtk.Orientation;
import pivot.wtk.effects.FadeDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.effects.easing.Easing;
import pivot.wtk.effects.easing.Quartic;

/**
 * Card pane skin.
 *
 * @author gbrown
 */
public class CardPaneSkin extends ContainerSkin implements CardPaneListener {
    public class SelectionChangeTransition extends Transition {
        private int previousSelectedIndex = -1;
        private int selectedIndex = -1;

        private FadeDecorator fadeOutDecorator = new FadeDecorator();
        private FadeDecorator fadeInDecorator = new FadeDecorator();

        private Easing slideEasing = new Quartic();

        public SelectionChangeTransition(int previousSelectedIndex, int selectedIndex,
            int duration, int rate) {
            super(duration, rate, false);

            this.previousSelectedIndex = previousSelectedIndex;
            this.selectedIndex = selectedIndex;
        }

        @Override
        public void start(TransitionListener transitionListener) {
            Component previousSelectedCard = getPreviousSelectedCard();
            Component selectedCard = getSelectedCard();

            if (matchSelectedCardSize) {
                if (previousSelectedCard != null) {
                    previousSelectedCard.getDecorators().add(fadeOutDecorator);
                }

                if (selectedCard != null) {
                    selectedCard.getDecorators().add(fadeInDecorator);
                }
            }

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            if (matchSelectedCardSize) {
                Component previousSelectedCard = getPreviousSelectedCard();
                Component selectedCard = getSelectedCard();

                if (previousSelectedCard != null) {
                    previousSelectedCard.getDecorators().remove(fadeOutDecorator);
                }

                if (selectedCard != null) {
                    selectedCard.getDecorators().remove(fadeInDecorator);
                }
            }

            invalidateComponent();

            super.stop();
        }

        @Override
        protected void update() {
            if (matchSelectedCardSize) {
                float percentComplete = getPercentComplete();
                fadeOutDecorator.setOpacity(1.0f - percentComplete);
                fadeInDecorator.setOpacity(percentComplete);
            }

            invalidateComponent();
        }

        public float getEasedSlidePercentComplete() {
            return slideEasing.easeOut(getElapsedTime(), 0, 1, getDuration());
        }

        public Component getPreviousSelectedCard() {
            CardPane cardPane = (CardPane)getComponent();
            return previousSelectedIndex == -1 ? null : cardPane.get(previousSelectedIndex);
        }

        public Component getSelectedCard() {
            CardPane cardPane = (CardPane)getComponent();
            return selectedIndex == -1 ? null : cardPane.get(selectedIndex);
        }
    }

    private boolean matchSelectedCardSize = false;

    private SelectionChangeTransition selectionChangeTransition = null;
    private static final int SELECTION_CHANGE_DURATION = 250;
    private static final int SELECTION_CHANGE_RATE = 30;

    @Override
    public void setSize(int width, int height) {
        if (selectionChangeTransition != null) {
            if (!matchSelectedCardSize) {
                CardPane cardPane = (CardPane)getComponent();
                Orientation orientation = cardPane.getOrientation();

                if ((orientation == Orientation.HORIZONTAL && width != getWidth())
                    || (orientation == Orientation.VERTICAL && height != getHeight())) {
                    selectionChangeTransition.end();
                    selectionChangeTransition = null;
                }
            }
        }

        super.setSize(width, height);
    }

    public void install(Component component) {
        super.install(component);

        CardPane cardPane = (CardPane)component;
        cardPane.getCardPaneListeners().add(this);
    }

    public void uninstall() {
        CardPane cardPane = (CardPane)getComponent();
        cardPane.getCardPaneListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        int preferredWidth = 0;

        CardPane cardPane = (CardPane)getComponent();

        if (selectionChangeTransition == null) {
            if (matchSelectedCardSize) {
                Component selectedCard = cardPane.getSelectedCard();
                if (selectedCard != null) {
                    preferredWidth = selectedCard.getPreferredWidth(height);
                }
            } else {
                int selectedIndex = cardPane.getSelectedIndex();
                Orientation orientation = cardPane.getOrientation();

                if (selectedIndex != -1
                    || orientation == Orientation.HORIZONTAL) {
                    for (Component card : cardPane) {
                        preferredWidth = Math.max(preferredWidth, card.getPreferredWidth(height));
                    }
                }
            }
        } else {
            float percentComplete = selectionChangeTransition.getPercentComplete();

            Component previousSelectedCard = selectionChangeTransition.getPreviousSelectedCard();
            int previousWidth = (previousSelectedCard == null) ? 0 : previousSelectedCard.getWidth();

            Component selectedCard = selectionChangeTransition.getSelectedCard();
            int width = (selectedCard == null) ? 0 : selectedCard.getWidth();

            Orientation orientation = cardPane.getOrientation();

            if (!matchSelectedCardSize
                && orientation == Orientation.HORIZONTAL) {
                preferredWidth = Math.max(previousWidth, width);
            } else {
                preferredWidth = previousWidth + (int)((float)(width - previousWidth)
                    * percentComplete);
            }
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        CardPane cardPane = (CardPane)getComponent();

        if (selectionChangeTransition == null) {
            if (matchSelectedCardSize) {
                Component selectedCard = cardPane.getSelectedCard();
                if (selectedCard != null) {
                    preferredHeight = selectedCard.getPreferredHeight(width);
                }
            } else {
                int selectedIndex = cardPane.getSelectedIndex();
                Orientation orientation = cardPane.getOrientation();

                if (selectedIndex != -1
                    || orientation == Orientation.VERTICAL) {
                    for (Component card : cardPane) {
                        preferredHeight = Math.max(preferredHeight, card.getPreferredHeight(width));
                    }
                }
            }
        } else {
            float percentComplete = selectionChangeTransition.getPercentComplete();

            Component previousSelectedCard = selectionChangeTransition.getPreviousSelectedCard();
            int previousHeight = (previousSelectedCard == null) ? 0 : previousSelectedCard.getHeight();

            Component selectedCard = selectionChangeTransition.getSelectedCard();
            int height = (selectedCard == null) ? 0 : selectedCard.getHeight();

            Orientation orientation = cardPane.getOrientation();

            if (!matchSelectedCardSize
                && orientation == Orientation.VERTICAL) {
                preferredHeight = Math.max(previousHeight, height);
            } else {
                preferredHeight = previousHeight + (int)((float)(height - previousHeight)
                    * percentComplete);
            }
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        Dimensions preferredSize;

        CardPane cardPane = (CardPane)getComponent();

        if (selectionChangeTransition == null) {
            if (matchSelectedCardSize) {
                Component selectedCard = cardPane.getSelectedCard();
                if (selectedCard == null) {
                    preferredSize = new Dimensions(0, 0);
                } else {
                    preferredSize = selectedCard.getPreferredSize();
                }
            } else {
                int preferredWidth = 0;
                int preferredHeight = 0;

                Orientation orientation = cardPane.getOrientation();

                int selectedIndex = cardPane.getSelectedIndex();
                for (Component card : cardPane) {
                    Dimensions cardSize = card.getPreferredSize();

                    if (selectedIndex != -1
                        || orientation == Orientation.HORIZONTAL) {
                        preferredWidth = Math.max(cardSize.width, preferredWidth);
                    }

                    if (selectedIndex != -1
                        || orientation == Orientation.VERTICAL) {
                        preferredHeight = Math.max(cardSize.height, preferredHeight);
                    }
                }

                preferredSize = new Dimensions(preferredWidth, preferredHeight);
            }
        } else {
            float percentComplete = selectionChangeTransition.getPercentComplete();

            int previousWidth;
            int previousHeight;
            Component previousSelectedCard = selectionChangeTransition.getPreviousSelectedCard();

            if (previousSelectedCard == null) {
                previousWidth = 0;
                previousHeight = 0;
            } else {
                previousWidth = previousSelectedCard.getWidth();
                previousHeight = previousSelectedCard.getHeight();
            }

            int width;
            int height;
            Component selectedCard = selectionChangeTransition.getSelectedCard();

            if (selectedCard == null) {
                width = 0;
                height = 0;
            } else {
                width = selectedCard.getWidth();
                height = selectedCard.getHeight();
            }

            int preferredWidth = 0;
            int preferredHeight = 0;

            Orientation orientation = cardPane.getOrientation();

            if (!matchSelectedCardSize
                && orientation == Orientation.HORIZONTAL) {
                preferredWidth = Math.max(previousWidth, width);
            } else {
                preferredWidth = previousWidth + (int)((float)(width - previousWidth)
                    * percentComplete);
            }

            if (!matchSelectedCardSize
                && orientation == Orientation.VERTICAL) {
                preferredHeight = Math.max(previousHeight, height);
            } else {
                preferredHeight = previousHeight + (int)((float)(height - previousHeight)
                    * percentComplete);
            }

            preferredSize = new Dimensions(preferredWidth, preferredHeight);
        }

        return preferredSize;
    }

    public void layout() {
        CardPane cardPane = (CardPane)getComponent();
        int width = getWidth();
        int height = getHeight();

        if (selectionChangeTransition == null) {
            Component selectedCard = cardPane.getSelectedCard();

            for (Component card : cardPane) {
                // Set the size of the selected component to the container's size
                // and show the card
                if (card == selectedCard) {
                    card.setLocation(0, 0);
                    card.setSize(width, height);
                    card.setVisible(true);
                } else {
                    card.setVisible(false);
                }
            }
        } else {
            if (matchSelectedCardSize) {
                Component previousSelectedCard = selectionChangeTransition.getPreviousSelectedCard();
                Component selectedCard = selectionChangeTransition.getSelectedCard();

                Orientation orientation = cardPane.getOrientation();

                if (selectionChangeTransition.isRunning()) {
                    for (Component card : cardPane) {
                        // Align old and new cards and ensure they are visible
                        if (card == previousSelectedCard
                            || card == selectedCard) {
                            int x = (orientation == Orientation.VERTICAL) ?
                                0 : Math.round((float)(width - card.getWidth()) / 2);
                            int y = (orientation == Orientation.HORIZONTAL) ?
                                0 : Math.round((float)(height - card.getHeight()) / 2);

                            card.setLocation(x, y);
                            card.setVisible(true);
                        } else {
                            card.setVisible(false);
                        }
                    }
                } else {
                    if (previousSelectedCard != null) {
                        previousSelectedCard.setSize(previousSelectedCard.getPreferredSize());
                    }

                    if (selectedCard != null) {
                        selectedCard.setSize(selectedCard.getPreferredSize());
                        selectedCard.setVisible(true);
                    }
                }
            } else {
                if (selectionChangeTransition.isRunning()) {
                    int previousSelectedIndex = selectionChangeTransition.previousSelectedIndex;
                    int selectedIndex = selectionChangeTransition.selectedIndex;

                    if (previousSelectedIndex != -1
                        && selectedIndex != -1) {
                        float percentComplete = selectionChangeTransition.getEasedSlidePercentComplete();

                        int direction = Integer.signum(previousSelectedIndex - selectedIndex);

                        int dx = (int)((float)width * percentComplete) * direction;
                        int dy = (int)((float)height * percentComplete) * direction;

                        Orientation orientation = cardPane.getOrientation();

                        for (int i = 0, n = cardPane.getLength(); i < n; i++) {
                            Component card = cardPane.get(i);

                            if (i == previousSelectedIndex) {
                                if (orientation == Orientation.HORIZONTAL) {
                                    card.setLocation(dx, 0);
                                } else {
                                    card.setLocation(0, dy);
                                }

                                card.setVisible(true);
                            } else if (i == selectedIndex) {
                                if (orientation == Orientation.HORIZONTAL) {
                                    card.setLocation(-width * direction + dx, 0);
                                } else {
                                    card.setLocation(0, -height * direction + dy);
                                }

                                card.setVisible(true);
                            } else {
                                card.setVisible(false);
                            }
                        }
                    }
                } else {
                    Component selectedCard = selectionChangeTransition.getSelectedCard();
                    Orientation orientation = cardPane.getOrientation();

                    if (selectedCard != null) {
                        if (orientation == Orientation.HORIZONTAL) {
                            for (Component card : cardPane) {
                                height = Math.max(height, card.getPreferredHeight(width));
                            }
                        } else {
                            for (Component card : cardPane) {
                                width = Math.max(width, card.getPreferredWidth(height));
                            }
                        }

                        selectedCard.setSize(width, height);
                        selectedCard.setVisible(true);
                    }
                }
            }
        }
    }

    public boolean getMatchSelectedCardSize() {
        return matchSelectedCardSize;
    }

    public void setMatchSelectedCardSize(boolean matchSelectedCardSize) {
        this.matchSelectedCardSize = matchSelectedCardSize;
        invalidateComponent();
    }

    @Override
    public void componentInserted(Container container, int index) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
        }

        super.componentInserted(container, index);

        invalidateComponent();
    }

    @Override
    public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
        }

        super.componentsRemoved(container, index, removed);

        invalidateComponent();
    }

    public void orientationChanged(CardPane cardPane, Orientation previousOrientation) {
        // No-op
    }

    public Vote previewSelectedIndexChange(final CardPane cardPane, final int selectedIndex) {
        Vote vote = Vote.APPROVE;

        if (cardPane.isShowing()) {
            if (selectionChangeTransition == null) {
                Orientation orientation = cardPane.getOrientation();

                if (matchSelectedCardSize
                    || orientation != null) {
                    int previousSelectedIndex = cardPane.getSelectedIndex();

                    selectionChangeTransition = new SelectionChangeTransition(previousSelectedIndex, selectedIndex,
                        SELECTION_CHANGE_DURATION, SELECTION_CHANGE_RATE);

                    layout();
                    selectionChangeTransition.start(new TransitionListener() {
                        public void transitionCompleted(Transition transition) {
                            cardPane.setSelectedIndex(selectedIndex);
                            selectionChangeTransition = null;

                            invalidateComponent();
                        }
                    });

                    vote = Vote.DEFER;
                }
            } else {
                if (selectionChangeTransition.isRunning()) {
                    vote = Vote.DEFER;
                }
            }
        }

        return vote;
    }

    public void selectedIndexChangeVetoed(CardPane cardPane, Vote reason) {
        if (reason == Vote.DENY
            && selectionChangeTransition != null) {
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
            invalidateComponent();
        }
    }

    public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
        invalidateComponent();
    }
}
