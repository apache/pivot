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
package pivot.wtk.skin;

import pivot.util.Vote;
import pivot.wtk.CardPane;
import pivot.wtk.CardPaneListener;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Dimensions;
import pivot.wtk.effects.FadeDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;

/**
 * Card pane skin.
 *
 * @author gbrown
 */
public class CardPaneSkin extends ContainerSkin implements CardPaneListener {
	public class SelectionChangeTransition extends Transition {
	    public final Component oldCard;
	    public final Component newCard;

	    private FadeDecorator fadeOutDecorator = new FadeDecorator();
	    private FadeDecorator fadeInDecorator = new FadeDecorator();

	    public SelectionChangeTransition(Component oldComponent, Component newComponent,
    		int duration, int rate) {
	        super(duration, rate, false);
	        this.oldCard = oldComponent;
	        this.newCard = newComponent;
	    }

	    @Override
	    public void start(TransitionListener transitionListener) {
	    	CardPane cardPane = (CardPane)getComponent();


	        if (cardPane.isPreferredWidthSet()) {
	        	int width = cardPane.getPreferredWidth();
		        oldCard.setSize(width, oldCard.getPreferredHeight(width));
		        newCard.setSize(width, newCard.getPreferredHeight(width));
	        } else if (cardPane.isPreferredHeightSet()) {
	        	int height = cardPane.getPreferredHeight();
		        oldCard.setSize(oldCard.getPreferredWidth(height), height);
		        newCard.setSize(newCard.getPreferredWidth(height), height);
	        } else {
		        oldCard.setSize(oldCard.getPreferredSize());
		        newCard.setSize(newCard.getPreferredSize());
	        }

	        oldCard.getDecorators().add(fadeOutDecorator);
	        newCard.getDecorators().add(fadeInDecorator);

	        newCard.setVisible(true);

	        super.start(transitionListener);
	    }

	    @Override
	    public void stop() {
	        oldCard.getDecorators().remove(fadeOutDecorator);
	        oldCard.setVisible(false);

	        newCard.getDecorators().remove(fadeInDecorator);

	        super.stop();
	    }

	    @Override
	    protected void update() {
	    	fadeOutDecorator.setOpacity(1.0f - getPercentComplete());
	        fadeInDecorator.setOpacity(getPercentComplete());

	        invalidateComponent();
	    }

	    public Dimensions getPreferredSize() {
	    	float percentComplete = getPercentComplete();

	    	int oldWidth = oldCard.getWidth();
	    	int newWidth = newCard.getWidth();
	    	int preferredWidth = oldWidth + (int)((float)(newWidth - oldWidth) * percentComplete);

	    	int oldHeight = oldCard.getHeight();
	    	int newHeight = newCard.getHeight();
	    	int preferredHeight = oldHeight + (int)((float)(newHeight - oldHeight) * percentComplete);

	    	return new Dimensions(preferredWidth, preferredHeight);
	    }
	}

	private boolean matchSelectedCardSize = false;

	private SelectionChangeTransition selectionChangeTransition = null;
	private static final int SELECTION_CHANGE_DURATION = 200;
	private static final int SELECTION_CHANGE_RATE = 30;

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

        if (selectionChangeTransition == null) {
            CardPane cardPane = (CardPane)getComponent();

            if (matchSelectedCardSize) {
            	int selectedIndex = cardPane.getSelectedIndex();
            	if (selectedIndex != -1) {
            		Component selectedCard = cardPane.get(selectedIndex);
            		preferredWidth = selectedCard.getPreferredWidth(height);
            	}
            } else {
                for (Component component : cardPane) {
                    preferredWidth = Math.max(preferredWidth,
                        component.getPreferredWidth(height));
                }
            }
        } else {
        	preferredWidth = selectionChangeTransition.getPreferredSize().width;
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int preferredHeight = 0;

        if (selectionChangeTransition == null) {
            CardPane cardPane = (CardPane)getComponent();

            if (matchSelectedCardSize) {
            	int selectedIndex = cardPane.getSelectedIndex();
            	if (selectedIndex != -1) {
            		Component selectedCard = cardPane.get(selectedIndex);
            		preferredHeight = selectedCard.getPreferredHeight(width);
            	}
            } else {
                for (Component component : cardPane) {
                    preferredHeight = Math.max(preferredHeight,
                        component.getPreferredHeight(width));
                }
            }
        } else {
        	preferredHeight = selectionChangeTransition.getPreferredSize().height;
        }

        return preferredHeight;
    }

