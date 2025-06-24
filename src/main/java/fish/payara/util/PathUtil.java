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
package fish.payara.util;

import java.io.File;
import java.util.List;
import fish.payara.PayaraBundle;
import static fish.payara.PayaraConstants.PAYARA_BIN_DIRECTORY_NAME;
import static fish.payara.PayaraConstants.PAYARA_JAR_PATTERN;
import static fish.payara.PayaraConstants.PAYARA_MODULES_DIRECTORY_NAME;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collections;
import java.util.regex.Pattern;
import static java.util.stream.Collectors.toList;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.util.text.StringUtil;

/**
 *
 * @author Gaurav Gupta
 */
public class PathUtil {

    public static void checkValidServerHome(String home, String version) throws Exception {
        if (StringUtil.isEmptyOrSpaces(home)) {
            throw new Exception(PayaraBundle.message("error.message.payara.home.not.specified"));
        }

        checkDir(new File(home, PAYARA_BIN_DIRECTORY_NAME));
        checkDir(new File(home, PAYARA_MODULES_DIRECTORY_NAME));

        if (!isValidServerPath(home)) {
            throw new FileNotFoundException(PayaraBundle.message("error.message.payara.api.jar.not.found"));
        }
    }

    private static void checkDir(File dir) throws FileNotFoundException {
        if (!dir.exists() || !dir.isDirectory()) {
            throw new FileNotFoundException("Directory not found or is not a directory: " + dir.getAbsolutePath());
        }
    }

    private static boolean isValidServerPath(@NotNull String home) {
        return findLibByPattern(home, PAYARA_JAR_PATTERN);
    }

    private static boolean findLibByPattern(@NotNull String home, @NotNull Pattern jarPattern) {
        final File libDir = new File(home, PAYARA_MODULES_DIRECTORY_NAME);
        return libDir.isDirectory() && !findFilesByMask(jarPattern, libDir).isEmpty();
    }

    private static List<File> findFilesByMask(@NotNull Pattern pattern, @NotNull File dir) {
        final File[] files = dir.listFiles();
        if (files != null) {
            return Arrays.stream(files)
                    .filter(f -> pattern.matcher(f.getName()).matches())
                    .collect(toList());
        }
        return Collections.emptyList();
    }

}
