/*
 * Copyright (c) 2024 Payara Foundation and/or its affiliates and others.
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
package fish.payara.cloud.maven;

import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.JavaParameters;
import com.intellij.execution.configurations.RemoteConnection;
import com.intellij.execution.configurations.RemoteConnectionCreator;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import com.intellij.execution.runners.ExecutionEnvironment;
import fish.payara.PayaraBundle;
import fish.payara.PayaraConstants;
import com.intellij.notification.*;
import com.intellij.openapi.util.JDOMExternalizerUtil;
import org.jetbrains.annotations.NotNull;
import org.jdom.Element;
import org.jetbrains.idea.maven.execution.MavenRunConfiguration;
import org.jetbrains.idea.maven.execution.MavenRunnerParameters;
import org.jetbrains.idea.maven.execution.run.configuration.MavenRunConfigurationSettingsEditor;

public class CloudMavenConfiguration extends MavenRunConfiguration {

    private String goals = CloudMavenProject.DEV_GOAL;

    protected CloudMavenConfiguration(Project project, ConfigurationFactory factory, String name) {
        super(project, factory, name);
    }

    public String getGoals() {
        return goals;
    }

    public void setGoals(String goals) {
        this.goals = goals;
    }

    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        MavenRunnerParameters parameters = this.getRunnerParameters();

        if (parameters != null) {
            if (parameters.getWorkingDirPath().isEmpty()
                    && super.getProject().getBasePath() != null) {
                parameters.setWorkingDirPath(super.getProject().getBasePath());
            }
            if (parameters.getGoals().isEmpty()) {
                CloudMavenProject mavenProject = CloudMavenProject.getInstance(super.getProject());
                if (mavenProject == null) {
                    parameters.getGoals().add(String.format(
                            "%s:%s:%s:%s",
                            CloudMavenProject.CLOUD_GROUP_ID,
                            CloudMavenProject.CLOUD_ARTIFACT_ID,
                            CloudMavenProject.CLOUD_VERSION,
                            getGoals()
                    ));
                } else {
                    parameters.getGoals().add(String.format(
                            "%s:%s",
                            CloudMavenProject.CLOUD_PLUGIN,
                            getGoals()
                    ));
                }
            }
        }
       return LazyEditorFactory.create(this);
    }
    private static final class LazyEditorFactory {
        static @NotNull SettingsEditor<? extends RunConfiguration> create(@NotNull CloudMavenConfiguration configuration) {
            return new CloudSettingsEditor(configuration.getProject());
        }
    }

    @Override
    public RemoteConnectionCreator createRemoteConnectionCreator(JavaParameters javaParameters) {
        return new RemoteConnectionCreator() {
            public RemoteConnection createRemoteConnection(ExecutionEnvironment environment) {
                Notifications.Bus.notify(
                        new Notification(
                                PayaraBundle.message("CloudDebug.notification.group"),
                                PayaraConstants.CLOUD_ICON,
                                PayaraBundle.message("CloudDebug.notification.group"),
                                "",
                                PayaraBundle.message("CloudDebug.notification.message"),
                                NotificationType.WARNING,
                                NotificationListener.URL_OPENING_LISTENER
                        ), null);
                return null;
            }
            public boolean isPollConnection() {
                return false;
            }
        };
    }
    

}
