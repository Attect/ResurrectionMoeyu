# AGENTS.md

This file contains information about the project setup and coding standards for agent use.

## Language
- 需要使用中文书写注释。
- 在分析和总结时，也应该使用中文。

## Build Commands
- `./gradlew build` - Build the entire project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumentation tests
- `./gradlew clean` - Clean build artifacts

## Test Commands
- For running a single test class: `./gradlew test --tests "com.example.MyTestClass"`
- For running a single test method: `./gradlew test --tests "com.example.MyTestClass.myTestMethod"`

## Code Style Guidelines

### Java Naming Conventions
- Class names: UpperCamelCase (e.g., `MyClass`)
- Method names: lowerCamelCase (e.g., `myMethod`)
- Constants: UPPER_CASE (e.g., `MAX_SIZE`)
- Package names: lower case (e.g., `jp.co.a_tm.moeyu.api`)
- Variables: lowerCamelCase (e.g., `myVariable`)

### Formatting
- Indentation: 4 spaces (not tabs)
- Line width: 100 characters
- Braces: K&R style (opening brace on same line)
- Imports: Alphabetical order, grouped by package

### Error Handling
- Use try-catch blocks appropriately
- Log exceptions with meaningful messages
- Don't ignore exceptions

### Documentation
- Use JavaDoc for all public classes and methods
- Include @param, @return, and @throws tags appropriately

## Repository Structure
This is an Android project using Gradle build system (version 9.0.0). 
- Source code in `app/src/main/java/`
- Resources in `app/src/main/res/`
- AndroidManifest.xml in `app/src/main/`
- Dependencies are managed through build.gradle files

## Configuration Files
- `build.gradle` - Project-level build configuration
- `app/build.gradle` - App-level build configuration  
- `gradle.properties` - Gradle properties
- `.gitignore` - Git ignore patterns

## Additional Notes
- The project uses Java 8 compatibility
- Tests are written using JUnit 4
- AndroidX libraries are used
- ProGuard rules are defined in `proguard-rules.pro`