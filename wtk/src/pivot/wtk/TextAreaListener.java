/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
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

import pivot.wtk.text.Document;

/**
 * Text area listener interface.
 *
 * @author gbrown
 */
public interface TextAreaListener {
    /**
     * Text area listener adapter.
     *
     * @author tvolkert
     */
    public static class Adapter implements TextAreaListener {
        public void documentChanged(TextArea textArea, Document previousDocument) {
        }

        public void editableChanged(TextArea textArea) {
        }

        public void textKeyChanged(TextArea textArea, String previousTextKey) {
        }
    }

    /**
     * Called when a text area's document has changed.
     *
     * @param textArea
     * @param previousDocument
     */
    public void documentChanged(TextArea textArea, Document previousDocument);

    /**
     * Called when a text area's editable state has changed.
     *
     * @param textArea
     */
    public void editableChanged(TextArea textArea);

    /**
     * Called when a text area's text key has changed.
     *
     * @param textArea
     * @param previousTextKey
     */
    public void textKeyChanged(TextArea textArea, String previousTextKey);
}
