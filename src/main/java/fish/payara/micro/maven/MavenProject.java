/*
 * Copyright (c) 2020-2022 Payara Foundation and/or its affiliates and others.
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

import com.intellij.diagnostic.ActivityCategory;
import com.intellij.openapi.extensions.PluginDescriptor;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import static fish.payara.PayaraConstants.DEFAULT_DEBUG_PORT;
import fish.payara.micro.PayaraMicroProject;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Logger;

import static java.util.logging.Level.SEVERE;
import static java.util.stream.Collectors.toList;

/**
 *
 * @author gaurav.gupta@payara.fish
 */
public class MavenProject extends PayaraMicroProject {

    private static final Logger LOG = Logger.getLogger(MavenProject.class.getName());

    private static final String PROFILES = "profiles";
    private static final String PROFILE = "profile";
    private static final String BUILD = "build";
    private static final String PLUGINS = "plugins";
    private static final String PLUGIN = "plugin";
    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String NAME = "name";
    public static final String MICRO_GROUP_ID = "fish.payara.maven.plugins";
    public static final String MICRO_ARTIFACT_ID = "payara-micro-maven-plugin";
    public static final String MICRO_VERSION = "1.1.0";
    public static final String MICRO_PLUGIN = "payara-micro";
    public static final String START_GOAL = "start";
    private static final String RELOAD_GOAL = "reload";
    private static final String STOP_GOAL = "stop";
    private static final String BUNDLE_GOAL = "bundle";
    private static final String WAR_EXPLODE_GOAL = "war:exploded";
    private static final String COMPILE_GOAL = "compiler:compile";
    private static final String RESOURCES_GOAL = "resources:resources";
    public static final String PACKAGE_GOAL = "package";
    public static final String DEBUG_PROPERTY = "-Ddebug=-agentlib:jdwp=transport=dt_socket,server=n,suspend=n,address=%s";
    private static final String BUILD_FILE = "pom.xml";
    private static final String USE_UBER_JAR = "useUberJar";
    private static final String EXPLODED = "exploded";
    private static final String EXPLODED_PROPERTY = "-Dexploded=true";
    public static final String DEPLOY_WAR_PROPERTY = "-DdeployWar=true";
    private boolean useUberJar, exploded;

    @Override
    public String getStartCommand(boolean debug) {
        String cmd;
        if (useUberJar) {
            cmd = getStartUberJarCommand();
        } else if (exploded) {
            cmd = getStartExplodedWarCommand();
        } else {
            cmd = String.format("mvn %s %s:%s:%s",
                    PACKAGE_GOAL,
                    MICRO_GROUP_ID, MICRO_ARTIFACT_ID, START_GOAL
            );
        }
        return debug ? cmd + ' ' + String.format(DEBUG_PROPERTY, DEFAULT_DEBUG_PORT) : cmd;
    }

    private String getStartUberJarCommand() {
        return String.format("mvn %s:%s:%s %s:%s:%s",
                MICRO_GROUP_ID, MICRO_ARTIFACT_ID, BUNDLE_GOAL,
                MICRO_GROUP_ID, MICRO_ARTIFACT_ID, START_GOAL
        );
    }

    private String getStartExplodedWarCommand() {
        return String.format("mvn %s %s %s %s:%s:%s %s %s",
                RESOURCES_GOAL,
                COMPILE_GOAL,
                WAR_EXPLODE_GOAL,
                MICRO_GROUP_ID, MICRO_ARTIFACT_ID, START_GOAL,
                EXPLODED_PROPERTY,
                DEPLOY_WAR_PROPERTY
        );
    }

    @Override
    public String getReloadCommand() {
        if (!exploded) {
            throw new IllegalStateException("Reload task is only functional for exploded war artifacts.");
        }
        return String.format("mvn %s %s %s %s:%s:%s",
                RESOURCES_GOAL,
                COMPILE_GOAL,
                WAR_EXPLODE_GOAL,
                MICRO_GROUP_ID, MICRO_ARTIFACT_ID, RELOAD_GOAL
        );
    }

    @Override
    public String getStopCommand() {
        return String.format("mvn %s:%s:%s",
                MICRO_GROUP_ID, MICRO_ARTIFACT_ID, STOP_GOAL
        );
    }

