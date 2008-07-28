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
 * <tt>DialogCloseHandler</tt> instances may be attached to <tt>Dialog</tt>
 * objects to intercept the closing of the dialog and validate that the dialog
 * is allowed to close.
 *
 * @author tvolkert
 */
public interface DialogCloseHandler {
    /**
     * Handles a dialog that's closing. This controls whether the dialog is
     * allowed to close. Returning <tt>false</tt> from this handler method
     * will abort the close.
     * <p>
     * When set on a dialog, this method will automatically be called
     * immediately before the dialog closes itself.
     *
     * @param dialog
     * The dialog being closed.
     * @param result
     * The result of the dialog (whether the user closed the dialog with input
     * or aborted the dialog).
     * @return
     * <tt>true</tt> to allow the dialog to close; <tt>false</tt> to prevent it
     * from closing.
     */
    public boolean close(Dialog dialog, boolean result);
}
