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
package org.apache.pivot.wtk;

/**
 * Base interface for content editors.
 */
public interface Editor {
    /**
     * Tells whether or not an edit is currently in progress.
     */
    public boolean isEditing();

    /**
     * Saves an edit that is in progress by updating the appropriate data
     * object. It is up to implementations to define the behavior when
     * <tt>isEditing() == false</tt>.
     *
     * @return
     * <tt>true</tt> if the changes were successfully saved; <tt>false</tt> otherwise.
     */
    public boolean saveChanges();

    /**
     * Cancels an edit that is in progress by reverting any edits the user has
     * made. It is up to implementations to define the behavior when
     * <tt>isEditing() == false</tt>.
     */
    public void cancelEdit();
}
