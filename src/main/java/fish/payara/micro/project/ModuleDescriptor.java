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

/**
 *
 * @author Gaurav Gupta <gaurav.gupta@payara.fish>
 */
public class ModuleDescriptor {

    private String groupId;
    private String artifactId;
    private boolean autoBindHttp;
    private String contextRoot;
    private String microVersion;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public boolean isAutoBindHttp() {
        return autoBindHttp;
    }

    public void setAutoBindHttp(boolean autoBindHttp) {
        this.autoBindHttp = autoBindHttp;
    }

    public String getContextRoot() {
        return contextRoot;
    }

    public void setContextRoot(String contextRoot) {
        this.contextRoot = contextRoot;
    }

    public String getMicroVersion() {
        return microVersion;
    }

    public void setMicroVersion(String microVersion) {
        this.microVersion = microVersion;
    }

    @Override
    public String toString() {
        return "ModuleDescriptor{" +
                "groupId='" + groupId + '\'' +
                ", artifactId='" + artifactId + '\'' +
                ", autoBindHttp=" + autoBindHttp +
                ", contextRoot='" + contextRoot + '\'' +
                ", microVersion='" + microVersion + '\'' +
                '}';
    }
}
