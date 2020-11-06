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
package fish.payara.micro;

import com.intellij.debugger.engine.RemoteStateState;
import com.intellij.debugger.impl.DebuggerManagerImpl;
import com.intellij.execution.*;
import com.intellij.execution.configurations.*;
import com.intellij.execution.executors.DefaultDebugExecutor;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jdom.Element;

import javax.swing.*;

import static fish.payara.PayaraConstants.DEFAULT_DEBUG_PORT;

public class DebuggerManager {

    public static void connect(Project project) {
        RunnerAndConfigurationSettings runSettings = RunManager.getInstance(project)
                .createConfiguration(project.getName(), ProcessAttachRunConfigurationType.FACTORY);
        ProgramRunnerUtil.executeConfiguration(runSettings, ProcessAttachDebugExecutor.INSTANCE);
    }

    public static class ProcessAttachRunConfigurationType implements ConfigurationType {

        static final ProcessAttachRunConfigurationType INSTANCE = new ProcessAttachRunConfigurationType();
        static final ConfigurationFactory FACTORY = new ConfigurationFactory(INSTANCE) {

            @Override
            public RunConfiguration createTemplateConfiguration(Project project) {
                return new ProcessAttachRunConfiguration(project);
            }

            @Override
            public String getId() {
                return INSTANCE.getId();
            }
        };

        @Override
        public String getDisplayName() {
            return getId();
        }

        @Override
        public String getConfigurationTypeDescription() {
            return getId();
        }

        @Override
        public Icon getIcon() {
            return null;
        }

        @Override
        public String getId() {
            return ProcessAttachRunConfigurationType.class.getSimpleName();
        }

        @Override
        public ConfigurationFactory[] getConfigurationFactories() {
            return new ConfigurationFactory[]{FACTORY};
        }
    }

    public static class ProcessAttachRunConfiguration extends RunConfigurationBase<Element> {

        protected ProcessAttachRunConfiguration(Project project) {
            super(
                    project,
                    ProcessAttachRunConfigurationType.FACTORY,
                    ProcessAttachRunConfiguration.class.getSimpleName()
            );
        }

        @Override
        public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
            throw new IllegalStateException("Configuration editor not supported");
        }

        @Override
        public RunProfileState getState(Executor executor, ExecutionEnvironment environment) {
            String host = DebuggerManagerImpl.LOCALHOST_ADDRESS_FALLBACK;
            String port = String.valueOf(DEFAULT_DEBUG_PORT);
            RemoteConnection connection = new RemoteConnection(true, host, port, true);
            return new RemoteStateState(getProject(), connection);
        }
    }

    static class ProcessAttachDebugExecutor extends DefaultDebugExecutor {

        static ProcessAttachDebugExecutor INSTANCE = new ProcessAttachDebugExecutor();

        private ProcessAttachDebugExecutor() {
        }

        @Override
        public String getId() {
            return "ProcessAttachDebug";
        }
    }

}
