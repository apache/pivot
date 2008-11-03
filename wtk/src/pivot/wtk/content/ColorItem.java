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
package pivot.wtk.content;

import java.awt.Color;

public class ColorItem {
	private Color color;
	private String name;

	public ColorItem() {
		this(Color.BLACK, null);
	}

	public ColorItem(Color color) {
		this(color, null);
	}

	public ColorItem(Color color, String name) {
		this.color = color;
		this.name = name;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		if (color == null) {
			throw new IllegalArgumentException("color is null.");
		}

		this.color = color;
	}

	public void setColor(String color) {
		if (color == null) {
			throw new IllegalArgumentException("color is null.");
		}

		setColor(Color.decode(color));
	}

	public String getName() {
		String name = this.name;

		if (name == null) {
	        name = String.format("#%02X%02X%02X", color.getRed(), color.getGreen(),
	            color.getBlue());
		}

		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
}
