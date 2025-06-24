/*
 * Copyright (c) 2025 Payara Foundation and/or its affiliates and others.
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
package fish.payara.server.maven;

import com.intellij.debugger.impl.DebuggerManagerImpl;
import com.intellij.debugger.settings.DebuggerSettings;
import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.ParametersList;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RemoteConnectionCreator;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.execution.util.JavaParametersUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

public class ServerMavenRemoteConnectionCreator implements RemoteConnectionCreator {

    private final JavaParameters javaParameters;
    private final ServerMavenConfiguration runConfiguration;

    ServerMavenRemoteConnectionCreator(JavaParameters javaParameters, ServerMavenConfiguration runConfiguration) {
        this.javaParameters = javaParameters;
        this.runConfiguration = runConfiguration;
    }

    @Nullable
    @Override
    public RemoteConnection createRemoteConnection(ExecutionEnvironment environment) {
        ParametersList programParametersList = javaParameters.getProgramParametersList();

        String port = runConfiguration.getDebugPort();
        programParametersList.add(String.format(ServerMavenProject.DEBUG_PROPERTY, port));

        Project project = runConfiguration.getProject();

        JavaParameters parameters = new JavaParameters();
        RemoteConnection connection;
        try {
            parameters.setJdk(JavaParametersUtil.createProjectJdk(project, null));
            connection = DebuggerManagerImpl.createDebugParameters(
                    parameters,
                    true,
                    DebuggerSettings.getInstance().getTransport(),
                    port,
                    false
            );
        } catch (ExecutionException e) {
            throw new RuntimeException("Cannot create the debug connection", e);
        }

        return connection;
    }

    @Override
    public boolean isPollConnection() {
        return false;
    }

}
