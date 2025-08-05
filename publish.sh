#!/bin/bash

# Maven Central Publishing Script for fuse-kt
# Usage: ./publish.sh [version]
# Example: ./publish.sh 1.0.1

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if version is provided
if [ -z "$1" ]; then
    print_error "Version number is required!"
    echo "Usage: $0 <version>"
    echo "Example: $0 1.0.1"
    exit 1
fi

NEW_VERSION="$1"
ARTIFACT_NAME="fuse-kt"
BUNDLE_DIR="bundle"
BUNDLE_ZIP="${ARTIFACT_NAME}-${NEW_VERSION}-bundle.zip"

print_status "Publishing ${ARTIFACT_NAME} version ${NEW_VERSION} to Maven Central"

# Validate version format (basic check)
if [[ ! $NEW_VERSION =~ ^[0-9]+\.[0-9]+\.[0-9]+(-[a-zA-Z0-9]+)?$ ]]; then
    print_warning "Version format seems unusual: $NEW_VERSION"
    print_warning "Expected format: x.y.z or x.y.z-suffix"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        print_error "Aborted by user"
        exit 1
    fi
fi

# Update version in gradle.properties and build.gradle.kts
print_status "Updating version in gradle.properties..."
sed -i.bak "s/^VERSION_NAME=.*/VERSION_NAME=${NEW_VERSION}/" gradle.properties
if [ $? -eq 0 ]; then
    print_success "Version updated in gradle.properties to ${NEW_VERSION}"
    rm gradle.properties.bak
else
    print_error "Failed to update gradle.properties"
    exit 1
fi

print_status "Updating version in build.gradle.kts..."
sed -i.bak "s/^version = \".*\"/version = \"${NEW_VERSION}\"/" build.gradle.kts
if [ $? -eq 0 ]; then
    print_success "Version updated in build.gradle.kts to ${NEW_VERSION}"
    rm build.gradle.kts.bak
else
    print_error "Failed to update build.gradle.kts"
    exit 1
fi

# Clean previous builds
print_status "Cleaning previous builds..."
./gradlew clean

# Build the project
print_status "Building project..."
./gradlew build -x signMavenPublication
if [ $? -ne 0 ]; then
    print_error "Build failed!"
    exit 1
fi
print_success "Build completed successfully"

# Generate POM file
print_status "Generating POM file..."
./gradlew generatePomFileForMavenPublication
if [ $? -ne 0 ]; then
    print_error "POM generation failed!"
    exit 1
fi

# Create bundle directory
print_status "Creating bundle directory..."
rm -rf "$BUNDLE_DIR"
mkdir -p "$BUNDLE_DIR"

# Copy JAR files
print_status "Copying JAR files..."
cp build/libs/${ARTIFACT_NAME}-${NEW_VERSION}*.jar "$BUNDLE_DIR/"
if [ $? -ne 0 ]; then
    print_error "Failed to copy JAR files"
    exit 1
fi

# Copy POM file
print_status "Copying POM file..."
cp build/publications/maven/pom-default.xml "$BUNDLE_DIR/${ARTIFACT_NAME}-${NEW_VERSION}.pom"
if [ $? -ne 0 ]; then
    print_error "Failed to copy POM file"
    exit 1
fi

# Change to bundle directory for signing
cd "$BUNDLE_DIR"

# Check if required files exist
REQUIRED_FILES=(
    "${ARTIFACT_NAME}-${NEW_VERSION}.jar"
    "${ARTIFACT_NAME}-${NEW_VERSION}-sources.jar"
    "${ARTIFACT_NAME}-${NEW_VERSION}-javadoc.jar"
    "${ARTIFACT_NAME}-${NEW_VERSION}.pom"
)

print_status "Verifying required files..."
for file in "${REQUIRED_FILES[@]}"; do
    if [ ! -f "$file" ]; then
        print_error "Required file missing: $file"
        exit 1
    fi
    print_success "✓ $file"
done

