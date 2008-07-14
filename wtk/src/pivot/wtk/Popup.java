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

/**
 * Represents a "popup" window. A popup is a non-activatable window that is
 * optionally associated with an "affiliate" component. A popup closes
 * automatically when:
 * <ul>
 * <li>A component mouse down event or a container mouse wheel event occurs
 * outside its bounds.</li>
 * <li>The absolute location of its affiliate component changes.</li>
 * <li>The absolute visibility of its affiliate component (the affiliate's
 * "showing" state) changes.</li>
 * <li>The affiliate's ancestry changes.</li>
 * </ul>
 *
 * @author gbrown
 */
public class Popup extends Window {
    private Component affiliate = null;

    /**
     * Creates a new popup.
     */
    public Popup() {
        this(null);
    }

    /**
     * Creates a new popup with an initial content component.
     *
     * @param content
     * The popup's content component.
     */
    public Popup(Component content) {
        super(content);

        installSkin(Popup.class);
    }

    /**
     * Returns the popup's affiliate component.
     *
     * @return
     * The component with which this popup is affiliated, or <tt>null</tt> if
     * the popup has no affiliate.
     */
    public Component getAffiliate() {
        return affiliate;
    }

    /**
     * @return
     * <tt>true</tt>; by default, popups are auxilliary windows.
     */
    @Override
    public boolean isAuxilliary() {
        return true;
    }

    /**
     * Opens the popup.
     *
     * @param affiliate
     * The component with which the popup is affiliated.
     */
    public void open(Component affiliate) {
        if (isOpen()) {
            throw new IllegalStateException("Popup is already open.");
        }

        if (affiliate == null) {
            throw new IllegalArgumentException("affiliate is null.");
        }

        this.affiliate = affiliate;

        super.open(affiliate.getWindow());
    }

    @Override
    public void close() {
        affiliate = null;

        super.close();
    }
}
