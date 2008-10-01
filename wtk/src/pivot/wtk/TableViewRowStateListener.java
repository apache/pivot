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

import pivot.util.Vote;

/**
 * Table view row state listener interface.
 *
 * @author gbrown
 * @author tvolkert
 */
public interface TableViewRowStateListener {
    /**
     * Called to preview a row disabled change event.
     *
     * @param tableView
     * @param index
     */
    public Vote previewRowDisabledChange(TableView tableView, int index);

    /**
     * Called when a row disabled change event has been vetoed.
     *
     * @param tableView
     * @param index
     * @param reason
     */
    public void rowDisabledChangeVetoed(TableView tableView, int index, Vote reason);

    /**
     * Called when a row's disabled state has changed.
     *
     * @param tableView
     * @param index
     */
    public void rowDisabledChanged(TableView tableView, int index);
}
