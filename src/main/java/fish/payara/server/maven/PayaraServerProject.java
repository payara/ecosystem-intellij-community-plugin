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

import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.util.ReflectionUtil;
import org.jetbrains.annotations.NotNull;
/**
 *
 * @author gaurav.gupta@payara.fish
 */
public abstract class PayaraServerProject {

    private final Project project;

    private final PsiFile buildFile;

    protected PayaraServerProject(Project project, PsiFile buildFile) {
        this.project = project;
        this.buildFile = buildFile;
    }

    public <T> T instantiateClass(@NotNull Class<T> aClass, @NotNull PluginId pluginId) {
        return ReflectionUtil.newInstance(aClass, false);
    }

    public Project getProject() {
        return project;
    }

    public PsiFile getBuildFile() {
        return buildFile;
    }

    public abstract String getProjectName();

    public abstract String getStartCommand(boolean debug);

}
