# Maven Central Publishing Guide

## Setup Status ✅
- [x] PGP keys generated and configured
- [x] Build configuration ready
- [x] Project metadata complete
- [x] Public key uploaded to keyserver
- [ ] Sonatype credentials (in progress)

## Steps to Publish

### 1. Get Credentials
- Register at: https://central.sonatype.com
- Use GitHub login for `neilsayok` account
- Note your username and password

### 2. Update gradle.properties
Uncomment and fill in:
```properties
ossrhUsername=your_central_portal_username
ossrhPassword=your_central_portal_password
```

### 3. Publish to Maven Central
```bash
# Test the build first
./gradlew clean build

# Publish to staging
./gradlew publishToSonatype

# Release to Maven Central (this makes it public)
./gradlew closeAndReleaseSonatypeStagingRepository
```

## Your Artifact Details
- **Group ID**: `io.github.neilsayok`
- **Artifact ID**: `fuse-kt`
- **Version**: `1.0.0`
- **Maven Coordinates**: `io.github.neilsayok:fuse-kt:1.0.0`

## PGP Key Info
- **Key ID**: `EFD6F74D07755432`
- **Expires**: August 1, 2027
- **Uploaded to**: keyserver.ubuntu.com

## Troubleshooting
- If signing fails: Check that PGP key is properly configured
- If credentials fail: Verify Central Portal registration
- For support: Email central-support@sonatype.com

## After Publishing
Your library will be available at:
- Maven Central: https://search.maven.org/artifact/io.github.neilsayok/fuse-kt/1.0.0/jar
- Usage in projects:
  ```kotlin
  implementation("io.github.neilsayok:fuse-kt:1.0.0")
  ```