    public Dimensions getPreferredSize() {
        Dimensions preferredSize;

        if (selectionChangeTransition == null) {
            CardPane cardPane = (CardPane)getComponent();
            if (matchSelectedCardSize) {
            	int selectedIndex = cardPane.getSelectedIndex();

            	if (selectedIndex == -1) {
            		preferredSize = new Dimensions(0, 0);
            	} else {
            		Component selectedCard = cardPane.get(selectedIndex);
            		preferredSize = selectedCard.getPreferredSize();
            	}
            } else {
            	preferredSize = new Dimensions(0, 0);

                for (Component component : cardPane) {
                    Dimensions preferredCardSize = component.getPreferredSize();

                    preferredSize.width = Math.max(preferredCardSize.width,
                		preferredSize.width);
                    preferredSize.height = Math.max(preferredCardSize.height,
                		preferredSize.height);
                }
            }
        } else {
        	preferredSize = selectionChangeTransition.getPreferredSize();
        }

        return preferredSize;
    }

    public void layout() {
        CardPane cardPane = (CardPane)getComponent();
        int width = getWidth();
        int height = getHeight();

        if (selectionChangeTransition == null) {
        	// Set the size of the selected component to the container's size
            int selectedIndex = cardPane.getSelectedIndex();
            if (selectedIndex != -1) {
            	Component selectedCard = cardPane.get(selectedIndex);
            	selectedCard.setLocation(0, 0);
            	selectedCard.setSize(width, height);
            }
        } else {
        	// Center old card and new card
        	Component oldCard = selectionChangeTransition.oldCard;
        	oldCard.setLocation((width - oldCard.getWidth()) / 2, (height - oldCard.getHeight()) / 2);

        	Component newCard = selectionChangeTransition.newCard;
        	newCard.setLocation((width - newCard.getWidth()) / 2, (height - newCard.getHeight()) / 2);
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
		super.componentInserted(container, index);

		CardPane cardPane = (CardPane)container;
		int selectedIndex = cardPane.getSelectedIndex();

		Component component = container.get(index);
		component.setVisible(index == selectedIndex);
    }

    public Vote previewSelectedIndexChange(final CardPane cardPane, final int selectedIndex) {
    	Vote vote = Vote.APPROVE;
    	if (matchSelectedCardSize) {
			if (!cardPane.isPreferredSizeSet()
				&& selectionChangeTransition == null) {
	    		int previousSelectedIndex = cardPane.getSelectedIndex();
	    		if (selectedIndex != -1
    				&& previousSelectedIndex != -1) {
		    		Component oldCard = cardPane.get(previousSelectedIndex);
		    		Component newCard = cardPane.get(selectedIndex);

		    		selectionChangeTransition = new SelectionChangeTransition(oldCard, newCard,
	    				SELECTION_CHANGE_DURATION, SELECTION_CHANGE_RATE);

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
	    		vote = selectionChangeTransition.isRunning() ? Vote.DENY : Vote.APPROVE;
	    	}
    	}

    	return vote;
    }

    public void selectedIndexChangeVetoed(CardPane cardPane, Vote reason) {
    	if (reason == Vote.DENY
			&& selectionChangeTransition != null) {
    		selectionChangeTransition.stop();
    		selectionChangeTransition = null;
    	}
    }

    public void selectedIndexChanged(CardPane cardPane, int previousSelectedIndex) {
        if (previousSelectedIndex != -1) {
        	Component oldCard = cardPane.get(previousSelectedIndex);
        	oldCard.setVisible(false);
        }

        int selectedIndex = cardPane.getSelectedIndex();
        if (selectedIndex != -1) {
        	Component newCard = cardPane.get(selectedIndex);
        	newCard.setVisible(true);
        }
    }
}
