# Maven Central Publishing - Updated Process (2024)

## 🚨 Important Changes

As of January 2024, Sonatype has updated their Maven Central publishing process:

- ❌ **OLD**: `issues.sonatype.org` (decommissioned)
- ✅ **NEW**: [Central Publisher Portal](https://central.sonatype.com/)

## Quick Setup Guide

### 1. Choose Your Namespace

For this project, you have two options:

#### Option A: GitHub Namespace (Recommended)
```kotlin
group = "io.github.yourusername"  // Replace with your GitHub username
```

**Requirements**:
- Public GitHub repository at `github.com/yourusername/fuse-kt`
- Automatic verification via GitHub

#### Option B: Domain Namespace
```kotlin
group = "com.yourcompany"  // If you own yourcompany.com
```

**Requirements**:
- Own the domain `yourcompany.com`
- DNS verification required

### 2. Update Configuration

Replace `yourusername` in these files:
- `build.gradle.kts` (group property)
- `gradle.properties` (GROUP property)
- `README.md` (installation instructions)

### 3. Register with Central Portal

1. Go to [central.sonatype.com](https://central.sonatype.com/)
2. Sign up with your GitHub account (for GitHub namespaces)
3. Request access to your namespace
4. Complete verification process

### 4. Set Up Credentials

Create `~/.gradle/gradle.properties`:
```properties
# Central Portal credentials (not the old OSSRH)
ossrhUsername=your_central_portal_username  
ossrhPassword=your_central_portal_token     # Generate from Central Portal

# GPG signing
signingKey=your_base64_encoded_private_key
signingPassword=your_gpg_key_passphrase
```

### 5. Publish

```bash
# Test locally first
./gradlew publishToMavenLocal

# Publish to Maven Central
./publish.sh
```

## Key Differences from Old Process

| Aspect | Old Process (2023) | New Process (2024) |
|--------|-------------------|-------------------|
| **Registration** | issues.sonatype.org | central.sonatype.com |
| **Support** | Jira tickets | Email: central-support@sonatype.com |
| **Namespace Verification** | Manual approval | Automated (GitHub/DNS) |
| **Credentials** | OSSRH username/password | Central Portal username/token |
| **Repository URLs** | Same (s01.oss.sonatype.org) | Same |

## GitHub Namespace Benefits

✅ **Easier Setup**: Automatic verification  
✅ **No Domain Required**: Use your GitHub username  
✅ **Quick Approval**: Usually within hours  
✅ **Version Control**: Tied to your GitHub account  

## Final Maven Coordinates

After publishing with GitHub namespace:
```kotlin
dependencies {
    implementation("io.github.yourusername:fuse-kt:1.0.0")
}
```

## Need Help?

- 📧 **Support**: central-support@sonatype.com
- 📖 **Documentation**: https://central.sonatype.org/
- 🔧 **This Project**: See `PUBLISHING.md` for detailed instructions