/*
 * Copyright (c) 2020-2023 Payara Foundation and/or its affiliates and others.
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

public interface PayaraMicroConstants {

    String MODULE_ID = "Build Tools";
    String MODULE_TITLE = "Payara Micro";
    String MODULE_DESCRIPTION = "Payara Micro plugin enables you to start and stop the Payara Micro maven web applications";

    String ARCHETYPE_GROUP_ID = "fish.payara.maven.archetypes";
    String ARCHETYPE_ARTIFACT_ID = "payara-micro-maven-archetype";
    String ARCHETYPE_VERSION_5X = "1.4.0";
    String ARCHETYPE_VERSION = "RELEASE";
    String ARCHETYPE_GROUP_ID_KEY = "archetypeGroupId";
    String ARCHETYPE_ARTIFACT_ID_KEY = "archetypeArtifactId";
    String ARCHETYPE_VERSION_KEY = "archetypeVersion";
    String ARCHETYPE_INTERACTIVE_MODE = "interactiveMode";

    String PROP_GROUP_ID = "groupId";
    String PROP_ARTIFACT_ID = "artifactId";
    String PROP_VERSION = "version";

    String PROP_AUTO_BIND_HTTP = "autoBindHttp";
    String PROP_CONTEXT_ROOT = "contextRoot";
    String PROP_ADD_PAYARA_API = "addPayaraApi";
    String PROP_JDK_VERSION = "jdkVersion";

}
