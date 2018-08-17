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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;

/**
 * Pivot script application launch shortcut.
 */
public class PivotScriptApplicationLaunchShortcut implements ILaunchShortcut {
    public static final String APPLICATION_TYPE_NAME = "org.apache.pivot.wtk.ScriptApplication";
    public static final String SRC_ARGUMENT = "src";

    @Override
    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structuredSelection = (IStructuredSelection) selection;
            Object[] elements = structuredSelection.toArray();

            if (elements.length == 1) {
                launch(elements[0], mode);
            }
        }
    }

    @Override
    public void launch(IEditorPart editor, String mode) {
        throw new UnsupportedOperationException();
    }

    private void launch(Object element, String mode) {
        if (element instanceof IAdaptable) {
            IFile file = (IFile) ((IAdaptable) element).getAdapter(IFile.class);

            if (file != null) {
                ILaunchConfiguration launchConfiguration = getExistingLaunchConfiguration(file);

                if (launchConfiguration == null) {
                    launchConfiguration = createLaunchConfiguration(file);
                }

                if (launchConfiguration != null) {
                    DebugUITools.launch(launchConfiguration, mode);
                }
            }
        }
    }

    private ILaunchConfiguration getExistingLaunchConfiguration(IFile file) {
        ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
        ILaunchConfigurationType launchConfigurationType =
            launchManager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);

        ILaunchConfiguration existingLaunchConfiguration = null;
        try {
            String fileProjectName = file.getProject().getName();

            ILaunchConfiguration[] launchConfigurations =
                DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(launchConfigurationType);

            for (int i = 0; i < launchConfigurations.length; i++) {
                ILaunchConfiguration launchConfiguration = launchConfigurations[i];

                String mainTypeName = launchConfiguration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, "");
                String projectName = launchConfiguration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, "");
                String programArguments = launchConfiguration.getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, "");

                if (mainTypeName.equals(PivotPlugin.MAIN_TYPE_NAME)
                    && projectName.equals(fileProjectName)
                    && programArguments.equals(getProgramArguments(file))) {
                    existingLaunchConfiguration = launchConfiguration;
                    break;
                }
            }
        } catch (CoreException exception) {
            MessageDialog.openError(PivotPlugin.getActiveWorkbenchShell(), exception.getMessage(),
                exception.getStatus().getMessage());
        }

        return existingLaunchConfiguration;
    }

    protected ILaunchConfiguration createLaunchConfiguration(IFile file) {
        ILaunchConfiguration launchConfiguration = null;

        try {
            String fileProjectName = file.getProject().getName();
            String fileName = file.getName();

            ILaunchManager launchManager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfigurationType configurationType =
                launchManager.getLaunchConfigurationType(IJavaLaunchConfigurationConstants.ID_JAVA_APPLICATION);
            String name = launchManager.generateUniqueLaunchConfigurationNameFrom(fileName);

            ILaunchConfigurationWorkingCopy workingLaunchConfiguration = configurationType.newInstance(
                null, name);
            workingLaunchConfiguration.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_MAIN_TYPE_NAME, PivotPlugin.MAIN_TYPE_NAME);
            workingLaunchConfiguration.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, fileProjectName);
            workingLaunchConfiguration.setAttribute(
                IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, getProgramArguments(file));
            workingLaunchConfiguration.setMappedResources(new IResource[] {file});

            launchConfiguration = workingLaunchConfiguration.doSave();
        } catch (CoreException exception) {
            MessageDialog.openError(PivotPlugin.getActiveWorkbenchShell(), exception.getMessage(),
                exception.getStatus().getMessage());
        }

        return launchConfiguration;
    }

    private static final String getProgramArguments(IFile file) {
        IContainer parent = file.getParent();
        IJavaElement javaElement = (IJavaElement) parent.getAdapter(IJavaElement.class);
        String src = "/" + javaElement.getElementName().replace('.', '/') + "/" + file.getName();

        return APPLICATION_TYPE_NAME + " " + "--" + SRC_ARGUMENT + "=" + src;
    }
}