# Sign all files and generate checksums
print_status "Signing files and generating checksums..."
for file in *.jar *.pom; do
    # Remove existing signature and checksums if they exist
    rm -f "${file}.asc" "${file}.md5" "${file}.sha1"
    
    # Sign the file
    echo "maven-central-signing-2025" | gpg --batch --pinentry-mode loopback --passphrase-fd 0 --armor --detach-sign "$file"
    if [ $? -ne 0 ]; then
        print_error "Failed to sign $file"
        exit 1
    fi
    print_success "✓ Signed $file"
    
    # Generate MD5 checksum
    if command -v md5sum >/dev/null 2>&1; then
        md5sum "$file" | cut -d' ' -f1 > "${file}.md5"
    elif command -v md5 >/dev/null 2>&1; then
        md5 -q "$file" > "${file}.md5"
    else
        print_error "Neither md5sum nor md5 command found"
        exit 1
    fi
    print_success "✓ Generated MD5 for $file"
    
    # Generate SHA1 checksum
    if command -v sha1sum >/dev/null 2>&1; then
        sha1sum "$file" | cut -d' ' -f1 > "${file}.sha1"
    elif command -v shasum >/dev/null 2>&1; then
        shasum -a 1 "$file" | cut -d' ' -f1 > "${file}.sha1"
    else
        print_error "Neither sha1sum nor shasum command found"
        exit 1
    fi
    print_success "✓ Generated SHA1 for $file"
done

# Verify all required files exist
print_status "Verifying all generated files..."
for file in *.jar *.pom; do
    for ext in ".asc" ".md5" ".sha1"; do
        if [ ! -f "${file}${ext}" ]; then
            print_error "Missing ${file}${ext}"
            exit 1
        fi
        print_success "✓ ${file}${ext}"
    done
done

# Create proper directory structure for Maven Central
print_status "Creating proper directory structure..."
GROUP_PATH="io/github/neilsayok"
ARTIFACT_PATH="${GROUP_PATH}/${ARTIFACT_NAME}/${NEW_VERSION}"
mkdir -p "$ARTIFACT_PATH"

# Move all files to proper Maven path
mv *.jar *.pom *.asc *.md5 *.sha1 "$ARTIFACT_PATH/"

# Create ZIP bundle with proper structure
print_status "Creating ZIP bundle with Maven directory structure..."
rm -f "$BUNDLE_ZIP"
zip -r "$BUNDLE_ZIP" io/
if [ $? -ne 0 ]; then
    print_error "Failed to create ZIP bundle"
    exit 1
fi

# Move back to project root
cd ..

print_success "Publishing bundle created successfully!"
echo
echo "📦 Bundle: ${BUNDLE_DIR}/${BUNDLE_ZIP}"
echo "📋 Contents (organized in Maven directory structure):"
echo "   • ${ARTIFACT_NAME}-${NEW_VERSION}.jar + .asc + .md5 + .sha1"
echo "   • ${ARTIFACT_NAME}-${NEW_VERSION}-sources.jar + .asc + .md5 + .sha1"
echo "   • ${ARTIFACT_NAME}-${NEW_VERSION}-javadoc.jar + .asc + .md5 + .sha1"
echo "   • ${ARTIFACT_NAME}-${NEW_VERSION}.pom + .asc + .md5 + .sha1"
echo "   • Proper Maven path: io/github/neilsayok/${ARTIFACT_NAME}/${NEW_VERSION}/"
echo
echo "🚀 Next steps:"
echo "   1. Go to https://central.sonatype.com"
echo "   2. Log in with your credentials"
echo "   3. Navigate to 'Upload Component'"
echo "   4. Upload: ${BUNDLE_DIR}/${BUNDLE_ZIP}"
echo "   5. Submit for publishing"
echo
echo "📖 Maven coordinates:"
echo "   implementation(\"io.github.neilsayok:${ARTIFACT_NAME}:${NEW_VERSION}\")"
echo
print_success "Ready for Maven Central publishing! 🎉"