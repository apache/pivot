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
 * <p>Button state listener interface.</p>
 *
 * @author gbrown
 * @author tvolkert
 */
public interface ButtonStateListener {
    /**
     * Called to preview a button state change event.
     *
     * @param button
     * @param state
     *
     * @return
     * <tt>true</tt> to allow the event to fire; <tt>false</tt> to veto the
     * event and prevent the change.
     */
    public boolean previewStateChange(Button button, Button.State state);

    /**
     * Called when a state change event has been vetoed.
     *
     * @param button
     */
    public void stateChangeVetoed(Button button);

    /**
     * Called when a button's state has changed.
     *
     * @param button
     * @param previousState
     */
    public void stateChanged(Button button, Button.State previousState);
}
