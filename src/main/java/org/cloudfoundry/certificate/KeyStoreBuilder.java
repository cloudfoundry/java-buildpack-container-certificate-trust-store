/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry.certificate;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicInteger;

final class KeyStoreBuilder {

    private static final AtomicInteger counter = new AtomicInteger();

    static KeyStore accumulate(KeyStore keyStore, X509Certificate certificate) {
        try {
            keyStore.setCertificateEntry(getAlias(), certificate);
            return keyStore;
        } catch (KeyStoreException e) {
            throw new WrapperException(e);
        }
    }

    static KeyStore combine(KeyStore left, KeyStore right) {
        try {
            Enumeration<String> aliases = right.aliases();
            while (aliases.hasMoreElements()) {
                String alias = aliases.nextElement();
                left.setCertificateEntry(alias, right.getCertificate(alias));
            }

            return left;
        } catch (KeyStoreException e) {
            throw new WrapperException(e);
        }
    }

    static KeyStore identity() {
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null);
            return keyStore;
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new WrapperException(e);
        }
    }

    private static String getAlias() {
        return String.valueOf(counter.getAndIncrement());
    }

}
