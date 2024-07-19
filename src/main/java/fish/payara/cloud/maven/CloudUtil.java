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

import java.util.List;
import javax.ws.rs.core.Link;
import fish.payara.tools.cloud.ApplicationContext;
import fish.payara.tools.cloud.ListSubscriptions;
import fish.payara.tools.cloud.ListNamespaces;

/**
 *
 * @author Gaurav Gupta
 */
public class CloudUtil {

    private static String CLIENT_ID = "qeqSpqVZtHGbr0YU75Z9p87HMle0RA5a";
    private static String CLIENT_NAME = "Payara Cloud Maven Plugin";

    static List<Link> getSubscriptions() throws Exception {
        ApplicationContext context = getApplicationContextBuilder(true).build();
        ListSubscriptions controller = new ListSubscriptions(context);
        return controller.call();
    }

    static List<Link> getNamespaces(String selectedSubscription) throws Exception {
        ApplicationContext.Builder builder = getApplicationContextBuilder(false);
        if (selectedSubscription != null && !selectedSubscription.trim().isEmpty()) {
            builder.subscriptionName(selectedSubscription);
        }
        ApplicationContext context = builder.build();
        ListNamespaces controller = new ListNamespaces(context);
        return controller.call();
    }

    private static ApplicationContext.Builder getApplicationContextBuilder(boolean intractive) {
        ApplicationContext.Builder builder = ApplicationContext.builder(CLIENT_ID, CLIENT_NAME)
                .clientOutput(new CloudMavenOutput(intractive))
                .interactive(intractive);
        return builder;
    }
}
