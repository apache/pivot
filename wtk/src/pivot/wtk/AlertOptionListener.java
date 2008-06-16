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

public interface AlertOptionListener {
    /**
     * Called when an option has been inserted into the alert dialog.
     *
     * @param alert
     * The source of the event.
     *
     * @param index
     * The index of the option that was inserted.
     */
    public void optionInserted(Alert alert, int index);

    /**
     * Called when options have been removed from the alert dialog.
     *
     * @param alert
     * The source of the event.
     *
     * @param index
     * The first index affected by the event.
     *
     * @param count
     * The number of options that were removed, or <tt>-1</tt> if all options
     * were removed.
     */
    public void optionsRemoved(Alert alert, int index, int count);

    /**
     * Called when an option in the alert dialog has been updated.
     *
     * @param alert
     * The source of the event.
     *
     * @param index
     * The index of the option affected by the event.
     */
    public void optionUpdated(Alert alert, int index);

    /**
     * Called when the options in an alert dialog have been sorted.
     *
     * @param alert
     * The source of the event.
     */
    public void optionsSorted(Alert alert);
}
