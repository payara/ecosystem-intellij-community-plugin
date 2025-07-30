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

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.logging.Logger;
import static fish.payara.PayaraConstants.DEFAULT_DEBUG_PORT;
import fish.payara.util.MavenUtil;
import static java.util.logging.Level.SEVERE;

/**
 * @author gaurav.gupta@payara.fish
 */
public class ServerMavenProject extends PayaraServerProject {

    private static final Logger LOG = Logger.getLogger(ServerMavenProject.class.getName());

    public static final String SERVER_PLUGIN_GROUP_ID = "fish.payara.maven.plugins";
    public static final String SERVER_PLUGIN_ARTIFACT_ID = "payara-server-maven-plugin";
    public static final String SERVER_PLUGIN_VERSION = "1.0.0-Alpha3";
    public static final String SERVER_PLUGIN = "payara-server";
    public static final String START_GOAL = "start";
    public static final String DEV_GOAL = "dev";
    private static final String WAR_EXPLODE_GOAL = "war:exploded";
    private static final String COMPILE_GOAL = "compiler:compile";
    private static final String RESOURCES_GOAL = "resources:resources";
    public static final String PACKAGE_GOAL = "package";
    public static final String DEBUG_PROPERTY = "-Dpayara.debug=-agentlib:jdwp=transport=dt_socket,server=n,suspend=n,address=%s";
    private static final String BUILD_FILE = "pom.xml";
    private static final String EXPLODED = "exploded";
    private static final String EXPLODED_PROPERTY = "-Dpayara.exploded=true";
    private boolean exploded;

    @Override
    public String getStartCommand(boolean debug) {
        String cmd;
        if (exploded) {
            cmd = getStartExplodedWarCommand();
        } else {
            cmd = String.format("mvn %s %s:%s:%s:%s",
                    PACKAGE_GOAL,
                    SERVER_PLUGIN_GROUP_ID, SERVER_PLUGIN_ARTIFACT_ID, SERVER_PLUGIN_VERSION, DEV_GOAL
            );
        }
        return debug ? cmd + ' ' + DEBUG_PROPERTY : cmd;
    }

    private String getStartExplodedWarCommand() {
        return String.format("mvn %s %s %s %s:%s:%s:%s %s",
                RESOURCES_GOAL,
                COMPILE_GOAL,
                WAR_EXPLODE_GOAL,
                SERVER_PLUGIN_GROUP_ID, SERVER_PLUGIN_ARTIFACT_ID, SERVER_PLUGIN_VERSION, DEV_GOAL,
                EXPLODED_PROPERTY
        );
    }

    public static ServerMavenProject getInstance(Project project) {
        PsiFile pom = getPomFile(project);
        if (pom != null) {
            return new ServerMavenProject(project, pom);
        }
        return null;
    }

    public ServerMavenProject(Project project, PsiFile pom) {
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
       return MavenUtil.getProjectName(super.getBuildFile());
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
     * @return true if pom.xml file includes Payara Server Maven plugin
     */
    private static boolean isValidPom(PsiFile pomFile) {
        try {
            Node pomRoot = MavenUtil.getPomRootNode(pomFile);
            return MavenUtil.getBuildNodes(pomRoot)
                    .stream()
                    .anyMatch(ServerMavenProject::isServerPlugin);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOG.log(SEVERE, null, ex);
        }
        return false;

    }

    /**
     * @param buildNode
     * @return true if pom.xml file includes Payara Server Maven plugin
     */
    private static boolean isServerPlugin(Node buildNode) {
        return MavenUtil.getPluginNode(buildNode, SERVER_PLUGIN_GROUP_ID, SERVER_PLUGIN_ARTIFACT_ID) != null;
    }

    /**
     * Parse the pom.xml to read the configuration of Payara Server Maven plugin.
     */
    private void parsePom() {
        try {
            Node pomRoot = MavenUtil.getPomRootNode(super.getBuildFile());
            for (Node buildNode : MavenUtil.getBuildNodes(pomRoot)) {
                Node plugin = MavenUtil.getPluginNode(buildNode, SERVER_PLUGIN_GROUP_ID, SERVER_PLUGIN_ARTIFACT_ID);
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
                              if (param.getNodeName().equals(EXPLODED)
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

}
