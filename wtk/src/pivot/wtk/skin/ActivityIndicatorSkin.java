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
package pivot.wtk.skin;

import pivot.wtk.ActivityIndicator;
import pivot.wtk.ActivityIndicatorListener;
import pivot.wtk.Component;

/**
 * Abstract base class for activity indicator skins.
 *
 * @author gbrown
 */
public abstract class ActivityIndicatorSkin extends ComponentSkin
    implements ActivityIndicatorListener {
    @Override
    public void install(Component component) {
        super.install(component);

        ActivityIndicator activityIndicator = (ActivityIndicator)component;
        activityIndicator.getActivityIndicatorListeners().add(this);
    }

    @Override
    public void uninstall() {
        ActivityIndicator activityIndicator = (ActivityIndicator)getComponent();
        activityIndicator.getActivityIndicatorListeners().remove(this);

        super.uninstall();
    }

    public void layout() {
        // No-op
    }
}
