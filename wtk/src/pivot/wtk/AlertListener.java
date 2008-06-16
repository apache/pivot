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

import pivot.collections.List;

public interface AlertListener {
    /**
     * Called when the type of an alert has changed.
     *
     * @param alert
     * The source of the event.
     *
     * @param previousBody
     * The previous alert type.
     */
    public void typeChanged(Alert alert, Alert.Type previousType);

    /**
     * Called when the subject of a alert has changed.
     *
     * @param alert
     * The source of the event.
     *
     * @param previousSubject
     * The previous alert subject.
     */
    public void subjectChanged(Alert alert, String previousSubject);

    /**
     * Called when the body of a alert has changed.
     *
     * @param alert
     * The source of the event.
     *
     * @param previousBody
     * The previous alert body.
     */
    public void bodyChanged(Alert alert, Component previousBody);

    /**
     * Called when the option data of an alert has changed.
     *
     * @param alert
     * The source of the event.
     *
     * @param previousBody
     * The previous option data.
     */
    public void optionDataChanged(Alert alert, List<String> previousOptionData);
}
