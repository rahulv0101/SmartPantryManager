# Smart Pantry Manager

Smart Pantry Manager is an Android application for reducing household food waste. It will allow users to record ingredients available in their pantry and, in later milestones, suggest recipes that can be prepared using only those ingredients in sufficient quantities.

## Current milestone

This repository currently contains the Day 1 foundation: a Java/XML Android project with the initial application theme and a single starter screen. Pantry storage, ingredient CRUD, recipe data, strict recipe matching, and the remaining screens will be implemented incrementally in later milestones.

## Technology stack

- Android Studio
- Java application code
- XML layouts with traditional Android Views
- Android Activities and Intents for navigation
- SQLite using `SQLiteOpenHelper` for local data storage (planned)
- Minimum Android SDK: API 24
- Package name: `com.example.smartpantrymanager`
- Gradle Kotlin DSL build scripts

The project intentionally does not use Kotlin application code, Jetpack Compose, Firebase, PostgreSQL, location services, or payment processing.

## Why SQLite?

SQLite is built into Android, works offline, and is well suited to the relational data required by this app: pantry ingredients, recipes, and recipe ingredient requirements. `SQLiteOpenHelper` will provide controlled database creation and upgrades without requiring a server or user account.

## Setup

1. Install Android Studio with an Android SDK platform and build tools compatible with the project configuration.
2. Clone this repository or open the project directory in Android Studio.
3. Allow Gradle to sync and download the declared dependencies.
4. Ensure the Android SDK location is configured in Android Studio. The machine-specific `local.properties` file is intentionally excluded from Git.
5. Select an emulator or Android device and run the `app` configuration.

From a terminal, the project can be built with:

```text
gradlew.bat build
```

The project is configured for Java 17 as the Gradle runtime, with Java 11 source and target compatibility for the Android application.

## Development approach

Development is incremental. Each milestone will be reviewed and tested before it is committed. The repository history will document genuine completed work; changes will not be backdated or fabricated.
