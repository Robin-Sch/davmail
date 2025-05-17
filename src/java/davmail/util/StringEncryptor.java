/*
 * DavMail POP/IMAP/SMTP/CalDav/LDAP Exchange Gateway
 * Copyright (C) 2010  Mickael Guessant
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package davmail.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Encrypt string with user password.
 * Simple implementation based on AES
 */
public class StringEncryptor {
    static final String ALGO = "PBEWithHmacSHA256AndAES_128";
    static final String DEFAULT_FINGERPRINT = "davmailgateway!&";
    static String fingerprint;

    static {
        try {
            fingerprint = (InetAddress.getLocalHost().getHostName()+DEFAULT_FINGERPRINT).substring(0, 16);
        } catch (Throwable t) {
            fingerprint = DEFAULT_FINGERPRINT;
        }
    }

    private final String password;

    public StringEncryptor(String password) {
        this.password = password;
    }

    public String encryptString(String value) throws IOException {
        try {
            byte[] plaintext = value.getBytes(StandardCharsets.UTF_8);

            // Encrypt
            Cipher enc = Cipher.getInstance(ALGO);
            enc.init(Cipher.ENCRYPT_MODE, getSecretKey(), getPBEParameterSpec());
            byte[] encrypted = enc.doFinal(plaintext);
            return "{AES}" + IOUtil.encodeBase64AsString(encrypted);

        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public String decryptString(String value) throws IOException {
        if (value != null && value.startsWith("{AES}")) {
            try {
                byte[] encrypted = IOUtil.decodeBase64(value.substring(5));

                Cipher dec = Cipher.getInstance(ALGO);
                dec.init(Cipher.DECRYPT_MODE, getSecretKey(), getPBEParameterSpec());
                byte[] decrypted = dec.doFinal(encrypted);
                return new String(decrypted, StandardCharsets.UTF_8);

            } catch (Exception e) {
                throw new IOException(e);
            }
        } else {
            return value;
        }
    }

    private SecretKey getSecretKey() throws InvalidKeySpecException, NoSuchAlgorithmException {
        PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());

        SecretKeyFactory kf = SecretKeyFactory.getInstance(ALGO);
        return kf.generateSecret(keySpec);
    }

    private PBEParameterSpec getPBEParameterSpec() {
        byte[] bytes = fingerprint.getBytes(StandardCharsets.UTF_8);
        return new PBEParameterSpec(bytes, 10000, new IvParameterSpec(bytes));
    }
}
