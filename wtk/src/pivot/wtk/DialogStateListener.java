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
 * <tt>DialogStateListener</tt> instances may be attached to <tt>Dialog</tt>
 * objects to intercept the closing of the dialog and validate that the dialog
 * is allowed to close.
 *
 * @author tvolkert
 */
public interface DialogStateListener {
    /**
     *
     *
     * @param dialog
     *
     * @param result
     *
     * @return
     * <tt>true</tt> to allow the dialog to close; <tt>false</tt> to disallow it
     */
    public boolean previewDialogClose(Dialog dialog, boolean result);

    /**
     *
     *
     * @param dialog
     */
    public void dialogClosed(Dialog dialog);
}
