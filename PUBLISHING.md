# Publishing Fuse-kt to Maven Central

This document provides a step-by-step guide for publishing the Fuse-kt library to Maven Central.

## Prerequisites

### 1. Sonatype OSSRH Account

1. Create a Sonatype JIRA account at [issues.sonatype.org](https://issues.sonatype.org)
2. Create a "New Project" ticket requesting access to publish under the `com.fusekt` group ID
3. Wait for approval (usually takes 1-2 business days)

### 2. GPG Key Pair

Generate a GPG key pair for signing your artifacts:

```bash
# Generate a new key pair
gpg --gen-key

# List your keys to get the KEY_ID
gpg --list-secret-keys --keyid-format=long

# Export the public key to a keyserver
gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID

# Export private key as base64 for use in CI/CD
gpg --export-secret-keys --armor YOUR_KEY_ID | base64 | pbcopy
```

### 3. Configure Credentials

Add your credentials to `~/.gradle/gradle.properties`:

```properties
# Sonatype OSSRH credentials
ossrhUsername=your_sonatype_username
ossrhPassword=your_sonatype_password

# GPG signing key (base64 encoded private key)
signingKey=your_base64_encoded_private_key
signingPassword=your_private_key_passphrase
```

**Important**: Never commit these credentials to version control!

## Publishing Process

### Option 1: Using the Publish Script (Recommended)

```bash
# Make sure the script is executable
chmod +x publish.sh

# Run the publish script
./publish.sh
```

The script will:
- Validate that credentials are configured
- Run tests
- Build the project
- Publish to the appropriate repository (snapshots or releases)
- For releases: automatically close and release the staging repository

### Option 2: Manual Gradle Commands

For **SNAPSHOT** versions:
```bash
./gradlew publishToSonatype
```

For **RELEASE** versions:
```bash
# Publish to staging and auto-release to Maven Central
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

# Or do it step by step:
./gradlew publishToSonatype                    # Publish to staging
./gradlew closeSonatypeStagingRepository       # Close staging repo
./gradlew releaseSonatypeStagingRepository     # Release to Maven Central
```

### Option 3: Local Testing

To test the publishing configuration locally:
```bash
./gradlew publishToMavenLocal
```

This publishes to your local Maven repository (`~/.m2/repository`).

## Version Management

### Versioning Scheme

- **Release versions**: `1.0.0`, `1.1.0`, `2.0.0`, etc.
- **Snapshot versions**: `1.0.0-SNAPSHOT`, `1.1.0-SNAPSHOT`, etc.

### Updating Version

Edit the `version` property in `build.gradle.kts`:

```kotlin
version = "1.1.0"  // For releases
version = "1.1.0-SNAPSHOT"  // For snapshots
```

### Release Process

1. **Update version** to remove `-SNAPSHOT` suffix
2. **Commit and tag** the release:
   ```bash
   git add build.gradle.kts
   git commit -m "Release version 1.0.0"
   git tag v1.0.0
   ```
3. **Publish** using the script or Gradle commands
4. **Bump version** to next snapshot:
   ```kotlin
   version = "1.1.0-SNAPSHOT"
   ```
5. **Commit** the version bump:
   ```bash
   git add build.gradle.kts
   git commit -m "Bump version to 1.1.0-SNAPSHOT"
   git push origin main --tags
   ```

## Repository Information

### Snapshot Repository
- **URL**: https://s01.oss.sonatype.org/content/repositories/snapshots/
- **Artifacts**: Available immediately after publishing
- **Usage**: For development and testing

### Release Repository (Maven Central)
- **URL**: https://repo1.maven.org/maven2/
- **Artifacts**: Available 10-30 minutes after release
- **Usage**: For production dependencies

## Troubleshooting

### Common Issues

1. **"401 Unauthorized"**
   - Check your OSSRH credentials
   - Ensure you have permission to publish under the group ID

2. **"No signing key found"**
   - Verify your GPG key is properly configured
   - Check that the base64 encoding is correct

3. **"Repository not found"**
   - Ensure you've been granted access to the group ID in Sonatype JIRA
   - Check that the repository URL is correct

4. **"Staging repository already exists"**
   - Close and release any existing staging repositories
   - Or use a different version number

### Verification

After publishing, verify your artifact is available:

1. **Snapshots**: Check https://s01.oss.sonatype.org/content/repositories/snapshots/com/fusekt/fuse-kt/
2. **Releases**: Check https://search.maven.org/artifact/com.fusekt/fuse-kt
3. **Local**: Check `~/.m2/repository/com/fusekt/fuse-kt/`

## Environment Variables (CI/CD)

For automated publishing in CI/CD pipelines, use environment variables:

```bash
export OSSRH_USERNAME="your_username"
export OSSRH_PASSWORD="your_password"
export SIGNING_KEY="your_base64_key"
export SIGNING_PASSWORD="your_key_passphrase"
```

## Security Best Practices

1. **Never commit credentials** to version control
2. **Use encrypted secrets** in CI/CD systems
3. **Rotate credentials** periodically
4. **Use separate credentials** for different projects/environments
5. **Monitor published artifacts** for any unauthorized changes

## Support

- **Sonatype Support**: [https://help.sonatype.com](https://help.sonatype.com)
- **Gradle Publishing Guide**: [https://docs.gradle.org/current/userguide/publishing_maven.html](https://docs.gradle.org/current/userguide/publishing_maven.html)
- **Maven Central Requirements**: [https://central.sonatype.org/publish/requirements/](https://central.sonatype.org/publish/requirements/)