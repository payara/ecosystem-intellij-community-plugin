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
package fish.payara.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import static java.util.stream.Collectors.toList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import com.intellij.psi.PsiFile;
import static java.util.logging.Level.SEVERE;
import java.util.logging.Logger;

/**
 *
 * @author Gaurav Gupta
 */
public class MavenUtil {

    private static final Logger LOG = Logger.getLogger(MavenUtil.class.getName());

    private static final String PROFILES = "profiles";
    private static final String PROFILE = "profile";
    private static final String BUILD = "build";
    private static final String PLUGINS = "plugins";
    private static final String PLUGIN = "plugin";
    private static final String GROUP_ID = "groupId";
    private static final String ARTIFACT_ID = "artifactId";
    private static final String NAME = "name";

    public static String getProjectName(PsiFile pomFile) {
        String artifactId = null;
        String name = null;
        try {
            Node pomRoot = getPomRootNode(pomFile);
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
            LOG.log(SEVERE, pomFile.getVirtualFile().getPath(), ex);
        }
        if (name != null) {
            return name;
        } else {
            return artifactId;
        }
    }

    public static Node getPluginNode(Node buildNode, String expectedGroupId, String expectedArtifactId) {
        NodeList buildChildNodes = buildNode.getChildNodes();
        for (int buildChildNodeIndex = 0; buildChildNodeIndex < buildChildNodes.getLength(); buildChildNodeIndex++) {
            Node buildChildNode = buildChildNodes.item(buildChildNodeIndex);
            for (Node pluginNode : MavenUtil.getPluginNodes(buildChildNode)) {
                NodeList pluginChildNodes = pluginNode.getChildNodes();
                boolean groupIdMatched = false;
                boolean artifactIdMatched = false;
                for (int i = 0; i < pluginChildNodes.getLength(); i++) {
                    Node node = pluginChildNodes.item(i);
                    if (node.getNodeName().equals(GROUP_ID)
                            && node.getTextContent().equals(expectedGroupId)) {
                        groupIdMatched = true;
                    } else if (node.getNodeName().equals(ARTIFACT_ID)
                            && node.getTextContent().equals(expectedArtifactId)) {
                        artifactIdMatched = true;
                    }
                    if (groupIdMatched && artifactIdMatched) {
                        return pluginNode;
                    }
                }
            }
        }
        return null;
    }

    public static Node getPomRootNode(PsiFile pomFile) throws ParserConfigurationException, SAXException, IOException {
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

    public static List<Node> getBuildNodes(Node pomRoot) {
        List<Node> buildNodes = new ArrayList<>();
        if (pomRoot != null) {
            NodeList childNodes = pomRoot.getChildNodes();
            for (int childNodeIndex = 0; childNodeIndex < childNodes.getLength(); childNodeIndex++) {
                Node childNode = childNodes.item(childNodeIndex);

                buildNodes.addAll(
                        getProfileNodes(childNode)
                                .stream()
                                .map(Node::getChildNodes)
                                .map(MavenUtil::getBuildNode)
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

    public static Node getBuildNode(NodeList childNode) {
        for (int index = 0; index < childNode.getLength(); index++) {
            if (childNode.item(index).getNodeName().equals(BUILD)) {
                return childNode.item(index);
            }
        }
        return null;
    }

    public static List<Node> getProfileNodes(Node childNode) {
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

    public static List<Node> getPluginNodes(Node childNode) {
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

}
