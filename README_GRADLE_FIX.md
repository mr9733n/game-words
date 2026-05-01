# Gradle Wrapper Fix Instructions

The Gradle wrapper error occurs because the gradle-wrapper.jar file is missing. To fix this:

## Option 1: Regenerate Gradle Wrapper (Recommended)
1. Delete the gradle directory in your project if it exists
2. Run this command in your project directory:
   ```
   gradle wrapper --gradle-version 8.0
   ```

## Option 2: Manual Download
1. Download the Gradle 8.0 distribution from https://services.gradle.org/distributions/gradle-8.0-bin.zip
2. Extract the gradle-wrapper.jar file from the downloaded archive
3. Place it in the gradle/wrapper/ directory in your project

## Option 3: If Gradle is not installed
1. Download gradle-wrapper.jar directly from a Maven repository:
   https://repo.gradle.org/gradle/libs-releases/org/gradle/gradle-wrapper/8.0/gradle-wrapper-8.0.jar
2. Place it in the gradle/wrapper/ directory in your project

After placing the jar file, try running .\gradlew.bat again.
