# Smart Pantry Manager

Smart Pantry Manager is an Android application for reducing household food waste. It will allow users to record ingredients available in their pantry and, in later milestones, suggest recipes that can be prepared using only those ingredients in sufficient quantities.

## Current milestone

Day 3, Milestone 5 is complete. The project contains a validated Add Ingredient form, a SQLite-backed RecyclerView pantry list, and complete pantry create, read, update, and delete functionality. Recipe data, recipe matching, and expiry notifications remain future work.

## Technology stack

- Android Studio
- Java application code
- XML layouts with traditional Android Views
- Android Activities and Intents for navigation
- RecyclerView with a custom Java `PantryAdapter`
- SQLite using `SQLiteOpenHelper` for local data storage
- Android instrumentation tests for the SQLite schema and pantry CRUD layer
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

- `PantryActivity` is the launcher, loads pantry records from SQLite into a RecyclerView, refreshes on return, and displays a clear empty state when no records exist.
- `AddEditIngredientActivity` validates ingredient names, positive decimal quantities, canonical units, and optional ISO-8601 expiry dates before inserting or updating a `PantryItem` in SQLite.
- `SuggestedRecipesActivity` displays the recipe empty state.
- `RecipeDetailActivity` provides a placeholder detail structure.
- `SettingsActivity` provides a clearly disabled placeholder expiry-alert preference.

The Add/Edit Ingredient form accepts `g`, `kg`, `ml`, `L`, and `pieces`. Quantity parsing rejects empty, non-numeric, zero, negative, non-finite, and locale-dependent comma values. Expiry dates are selected with an Android date picker and stored as `YYYY-MM-DD`; the field may remain empty. Saving returns to the pantry, where records persist after the application is closed and reopened.

Each pantry row provides Edit and Delete actions. Edit preserves the SQLite record ID and updates only that row. Delete requires confirmation, names the selected ingredient, and refreshes the list and empty state after deletion. Recipe seeding, strict recipe matching, functional settings, and expiry notifications remain planned work.

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
