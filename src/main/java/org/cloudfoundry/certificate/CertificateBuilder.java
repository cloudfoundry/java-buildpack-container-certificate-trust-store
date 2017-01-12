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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

final class CertificateBuilder {

    private static final String BEGIN_CERTIFICATE = "-----BEGIN CERTIFICATE-----";

    private static final String END_CERTIFICATE = "-----END CERTIFICATE-----";

    private final List<String> certificates = new ArrayList<>();

    private List<String> current;

    private CertificateBuilder() {
    }

    static CertificateBuilder accumulate(CertificateBuilder builder, String s) {
        if (BEGIN_CERTIFICATE.equals(s)) {
            builder.start();
        }

        builder.add(s);

        if (END_CERTIFICATE.equals(s)) {
            builder.end();
        }

        return builder;

    }

    static CertificateBuilder combine(CertificateBuilder left, CertificateBuilder right) {
        left.pemEncodedCertificates().addAll(right.certificates);
        return left;
    }

    static CertificateBuilder identity() {
        return new CertificateBuilder();
    }

    List<String> pemEncodedCertificates() {
        return this.certificates;
    }

    private void add(String s) {
        if (this.current == null) {
            return;
        }

        this.current.add(s);
    }

    private void end() {
        if (this.current == null) {
            return;
        }

        this.certificates.add(this.current.stream().collect(Collectors.joining("\n")));
        this.current = null;
    }

    private void start() {
        this.current = new ArrayList<>();
    }

}
