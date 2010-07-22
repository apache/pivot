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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.debug.ui.launchConfigurations.JavaLaunchShortcut;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.operation.IRunnableContext;

public class PivotApplicationLaunchShortcut extends JavaLaunchShortcut {
    @Override
    protected ILaunchConfiguration createConfiguration(IType type) {
        // TODO
        return null;
    }

    @Override
    protected ILaunchConfigurationType getConfigurationType() {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        return launchManager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
    }

    @Override
    protected IType[] findTypes(Object[] elements, IRunnableContext context)
        throws InterruptedException, CoreException {
        // TODO
        return null;
    }

    @Override
    protected String getTypeSelectionTitle() {
        return "Select Type"; // TODO i18n
    }

    @Override
    protected String getEditorEmptyMessage() {
        return "Editor does not contain a launchable type."; // TODO i18n
    }

    @Override
    protected String getSelectionEmptyMessage() {
        return "Selection does not contain a launchable type."; // TODO i18n
    }
}
