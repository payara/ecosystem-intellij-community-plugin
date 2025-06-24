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

import com.intellij.execution.configuration.EnvironmentVariablesComponent;
import com.intellij.execution.target.LanguageRuntimeConfiguration;
import com.intellij.execution.target.TargetEnvironmentConfiguration;
import com.intellij.execution.target.TargetEnvironmentsManager;
import com.intellij.execution.target.java.JavaLanguageRuntimeConfiguration;
import com.intellij.openapi.externalSystem.service.ui.ExternalSystemJdkComboBox;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.ui.ComponentUtil;
import com.intellij.ui.IdeBorderFactory;
import com.intellij.ui.RawCommandLineEditor;
import com.intellij.ui.UserActivityWatcher;
import com.intellij.util.ui.JBFont;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.idea.maven.project.MavenConfigurableBundle;
import org.jetbrains.idea.maven.project.MavenProject;
import org.jetbrains.idea.maven.project.MavenProjectsManager;
import org.jetbrains.idea.maven.execution.MavenPropertiesPanel;
import org.jetbrains.idea.maven.execution.MavenRunnerSettings;
import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.IntStream;
import com.intellij.ui.HideableDecorator;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import static fish.payara.util.PathUtil.checkValidServerHome;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

/**
 *
 * @author Gaurav Gupta
 */
public class ServerPanel {

    protected final Project myProject;
    private final boolean myRunConfigurationMode;

    private JCheckBox myDelegateToMavenCheckbox;
    private RawCommandLineEditor myVMParametersEditor;
    private EnvironmentVariablesComponent myEnvVariablesComponent;
    private JLabel myJdkLabel;
    private ExternalSystemJdkComboBox myJdkCombo;
    private ComboBox<String> myTargetJdkCombo;

    private JCheckBox mySkipTestsCheckBox;
    private MavenPropertiesPanel mavenPropertiesPanel;
    private MavenPropertiesPanel payaraPropertiesPanel;

    private Map<String, String> mavenProperties;
    private Map<String, String> payaraProperties = new HashMap<>();
    private String myTargetName;

    private ComboBox<String> goalsComboBox;
    private final static String LOADING = "Loading...";

    private JCheckBox explodedCheckBox;
    private JTextField contextRootField;
    private TextFieldWithBrowseButton payaraHomeField;
    private JTextField payaraVersionField, domainNameField, instanceNameField, debugPortField;
    private JCheckBox remoteCheckBox;
    private JPanel remoteSettingsPanel;
    private JComboBox<String> protocolComboBox;
    private JTextField hostField, httpPortField, httpsPortField, adminPortField;
    private JTextField userField;
    private JPasswordField passwordField;

    public ServerPanel(@NotNull Project p, boolean isRunConfiguration) {
        myProject = p;
        myRunConfigurationMode = isRunConfiguration;
    }

    public JComponent createComponent() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        Insets defaultInsets = new Insets(5, 5, 5, 5);
        c.insets = defaultInsets;

        myDelegateToMavenCheckbox = new JCheckBox(MavenConfigurableBundle.message("maven.settings.runner.delegate"));

        if (!myRunConfigurationMode) {
            c.gridx = 0;
            c.gridy++;
            c.weightx = 1;
            c.gridwidth = GridBagConstraints.REMAINDER;
            panel.add(myDelegateToMavenCheckbox, c);
        }

        // Goals
        c.gridwidth = 1;
        c.gridx = 0;
        c.gridy++;
        panel.add(new JLabel("Goals:"), c);
        c.gridx = 1;
        List<String> goals = new ArrayList<>();
        goals.add(ServerMavenProject.DEV_GOAL);
        goals.add(ServerMavenProject.START_GOAL);
        goalsComboBox = new ComboBox<>(goals.toArray(new String[0]));
        goalsComboBox.setEditable(true);
        panel.add(goalsComboBox, c);

        // JDK Combo
        myJdkLabel = new JLabel(MavenConfigurableBundle.message("maven.settings.runner.jre"));
        myJdkLabel.setLabelFor(myJdkCombo = new ExternalSystemJdkComboBox(myProject));
        c.gridx = 0;
        c.gridy++;
        c.weightx = 0;
        panel.add(myJdkLabel, c);
        c.gridx = 1;
        c.weightx = 1;
        panel.add(myJdkCombo, c);

