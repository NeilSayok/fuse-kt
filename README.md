# Fuse-kt

A lightweight fuzzy-search library for Kotlin, ported from the popular [Fuse.js](https://fusejs.io) JavaScript library.

## Features

- **Lightweight and fast**: Zero external dependencies (except Kotlin stdlib)
- **Fuzzy searching**: Uses the Bitap algorithm for approximate string matching
- **Flexible configuration**: Customizable search options (threshold, location, distance, etc.)
- **Multiple search types**: Search within string lists or object properties
- **Match highlighting**: Get indices of matched characters for highlighting
- **Type-safe**: Written in Kotlin with full type safety

## Installation

### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("com.fusekt:fuse-kt:1.0.0")
}
```

### Gradle (Groovy)
```groovy
dependencies {
    implementation 'com.fusekt:fuse-kt:1.0.0'
}
```

### Maven
```xml
<dependency>
    <groupId>com.fusekt</groupId>
    <artifactId>fuse-kt</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Quick Start

### Simple String Search
```kotlin
import com.fusekt.Fuse

val fruits = listOf("apple", "banana", "orange", "grape", "pineapple")
val fuse = Fuse(fruits)

val results = fuse.search("app")
results.forEach { result ->
    println("${result.item} (score: ${result.score})")
}
// Output:
// apple (score: 0.0)
// pineapple (score: 0.25)
```

### Object Search
```kotlin
import com.fusekt.Fuse
import com.fusekt.core.FuseOptions

val books = listOf(
    mapOf("title" to "The Great Gatsby", "author" to "F. Scott Fitzgerald"),
    mapOf("title" to "To Kill a Mockingbird", "author" to "Harper Lee"),
    mapOf("title" to "1984", "author" to "George Orwell")
)

val options = FuseOptions(
    keys = listOf("title", "author"),
    includeScore = true,
    includeMatches = true,
    threshold = 0.6
)

val fuse = Fuse(books, options)
val results = fuse.search("gatsby")

results.forEach { result ->
    val book = result.item as Map<*, *>
    println("${book["title"]} by ${book["author"]} (score: ${result.score})")
    
    if (result.matches.isNotEmpty()) {
        result.matches.forEach { match ->
            println("  Match: '${match.value}' in ${match.key}")
        }
    }
}
```

## Configuration Options

```kotlin
val options = FuseOptions(
    // Basic options
    isCaseSensitive = false,          // Case sensitivity
    includeScore = false,             // Include match scores in results
    includeMatches = false,           // Include match indices for highlighting
    keys = emptyList(),               // Properties to search (for objects)
    shouldSort = true,                // Sort results by score
    
    // Fuzzy matching options
    location = 0,                     // Expected location of match
    threshold = 0.6,                  // Match threshold (0.0 = perfect, 1.0 = anything)
    distance = 100,                   // Maximum distance from expected location
    
    // Advanced options
    ignoreLocation = false,           // Ignore location in scoring
    ignoreFieldNorm = false,          // Ignore field length normalization
    fieldNormWeight = 1.0,            // Field length norm weight
    findAllMatches = false,           // Find all matches vs first match
    minMatchCharLength = 1            // Minimum match length
)
```

## API Reference

### Fuse Class

#### Constructor
```kotlin
Fuse(docs: List<Any>, options: FuseOptions = FuseOptions(), index: FuseIndex? = null)
```

#### Methods
- `search(query: String, limit: Int = -1): List<FormattedResult>` - Search for a query
- `add(doc: Any)` - Add a document to the index
- `remove(predicate: (Any, Int) -> Boolean): List<Any>` - Remove documents matching predicate
- `removeAt(idx: Int)` - Remove document at specific index
- `getIndex(): FuseIndex` - Get the search index

#### Static Methods
- `Fuse.createIndex(keys: List<Any>, docs: List<Any>, options: FuseOptions): FuseIndex`
- `Fuse.parseIndex(data: Map<String, Any>, options: FuseOptions): FuseIndex`

## Publishing to Maven Central

This project is configured for publishing to Maven Central. To publish:

### Prerequisites

1. **Sonatype OSSRH Account**: Create an account at [issues.sonatype.org](https://issues.sonatype.org)
2. **GPG Key Pair**: Generate a GPG key pair for signing artifacts
3. **GitHub Repository**: Host your code on GitHub (or update URLs in build.gradle.kts)

### Setup

1. **Configure credentials** in your `~/.gradle/gradle.properties`:
```properties
# Sonatype OSSRH credentials
ossrhUsername=your_sonatype_username
ossrhPassword=your_sonatype_password

# PGP signing
signingKey=your_base64_encoded_private_key
signingPassword=your_private_key_password
```

2. **Update project metadata** in `build.gradle.kts`:
   - Replace `yourusername` with your GitHub username
   - Update developer information
   - Update URLs to point to your repository

3. **Export your GPG private key as base64**:
```bash
gpg --export-secret-keys --armor KEY_ID | base64 | pbcopy
```

### Publishing Commands

```bash
# Build and test
./gradlew clean build test

# Publish to staging repository
./gradlew publishToSonatype

# Close and release staging repository (promotes to Maven Central)
./gradlew closeAndReleaseSonatypeStagingRepository
```

Or publish locally for testing:
```bash
./gradlew publishToMavenLocal
```

### Version Management

- Release versions: `1.0.0`, `1.1.0`, etc.
- Snapshot versions: `1.0.0-SNAPSHOT`, `1.1.0-SNAPSHOT`, etc.

Snapshots are automatically published to the snapshots repository, while release versions go through staging.

## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Credits

This library is a Kotlin port of [Fuse.js](https://fusejs.io) by Kiro Risk.