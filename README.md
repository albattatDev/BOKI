# BOKI

An Android application built with Java and Gradle.

## Project Information

- **Package Name**: com.example.boki
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Compile SDK**: 36
- **Version**: 1.0 (versionCode: 1)

## Technologies Used

- **Language**: Java
- **Build System**: Gradle with Kotlin DSL
- **Architecture**: AndroidX
- **UI Components**: Material Design, ConstraintLayout

## Dependencies

- AndroidX AppCompat
- Material Design Components
- ConstraintLayout
- Activity (AndroidX)

## Development Setup

### Prerequisites

- Android Studio (latest version recommended)
- JDK 11 or higher
- Android SDK with API level 36

### Getting Started

1. Clone the repository:
   ```bash
   git clone git@github.com:albattatDev/BOKI.git
   cd BOKI
   ```

2. Open the project in Android Studio

3. Sync Gradle files when prompted

4. Run the app on an emulator or physical device

### Building the Project

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Run tests
./gradlew test

# Run instrumented tests
./gradlew connectedAndroidTest
```

## Project Structure

```
BOKI/
├── app/                        # Main application module
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/          # Java source files
│   │   │   ├── res/           # Resources (layouts, drawables, etc.)
│   │   │   └── AndroidManifest.xml
│   │   ├── androidTest/       # Instrumented tests
│   │   └── test/              # Unit tests
│   └── build.gradle.kts       # App-level build configuration
├── gradle/                     # Gradle wrapper and version catalog
└── build.gradle.kts           # Project-level build configuration
```

## Configuration Files

- `gradle/libs.versions.toml` - Centralized dependency version management
- `gradle.properties` - Gradle build properties
- `proguard-rules.pro` - ProGuard rules for code obfuscation

## Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request