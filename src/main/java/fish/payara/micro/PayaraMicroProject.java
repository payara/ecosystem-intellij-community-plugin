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

import com.intellij.openapi.extensions.ExtensionsArea;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.ReflectionUtil;
import com.intellij.util.messages.MessageBus;
import org.jetbrains.annotations.NotNull;
import org.picocontainer.PicoContainer;
import kotlinx.coroutines.CoroutineScope;
import kotlinx.coroutines.GlobalScope;
/**
 *
 * @author gaurav.gupta@payara.fish
 */
public abstract class PayaraMicroProject {

    private final Project project;

    private final PsiFile buildFile;

    protected PayaraMicroProject(Project project, PsiFile buildFile) {
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

    public abstract String getReloadCommand();

    public abstract String getStopCommand();

    public abstract String getBundleCommand();

    public abstract String getTransformCommand(String srcPath, String targetPath);

}
