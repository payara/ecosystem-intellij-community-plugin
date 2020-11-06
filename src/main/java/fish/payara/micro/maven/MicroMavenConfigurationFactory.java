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
package fish.payara.micro.maven;

import com.intellij.execution.configurations.*;
import com.intellij.openapi.project.Project;
import static icons.OpenapiIcons.RepositoryLibraryLogo;
import javax.swing.Icon;
import org.jetbrains.annotations.NotNull;

public class MicroMavenConfigurationFactory extends ConfigurationFactory {

    private static final String FACTORY_NAME = "Maven";

    public MicroMavenConfigurationFactory(ConfigurationType type) {
        super(type);
    }

    @NotNull
    @Override
    public RunConfiguration createTemplateConfiguration(@NotNull Project project) {
        return new MicroMavenConfiguration(project, this, "Micro Maven");
    }

    @NotNull
    @Override
    public String getName() {
        return FACTORY_NAME;
    }

    @NotNull
    @Override
    public String getId() {
        return FACTORY_NAME;
    }

    @NotNull
    @Override
    public Icon getIcon() {
        return RepositoryLibraryLogo;
    }

}
