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

import com.intellij.ide.util.PropertiesComponent;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gaurav Gupta
 */
public class SecurePasswordStore {

    private static final Logger LOG = Logger.getLogger(SecurePasswordStore.class.getName());
    private static final String PASSWORD_KEY = "PAYARA_ADMIN_PASSWORD";
    private static final String DEFAULT_KEY = "PayaraServerKey1";

    private static String getEncryptionKey() {
        try {
            Path keyPath = Path.of(System.getProperty("user.home"), ".payara", "plugin_key");
            if (Files.exists(keyPath)) {
                String key = Files.readString(keyPath, StandardCharsets.UTF_8).trim();
                if (key.length() == 16) {
                    return key;
                } else {
                    LOG.log(Level.FINE, "Key in file is not 16 characters. Using default key.");
                }
            } else {
                LOG.log(Level.FINE, "Key file {0} not found. Using default key.", keyPath);
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error reading key file. Using default key.");
        }

        return DEFAULT_KEY;
    }

    public static void storePassword(String password) {
        try {
            String key = getEncryptionKey();
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encrypted = cipher.doFinal(password.getBytes(StandardCharsets.UTF_8));

            String encoded = Base64.getEncoder().encodeToString(encrypted);
            PropertiesComponent.getInstance().setValue(PASSWORD_KEY, encoded);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to store password securely: {0}", e.getMessage());
        }
    }

    public static String loadPassword() {
        try {
            String encoded = PropertiesComponent.getInstance().getValue(PASSWORD_KEY);
            if (encoded == null) return null;

            byte[] encrypted = Base64.getDecoder().decode(encoded);
            String key = getEncryptionKey();
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decrypted = cipher.doFinal(encrypted);

            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to load password securely: {0}", e.getMessage());
            return null;
        }
    }
}
