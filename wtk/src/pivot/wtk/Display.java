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

import pivot.wtk.skin.DisplaySkin;

public final class Display extends Container {
    private class ValidateCallback implements Runnable {
        public void run() {
            validate();
        }
    }

    private static Display instance = new Display();

	private Display() {
	    super();

		assert (instance == null) : "Display already exists.";

		super.setSkinClass(DisplaySkin.class);
	}

	@Override
	public void setSkinClass(Class<? extends Skin> skinClass) {
	    throw new UnsupportedOperationException("Can't replace Display skin.");
	}

	@Override
	public void setLocation(int x, int y) {
	    throw new UnsupportedOperationException("Can't change the location of the display.");
	}

    @Override
    public void invalidate() {
        if (isValid()) {
            super.invalidate();
            ApplicationContext.queueCallback(new ValidateCallback());
        }
    }

    @Override
    public void insert(Component component, int index) {
        if (!(component instanceof Window)) {
            throw new IllegalArgumentException("component must be an instance "
                + "of " + Window.class);
        }

        super.insert(component, index);
    }

    public static Display getInstance() {
		return instance;
	}
}
