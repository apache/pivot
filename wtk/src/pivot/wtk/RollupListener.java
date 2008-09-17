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
 * Defines event listener methods that pertain to rollups. Developers register
 * for such events by adding themselves to a rollup's list of "rollup
 * listeners" (see {@link Rollup#getRollupListeners()}).
 *
 * @author tvolkert
 */
public interface RollupListener {
    /**
     * Called when a rollup's expanded state changed.
     *
     * @param rollup
     */
    public void expandedChanged(Rollup rollup);
}
