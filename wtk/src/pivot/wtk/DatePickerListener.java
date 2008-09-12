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
 * <p>Date picker listener interface.</p>
 *
 * @author tvolkert
 */
public interface DatePickerListener {
    /**
     * Called when a date picker's year value has changed.
     *
     * @param datePicker
     * @param previousYear
     */
    public void yearChanged(DatePicker datePicker, int previousYear);

    /**
     * Called when a date picker's month value has changed.
     *
     * @param datePicker
     * @param previousMonth
     */
    public void monthChanged(DatePicker datePicker, int previousMonth);

    /**
     * Called when a date picker's selected date key has changed.
     *
     * @param datePicker
     * @param previousSelectedDateKey
     */
    public void selectedDateKeyChanged(DatePicker datePicker,
        String previousSelectedDateKey);
}
