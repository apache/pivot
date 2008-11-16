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

import pivot.collections.Dictionary;
import pivot.serialization.JSONSerializer;
import pivot.util.ListenerList;

/**
 * Allows a user to select one of a range of values.
 *
 * @author gbrown
 */
public class Slider extends Container {
	private static class SliderListenerList extends ListenerList<SliderListener>
		implements SliderListener {
		public void boundsChanged(Slider slider, int previousMinimum, int previousMaximum) {
			for (SliderListener listener : this) {
				listener.boundsChanged(slider, previousMinimum, previousMaximum);
			}
		}
	}

	private static class SliderValueListenerList extends ListenerList<SliderValueListener>
		implements SliderValueListener {
		public void valueChanged(Slider slider, int previousValue) {
			for (SliderValueListener listener : this) {
				listener.valueChanged(slider, previousValue);
			}
		}
	}

	private int minimum = DEFAULT_MINIMUM;
	private int maximum = DEFAULT_MAXIMUM;
	private int value = DEFAULT_VALUE;

	private SliderListenerList sliderListeners = new SliderListenerList();
	private SliderValueListenerList sliderValueListeners = new SliderValueListenerList();

	public static final int DEFAULT_MINIMUM = 0;
	public static final int DEFAULT_MAXIMUM = 100;
	public static final int DEFAULT_VALUE = 0;

	public static final String MINIMUM_KEY = "minimum";
	public static final String MAXIMUM_KEY = "maximum";

	public Slider() {
		installSkin(Slider.class);
	}

	public int getMinimum() {
		return minimum;
	}

	public void setMinimum(int minimum) {
		setBounds(minimum, maximum);
	}

	public int getMaximum() {
		return maximum;
	}

	public void setMaximum(int maximum) {
		setBounds(minimum, maximum);
	}

	public void setBounds(int minimum, int maximum) {
		if (minimum > maximum) {
			throw new IllegalArgumentException("minimum is greater than maximum.");
		}

		int previousMinimum = this.minimum;
		int previousMaximum = this.maximum;
		int previousValue = this.value;

		if (minimum != previousMinimum
			|| maximum != previousMaximum) {
			this.minimum = minimum;
			if (value < minimum) {
				this.value = minimum;
			}

			this.maximum = maximum;
			if (value > maximum) {
				this.value = maximum;
			}

			sliderListeners.boundsChanged(this, previousMinimum, previousMaximum);

			if (previousValue < minimum
				|| previousValue > maximum) {
				sliderValueListeners.valueChanged(this, previousValue);
			}
		}
	}

	public final void setBounds(Dictionary<String, ?> bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds is null.");
        }

        int minimum = DEFAULT_MINIMUM;
        if (bounds.containsKey(MINIMUM_KEY)) {
            minimum = ((Number)bounds.get(MINIMUM_KEY)).intValue();
        }

        int maximum = DEFAULT_MAXIMUM;
        if (bounds.containsKey(MAXIMUM_KEY)) {
        	maximum = ((Number)bounds.get(MAXIMUM_KEY)).intValue();
        }

        setBounds(minimum, maximum);
	}

	public final void setBounds(String bounds) {
        if (bounds == null) {
            throw new IllegalArgumentException("bounds is null.");
        }

        setBounds(JSONSerializer.parseMap(bounds));
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		if (value < minimum) {
			throw new IllegalArgumentException("value is less than minimum.");
		}

		if (value > maximum) {
			throw new IllegalArgumentException("value is greater than maximum.");
		}

		int previousValue = this.value;

		if (value != previousValue) {
			this.value = value;
			sliderValueListeners.valueChanged(this, previousValue);
		}
	}

	public ListenerList<SliderListener> getSliderListeners() {
		return sliderListeners;
	}

    public void setSliderListener(SliderListener listener) {
        sliderListeners.add(listener);
    }

    public ListenerList<SliderValueListener> getSliderValueListeners() {
		return sliderValueListeners;
	}

    public void setSliderValueListener(SliderValueListener listener) {
        sliderValueListeners.add(listener);
    }
}
