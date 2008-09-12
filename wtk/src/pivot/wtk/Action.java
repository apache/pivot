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
 * <p>Interface representing an action. Actions are common application behaviors
 * that are generally added to a window's global action map and triggered by
 * user interface elements such as buttons and menus.</p>
 *
 * @author gbrown
 */
public interface Action {
    /**
     * Returns a text description of the action.
     */
    public String getDescription();

    /**
     * Performs the action.
     */
    public void perform();
}
