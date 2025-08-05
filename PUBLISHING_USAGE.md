# Publishing Script Usage

## Quick Start

To publish a new version to Maven Central:

```bash
./publish.sh 1.0.2
```

## What the script does:

1. ✅ **Updates version** in both `gradle.properties` and `build.gradle.kts`
2. ✅ **Cleans and builds** the project with tests
3. ✅ **Generates** all required files (JAR, sources, javadoc, POM)
4. ✅ **Signs** all files with PGP
5. ✅ **Creates checksums** (MD5 and SHA1) for all files
6. ✅ **Organizes** files in proper Maven directory structure
7. ✅ **Creates ZIP bundle** ready for Central Portal upload

## Output

The script creates:
```
bundle/fuse-kt-{version}-bundle.zip
```

This ZIP contains:
```
io/github/neilsayok/fuse-kt/{version}/
├── fuse-kt-{version}.jar + .asc + .md5 + .sha1
├── fuse-kt-{version}-sources.jar + .asc + .md5 + .sha1
├── fuse-kt-{version}-javadoc.jar + .asc + .md5 + .sha1
└── fuse-kt-{version}.pom + .asc + .md5 + .sha1
```

## Upload to Maven Central

1. Go to https://central.sonatype.com
2. Log in with your credentials  
3. Navigate to "Upload Component"
4. Upload the generated ZIP file
5. Submit for publishing

## Version Examples

```bash
# Release versions
./publish.sh 1.0.0
./publish.sh 1.2.3
./publish.sh 2.0.0

# Pre-release versions  
./publish.sh 1.0.0-alpha
./publish.sh 1.0.0-beta.1
./publish.sh 1.0.0-rc.1
```

## Troubleshooting

### Build fails
- Make sure all tests pass: `./gradlew test`
- Check Kotlin compilation errors

### Signing fails  
- Verify GPG key is set up: `gpg --list-secret-keys`
- Check passphrase in script matches your key

### Missing files
- Ensure all required JARs are generated
- Check build configuration includes sources and javadoc

### Upload fails
- Verify all checksums are present
- Check directory structure in ZIP
- Confirm Central Portal credentials are valid

## Current Configuration

- **Group ID**: `io.github.neilsayok`  
- **Artifact ID**: `fuse-kt`
- **PGP Key**: `EFD6F74D07755432`
- **PGP Passphrase**: `maven-central-signing-2025`

## Publishing Checklist

Before running the script:

- [ ] All tests passing
- [ ] Code committed to git
- [ ] Version number decided
- [ ] Release notes prepared
- [ ] Central Portal account active

After publishing:

- [ ] Verify on Maven Central search
- [ ] Update documentation with new version
- [ ] Tag release in git
- [ ] Announce new version

## Maven Usage

Once published, users can add to their projects:

```kotlin
// Gradle (Kotlin DSL)
implementation("io.github.neilsayok:fuse-kt:1.0.0")

// Gradle (Groovy DSL)  
implementation 'io.github.neilsayok:fuse-kt:1.0.0'

// Maven
<dependency>
    <groupId>io.github.neilsayok</groupId>
    <artifactId>fuse-kt</artifactId>
    <version>1.0.0</version>
</dependency>
```