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
package pivot.wtk.skin.terra;

import pivot.collections.Sequence;
import pivot.util.Vote;
import pivot.wtk.Component;
import pivot.wtk.Container;
import pivot.wtk.Dimensions;
import pivot.wtk.Sheet;
import pivot.wtk.effects.FadeDecorator;
import pivot.wtk.effects.Transition;
import pivot.wtk.effects.TransitionListener;
import pivot.wtk.skin.ContainerSkin;

/**
 * Terra sheet component pane skin.
 *
 * @author gbrown
 */
public class TerraSheetComponentPaneSkin extends ContainerSkin
    implements Sheet.ComponentPaneListener {
    /**
     * Class that performs selection change transitions.
     *
     * @author gbrown
     */
    public class SelectionChangeTransition extends Transition {
        private FadeDecorator fadeOutDecorator = new FadeDecorator();
        private FadeDecorator fadeInDecorator = new FadeDecorator();

        private int from;
        private int to;

        public SelectionChangeTransition(int from, int to) {
            super(SELECTION_CHANGE_DURATION, SELECTION_CHANGE_RATE, false);

            this.from = from;
            this.to = to;
        }

        public int getFrom() {
            return from;
        }

        public Component getFromPanel() {
            Sheet.ComponentPane componentPane = (Sheet.ComponentPane)getComponent();
            return (from == -1) ? null : componentPane.get(from);
        }

        public int getTo() {
            return to;
        }

        public Component getToPanel() {
            Sheet.ComponentPane componentPane = (Sheet.ComponentPane)getComponent();
            return (to == -1) ? null : componentPane.get(to);
        }

        @Override
        public void start(TransitionListener transitionListener) {
            Component fromPanel = getFromPanel();
            if (fromPanel != null) {
                fromPanel.getDecorators().add(fadeOutDecorator);
            }

            Component toPanel = getToPanel();
            if (toPanel != null) {
                toPanel.getDecorators().add(fadeInDecorator);
                toPanel.setSize(toPanel.getPreferredSize());
                toPanel.setVisible(true);
            }

            super.start(transitionListener);
        }

        @Override
        public void stop() {
            super.stop();

            Component fromPanel = getFromPanel();
            if (fromPanel != null) {
                fromPanel.getDecorators().remove(fadeOutDecorator);
                fromPanel.setVisible(false);
            }

            Component toPanel = getToPanel();
            if (toPanel != null) {
                toPanel.getDecorators().remove(fadeInDecorator);
            }
        }

        @Override
        protected void update() {
            float percentComplete = getPercentComplete();

            int width = getWidth();
            int height = getHeight();

            // Center components
            Component fromPanel = getFromPanel();
            if (fromPanel != null) {
                fromPanel.setLocation((width - fromPanel.getWidth()) / 2,
                    (height - fromPanel.getHeight()) / 2);
            }

            Component toPanel = getToPanel();
            if (toPanel != null) {
                toPanel.setLocation((width - toPanel.getWidth()) / 2,
                    (height - toPanel.getHeight()) / 2);
            }

            fadeOutDecorator.setOpacity(1.0f - percentComplete);
            fadeInDecorator.setOpacity(percentComplete);

            invalidateComponent();
        }
    }

    private SelectionChangeTransition selectionChangeTransition = null;

    public static final int SELECTION_CHANGE_DURATION = 250;
    public static final int SELECTION_CHANGE_RATE = 30;

    public void install(Component component) {
        super.install(component);

        Sheet.ComponentPane componentPane = (Sheet.ComponentPane)component;
        componentPane.getComponentPaneListeners().add(this);
    }

    public void uninstall() {
        Sheet.ComponentPane componentPane = (Sheet.ComponentPane)getComponent();
        componentPane.getComponentPaneListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        Dimensions preferredSize = getPreferredSize();
        return preferredSize.width;
    }

    public int getPreferredHeight(int width) {
        Dimensions preferredSize = getPreferredSize();
        return preferredSize.height;
    }

    public Dimensions getPreferredSize() {
        Dimensions preferredSize;

        Sheet.ComponentPane componentPane = (Sheet.ComponentPane)getComponent();

        if (selectionChangeTransition == null) {
            Component selectedPanel = componentPane.getSelectedPanel();

            if (selectedPanel == null) {
                preferredSize = new Dimensions(0, 0);
            } else {
                preferredSize = selectedPanel.getPreferredSize();
            }
        } else {
            float percentComplete = selectionChangeTransition.getPercentComplete();

            int previousWidth;
            int previousHeight;
            Component fromPanel = selectionChangeTransition.getFromPanel();

            if (fromPanel == null) {
                previousWidth = 0;
                previousHeight = 0;
            } else {
                Dimensions fromSize = fromPanel.getPreferredSize();
                previousWidth = fromSize.width;
                previousHeight = fromSize.height;
            }

            int width;
            int height;
            Component toPanel = selectionChangeTransition.getToPanel();

            if (toPanel == null) {
                width = 0;
                height = 0;
            } else {
                Dimensions toSize = toPanel.getPreferredSize();
                width = toSize.width;
                height = toSize.height;
            }

            int preferredWidth = previousWidth + (int)((float)(width - previousWidth) * percentComplete);
            int preferredHeight = previousHeight + (int)((float)(height - previousHeight) * percentComplete);

            preferredSize = new Dimensions(preferredWidth, preferredHeight);
        }

        return preferredSize;
    }

    public void layout() {
        Sheet.ComponentPane componentPane = (Sheet.ComponentPane)getComponent();
        int width = getWidth();
        int height = getHeight();

        for (Component panel : componentPane) {
            // If the panel is visible, set its size and location
            if (panel.isVisible()) {
                panel.setLocation(0, 0);
                panel.setSize(width, height);
            }
        }
    }

    @Override
    public void componentInserted(Container container, int index) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
        }

        super.componentInserted(container, index);

        Component panel = container.get(index);
        panel.setVisible(false);

        invalidateComponent();
    }

    @Override
    public void componentsRemoved(Container container, int index, Sequence<Component> removed) {
        if (selectionChangeTransition != null) {
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
        }

        super.componentsRemoved(container, index, removed);

        for (int i = 0, n = removed.getLength(); i < n; i++){
            Component panel = removed.get(i);
            panel.setVisible(true);
        }

        invalidateComponent();
    }

    public Vote previewSelectedIndexChange(Sheet.ComponentPane componentPane, int selectedIndex) {
        Vote vote;

        if (componentPane.isShowing()
            && selectionChangeTransition == null) {
            int previousSelectedIndex = componentPane.getSelectedIndex();

            selectionChangeTransition =
                new SelectionChangeTransition(previousSelectedIndex, selectedIndex);

            selectionChangeTransition.start(new TransitionListener() {
                public void transitionCompleted(Transition transition) {
                    Sheet.ComponentPane componentPane = (Sheet.ComponentPane)getComponent();

                    SelectionChangeTransition selectionChangeTransition =
                        (SelectionChangeTransition)transition;
                    componentPane.setSelectedIndex(selectionChangeTransition.getTo());
                    TerraSheetComponentPaneSkin.this.selectionChangeTransition = null;

                    invalidateComponent();
                }
            });
        }

        if (selectionChangeTransition == null
            || !selectionChangeTransition.isRunning()) {
            vote = Vote.APPROVE;
        } else {
            vote = Vote.DEFER;
        }

        return vote;
    }

    public void selectedIndexChangeVetoed(Sheet.ComponentPane componentPane, Vote reason) {
        if (reason == Vote.DENY
            && selectionChangeTransition != null) {
            selectionChangeTransition.stop();
            selectionChangeTransition = null;
            invalidateComponent();
        }
    }

    public void selectedIndexChanged(Sheet.ComponentPane componentPane, int previousSelectedIndex) {
        int selectedIndex = componentPane.getSelectedIndex();
        if (selectedIndex != -1) {
            Component selectedPanel = componentPane.get(selectedIndex);
            selectedPanel.setVisible(true);
        }

        if (previousSelectedIndex != -1) {
            Component previousSelectedPanel = componentPane.get(previousSelectedIndex);
            previousSelectedPanel.setVisible(false);
        }

        invalidateComponent();
    }
}
