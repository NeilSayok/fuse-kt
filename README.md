# Fuse-kt

A lightweight fuzzy-search library for Kotlin Multiplatform, ported from the popular [Fuse.js](https://fusejs.io) JavaScript library.

## Features

- **Kotlin Multiplatform**: Supports JVM, JS, WasmJs, iOS, macOS, Linux, and Windows
- **Lightweight and fast**: Minimal dependencies (only kotlinx-serialization-json)
- **Fuzzy searching**: Uses the Bitap algorithm for approximate string matching
- **Flexible configuration**: Customizable search options (threshold, location, distance, etc.)
- **Multiple search types**: Search within string lists or object properties
- **Match highlighting**: Get indices of matched characters for highlighting
- **Type-safe**: Written in Kotlin with full type safety

## Supported Platforms

- **JVM** (Java 21+)
- **JavaScript** (Browser & Node.js)
- **WebAssembly** (WasmJs)
- **iOS** (x64, ARM64, Simulator ARM64)
- **macOS** (x64, ARM64)
- **Linux** (x64)
- **Windows** (MinGW x64)

## Getting Started

Here's a quick overview of how to start using Fuse-kt:

1. **Add GitHub Packages repository** to your build configuration
2. **Configure GitHub credentials** with a Personal Access Token
3. **Add the dependency** to your project
4. **Start fuzzy searching** in your code

Detailed steps are provided in the sections below.

## Installation

### Step 1: Add GitHub Packages Repository

Add the GitHub Packages repository to your `settings.gradle.kts` or `build.gradle.kts`:

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/NeilSayok/fuse-kt")
        credentials {
            username = providers.gradleProperty("gpr.user").orElse(providers.environmentVariable("GITHUB_ACTOR")).get()
            password = providers.gradleProperty("gpr.key").orElse(providers.environmentVariable("GITHUB_TOKEN")).get()
        }
    }
}
```

### Step 2: Configure GitHub Credentials

Add your GitHub credentials to `~/.gradle/gradle.properties` or `local.properties` in your project root:

```properties
gpr.user=your_github_username
gpr.key=your_github_personal_access_token
```

**Note**: You need a GitHub Personal Access Token with `read:packages` permission. Create one at [GitHub Settings > Developer settings > Personal access tokens](https://github.com/settings/tokens).

### Step 3: Add Dependency

#### Gradle (Kotlin DSL)
```kotlin
dependencies {
    implementation("io.github.neilsayok:fuse-kt:1.0.4")
}
```

#### Gradle (Groovy)
```groovy
dependencies {
    implementation 'io.github.neilsayok:fuse-kt:1.0.4'
}
```

#### Maven
```xml
<dependency>
    <groupId>io.github.neilsayok</groupId>
    <artifactId>fuse-kt-jvm</artifactId>
    <version>1.0.4</version>
</dependency>
```

**Note**: For Maven, you also need to configure the GitHub Packages repository in your `pom.xml`.

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

### Kotlin Multiplatform Usage

For multiplatform projects, add the dependency in your `commonMain` source set:

```kotlin
kotlin {
    sourceSets {
        commonMain {
            dependencies {
                implementation("io.github.neilsayok:fuse-kt:1.0.4")
            }
        }
    }
}
```

The same API works across all platforms without any platform-specific code.

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



## License

Licensed under the Apache License 2.0. See [LICENSE](LICENSE) for details.

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Credits

This library is a Kotlin port of [Fuse.js](https://fusejs.io) by Kiro Risk.