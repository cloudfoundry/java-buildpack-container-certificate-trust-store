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
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.List;

public final class ContainerCertificateTrustStoreBuilder {

    public static void main(String[] args) throws Throwable {
        try {
            int count = createTrustStore(Paths.get(args[0]), Paths.get(args[1]), args[2]);
            System.out.println(count);
        } catch (WrapperException e) {
            throw e.getCause();
        }
    }

    static int createTrustStore(Path source, Path destination, String password) throws IOException, KeyStoreException {
        List<String> lines = Files.readAllLines(source, Charset.defaultCharset());

        CertificateBuilder certificateBuilder = CertificateBuilder.identity();
        for (String line : lines) {
            CertificateBuilder.accumulate(certificateBuilder, line);
        }

        KeyStore keyStore = KeyStoreBuilder.identity();
        for (String certificate : certificateBuilder.pemEncodedCertificates()) {
            KeyStoreBuilder.accumulate(keyStore, X509CertificateBuilder.toCertificate(certificate));
        }

        store(destination, keyStore, password);
        return keyStore.size();
    }

    private static void store(Path destination, KeyStore keyStore, String password) {
        try (OutputStream out = Files.newOutputStream(destination, StandardOpenOption.CREATE_NEW, StandardOpenOption.WRITE)) {
            keyStore.store(out, password.toCharArray());
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            throw new WrapperException(e);
        }
    }

}
