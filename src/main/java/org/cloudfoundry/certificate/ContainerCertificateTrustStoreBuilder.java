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
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;
import java.util.List;

public final class ContainerCertificateTrustStoreBuilder {

    public static void main(String[] args) throws Throwable {
        Arguments arguments = Arguments.parse(args);
        createTrustStore(arguments.getContainerSource(), arguments.getDestination(), arguments.getDestinationPassword(), arguments.getJreSource(), arguments.getJreSourcePassword());
    }

    private static void addContainerCertificates(Path source, KeyStore keyStore) throws IOException, KeyStoreException {
        List<String> lines = Files.readAllLines(source, Charset.defaultCharset());

        CertificateBuilder certificateBuilder = CertificateBuilder.identity();
        for (String line : lines) {
            CertificateBuilder.accumulate(certificateBuilder, line);
        }

        for (String certificate : certificateBuilder.pemEncodedCertificates()) {
            KeyStoreBuilder.accumulate(keyStore, X509CertificateBuilder.toCertificate(certificate));
        }

    }

    private static void addJreCertificates(Path jreSource, String jreSourcePassword, KeyStore keyStore) throws KeyStoreException, IOException, CertificateException, NoSuchAlgorithmException {
        KeyStore jreKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        jreKeyStore.load(Files.newInputStream(jreSource), jreSourcePassword.toCharArray());

        Enumeration<String> aliases = jreKeyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();

            if (jreKeyStore.isCertificateEntry(alias)) {
                KeyStoreBuilder.accumulate(keyStore, jreKeyStore.getCertificate(alias));
            }
        }
    }

    private static void createTrustStore(Path containerSource, Path destination, String destinationPassword, Path jreSource, String jreSourcePassword) throws IOException, KeyStoreException,
        CertificateException, NoSuchAlgorithmException {
        KeyStore keyStore = KeyStoreBuilder.identity();

        addContainerCertificates(containerSource, keyStore);
        addJreCertificates(jreSource, jreSourcePassword, keyStore);

        store(destination, keyStore, destinationPassword);
    }

    private static void store(Path destination, KeyStore keyStore, String password) throws IOException, CertificateException, NoSuchAlgorithmException, KeyStoreException {
        try (OutputStream out = Files.newOutputStream(destination, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            keyStore.store(out, password.toCharArray());
        }
    }

}
