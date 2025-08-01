# Publishing Fuse-kt to Maven Central

This document provides a step-by-step guide for publishing the Fuse-kt library to Maven Central.

## Prerequisites

### 1. Maven Central Publisher Account

**Note**: As of January 2024, the old issues.sonatype.org process has been replaced with the Central Publisher Portal.

1. Register at the [Central Publisher Portal](https://central.sonatype.com/)
2. Follow the [Central Portal registration documentation](https://central.sonatype.org/register/central-portal/)
3. Verify your namespace ownership (GitHub repository or domain verification)
4. Wait for approval of your namespace (usually takes 1-2 business days)

**Namespace Selection**:
- **GitHub-based** (recommended): `io.github.yourusername` - Requires a public GitHub repository
- **Domain-based**: `com.yourcompany` - Requires ownership of `yourcompany.com` domain
- **Reverse domain**: Follow the reverse domain naming convention

This project is configured to use `io.github.yourusername` by default. You'll need to:
1. Replace `yourusername` with your actual GitHub username in the build files
2. Create a public GitHub repository for namespace verification

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
# Central Portal credentials
ossrhUsername=your_central_portal_username
ossrhPassword=your_central_portal_token

# GPG signing key (base64 encoded private key)
signingKey=your_base64_encoded_private_key
signingPassword=your_private_key_passphrase
```

**Important**: Never commit these credentials to version control!

## Step-by-Step Publishing Process

### Phase 1: Initial Setup (One-time)

#### Step 1: Prepare Your GitHub Repository

1. **Create a public GitHub repository**:
   ```bash
   # On GitHub, create a new repository named 'fuse-kt'
   # Make sure it's public (required for namespace verification)
   ```

2. **Push your code to GitHub**:
   ```bash
   cd /path/to/your/fuse-kt
   git init
   git add .
   git commit -m "Initial commit: Fuse-kt library"
   git branch -M main
   git remote add origin https://github.com/yourusername/fuse-kt.git
   git push -u origin main
   ```

#### Step 2: Update Project Configuration

1. **Replace placeholder values** in the following files:

   **`build.gradle.kts`**:
   ```kotlin
   group = "io.github.yourusername"  // Replace 'yourusername'
   ```

   **`gradle.properties`**:
   ```properties
   GROUP=io.github.yourusername  # Replace 'yourusername'
   ```

   **POM metadata in `build.gradle.kts`**:
   ```kotlin
   pom {
       name.set("Fuse-kt")
       description.set("A lightweight fuzzy-search library for Kotlin, ported from Fuse.js")
       url.set("https://github.com/yourusername/fuse-kt")  // Update URL
       
       developers {
           developer {
               id.set("yourusername")           // Your GitHub username
               name.set("Your Full Name")       // Your actual name
               email.set("your.email@example.com")  // Your email
           }
       }
       
       scm {
           connection.set("scm:git:git://github.com/yourusername/fuse-kt.git")
           developerConnection.set("scm:git:ssh://github.com:yourusername/fuse-kt.git")
           url.set("https://github.com/yourusername/fuse-kt")
       }
   }
   ```

2. **Commit your changes**:
   ```bash
   git add .
   git commit -m "Update project metadata with actual GitHub username"
   git push
   ```

#### Step 3: Generate GPG Key Pair

1. **Generate a new GPG key**:
   ```bash
   gpg --gen-key
   # Follow prompts:
   # - Select RSA and RSA (default)
   # - Use 4096 bits
   # - Set expiration (recommend 2 years)
   # - Enter your name and email (should match GitHub)
   # - Create a secure passphrase
   ```

2. **List your keys to get the KEY_ID**:
   ```bash
   gpg --list-secret-keys --keyid-format=long
   # Output will show something like:
   # sec   rsa4096/ABCDEF1234567890 2024-01-01 [SC] [expires: 2026-01-01]
   # The KEY_ID is: ABCDEF1234567890
   ```

3. **Upload public key to keyserver**:
   ```bash
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   # Also upload to other keyservers for redundancy:
   gpg --keyserver keys.openpgp.org --send-keys YOUR_KEY_ID
   ```

4. **Export private key as base64**:
   ```bash
   gpg --export-secret-keys --armor YOUR_KEY_ID | base64 | pbcopy
   # This copies the base64-encoded private key to clipboard
   ```

#### Step 4: Register with Central Portal

1. **Go to Central Portal**: https://central.sonatype.com/

2. **Sign up with GitHub**:
   - Click "Sign up"
   - Choose "Sign up with GitHub" 
   - Authorize the application

3. **Request namespace access**:
   - Go to "Namespaces" in your account
   - Click "Add Namespace"
   - Enter `io.github.yourusername`
   - The system will automatically verify via your GitHub repository

4. **Wait for approval**: Usually takes a few hours to 1 business day

#### Step 5: Configure Credentials

1. **Create `~/.gradle/gradle.properties`** (if it doesn't exist):
   ```bash
   touch ~/.gradle/gradle.properties
   chmod 600 ~/.gradle/gradle.properties  # Secure permissions
   ```

2. **Add your credentials**:
   ```properties
   # Central Portal credentials
   ossrhUsername=your_github_username
   ossrhPassword=your_central_portal_token  # Generate this from Central Portal

   # GPG signing key
   signingKey=your_base64_encoded_private_key
   signingPassword=your_gpg_key_passphrase
   ```

3. **Generate Central Portal token**:
   - In Central Portal, go to "View Account"
   - Click "Generate User Token"
   - Copy the token and use it as `ossrhPassword`

### Phase 2: Publishing (Every Release)

#### Step 6: Prepare Release

1. **Update version** in `build.gradle.kts`:
   ```kotlin
   version = "1.0.0"  // Remove -SNAPSHOT for releases
   ```

2. **Test locally**:
   ```bash
   ./gradlew clean build test
   ./gradlew publishToMavenLocal
   
   # Verify local publication
   ls ~/.m2/repository/io/github/yourusername/fuse-kt/1.0.0/
   ```

3. **Commit version update**:
   ```bash
   git add build.gradle.kts
   git commit -m "Release version 1.0.0"
   git tag v1.0.0
   ```

#### Step 7: Publish to Maven Central

**Option A: Using the Publish Script (Recommended)**:
```bash
chmod +x publish.sh
./publish.sh
```

**Option B: Manual Commands**:
```bash
# Publish and auto-promote to Maven Central
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository
```

#### Step 8: Verify Publication

1. **Check staging repository** (for manual verification):
   - Go to https://s01.oss.sonatype.org/
   - Login with your Central Portal credentials
   - Check "Staging Repositories" for your artifact

2. **Wait for Maven Central sync** (10-30 minutes):
   ```bash
   # Check if available on Maven Central
   curl -s "https://search.maven.org/solrsearch/select?q=g:io.github.yourusername+AND+a:fuse-kt&rows=1&wt=json" | jq '.response.numFound'
   ```

3. **Verify artifact download**:
   ```bash
   # Create a test project to verify
   mkdir test-fuse-kt && cd test-fuse-kt
   gradle init --type kotlin-library
   
   # Add dependency to build.gradle.kts
   echo 'dependencies { implementation("io.github.yourusername:fuse-kt:1.0.0") }' >> build.gradle.kts
   
   # Try to resolve dependency
   ./gradlew dependencies --configuration compileClasspath
   ```

#### Step 9: Post-Release Tasks

1. **Push release to GitHub**:
   ```bash
   git push origin main --tags
   ```

2. **Create GitHub release**:
   - Go to your GitHub repository
   - Click "Releases" → "Create a new release"
   - Select tag `v1.0.0`
   - Add release notes
   - Publish release

3. **Prepare next development version**:
   ```bash
   # Update to next snapshot version
   # In build.gradle.kts:
   version = "1.1.0-SNAPSHOT"
   
   git add build.gradle.kts
   git commit -m "Bump version to 1.1.0-SNAPSHOT"
   git push
   ```

### Quick Reference Commands

```bash
# Test everything locally
./gradlew clean build test publishToMavenLocal

# Publish SNAPSHOT version
./gradlew publishToSonatype

# Publish RELEASE version (with auto-promotion)
./gradlew publishToSonatype closeAndReleaseSonatypeStagingRepository

# Use convenience script
./publish.sh
```

### Troubleshooting the Process

If you encounter issues during any step:

1. **Namespace not approved**: Wait longer or contact central-support@sonatype.com
2. **GPG key issues**: Verify key is uploaded to keyservers and base64 encoding is correct
3. **Authentication failures**: Double-check your Central Portal token
4. **Build failures**: Run `./gradlew build --info` for detailed error messages

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
   - Check your Central Portal credentials
   - Ensure you're using a token (not password) from the Central Portal
   - Verify you have permission to publish under the group ID

2. **"No signing key found"**
   - Verify your GPG key is properly configured
   - Check that the base64 encoding is correct

3. **"Repository not found"**
   - Ensure your namespace has been approved in the Central Portal
   - Check that the repository URL is correct

4. **"Staging repository already exists"**
   - Close and release any existing staging repositories
   - Or use a different version number

5. **"Namespace not verified"**
   - Complete namespace verification in the Central Portal
   - For GitHub namespaces, ensure the repository exists and is public

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

- **Central Portal Support**: Email central-support@sonatype.com (do not include previous reply threads)
- **Central Portal Documentation**: [https://central.sonatype.org/](https://central.sonatype.org/)
- **Gradle Publishing Guide**: [https://docs.gradle.org/current/userguide/publishing_maven.html](https://docs.gradle.org/current/userguide/publishing_maven.html)
- **Maven Central Requirements**: [https://central.sonatype.org/publish/requirements/](https://central.sonatype.org/publish/requirements/)