    @Override
    public String getBundleCommand() {
        return String.format("mvn %s:%s:%s",
                MICRO_GROUP_ID, MICRO_ARTIFACT_ID, BUNDLE_GOAL
        );
    }

    public static MavenProject getInstance(Project project) {
        PsiFile pom = getPomFile(project);
        if (pom != null) {
            return new MavenProject(project, pom);
        }
        return null;
    }

    private MavenProject(Project project, PsiFile pom) {
        super(project, pom);
        parsePom();
    }

    /**
     * Return the project name from pom.xml artifactId
     *
     * @return
     */
    @Override
    public String getProjectName() {
        String artifactId = null;
        String name = null;
        try {
            Node pomRoot = getPomRootNode(super.getBuildFile());
            if (pomRoot != null) {
                NodeList childNodes = pomRoot.getChildNodes();
                for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++) {
                    Node childNode = childNodes.item(childNodeIndex);
                    if (childNode.getNodeName().equals(NAME)
                            && childNode.getTextContent() != null) {
                        name = childNode.getTextContent();
                        break;
                    }
                    if (childNode.getNodeName().equals(ARTIFACT_ID)
                            && childNode.getTextContent() != null) {
                        artifactId = childNode.getTextContent();
                    }
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOG.log(SEVERE, super.getBuildFile().getVirtualFile().getPath(), ex);
        }
        if (name != null) {
            return name;
        } else {
            return artifactId;
        }
    }

    /**
     * @param project
     * @return the pom.xml file
     */
    private static PsiFile getPomFile(Project project) {
        PsiFile[] poms = FilenameIndex.getFilesByName(project, BUILD_FILE, GlobalSearchScope.projectScope(project));
        for (PsiFile pom : poms) {
            if (isValidPom(pom)) {
                return pom;
            }
        }
        return null;
    }

    /**
     * @param pomFile the pom.xml file
     * @return true if pom.xml file includes Payara Micro Maven plugin
     */
    private static boolean isValidPom(PsiFile pomFile) {
        try {
            Node pomRoot = getPomRootNode(pomFile);
            return getBuildNodes(pomRoot)
                    .stream()
                    .anyMatch(MavenProject::isMicroPlugin);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOG.log(SEVERE, null, ex);
        }
        return false;

    }

    /**
     * @param buildNode
     * @return true if pom.xml file includes Payara Micro Maven plugin
     */
    private static boolean isMicroPlugin(Node buildNode) {
        return getMicroPluginNode(buildNode) != null;
    }

    private static Node getMicroPluginNode(Node buildNode) {
        NodeList buildChildNodes = buildNode.getChildNodes();
        for (int buildChildNodeIndex = 0; buildChildNodeIndex < buildChildNodes.getLength(); buildChildNodeIndex++) {
            Node buildChildNode = buildChildNodes.item(buildChildNodeIndex);
            for (Node pluginNode : getPluginNodes(buildChildNode)) {
                NodeList pluginChildNodes = pluginNode.getChildNodes();
                boolean microGroupId = false;
                boolean microArtifactId = false;
                for (int i = 0; i < pluginChildNodes.getLength(); i++) {
                    Node node = pluginChildNodes.item(i);
                    if (node.getNodeName().equals(GROUP_ID)
                            && node.getTextContent().equals(MICRO_GROUP_ID)) {
                        microGroupId = true;
                    } else if (node.getNodeName().equals(ARTIFACT_ID)
                            && node.getTextContent().equals(MICRO_ARTIFACT_ID)) {
                        microArtifactId = true;
                    }
                    if (microGroupId && microArtifactId) {
                        return pluginNode;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Parse the pom.xml to read the configuration of Payara Micro Maven plugin.
     */
    private void parsePom() {
        try {
            Node pomRoot = getPomRootNode(super.getBuildFile());
            for (Node buildNode : getBuildNodes(pomRoot)) {
                Node plugin = getMicroPluginNode(buildNode);
                if (plugin == null) {
                    continue;
                }
                NodeList pluginChildNodes = plugin.getChildNodes();
                for (int i = 0; i < pluginChildNodes.getLength(); i++) {
                    Node configurationNode = pluginChildNodes.item(i);
                    if (configurationNode.getNodeName().equals("configuration")) {
                        NodeList configurationChildNodes = configurationNode.getChildNodes();
                        for (int j = 0; j < configurationChildNodes.getLength(); j++) {
                            Node param = configurationChildNodes.item(j);
                            if (param.getNodeName().equals(USE_UBER_JAR)
                                    && param.getTextContent().equals("true")) {
                                useUberJar = true;
                            } else if (param.getNodeName().equals(EXPLODED)
                                    && param.getTextContent().equals("true")) {
                                exploded = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            LOG.log(SEVERE, super.getBuildFile().getVirtualFile().getPath(), ex);
        }
    }

    private static Node getPomRootNode(PsiFile pomFile) throws ParserConfigurationException, SAXException, IOException {
        Node root = null;
        if (pomFile.getVirtualFile() != null) {
            File inputFile = new File(pomFile.getVirtualFile().getCanonicalPath());
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document buildDocument = builder.parse(inputFile);
            buildDocument.getDocumentElement().normalize();
            root = buildDocument.getDocumentElement();
        }
        return root;
    }

    private static List<Node> getBuildNodes(Node pomRoot) {
        List<Node> buildNodes = new ArrayList<>();
        if (pomRoot != null) {
            NodeList childNodes = pomRoot.getChildNodes();
            for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++) {
                Node childNode = childNodes.item(childNodeIndex);

                buildNodes.addAll(
                        getProfileNodes(childNode)
                                .stream()
                                .map(Node::getChildNodes)
                                .map(MavenProject::getBuildNode)
                                .filter(Objects::nonNull)
                                .collect(toList())
                );

                if (childNode.getNodeName().equals(BUILD)) {
                    buildNodes.add(childNode);
                }
            }
        }
        return buildNodes;
    }

    private static Node getBuildNode(NodeList childNode) {
        for (int index = 0; index < childNode.getLength(); index++) {
            if (childNode.item(index).getNodeName().equals(BUILD)) {
                return childNode.item(index);
            }
        }
        return null;
    }

    private static List<Node> getProfileNodes(Node childNode) {
        List<Node> profildes = new ArrayList<>();
        if (childNode.getNodeName().equals(PROFILES)) {
            NodeList profileNodes = childNode.getChildNodes();
            for (int profileIndex = 0; profileIndex < profileNodes.getLength(); profileIndex++) {
                Node profile = profileNodes.item(profileIndex);
                if (profile.getNodeName().equals(PROFILE)) {
                    profildes.add(profile);
                }
            }
        }
        return profildes;
    }

    private static List<Node> getPluginNodes(Node childNode) {
        List<Node> plugins = new ArrayList<>();
        if (childNode.getNodeName().equals(PLUGINS)) {
            NodeList pluginNodes = childNode.getChildNodes();
            for (int pluginIndex = 0; pluginIndex < pluginNodes.getLength(); pluginIndex++) {
                Node pluginNode = pluginNodes.item(pluginIndex);
                if (pluginNode.getNodeName().equals(PLUGIN)) {
                    plugins.add(pluginNode);
                }
            }
        }
        return plugins;
    }

    @Override
    public RuntimeException createError(Throwable error, PluginId pluginId) {
        return getProject().createError(error, pluginId);
    }

    @Override
    public RuntimeException createError(String message, PluginId pluginId) {
        return getProject().createError(message, pluginId);
    }

    @Override
    public RuntimeException createError(String message, Throwable throwable, PluginId pluginId, Map<String, String> attachments) {
        return getProject().createError(message, throwable, pluginId, attachments);
    }

    @Override
    public @NotNull
    ActivityCategory getActivityCategory(boolean isExtension) {
        return getProject().getActivityCategory(isExtension);
    }

    @Override
    public <T> T [] getComponents(@NotNull Class<T> baseClass) {
        return getProject().getComponents(baseClass);
    }

    @Override
    public boolean isInjectionForExtensionSupported() {
        return getProject().isInjectionForExtensionSupported();
    }

    @Override
    public <T> T getService(@NotNull Class<T> serviceClass) {
        return getProject().getService(serviceClass);
    }

    @Override
    public <T> T instantiateClassWithConstructorInjection(@NotNull Class<T> aClass, @NotNull Object key, @NotNull PluginId pluginId) {
        return getProject().instantiateClassWithConstructorInjection(aClass, key, pluginId);
    }

    @Override
    public <T> Class<T> loadClass(String className, PluginDescriptor pluginDescriptor) throws ClassNotFoundException {
        return getProject().loadClass(className, pluginDescriptor);
    }
}
