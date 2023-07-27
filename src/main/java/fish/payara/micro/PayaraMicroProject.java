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
public abstract class PayaraMicroProject implements Project {

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

    @Override
    public String getName() {
        return project.getName();
    }

    @Override
    public VirtualFile getBaseDir() {
        return project.getBaseDir();
    }

    @Override
    public String getBasePath() {
        return project.getBasePath();
    }

    @Override
    public VirtualFile getProjectFile() {
        return project.getProjectFile();
    }

    @Override
    public String getProjectFilePath() {
        return project.getProjectFilePath();
    }

    @Override
    public VirtualFile getWorkspaceFile() {
        return project.getWorkspaceFile();
    }

    @Override
    public String getLocationHash() {
        return project.getLocationHash();
    }

    @Override
    public void save() {
        project.save();
    }

    @Override
    public boolean isOpen() {
        return project.isOpen();
    }

    @Override
    public boolean isInitialized() {
        return project.isInitialized();
    }

    @Override
    public boolean isDefault() {
        return project.isDefault();
    }

    @Override
    public <T> T getComponent(Class<T> type) {
        return project.getComponent(type);
    }

    @Override
    public MessageBus getMessageBus() {
        return project.getMessageBus();
    }

    @Override
    public boolean isDisposed() {
        return project.isDisposed();
    }

    @Override
    public Condition<?> getDisposed() {
        return project.getDisposed();
    }

    @Override
    public <T> T getUserData(Key<T> key) {
        return project.getUserData(key);
    }

    @Override
    public <T> void putUserData(Key<T> key, T t) {
        project.putUserData(key, t);
    }

    @Override
    public void dispose() {
        project.dispose();
    }

    public CoroutineScope getCoroutineScope() {
        return GlobalScope.INSTANCE;
    }

    @Override
    public <@NotNull T> T instantiateClass(@NotNull String baseClass, PluginDescriptor pluginDescriptor) {
        return getProject().instantiateClass(baseClass, pluginDescriptor);
    }

    @Override
    public ExtensionsArea getExtensionArea() {
        return getProject().getExtensionArea();
    }

    @Override
    public boolean hasComponent(@NotNull Class<?> interfaceClass) {
        return getProject().hasComponent(interfaceClass);
    }

}
