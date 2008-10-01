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
 * Date picker button listener interface.
 *
 * @author tvolkert
 */
public interface DatePickerButtonListener {
    /**
     * Called when a date picker button's year value has changed.
     *
     * @param datePickerButton
     * @param previousYear
     */
    public void yearChanged(DatePickerButton datePickerButton, int previousYear);

    /**
     * Called when a date picker button's month value has changed.
     *
     * @param datePickerButton
     * @param previousMonth
     */
    public void monthChanged(DatePickerButton datePickerButton, int previousMonth);

    /**
     * Called when a date picker button's selected date key has changed.
     *
     * @param datePickerButton
     * @param previousSelectedDateKey
     */
    public void selectedDateKeyChanged(DatePickerButton datePickerButton,
        String previousSelectedDateKey);
}
