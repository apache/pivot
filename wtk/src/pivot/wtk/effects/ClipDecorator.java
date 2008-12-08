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
package pivot.wtk.effects;

import java.awt.Graphics2D;

import pivot.wtk.Bounds;
import pivot.wtk.Component;

/**
 * Decorator that adds a rectangular region to the current clip.
 *
 * @author gbrown
 */
public class ClipDecorator implements Decorator {
	private int x = 0;
	private int y = 0;
	private int width = 0;
	private int height = 0;

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Graphics2D prepare(Component component, Graphics2D graphics) {
		graphics.clipRect(x, y, width, height);
		return graphics;
	}

	public void update() {
		// No-op
	}

	public Bounds getAffectedArea(Component component, int x, int y, int width, int height) {
		return new Bounds(x, y, width, height);
	}
}
