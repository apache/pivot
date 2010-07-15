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
package org.apache.pivot.eclipse;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchTab;
import org.eclipse.swt.widgets.Composite;

public class ScriptApplicationMainTab extends JavaLaunchTab {
    public static final String NAME = "Main";
    
    @Override
    public void createControl(Composite parent) {
        // TODO
    }

    @Override
    public String getName() {
        // TODO Localize
        return NAME;
    }

    @Override
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        // TODO
    }

    @Override
    public void initializeFrom(ILaunchConfiguration configuration) {
        // TODO         
    }
    
    @Override
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        // TODO
    }
    
    @Override
    public boolean isValid(ILaunchConfiguration configuration) {
        // TODO
        return true;
    }
}
