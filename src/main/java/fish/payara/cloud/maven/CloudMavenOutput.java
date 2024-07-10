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

import fish.payara.cloud.client.ClientOutput;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gaurav Gupta
 */
public class CloudMavenOutput implements ClientOutput {

    private static final Logger LOG = Logger.getLogger(CloudMavenOutput.class.getName());

    private final boolean interactive;

    public CloudMavenOutput(boolean interactive) {
        this.interactive = interactive;
    }

    @Override
    public void warning(String message) {
        LOG.warning(message);
    }

    @Override
    public void info(String message) {
        LOG.info(message);
    }

    @Override
    public void error(String message, Throwable cause) {
        LOG.log(Level.SEVERE, message, cause);
    }

    @Override
    public void started(Object processId, Runnable cancellation) {
        LOG.log(Level.FINE, "Started: {0}", processId);
    }

    @Override
    public void progress(Object processId, String message) {
        LOG.log(Level.FINE, "Progress: {0} {1}", new Object[]{processId, message});
    }

    @Override
    public void finished(Object processId) {
        LOG.log(Level.FINE, "Finished: {0}", processId);
    }

    @Override
    public void openUrl(URI uri) {
        LOG.info("Opening URL: " + uri);
        if (interactive) {
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(uri);
                } else {
                    LOG.warning("Desktop browsing is not supported on this platform.");
                }
            } catch (IOException e) {
                LOG.log(Level.SEVERE, "Failed to open URL: " + uri, e);
            }
        }
    }

    @Override
    public void failure(String message, Throwable cause) {
        LOG.log(Level.SEVERE, "Failure: " + message, cause);
    }
}