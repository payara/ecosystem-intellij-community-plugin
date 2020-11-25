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

import fish.payara.micro.gradle.MicroGradleConfigurationFactory;
import fish.payara.micro.maven.MicroMavenConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.ConfigurationType;
import static fish.payara.PayaraConstants.PAYARA_ICON;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class PayaraMicroConfigurationType implements ConfigurationType {

    @NotNull
    @Override
    public String getDisplayName() {
        return "Payara Micro";
    }

    @Override
    public String getConfigurationTypeDescription() {
        return "Payara Micro configuration type";
    }

    @Override
    public Icon getIcon() {
        return PAYARA_ICON;
    }

    @NotNull
    @Override
    public String getId() {
        return "PAYARA_MICRO_CONFIGURATION";
    }

    @Override
    public ConfigurationFactory[] getConfigurationFactories() {
        return new ConfigurationFactory[]{
            new MicroMavenConfigurationFactory(this),
            new MicroGradleConfigurationFactory(this)
        };
    }
}
