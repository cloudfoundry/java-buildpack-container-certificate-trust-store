#!/usr/bin/env sh

set -e

cd java-buildpack-container-certificate-trust-store
./mvnw -q package
