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

import pivot.collections.Sequence;

/**
 * <p>Form listener interface.</p>
 *
 * @author gbrown
 */
public interface FormListener {
    /**
     * Called when a field has been inserted into a form's field sequence.
     *
     * @param form
     * @param index
     */
    public void fieldInserted(Form form, int index);

    /**
     * Called when a field has been removed from a form's field sequence.
     *
     * @param form
     * @param index
     * @param fields
     */
    public void fieldsRemoved(Form form, int index, Sequence<Component> fields);
}
