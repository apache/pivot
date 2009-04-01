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

import pivot.wtk.Component;
import pivot.wtk.Slider;
import pivot.wtk.SliderListener;
import pivot.wtk.SliderValueListener;

/**
 * Abstract base class for slider skins.
 *
 * @author gbrown
 */
public abstract class SliderSkin extends ContainerSkin
	implements SliderListener, SliderValueListener {
	@Override
	public void install(Component component) {
		super.install(component);

		Slider slider = (Slider)component;
		slider.getSliderListeners().add(this);
		slider.getSliderValueListeners().add(this);
	}

	@Override
	public void uninstall() {
		Slider slider = (Slider)getComponent();
		slider.getSliderListeners().remove(this);
		slider.getSliderValueListeners().remove(this);

		super.uninstall();
	}

	public void boundsChanged(Slider slider, int previousMinimum, int previousMaximum) {
		invalidateComponent();
	}

	public void valueChanged(Slider slider, int previousValue) {
		// No-op
	}
}