        // Hidden Target JDK Combo
        myTargetJdkCombo = new ComboBox<>();
        ComponentUtil.putClientProperty(myTargetJdkCombo, UserActivityWatcher.DO_NOT_WATCH, true);
        myTargetJdkCombo.setVisible(false);
        panel.add(myTargetJdkCombo, c);

        // Application Settings (Collapsible)
        JPanel appSettingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints apc = new GridBagConstraints();
        apc.insets = new Insets(2, 5, 2, 5);

        apc.gridx = 0;
        apc.gridy = 0;
        apc.gridwidth = 2;
        apc.fill = GridBagConstraints.HORIZONTAL;
        explodedCheckBox = new JCheckBox("Deploy in Exploded Format");
        appSettingsPanel.add(explodedCheckBox, apc);

        apc.gridy++;
        apc.gridwidth = 1;
        apc.gridx = 0;
        apc.weightx = 0;
        apc.fill = GridBagConstraints.NONE;
        JLabel contextRootLabel = new JLabel("Context Root:");
        appSettingsPanel.add(contextRootLabel, apc);

        apc.gridx = 1;
        apc.weightx = 1;
        apc.fill = GridBagConstraints.HORIZONTAL;
        contextRootField = new JTextField(30);
        appSettingsPanel.add(contextRootField, apc);

        JPanel appSettingsCollapsible = new JPanel(new BorderLayout());
        HideableDecorator appSettingsDecorator = new HideableDecorator(appSettingsCollapsible, "Application Deployment Settings", true);
        appSettingsDecorator.setContentComponent(appSettingsPanel);
        appSettingsDecorator.setOn(false);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(appSettingsCollapsible, c);

        // Advanced Settings (Collapsible)
        userField = new JTextField();
        hostField = new JTextField();
        protocolComboBox = new JComboBox<>(new String[]{"http", "https"});
        httpPortField = new JTextField();
        httpsPortField = new JTextField();
        adminPortField = new JTextField();
        debugPortField = new JTextField();
        payaraHomeField = new TextFieldWithBrowseButton();
        payaraHomeField.setText(payaraProperties.getOrDefault("payara.home", ""));
        payaraHomeField.addBrowseFolderListener("Select Payara Home Directory", null, myProject,
                FileChooserDescriptorFactory.createSingleFolderDescriptor());

        payaraVersionField = new JTextField();
        domainNameField = new JTextField();
        instanceNameField = new JTextField();
        remoteCheckBox = new JCheckBox();
        passwordField = new JPasswordField();

        JPanel payaraSettingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints ac = new GridBagConstraints();
        ac.gridx = 0;
        ac.gridy = 0;
        ac.gridwidth = 1;
        ac.weightx = 1;
        ac.fill = GridBagConstraints.HORIZONTAL;
        ac.insets = new Insets(2, 5, 2, 5);

        payaraSettingsPanel.add(new JLabel("Payara Home:"), ac);
        ac.gridx = 1;
        payaraSettingsPanel.add(payaraHomeField, ac);
        ac.gridx = 0;
        ac.gridy++;

        JLabel payaraHomeErrorLabel = new JLabel();
        payaraHomeErrorLabel.setForeground(Color.RED);
        payaraSettingsPanel.add(payaraHomeErrorLabel, ac);
        ac.gridy++;

