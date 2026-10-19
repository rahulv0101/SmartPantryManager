# Smart Pantry Manager

Smart Pantry Manager is an Android application for reducing household food waste. Users record ingredients available in their pantry and receive recipe suggestions only when every required ingredient is available in sufficient quantity.

## Current milestone

Day 5, Milestone 10 final implementation is complete. The project contains a validated Add Ingredient form, a SQLite-backed RecyclerView pantry list, complete pantry CRUD functionality, a transactional catalogue of 20 seeded recipes, strict recipe matching, SQLite-backed recipe detail screens, and a persistent Settings preference for expiring-soon alerts. Alert scheduling and notification delivery remain future work.

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

Indexes support lookup of recipe requirements by recipe and ingredient name. Recipe names have a unique index so repeated initialization cannot create duplicates. The database version is now 2; upgrading a version 1 database adds the seed without dropping or recreating `pantry_items`.

The database models are represented by `PantryItem`, `Recipe`, and `RecipeIngredient`. The current seed uses only `g`, `kg`, `ml`, `L`, and `pieces`. `UnitConverter` supports g/kg, ml/L, and pieces conversions while rejecting incompatible dimensions. The pure-Java `RecipeMatcher` normalises names, applies explicit safe singular/plural aliases, aggregates duplicate pantry records, sums duplicate recipe requirements, and requires every recipe requirement to be satisfied.

## Activities and current functionality

- `PantryActivity` is the launcher, loads pantry records from SQLite into a RecyclerView, refreshes on return, and displays a clear empty state when no records exist.
- `AddEditIngredientActivity` validates ingredient names, positive decimal quantities, canonical units, and optional ISO-8601 expiry dates before inserting or updating a `PantryItem` in SQLite.
- `SuggestedRecipesActivity` loads recipes and pantry data from SQLite and displays only recipes that pass strict all-ingredients and sufficient-quantity matching.
- `RecipeDetailActivity` loads the selected recipe and its required ingredients and instructions from SQLite.
- `SettingsActivity` provides a persistent expiring-soon alerts preference backed by Android `SharedPreferences`. The preference records the user's choice, but it does not yet schedule notifications or display expiry warnings.

The Add/Edit Ingredient form accepts `g`, `kg`, `ml`, `L`, and `pieces`. Quantity parsing rejects empty, non-numeric, zero, negative, non-finite, and locale-dependent comma values. Expiry dates are selected with an Android date picker and stored as `YYYY-MM-DD`; the field may remain empty. Saving returns to the pantry, where records persist after the application is closed and reopened.

Each pantry row provides Edit and Delete actions. Edit preserves the SQLite record ID and updates only that row. Delete requires confirmation, names the selected ingredient, and refreshes the list and empty state after deletion. Recipe rows open the corresponding SQLite-backed detail screen through an explicit Intent.

Edit and Delete controls expose ingredient-specific accessibility descriptions. Canonical unit validation accepts only `g`, `kg`, `ml`, `L`, and `pieces`, including when an existing record is edited. `DatabaseHelper` seeds 20 distinct recipes with instructions and complete ingredient requirements inside a transaction. New databases seed during creation; existing version 1 databases receive the same data through a non-destructive version 2 upgrade. Recipe data can be retrieved through `getAllRecipes()`, `getRecipeById()`, `getRecipeIngredients()`, and `countRecipes()`.

Recipe matching intentionally does not perform arbitrary mass-to-volume conversions: grams and kilograms are compatible, millilitres and litres are compatible, and pieces only match pieces. Missing ingredients, insufficient quantities, invalid requirements, and incompatible units exclude a recipe from the main suggestions list. Pantry records are not modified by matching.

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

The project is configured for Java 17 as the Gradle runtime, with Java 11 source and target compatibility for the Android application. The verified SDK location used during Milestone 9 was `C:\Users\User\AppData\Local\Android\Sdk`.

To run the local and instrumentation test suites from a terminal:

```text
gradlew.bat test
gradlew.bat lint
gradlew.bat connectedDebugAndroidTest
```

`connectedAndroidTest` requires a working Android emulator or physical device. If no device is connected, the database instrumentation tests remain pending until an Android test target is available.

The configured test AVD is `Medium_Phone_API_37.0`. If `adb devices` reports it as `offline`, allow the emulator to finish booting, then run the SDK's `platform-tools\adb.exe kill-server`, `platform-tools\adb.exe start-server`, and `platform-tools\adb.exe devices` before retrying the instrumentation task. Do not wipe the AVD unless its data is intentionally disposable.

## Milestone 9 verification

On 19 October 2026, the existing AVD briefly reported `offline` after a non-destructive cold boot, then became stable as `emulator-5554 device` across repeated ADB checks. The debug APK installed and launched into `PantryActivity`. `build`, `test`, and `lint` succeeded, and `connectedDebugAndroidTest` completed successfully, including the SharedPreferences tests. The Settings switch was toggled on, the application was force-stopped and relaunched, and the switch remained enabled after reopening Settings.

Genuine evidence screenshots were saved outside the source tree in `C:\Users\User\Documents\Codex\SmartPantryManager-evidence`. Full add/edit/delete and recipe workflow coverage still requires a broader dedicated live test pass; no unverified workflow is represented as passed here.

## Final Milestone 10 verification

Using disposable data on `Medium_Phone_API_37.0`, the final live pass verified the empty pantry state, ingredient creation, display of name/quantity/unit/expiry status, edit form prepopulation, edit cancellation, quantity update, delete confirmation cancellation, confirmed deletion, and restoration of the empty state. The app launched into `PantryActivity` and navigated to Suggested Recipes and Recipe Detail.

The strict matching demonstration used the seeded `Boiled Eggs` recipe. With one egg, the recipe was excluded; with two eggs, it appeared; Recipe Detail displayed `Eggs — 2 pieces` and the seeded preparation instructions; deleting the required egg removed the recipe again. Automated matcher tests cover kg/g, L/ml, incompatible units, normalization, duplicate aggregation, missing ingredients, and insufficient quantities. Those conversions were not separately demonstrated through the live form in this pass.

The evidence folder contains genuine emulator screenshots and UI hierarchy captures for the empty pantry, pantry ingredient, edit result, delete confirmation, recipe empty state, matched recipe, recipe detail, required-ingredient deletion, invalid input, and Settings screens. Full expiry-date entry, decimal quantity, multi-ingredient recipe, and every five-screen navigation path remain suitable for an additional demonstration pass rather than being claimed as completed here.

## Development approach

Development is incremental. Each milestone will be reviewed and tested before it is committed. The repository history will document genuine completed work; changes will not be backdated or fabricated. Remaining work includes expiry warnings/notifications, final UI polish, and any later assignment features outside the current milestone.
