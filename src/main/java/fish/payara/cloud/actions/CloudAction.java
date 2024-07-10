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
package fish.payara.cloud.actions;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.terminal.JBTerminalWidget;
import com.intellij.ui.content.Content;
import com.jediterm.terminal.TtyConnector;
import fish.payara.PayaraBundle;
import fish.payara.PayaraConstants;
import fish.payara.cloud.maven.CloudMavenProject;
import fish.payara.cloud.PayaraCloudProject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.terminal.LocalTerminalDirectRunner;
import org.jetbrains.plugins.terminal.ShellTerminalWidget;
import org.jetbrains.plugins.terminal.TerminalTabState;
import org.jetbrains.plugins.terminal.TerminalView;

import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author gaurav.gupta@payara.fish
 */
public abstract class CloudAction extends AnAction {

    private static final Logger LOG = Logger.getLogger(CloudAction.class.getName());

    private static final String TOOL_WINDOW_ID = "Terminal";

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        try {
            final Project project = CommonDataKeys.PROJECT.getData(e.getDataContext());
            if (project == null) {
                LOG.warning("Unable to resolve project type.");
                return;
            }

            PayaraCloudProject cloudProject = CloudMavenProject.getInstance(project);
            if (cloudProject == null) {
                LOG.warning(PayaraBundle.message("CloudAction.notification.message"));
                Notifications.Bus.notify(
                        new Notification(
                                PayaraBundle.message("CloudAction.notification.group"),
                                PayaraConstants.CLOUD_ICON,
                                e.getPresentation().getDescription(),
                                "",
                                PayaraBundle.message("CloudAction.notification.message"),
                                NotificationType.WARNING,
                                NotificationListener.URL_OPENING_LISTENER
                        ), project);
                return;
            }
            if (cloudProject != null) {
                onAction(cloudProject);
            }
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    public abstract void onAction(PayaraCloudProject project);

    public JBTerminalWidget getTerminal(Project project, String tabName) {
        ToolWindowManager windowManager = ToolWindowManager.getInstance(project);
        ToolWindow terminalWindow = windowManager.getToolWindow(TOOL_WINDOW_ID);
        if (terminalWindow != null) {
            JBTerminalWidget widget = findTerminal(terminalWindow, tabName);
            if (widget != null) {
                return widget;
            }
            createTerminal(project, tabName);
            return findTerminal(terminalWindow, tabName);
        }
        return null;
    }

    private void createTerminal(Project project, String tabName) {
        TerminalTabState tabState = new TerminalTabState();
        tabState.myTabName = tabName;
        tabState.myWorkingDirectory = project.getBasePath();
        TerminalView.getInstance(project)
                .createNewSession(new LocalTerminalDirectRunner(project), tabState);
    }

    private JBTerminalWidget findTerminal(ToolWindow terminalWindow, String tabName) {
        Content[] contents = terminalWindow.getContentManager().getContents();
        for (Content content : contents) {
            if (content.getTabName().equals(tabName)) {
                return TerminalView.getWidgetByContent(content);
            }
        }
        return null;
    }

    public void executeCommand(JBTerminalWidget widget, String command) {
        try {
            widget.setRequestFocusEnabled(true);
            widget.requestFocusInWindow();
            widget.requestFocus(true);
            widget.requestFocus();
            widget.grabFocus();
            TtyConnector connector = widget.getTtyConnector();
            if (connector != null) {
                connector.write(command);
                connector.write(widget.getTerminalStarter().getCode(KeyEvent.VK_ENTER, 0));
            } else if (widget instanceof ShellTerminalWidget) {
                ((ShellTerminalWidget) widget).executeCommand(command);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
