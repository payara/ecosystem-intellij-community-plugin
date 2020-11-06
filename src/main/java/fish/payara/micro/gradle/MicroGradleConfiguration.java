/*
 * Copyright (c) 2020 Payara Foundation and/or its affiliates and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package fish.payara.micro.gradle;

import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.externalSystem.model.execution.ExternalSystemTaskExecutionSettings;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunConfiguration;
import com.intellij.openapi.externalSystem.service.execution.ExternalSystemRunnableState;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.util.execution.ParametersListUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.gradle.service.execution.GradleRunConfiguration;

import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

public class MicroGradleConfiguration extends GradleRunConfiguration {

    protected MicroGradleConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    @NotNull
    @Override
    public SettingsEditor<ExternalSystemRunConfiguration> getConfigurationEditor() {
        ExternalSystemTaskExecutionSettings settings = super.getSettings();
        if (settings.getExternalProjectPath() == null
                || settings.getExternalProjectPath().isEmpty()) {
            settings.setExternalProjectPath(super.getProject().getBasePath());
        }
        if (settings.getTaskNames().isEmpty()) {
            settings.getTaskNames().add(GradleProject.BUILD_GOAL);
            settings.getTaskNames().add(GradleProject.START_GOAL);
        }
        return super.getConfigurationEditor();
    }

    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment env) {
        RunProfileState state = super.getState(executor, env);
        String scriptParameters = getSettings().getScriptParameters();
        if (state instanceof ExternalSystemRunnableState) {
            ExternalSystemRunnableState runnableState = (ExternalSystemRunnableState) state;
            int debugPort = runnableState.getDebugPort();
            final List<String> arguments = scriptParameters != null ? ParametersListUtil.parse(scriptParameters, false, true) : new ArrayList<>();
            boolean debugArgExist = arguments.stream()
                    .anyMatch(a -> a.startsWith(GradleProject.DEBUG_PROPERTY_NAME));
            List<String> args = arguments.stream()
                    .filter(a -> !a.startsWith(GradleProject.DEBUG_PROPERTY_NAME))
                    .collect(toList());
            if (debugPort > 0) {
                args.add(String.format(GradleProject.DEBUG_PROPERTY, debugPort));
                getSettings().setScriptParameters(String.join(" ", args));
            } else if (debugArgExist) {
                getSettings().setScriptParameters(
                        String.join(" ", args)
                );
            }
        }
        return state;
    }

}
