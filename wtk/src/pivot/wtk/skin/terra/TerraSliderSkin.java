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
package pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Graphics2D;

import pivot.wtk.Component;
import pivot.wtk.ComponentMouseButtonListener;
import pivot.wtk.ComponentMouseListener;
import pivot.wtk.Cursor;
import pivot.wtk.Dimensions;
import pivot.wtk.Display;
import pivot.wtk.Mouse;
import pivot.wtk.Point;
import pivot.wtk.Slider;
import pivot.wtk.skin.ComponentSkin;
import pivot.wtk.skin.SliderSkin;

/**
 * Terra slider skin.
 *
 * @author gbrown
 */
public class TerraSliderSkin extends SliderSkin {
	protected class Thumb extends Component {
		public Thumb() {
			setSkin(new ThumbSkin());
		}
	}

	protected class ThumbSkin extends ComponentSkin {
		public ThumbSkin() {
		}

		public int getPreferredWidth(int height) {
			return 0;
		}

		public int getPreferredHeight(int width) {
			return 0;
		}

		public void layout() {
			// No-op
		}

		public void paint(Graphics2D graphics) {
			int width = getWidth();
			int height = getHeight();
			graphics.setColor(Color.RED);
			graphics.fillRect(0, 0, width, height);
		}

		@Override
	    public void mouseOver(Component component) {
			super.mouseOver(component);

			Mouse.setCursor(Cursor.HAND);
	    }

		@Override
	    public void mouseOut(Component component) {
			if (Mouse.getButtons() == 0) {
				Mouse.setCursor(Cursor.DEFAULT);
			}

			super.mouseOut(component);
	    }

	    @Override
	    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
	        boolean consumed = super.mouseDown(component, button, x, y);

	        if (button == Mouse.Button.LEFT) {
		        Thumb thumb = (Thumb)getComponent();
		        Display display = thumb.getDisplay();
                display.getComponentMouseListeners().add(moveMouseHandler);
                display.getComponentMouseButtonListeners().add(moveMouseHandler);
	        }

	        return consumed;
	    }
	}

    private class MoveMouseHandler implements ComponentMouseListener,
    	ComponentMouseButtonListener {
        public boolean mouseMove(Component component, int x, int y) {
            Display display = (Display)component;

            Slider slider = (Slider)getComponent();
            Point sliderCoordinates = slider.mapPointFromAncestor(display, x, y);

            int sliderX = sliderCoordinates.x;
            int sliderWidth = slider.getWidth();

            if (sliderX < 0) {
            	sliderX = 0;
            }

            if (sliderX > sliderWidth) {
            	sliderX = sliderWidth;
            }

            float ratio = (float)sliderX / sliderWidth;

            int minimum = slider.getMinimum();
            int maximum = slider.getMaximum();

            int value = (int)(minimum + (float)(maximum - minimum) * ratio);
            slider.setValue(value);

            return false;
        }

        public void mouseOver(Component component) {
        }

        public void mouseOut(Component component) {
        }

        public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
            return false;
        }

        public boolean mouseUp(Component component, Mouse.Button button, int x, int y) {
            assert (component instanceof Display);
            component.getComponentMouseListeners().remove(this);
            component.getComponentMouseButtonListeners().remove(this);

            Mouse.setCursor(Cursor.DEFAULT);

            return false;
        }

        public void mouseClick(Component component, Mouse.Button button, int x, int y,
            int count) {
        }
    }

	private Thumb thumb = new Thumb();
    private MoveMouseHandler moveMouseHandler = new MoveMouseHandler();

	private int thumbWidth = 8;
	private int thumbHeight = 16;

	public static final int DEFAULT_WIDTH = 120;

	public TerraSliderSkin() {
	}

	@Override
	public void install(Component component) {
		super.install(component);

		Slider slider = (Slider)component;
		slider.add(thumb);
	}

	public void uninstall() {
		Slider slider = (Slider)getComponent();
		slider.remove(thumb);

		super.uninstall();
	}

	public int getPreferredWidth(int height) {
		return DEFAULT_WIDTH;
	}

	public int getPreferredHeight(int width) {
		return thumbHeight;
	}

	public Dimensions getPreferredSize() {
		return new Dimensions(getPreferredWidth(-1), getPreferredHeight(-1));
	}

	public void layout() {
		Slider slider = (Slider)getComponent();

		int width = getWidth();
		int height = getHeight();

		int minimum = slider.getMinimum();
		int maximum = slider.getMaximum();
		int value = slider.getValue();

		float ratio = (float)(value - minimum) / (maximum - minimum);

		thumb.setSize(thumbWidth, thumbHeight);
		thumb.setLocation((int)((float)(width - thumbWidth) * ratio),
			(height - thumbHeight) / 2);
	}

	public void paint(Graphics2D graphics) {
		super.paint(graphics);

		int width = getWidth();
		int height = getHeight();

		graphics.setColor(Color.GREEN);
		graphics.drawLine(0, height / 2, width, height / 2);
	}

	public void valueChanged(Slider slider, int previousValue) {
		layout();
	}
}
