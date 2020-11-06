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
package fish.payara.micro.project;

import com.intellij.ide.util.projectWizard.JavaSettingsStep;
import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.SettingsStep;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.ui.Messages;
import com.intellij.util.lang.JavaVersion;
import fish.payara.PayaraBundle;
import fish.payara.PayaraConstants;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

import static fish.payara.micro.project.PayaraMicroConstants.*;

public class PayaraMicroModuleType extends ModuleType<PayaraMicroModuleBuilder> {

    public PayaraMicroModuleType() {
        super(MODULE_ID);
    }

    protected PayaraMicroModuleType(@NotNull String id) {
        super(id);
    }

    @NotNull
    @Override
    public PayaraMicroModuleBuilder createModuleBuilder() {
        return new PayaraMicroModuleBuilder();
    }

    @Nls(capitalization = Nls.Capitalization.Title)
    @NotNull
    @Override
    public String getName() {
        return MODULE_TITLE;
    }

    @Nls(capitalization = Nls.Capitalization.Sentence)
    @NotNull
    @Override
    public String getDescription() {
        return MODULE_DESCRIPTION;
    }

    @NotNull
    @Override
    public Icon getNodeIcon(boolean isOpened) {
        return PayaraConstants.PAYARA_ICON;
    }

    @Nullable
    @Override
    public ModuleWizardStep modifyProjectTypeStep(
            @NotNull SettingsStep settingsStep,
            @NotNull ModuleBuilder moduleBuilder) {

        return new JavaSettingsStep(settingsStep, moduleBuilder, moduleBuilder::isSuitableSdkType) {
            @Override
            public boolean validate() throws ConfigurationException {
                boolean result = super.validate();

                if (result) {
                    Sdk jdk = myJdkComboBox.getSelectedJdk();
                    if (jdk != null && jdk.getVersionString() != null) {
                        JavaVersion javaVersion = JavaVersion.parse(jdk.getVersionString());
                        if (javaVersion.feature < 8) {
                            Messages.showErrorDialog(
                                    PayaraBundle.message("PayaraMicroModuleType.validation.jdk.description"),
                                    PayaraBundle.message("PayaraMicroModuleType.validation.jdk.title")
                            );
                            result = false;
                        }
                    } else {
                        Messages.showErrorDialog(
                                PayaraBundle.message("PayaraMicroModuleType.validation.jdk.description"),
                                PayaraBundle.message("PayaraMicroModuleType.validation.jdk.title")
                        );
                        result = false;
                    }
                }

                return result;
            }
        };
    }
}
