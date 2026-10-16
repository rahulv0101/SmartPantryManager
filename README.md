# Smart Pantry Manager

Smart Pantry Manager is an Android application for reducing household food waste. It will allow users to record ingredients available in their pantry and, in later milestones, suggest recipes that can be prepared using only those ingredients in sufficient quantities.

## Current milestone

Day 2, Milestone 3 is complete. The project now contains the five-screen Java/XML foundation and a local SQLite database schema with pantry CRUD methods. The database foundation is deliberately separate from the UI; ingredient entry and pantry list integration are planned for the next milestone.

## Technology stack

- Android Studio
- Java application code
- XML layouts with traditional Android Views
- Android Activities and Intents for navigation
- SQLite using `SQLiteOpenHelper` for local data storage
- Android instrumentation tests for the SQLite schema and CRUD layer
- Minimum Android SDK: API 24
- Package name: `com.example.smartpantrymanager`
- Gradle Kotlin DSL build scripts

The project intentionally does not use Kotlin application code, Jetpack Compose, Firebase, PostgreSQL, location services, or payment processing.

## Database design

SQLite is built into Android, works offline, and is well suited to the relational data required by this app. `DatabaseHelper` extends `SQLiteOpenHelper`, creates the schema on first use, enables foreign-key enforcement, and provides parameterised pantry CRUD operations. Database version upgrades are reserved for non-destructive migrations so normal application restarts do not erase pantry data.

The schema contains:

- `pantry_items`: `id`, `name`, positive `quantity`, `unit`, and nullable ISO-8601 `expiry_date`.
- `recipes`: `id`, `name`, and preparation `instructions`.
- `recipe_ingredients`: `id`, `recipe_id`, ingredient name, positive required quantity, and unit. Each requirement references a recipe with a foreign key and cascade delete.

Indexes support lookup of recipe requirements by recipe and ingredient name. Recipe rows and the 20 required preloaded recipes will be added in later milestones; no recipe data is seeded yet.

The database models are represented by `PantryItem`, `Recipe`, and `RecipeIngredient`. The future matching layer will be able to normalise ingredient names and compare required quantities and compatible units without coupling that logic to an Activity.

## Activities and current functionality

- `PantryActivity` is the launcher and displays the current pantry empty state.
- `AddEditIngredientActivity` contains the planned ingredient form, but database saving is not connected yet.
- `SuggestedRecipesActivity` displays the recipe empty state.
- `RecipeDetailActivity` provides a placeholder detail structure.
- `SettingsActivity` provides a clearly disabled placeholder expiry-alert preference.

The current milestone adds the database foundation only. UI-based ingredient entry, RecyclerView display, editing, deleting, recipe seeding, recipe matching, and expiry notifications remain planned work.

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

To run the local and instrumentation test suites from a terminal:

```text
gradlew.bat test
gradlew.bat lint
gradlew.bat connectedDebugAndroidTest
```

`connectedAndroidTest` requires a working Android emulator or physical device. If no device is connected, the database instrumentation tests remain pending until an Android test target is available.

## Development approach

Development is incremental. Each milestone will be reviewed and tested before it is committed. The repository history will document genuine completed work; changes will not be backdated or fabricated.
