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

public interface ScrollBarListener {
    public void orientationChanged(ScrollBar scrollBar, Orientation previousOrientation);
    public void scopeChanged(ScrollBar scrollBar, int previousRangeStart,
        int previousRangeEnd, int previousExtent);
    public void unitIncrementChanged(ScrollBar scrollBar, int previousUnitIncrement);
    public void blockIncrementChanged(ScrollBar scrollBar, int previousBlockIncrement);
}
