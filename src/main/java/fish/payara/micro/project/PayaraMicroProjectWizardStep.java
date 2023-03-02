/*
 * Copyright (c) 2020-2023 Payara Foundation and/or its affiliates and others.
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
package fish.payara.micro.project;

import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.util.ui.JBUI;
import fish.payara.PayaraBundle;

import javax.swing.*;
import java.awt.*;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

public class PayaraMicroProjectWizardStep extends ModuleWizardStep {

    private final ModuleDescriptor moduleDescriptor;
    private final WizardContext wizardContext;

    private JTextField groupIdTextField;
    private JTextField artifactIdTextField;
    private JCheckBox autoBindHttpCheckBox;
    private JTextField contextRootTextField;
    private JComboBox<String> microVersionComboBox;
    
    public static final String[] ARCHETYPE_MICRO_VERSIONS = new String[]{
            "6.2023.2", "6.2023.1", "6.2022.2", "6.2022.1",
            "5.2022.5", "5.2022.4", "5.2022.3",
            "5.2022.2", "5.2022.1", "5.2021.10",
            "5.2021.9", "5.2021.8", "5.2021.7",
            "5.2021.6", "5.2021.5", "5.2021.4",
            "5.2021.3", "5.2021.2", "5.2021.1",
            "5.2020.7", "5.2020.6", "5.2020.5",
            "5.2020.4", "5.2020.3", "5.2020.2",
            "5.201", "5.194", "5.193.1", "5.192",
            "5.191", "5.184", "5.183", "5.182", "5.181"};
        
    private static final Pattern GROUP_ID_PATTERN = Pattern.compile("^[a-z0-9_]+(\\.[a-z0-9_]+)*$");
    private static final Pattern ARTIFACT_ID_PATTERN = Pattern.compile("^[a-z0-9_]+([\\-\\.][a-z0-9_]+)*$");

    public PayaraMicroProjectWizardStep(ModuleDescriptor moduleDescriptor, WizardContext context) {
        this.moduleDescriptor = moduleDescriptor;
        this.wizardContext = context;
    }

    @Override
    public JComponent getComponent() {
        return JBUI.Panels.simplePanel(0, 10)
                .addToTop(createPanel());
    }

    private JPanel createPanel() {
        JPanel panel = new JPanel();
        GridBagLayout panelLayout = new GridBagLayout();
        panel.setLayout(panelLayout);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = JBUI.insets(2);
        constraints.weightx = 0.25;
        constraints.gridx = 0;

        groupIdTextField = new JTextField(
                PayaraBundle.message("PayaraMicroProjectWizardStep.groupId.default")
        );
        constraints.gridy = 0;
        panel.add(
                LabeledComponent.create(
                        groupIdTextField,
                        PayaraBundle.message("PayaraMicroProjectWizardStep.groupId.label")
                ),
                constraints
        );

        artifactIdTextField = new JTextField(
                PayaraBundle.message("PayaraMicroProjectWizardStep.artifactId.default")
        );
        constraints.gridy++;
        panel.add(
                LabeledComponent.create(
                        artifactIdTextField,
                        PayaraBundle.message("PayaraMicroProjectWizardStep.artifactId.label")
                ),
                constraints
        );

        autoBindHttpCheckBox = new JCheckBox();
        constraints.gridy++;
        panel.add(
                LabeledComponent.create(
                        autoBindHttpCheckBox,
                        PayaraBundle.message("PayaraMicroProjectWizardStep.autobindHttp.label")
                ),
                constraints
        );

        contextRootTextField = new JTextField("/");
        constraints.gridy++;
        panel.add(
                LabeledComponent.create(
                        contextRootTextField,
                        PayaraBundle.message("PayaraMicroProjectWizardStep.contextRoot.label")
                ),
                constraints
        );
        
        microVersionComboBox = new JComboBox<String>(ARCHETYPE_MICRO_VERSIONS);
        microVersionComboBox.setEditable(true);
        constraints.gridy++;
        panel.add(
                LabeledComponent.create(
                        microVersionComboBox,
                        PayaraBundle.message("PayaraMicroProjectWizardStep.microVersion.label")
                ),
                constraints
        );

        return panel;
    }

    @Override
    public boolean validate() throws ConfigurationException {
        if (getSelectedGroupId().trim().isEmpty()) {
            throw new ConfigurationException(
                    PayaraBundle.message("PayaraMicroProjectWizardStep.groupId.empty")
            );
        } else if (!GROUP_ID_PATTERN.matcher(getSelectedGroupId().trim()).matches()) {
            throw new ConfigurationException(
                    PayaraBundle.message("PayaraMicroProjectWizardStep.groupId.pattern")
            );
        }

        if (getSelectedArtifactId().trim().isEmpty()) {
            throw new ConfigurationException(
                    PayaraBundle.message("PayaraMicroProjectWizardStep.artifactId.empty")
            );
        } else if(!ARTIFACT_ID_PATTERN.matcher(getSelectedArtifactId().trim()).matches()) {
            throw new ConfigurationException(
                    PayaraBundle.message("PayaraMicroProjectWizardStep.artifactId.pattern")
            );
        }

        return true;
    }
    
    @Override
    public void updateDataModel() {
        moduleDescriptor.setGroupId(getSelectedGroupId());
        moduleDescriptor.setArtifactId(getSelectedArtifactId());
        moduleDescriptor.setAutoBindHttp(isAutoBindHttpSelected());
        moduleDescriptor.setContextRoot(getContextRoot());
        moduleDescriptor.setMicroVersion(getMicroVersion());

        wizardContext.setProjectName(getSelectedArtifactId());
        wizardContext.setDefaultModuleName(getSelectedArtifactId());
    }

    private String getSelectedGroupId() {
        return groupIdTextField.getText().trim();
    }

    private String getSelectedArtifactId() {
        return artifactIdTextField.getText().trim();
    }

    private boolean isAutoBindHttpSelected() {
        return autoBindHttpCheckBox.isSelected();
    }

    private String getContextRoot() {
        String contextRoot = contextRootTextField.getText().trim();
        try {
            return contextRoot.startsWith("/")
                    ? '/' + URLEncoder.encode(contextRoot.substring(1), UTF_8.name())
                    : URLEncoder.encode(contextRoot, UTF_8.name());
        } catch (UnsupportedEncodingException ex) {
            throw new IllegalStateException("Invalid context root value " + contextRootTextField.getText());
        }
    }

    private String getMicroVersion() {
        return microVersionComboBox.getSelectedItem().toString();
    }

}
