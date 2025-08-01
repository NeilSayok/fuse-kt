#!/bin/bash

# Fuse-kt Publishing Script
# This script helps publish the library to Maven Central

set -e

echo "🚀 Fuse-kt Publishing Script"
echo "=============================="

# Check if credentials are set
if [[ -z "$OSSRH_USERNAME" && -z "$(grep ossrhUsername ~/.gradle/gradle.properties 2>/dev/null || echo '')" ]]; then
    echo "❌ Error: OSSRH credentials not found!"
    echo "Please set either:"
    echo "  - Environment variables: OSSRH_USERNAME and OSSRH_PASSWORD"
    echo "  - Or add them to ~/.gradle/gradle.properties"
    exit 1
fi

if [[ -z "$SIGNING_KEY" && -z "$(grep signingKey ~/.gradle/gradle.properties 2>/dev/null || echo '')" ]]; then
    echo "❌ Error: Signing key not found!"
    echo "Please set either:"
    echo "  - Environment variables: SIGNING_KEY and SIGNING_PASSWORD"
    echo "  - Or add them to ~/.gradle/gradle.properties"
    exit 1
fi

# Get current version
VERSION=$(grep "version = " build.gradle.kts | sed 's/.*version = "\(.*\)".*/\1/')
echo "📦 Current version: $VERSION"

# Check if this is a snapshot version
if [[ $VERSION == *"SNAPSHOT"* ]]; then
    echo "📋 This is a SNAPSHOT version - will be published to snapshots repository"
else
    echo "📋 This is a RELEASE version - will be published to staging repository"
fi

echo ""
echo "🔍 Running tests..."
./gradlew clean test

echo ""
echo "🔨 Building project..."
./gradlew build

echo ""
if [[ $VERSION == *"SNAPSHOT"* ]]; then
    echo "📦 Publishing SNAPSHOT..."
    ./gradlew publishToSonatype
    echo ""
    echo "✅ Snapshot published successfully!"
    echo "Available at: https://s01.oss.sonatype.org/content/repositories/snapshots/"
else
    echo "📦 Publishing RELEASE and auto-promoting to Maven Central..."
    ./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
    echo ""
    echo "✅ Release published and promoted to Maven Central!"
    echo ""
    echo "The library should be available in Maven Central within 10-30 minutes:"
    echo "https://search.maven.org/search?q=g:io.github.yourusername+AND+a:fuse-kt"
fi

echo ""
echo "🎉 Publishing completed!"