/*
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
package org.apache.pivot.wtk.validation;

/**
 * Validation interface for text components. Allows the programmer to specify
 * constraints on text data.
 * <p>
 * This is indicated visually to the user (a red background would be typical),
 * and events are fired by the TextInput if the programmer wishes to take
 * further action.
 */
public interface Validator {
    /**
     * Determines if a text value is valid based on the rules of the
     * validator.
     *
     * @param text
     *
     * @return
     * <tt>true</tt> if the value is valid; <tt>false</tt>, otherwise.
     */
    public boolean isValid(String text);
}