        payaraHomeField.getTextField().addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                SwingUtilities.invokeLater(() -> {
                    String selectedPath = payaraHomeField.getText().trim();
                    if (!selectedPath.isEmpty()) {
                        try {
                            checkValidServerHome(selectedPath, null);
                            payaraHomeErrorLabel.setText("");
                        } catch (Exception ex) {
                            payaraHomeErrorLabel.setText("Invalid Payara Home");
                        }
                    }
                });
            }
        });

        payaraSettingsPanel.add(new JLabel("Payara Version:"), ac);
        ac.gridx = 1;
        payaraSettingsPanel.add(payaraVersionField, ac);
        ac.gridx = 0;
        ac.gridy++;

        payaraSettingsPanel.add(new JLabel("Domain Name:"), ac);
        ac.gridx = 1;
        payaraSettingsPanel.add(domainNameField, ac);
        ac.gridx = 0;
        ac.gridy++;

        payaraSettingsPanel.add(new JLabel("Instance Name:"), ac);
        ac.gridx = 1;
        payaraSettingsPanel.add(instanceNameField, ac);
        ac.gridx = 0;
        ac.gridy++;

        payaraSettingsPanel.add(new JLabel("Debug Port:"), ac);
        ac.gridx = 1;
        payaraSettingsPanel.add(debugPortField, ac);
        ac.gridx = 0;
        ac.gridy++;

        payaraSettingsPanel.add(new JLabel("Is Remote:"), ac);
        ac.gridx = 1;
        payaraSettingsPanel.add(remoteCheckBox, ac);
        ac.gridx = 0;
        ac.gridy++;

        remoteSettingsPanel = new JPanel(new GridBagLayout());
        remoteSettingsPanel.setName("remoteSettingsPanel");

        remoteSettingsPanel.add(new JLabel("Host:"), ac);
        ac.gridx = 1;
        remoteSettingsPanel.add(hostField, ac);
        ac.gridx = 0;
        ac.gridy++;

        remoteSettingsPanel.add(new JLabel("Protocol:"), ac);
        ac.gridx = 1;
        remoteSettingsPanel.add(protocolComboBox, ac);
        ac.gridx = 0;
        ac.gridy++;

        remoteSettingsPanel.add(new JLabel("HTTP Port:"), ac);
        ac.gridx = 1;
        remoteSettingsPanel.add(httpPortField, ac);
        ac.gridx = 0;
        ac.gridy++;

        remoteSettingsPanel.add(new JLabel("HTTPS Port:"), ac);
        ac.gridx = 1;
        remoteSettingsPanel.add(httpsPortField, ac);
        ac.gridx = 0;
        ac.gridy++;

        remoteSettingsPanel.add(new JLabel("Admin Port:"), ac);
        ac.gridx = 1;
        remoteSettingsPanel.add(adminPortField, ac);

        ac.gridx = 0;
        ac.gridy++;
        ac.gridwidth = 2;
        payaraSettingsPanel.add(remoteSettingsPanel, ac);

        remoteCheckBox.addActionListener(e -> {
            remoteSettingsPanel.setVisible(remoteCheckBox.isSelected());
        });

        JPanel collapsiblePanel = new JPanel(new BorderLayout());
        HideableDecorator decorator = new HideableDecorator(collapsiblePanel, "Payara Server Settings", true);
        decorator.setContentComponent(payaraSettingsPanel);
        decorator.setOn(false);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(collapsiblePanel, c);

        // Environment Variables + Payara Server Properties (Collapsible)
        JPanel envAndPayaraPanel = new JPanel(new GridBagLayout());
        GridBagConstraints epc = new GridBagConstraints();
        epc.gridx = 0;
        epc.gridy = 0;
        epc.gridwidth = 2;
        epc.fill = GridBagConstraints.BOTH;
        epc.weightx = 1;
        epc.insets = new Insets(2, 4, 2, 4);

        myEnvVariablesComponent = new EnvironmentVariablesComponent();
        myEnvVariablesComponent.setPassParentEnvs(true);
        myEnvVariablesComponent.setLabelLocation(BorderLayout.WEST);
        envAndPayaraPanel.add(myEnvVariablesComponent, epc);
        epc.gridy++;

        JPanel payaraPropertiesPanelWrapper = new JPanel(new GridBagLayout());
        payaraPropertiesPanelWrapper.setBorder(IdeBorderFactory.createTitledBorder("Payara Server Properties", false));
        payaraPropertiesPanel = new MavenPropertiesPanel(payaraProperties);
        payaraPropertiesPanel.getTable().setShowGrid(false);
        payaraPropertiesPanel.getEmptyText().setText("Payara Server Properties");

        GridBagConstraints pc = new GridBagConstraints();
        pc.gridx = 0;
        pc.gridy = 0;
        pc.gridwidth = 2;
        pc.fill = GridBagConstraints.BOTH;
        pc.weightx = 1;
        pc.weighty = 1;
        pc.insets = new Insets(2, 5, 2, 5);

        payaraPropertiesPanelWrapper.add(payaraPropertiesPanel, pc);
        envAndPayaraPanel.add(payaraPropertiesPanelWrapper, epc);

        JPanel envAndPayaraCollapsible = new JPanel(new BorderLayout());
        HideableDecorator envDecorator = new HideableDecorator(envAndPayaraCollapsible, "Environment and Server Properties", true);
        envDecorator.setContentComponent(envAndPayaraPanel);
        envDecorator.setOn(false); // collapsed by default

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(envAndPayaraCollapsible, c);

        // Maven VM Options + Properties (Collapsible)
        JPanel mavenSettingsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints mc = new GridBagConstraints();
        mc.gridx = 0;
        mc.gridy = 0;
        mc.gridwidth = 1;
        mc.weightx = 0;
        mc.fill = GridBagConstraints.NONE;
        mc.insets = new Insets(2, 4, 2, 4);

        JLabel vmOptionsLabel = new JLabel("Maven VM Options:");
        mavenSettingsPanel.add(vmOptionsLabel, mc);

        mc.gridx = 1;
        mc.weightx = 1;
        mc.fill = GridBagConstraints.HORIZONTAL;
        myVMParametersEditor = new RawCommandLineEditor();
        myVMParametersEditor.setDialogCaption("Maven VM Options");
        mavenSettingsPanel.add(myVMParametersEditor, mc);

        mc.gridx = 1;
        mc.gridy++;
        mc.gridwidth = 1;
        mc.fill = GridBagConstraints.HORIZONTAL;
        mc.insets = new Insets(2, 4, 0, 0);
        JLabel hintLabel = new JLabel(MavenConfigurableBundle.message("maven.settings.vm.options.tooltip"));
        hintLabel.setFont(JBFont.small());
        mavenSettingsPanel.add(hintLabel, mc);

        JPanel mavenPropertiesPanelWrapper = new JPanel(new BorderLayout());
        mavenPropertiesPanelWrapper.setBorder(IdeBorderFactory.createTitledBorder("Maven Properties", false));
        mavenPropertiesPanelWrapper.add(mySkipTestsCheckBox = new JCheckBox(MavenConfigurableBundle.message("maven.settings.runner.skip.tests")), BorderLayout.NORTH);
        collectMavenProperties();
        mavenPropertiesPanelWrapper.add(mavenPropertiesPanel = new MavenPropertiesPanel(mavenProperties), BorderLayout.CENTER);
        mavenPropertiesPanel.getTable().setShowGrid(false);
        mavenPropertiesPanel.getEmptyText().setText("Maven " + MavenConfigurableBundle.message("maven.settings.runner.properties.not.defined"));

        mc.gridx = 0;
        mc.gridy++;
        mc.gridwidth = 2;
        mc.weightx = mc.weighty = 1;
        mc.fill = GridBagConstraints.BOTH;
        mavenSettingsPanel.add(mavenPropertiesPanelWrapper, mc);

        JPanel mavenSettingsCollapsible = new JPanel(new BorderLayout());
        HideableDecorator mavenDecorator = new HideableDecorator(mavenSettingsCollapsible, "Maven Options and Properties", true);
        mavenDecorator.setContentComponent(mavenSettingsPanel);
        mavenDecorator.setOn(false);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = c.weighty = 1;
        c.gridheight = 1;
        c.fill = GridBagConstraints.BOTH;
        panel.add(mavenSettingsCollapsible, c);

        return panel;
    }

    private void collectMavenProperties() {
        MavenProjectsManager s = MavenProjectsManager.getInstance(myProject);
        Map<String, String> result = new LinkedHashMap<>();

        for (MavenProject each : s.getProjects()) {
            Properties properties = each.getProperties();
            result.putAll((Map) properties);
        }

        mavenProperties = result;
    }

    protected void getData(MavenRunnerSettings data, ServerMavenConfiguration config) {
        myDelegateToMavenCheckbox.setSelected(data.isDelegateBuildToMaven());
        myVMParametersEditor.setText(data.getVmOptions());
        mySkipTestsCheckBox.setSelected(data.isSkipTests());

        myJdkCombo.refreshData(data.getJreName());
        myTargetJdkCombo.setSelectedItem(data.getJreName());

        mavenPropertiesPanel.setDataFromMap(data.getMavenProperties());

        myEnvVariablesComponent.setEnvs(data.getEnvironmentProperties());
        myEnvVariablesComponent.setPassParentEnvs(data.isPassParentEnv());

        System.out.println("config.getHttpPort() " + config.getHttpPort());
        goalsComboBox.setSelectedItem(config.getGoals());
        explodedCheckBox.setSelected(config.isExploded());
        contextRootField.setText(config.getContextRoot() != null ? config.getContextRoot() : "");
        payaraHomeField.setText(config.getPayaraHome() != null ? config.getPayaraHome() : "");
        payaraVersionField.setText(config.getPayaraVersion() != null ? config.getPayaraVersion() : "");
        domainNameField.setText(config.getDomainName() != null ? config.getDomainName() : "");
        instanceNameField.setText(config.getInstanceName() != null ? config.getInstanceName() : "");
        hostField.setText(config.getHost() != null ? config.getHost() : "");
        protocolComboBox.setSelectedItem(config.getProtocol() != null ? config.getProtocol() : "http");
        httpPortField.setText(config.getHttpPort() != null ? config.getHttpPort() : "");
        httpsPortField.setText(config.getHttpsPort() != null ? config.getHttpsPort() : "");
        adminPortField.setText(config.getAdminPort() != null ? config.getAdminPort() : "");
        debugPortField.setText(config.getDebugPort() != null ? config.getDebugPort() : "");
        remoteCheckBox.setSelected(config.isRemote());
        remoteSettingsPanel.setVisible(remoteCheckBox.isSelected());
        userField.setText(config.getUser() != null ? config.getUser() : "");
        passwordField.setText(config.getPassword() != null ? config.getPassword() : "");

        payaraProperties = config.getPayaraProperties();
        if (payaraProperties != null) {
            payaraPropertiesPanel.setDataFromMap(payaraProperties);
        } else {
            payaraPropertiesPanel.setDataFromMap(new HashMap<>());
        }

        if (config.getEnvVars() != null) {
            myEnvVariablesComponent.setEnvs(config.getEnvVars());
        } else {
            myEnvVariablesComponent.setEnvs(new HashMap<>());
        }
    }

    protected void setData(MavenRunnerSettings data, ServerMavenConfiguration config) {
        data.setDelegateBuildToMaven(myDelegateToMavenCheckbox.isSelected());
        data.setVmOptions(myVMParametersEditor.getText().trim());
        data.setSkipTests(mySkipTestsCheckBox.isSelected());
        if (myJdkCombo.getSelectedValue() != null) {
            data.setJreName(myJdkCombo.getSelectedValue());
        } else {
            data.setJreName(StringUtil.notNullize(myTargetJdkCombo.getItem(), MavenRunnerSettings.USE_PROJECT_JDK));
        }
        Map<String, String> mavenProperties = new HashMap<>(mavenPropertiesPanel.getDataAsMap());

        data.setMavenProperties(mavenProperties);

        Map<String, String> envVars = new HashMap<>(myEnvVariablesComponent.getEnvs());
        payaraProperties = new HashMap<>(payaraPropertiesPanel.getDataAsMap());
        fillEnvVars(envVars);
        config.setExploded(explodedCheckBox.isSelected());
        config.setContextRoot(contextRootField.getText().trim());
        config.setPayaraHome(payaraHomeField.getText().trim());
        config.setPayaraVersion(payaraVersionField.getText().trim());
        config.setDomainName(domainNameField.getText().trim());
        config.setInstanceName(instanceNameField.getText().trim());
        config.setHost(hostField.getText().trim());
        Object selectedProtocol = protocolComboBox.getSelectedItem();
        config.setProtocol(selectedProtocol != null ? selectedProtocol.toString() : "http");
        config.setHttpPort(httpPortField.getText().trim());
        config.setHttpsPort(httpsPortField.getText().trim());
        config.setAdminPort(adminPortField.getText().trim());
        config.setDebugPort(debugPortField.getText().trim());
        config.setRemote(remoteCheckBox.isSelected());
        config.setUser(userField.getText().trim());
        config.setPassword(new String(passwordField.getPassword()));
        config.setPayaraProperties(payaraProperties);
        config.setEnvVars(myEnvVariablesComponent.getEnvs());
        if (!payaraProperties.isEmpty()) {
            StringBuilder optionsBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : payaraProperties.entrySet()) {
                if (optionsBuilder.length() > 0) {
                    optionsBuilder.append(",");
                }
                optionsBuilder.append("-D").append(entry.getKey()).append("=").append(entry.getValue());
            }
            envVars.put("PAYARA_JAVA_COMMANDLINE_OPTIONS", optionsBuilder.toString());
        }

        data.setEnvironmentProperties(envVars);
        data.setPassParentEnv(myEnvVariablesComponent.isPassParentEnvs());

        config.setGoals((String) goalsComboBox.getSelectedItem());
    }

    private void putIfNotEmpty(Map<String, String> map, String key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            map.put(key, value.trim());
        }
    }

    private void fillEnvVars(Map<String, String> envVars) {
        envVars.put("PAYARA_EXPLODED", Boolean.toString(explodedCheckBox.isSelected()));
        envVars.put("PAYARA_REMOTE", Boolean.toString(remoteCheckBox.isSelected()));

        putIfNotEmpty(envVars, "PAYARA_CONTEXT_PATH", contextRootField.getText());
        putIfNotEmpty(envVars, "PAYARA_SERVER_PATH", payaraHomeField.getText());
        putIfNotEmpty(envVars, "PAYARA_SERVER_VERSION", payaraVersionField.getText());
        putIfNotEmpty(envVars, "PAYARA_DOMAIN_NAME", domainNameField.getText());
        putIfNotEmpty(envVars, "PAYARA_INSTANCE_NAME", instanceNameField.getText());
        putIfNotEmpty(envVars, "PAYARA_HOST_NAME", hostField.getText());

        Object protocol = protocolComboBox.getSelectedItem();
        if (protocol != null && !protocol.toString().trim().isEmpty()) {
            envVars.put("PAYARA_PROTOCOL", protocol.toString().trim());
        }

        putIfNotEmpty(envVars, "PAYARA_HTTP_PORT", httpPortField.getText());
        putIfNotEmpty(envVars, "PAYARA_HTTPS_PORT", httpsPortField.getText());
        putIfNotEmpty(envVars, "PAYARA_ADMIN_PORT", adminPortField.getText());

        if (!debugPortField.getText().isBlank()) {
            putIfNotEmpty(envVars, "PAYARA_DEBUG", "true");
        }
        putIfNotEmpty(envVars, "PAYARA_DEBUG_PORT", debugPortField.getText());
        putIfNotEmpty(envVars, "PAYARA_ADMIN_USER", userField.getText());

        String password = new String(passwordField.getPassword()).trim();
        if (!password.isEmpty()) {
            envVars.put("PAYARA_ADMIN_PASSWORD", password);
        }
    }

    public Project getProject() {
        return myProject;
    }

    void applyTargetEnvironmentConfiguration(@Nullable String targetName) {
        boolean localTarget = targetName == null;
        boolean targetChanged = !Objects.equals(myTargetName, targetName);
        if (targetChanged) {
            myTargetName = targetName;
            updateJdkComponents(targetName);
            if (localTarget) {
                myJdkCombo.refreshData(null);
            }
        } else if (!localTarget) {
            updateJdkComponents(targetName);
        }
    }

    private void updateJdkComponents(@Nullable String targetName) {
        boolean localTarget = targetName == null;
        myTargetJdkCombo.setVisible(!localTarget);
        myJdkCombo.setVisible(localTarget);
        if (!localTarget) {
            List<String> items = IntStream.range(0, myTargetJdkCombo.getItemCount())
                    .mapToObj(i -> myTargetJdkCombo.getItemAt(i))
                    .toList();

            List<String> targetItems = new ArrayList<>();
            TargetEnvironmentConfiguration targetEnvironmentConfiguration = TargetEnvironmentsManager.getInstance(myProject)
                    .getTargets().findByName(targetName);
            if (targetEnvironmentConfiguration != null) {
                for (LanguageRuntimeConfiguration runtimeConfiguration : targetEnvironmentConfiguration.getRuntimes().resolvedConfigs()) {
                    if (runtimeConfiguration instanceof JavaLanguageRuntimeConfiguration) {
                        String homePath = ((JavaLanguageRuntimeConfiguration) runtimeConfiguration).getHomePath();
                        targetItems.add(homePath);
                    }
                }
            }

            if (!items.equals(targetItems)) {
                myTargetJdkCombo.removeAllItems();
                targetItems.forEach(myTargetJdkCombo::addItem);
            }
            myJdkLabel.setLabelFor(myTargetJdkCombo);
        } else {
            myJdkLabel.setLabelFor(myJdkCombo);
        }
    }

}