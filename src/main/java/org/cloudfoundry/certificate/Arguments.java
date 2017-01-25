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

import java.nio.file.Path;
import java.nio.file.Paths;

final class Arguments {

    private final Path containerSource;

    private final Path destination;

    private final String destinationPassword;

    private final Path jreSource;

    private final String jreSourcePassword;

    private Arguments(Path containerSource, Path destination, String destinationPassword, Path jreSource, String jreSourcePassword) {
        this.containerSource = containerSource;
        this.destination = destination;
        this.destinationPassword = destinationPassword;
        this.jreSource = jreSource;
        this.jreSourcePassword = jreSourcePassword;
    }

    static Arguments parse(String[] args) {
        String containerSource = null;
        String destination = null;
        String destinationPassword = null;
        String jreSource = null;
        String jreSourcePassword = null;

        int index = 0;
        while (index < args.length) {
            switch (args[index++]) {
                case "--container-source":
                    containerSource = args[index++];
                    break;
                case "--destination":
                    destination = args[index++];
                    break;
                case "--destination-password":
                    destinationPassword = args[index++];
                    break;
                case "--jre-source":
                    jreSource = args[index++];
                    break;
                case "--jre-source-password":
                    jreSourcePassword = args[index++];
                    break;
                default:
                    break;
            }
        }

        if (containerSource == null) {
            throw new IllegalArgumentException("Must specify --container-source");
        }

        if (destination == null) {
            throw new IllegalArgumentException("Must specify --destination");
        }

        if (destinationPassword == null) {
            throw new IllegalArgumentException("Must specify --destination-password");
        }

        if (jreSource == null) {
            throw new IllegalArgumentException("Must specify --jre-source");
        }

        if (jreSourcePassword == null) {
            throw new IllegalArgumentException("Must specify --jre-source-password");
        }

        return new Arguments(Paths.get(containerSource), Paths.get(destination), destinationPassword, Paths.get(jreSource), jreSourcePassword);
    }

    Path getContainerSource() {
        return this.containerSource;
    }

    Path getDestination() {
        return this.destination;
    }

    String getDestinationPassword() {
        return this.destinationPassword;
    }

    Path getJreSource() {
        return this.jreSource;
    }

    String getJreSourcePassword() {
        return this.jreSourcePassword;
    }

}
