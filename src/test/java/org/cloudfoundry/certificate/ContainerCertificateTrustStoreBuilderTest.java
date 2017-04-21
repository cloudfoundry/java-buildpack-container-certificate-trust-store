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

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

import static org.assertj.core.api.Assertions.assertThat;

public final class ContainerCertificateTrustStoreBuilderTest {

    @Test
    public void darwin() throws Throwable {
        File containerSource = new ClassPathResource("cert.pem").getFile();
        Path destination = Files.createTempFile("keystore-", ".jks");
        String destinationPassword = "test-password";
        File jreSource = new ClassPathResource("cacerts").getFile();

        Files.deleteIfExists(destination);
        ContainerCertificateTrustStoreBuilder.main(new String[]{"--container-source", containerSource.getCanonicalPath(), "--destination", destination.toFile().getCanonicalPath(),
            "--destination-password", destinationPassword, "--jre-source", jreSource.getCanonicalPath(), "--jre-source-password", "changeit"});

        assertThat(getKeyStoreSize(destination, destinationPassword)).isEqualTo(206);
    }

    @Test
    public void darwinNoJreSource() throws Throwable {
        File containerSource = new ClassPathResource("cert.pem").getFile();
        Path destination = Files.createTempFile("keystore-", ".jks");
        String destinationPassword = "test-password";

        Files.deleteIfExists(destination);
        ContainerCertificateTrustStoreBuilder.main(new String[]{"--container-source", containerSource.getCanonicalPath(), "--destination", destination.toFile().getCanonicalPath(),
            "--destination-password", destinationPassword});

        assertThat(getKeyStoreSize(destination, destinationPassword)).isEqualTo(48);
    }

    @Test
    public void unix() throws Throwable {
        File containerSource = new ClassPathResource("ca-certificates.crt").getFile();
        Path destination = Files.createTempFile("keystore-", ".jks");
        String destinationPassword = "test-password";
        File jreSource = new ClassPathResource("cacerts").getFile();

        Files.deleteIfExists(destination);
        ContainerCertificateTrustStoreBuilder.main(new String[]{"--container-source", containerSource.getCanonicalPath(), "--destination", destination.toFile().getCanonicalPath(),
            "--destination-password", destinationPassword, "--jre-source", jreSource.getCanonicalPath(), "--jre-source-password", "changeit"});

        assertThat(getKeyStoreSize(destination, destinationPassword)).isEqualTo(331);
    }

    @Test
    public void unixNoJreSource() throws Throwable {
        File containerSource = new ClassPathResource("ca-certificates.crt").getFile();
        Path destination = Files.createTempFile("keystore-", ".jks");
        String destinationPassword = "test-password";

        Files.deleteIfExists(destination);
        ContainerCertificateTrustStoreBuilder.main(new String[]{"--container-source", containerSource.getCanonicalPath(), "--destination", destination.toFile().getCanonicalPath(),
            "--destination-password", destinationPassword});

        assertThat(getKeyStoreSize(destination, destinationPassword)).isEqualTo(173);
    }

    private int getKeyStoreSize(Path destination, String password) throws KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {
        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        try (InputStream in = Files.newInputStream(destination, StandardOpenOption.READ)) {
            keyStore.load(in, password.toCharArray());
        }

        int count = 0;
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            aliases.nextElement();
            count++;
        }

        return count;
    }

}
