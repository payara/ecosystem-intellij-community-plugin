/*
 * Copyright (c) 2024 Payara Foundation and/or its affiliates and others.
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
package fish.payara.cloud.maven;

import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import fish.payara.cloud.PayaraCloudProject;
import fish.payara.util.MavenUtil;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.logging.Logger;
import static java.util.logging.Level.SEVERE;

/**
 * @author gaurav.gupta@payara.fish
 */
public class CloudMavenProject extends PayaraCloudProject {

    private static final Logger LOG = Logger.getLogger(CloudMavenProject.class.getName());

    public static final String CLOUD_GROUP_ID = "fish.payara.maven.plugins";
    public static final String CLOUD_ARTIFACT_ID = "payara-cloud-maven-plugin";
    public static final String CLOUD_VERSION = "1.0-Alpha4";
    public static final String CLOUD_PLUGIN = "payara-cloud";
    public static final String START_GOAL = "start";
    public static final String LOGIN_GOAL = "login";
    public static final String DEV_GOAL = "dev";
    public static final String DEPLOY_GOAL = "deploy";
    public static final String UNDEPLOY_GOAL = "undeploy";
    private static final String STOP_GOAL = "stop";
    private static final String APPLICATION_GOAL = "list-applications";
    private static final String NAMESPACE_GOAL = "list-namespaces";
    private static final String SUBSCRIPTION_GOAL = "list-subscriptions";
    public static final String PACKAGE_GOAL = "package";
    private static final String BUILD_FILE = "pom.xml";
    public static final String NAMESPACE_ATTR = "namespaceName";
    public static final String SUBSCRIPTION_ATTR = "subscriptionName";
    public static final String APPLICATION_NAME_ATTR = "applicationName";

    @Override
    public String getLoginCommand() {
        return String.format("mvn %s:%s:%s",
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, LOGIN_GOAL
        );
    }

    @Override
    public String getDevCommand() {
        return String.format("mvn %s %s:%s:%s",
                PACKAGE_GOAL,
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, DEV_GOAL
        );
    }

    @Override
    public String getDeployCommand() {
        return String.format("mvn %s %s:%s:%s",
                PACKAGE_GOAL,
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, DEPLOY_GOAL
        );
    }

    @Override
    public String getUndeployCommand() {
        return String.format("mvn %s %s:%s:%s",
                PACKAGE_GOAL,
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, UNDEPLOY_GOAL
        );
    }

    @Override
    public String getStartCommand() {
        return String.format("mvn %s %s:%s:%s",
                PACKAGE_GOAL,
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, START_GOAL
        );
    }

    @Override
    public String getStopCommand() {
        return String.format("mvn %s:%s:%s",
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, STOP_GOAL
        );
    }

    @Override
    public String getApplicationCommand() {
        return String.format("mvn %s:%s:%s",
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, APPLICATION_GOAL
        );
    }

    @Override
    public String getNamespaceCommand() {
        return String.format("mvn %s:%s:%s",
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, NAMESPACE_GOAL
        );
    }

    @Override
    public String getSubscriptionCommand() {
        return String.format("mvn %s:%s:%s",
                CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID, SUBSCRIPTION_GOAL
        );
    }

    public static CloudMavenProject getInstance(Project project) {
        PsiFile pom = getPomFile(project);
        if (pom != null) {
            return new CloudMavenProject(project, pom);
        }
        return null;
    }

    public CloudMavenProject(Project project, PsiFile pom) {
        super(project, pom);
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
     * @return true if pom.xml file includes Payara Cloud Maven plugin
     */
    private static boolean isValidPom(PsiFile pomFile) {
        try {
            Node pomRoot = MavenUtil.getPomRootNode(pomFile);
            return MavenUtil.getBuildNodes(pomRoot)
                    .stream()
                    .anyMatch(CloudMavenProject::isCloudPlugin);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            LOG.log(SEVERE, null, ex);
        }
        return false;
    }

    /**
     * @param buildNode
     * @return true if pom.xml file includes Payara Cloud Maven plugin
     */
    private static boolean isCloudPlugin(Node buildNode) {
        return MavenUtil.getPluginNode(buildNode, CLOUD_GROUP_ID, CLOUD_ARTIFACT_ID) != null;
    }

}
