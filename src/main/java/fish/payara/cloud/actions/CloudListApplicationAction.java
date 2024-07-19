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

import com.intellij.openapi.ui.Messages;
import com.intellij.terminal.JBTerminalWidget;
import fish.payara.PayaraConstants;
import fish.payara.cloud.PayaraCloudProject;
import fish.payara.cloud.maven.CloudMavenProject;
import javax.swing.*;
import java.awt.*;
import static java.util.logging.Level.WARNING;
import java.util.logging.Logger;

/**
 *
 * @author gaurav.gupta@payara.fish
 */
public class CloudListApplicationAction extends CloudAction {

    private static final Logger LOG = Logger.getLogger(CloudListApplicationAction.class.getName());

    @Override
    public void onAction(PayaraCloudProject project) {
        String projectName = project.getProjectName();
        JBTerminalWidget terminal = getTerminal(project, projectName);
        if (terminal != null) {
            JPanel panel = new JPanel(new GridBagLayout());
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(2, 2, 2, 2);
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridx = 0;
            gbc.gridy = 0;

            JLabel subscriptionLabel = new JLabel("Subscription:");
            panel.add(subscriptionLabel, gbc);

            gbc.gridx = 1;
            JTextField subscriptionField = new JTextField(20);
            panel.add(subscriptionField, gbc);

            gbc.gridx = 0;
            gbc.gridy = 1;
            JLabel namespaceLabel = new JLabel("Namespace:");
            panel.add(namespaceLabel, gbc);

            gbc.gridx = 1;
            JTextField namespaceField = new JTextField(20);
            panel.add(namespaceField, gbc);

            int result = JOptionPane.showConfirmDialog(
                null,
                panel,
                "List Cloud Applications",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                PayaraConstants.CLOUD_ICON
            );

            if (result == JOptionPane.OK_OPTION) {
                String subscriptionValue = subscriptionField.getText().trim();
                String namespaceValue = namespaceField.getText().trim();

                String cmd = project.getApplicationCommand();
                if (!subscriptionValue.isEmpty()) {
                    cmd += " -D" + CloudMavenProject.SUBSCRIPTION_ATTR + "='" + subscriptionValue + "'";
                }
                if (!namespaceValue.isEmpty()) {
                    cmd += " -D" + CloudMavenProject.NAMESPACE_ATTR + "='" + namespaceValue + "'";
                }
                executeCommand(terminal, cmd);
            }
        } else {
            LOG.log(WARNING, "Shell window for {0} is not available.", projectName);
        }
    }
}
