This is a Kotlin Multiplatform project targeting Android, iOS, Desktop (JVM).

* [/iosApp](./iosApp/iosApp) contains an iOS application. Even if you’re sharing your UI with Compose Multiplatform,
  you need this entry point for your iOS app. This is also where you should add SwiftUI code for your project.

* [/shared](./shared/src) is for code that will be shared across your Compose Multiplatform applications.
  It contains several subfolders:
  - [commonMain](./shared/src/commonMain/kotlin) is for code that’s common for all targets.
  - Other folders are for Kotlin code that will be compiled for only the platform indicated in the folder name.
    For example, if you want to use Apple’s CoreCrypto for the iOS part of your Kotlin app,
    the [iosMain](./shared/src/iosMain/kotlin) folder would be the right place for such calls.
    Similarly, if you want to edit the Desktop (JVM) specific part, the [jvmMain](./shared/src/jvmMain/kotlin)
    folder is the appropriate location.

### Running the apps

Use the run configurations provided by the run widget in your IDE's toolbar. You can also use these commands and options:

- Android app: `./gradlew :androidApp:assembleDebug`
- Desktop app:
  - Hot reload: `./gradlew :desktopApp:hotRun --auto`
  - Standard run: `./gradlew :desktopApp:run`
- iOS app: open the [/iosApp](./iosApp) directory in Xcode and run it from there.

### Running tests

Use the run button in your IDE's editor gutter, or run tests using Gradle tasks:

- Android tests: `./gradlew :shared:testAndroidHostTest`
- Desktop tests: `./gradlew :shared:jvmTest`
- iOS tests: `./gradlew :shared:iosSimulatorArm64Test`

### Endless mode

Select a difficulty in the main menu, then choose Endless and a map preview.
After a normal victory, Continue Endless keeps the remaining lives, score, coins
and difficulty on the final battlefield. Endless never activates the catapult
or a victory timer. The HUD identifies the mode as ENDLESS.

Enemy speed increases with active play time and stops at a configurable maximum
for each difficulty. Spacing grows with speed to preserve reaction time and RUSH
warnings. Pauses and the transition into Endless do not advance this progression.

Losing focus, minimizing or backgrounding pauses an active session; returning
leaves the pause menu open until Resume. Play again retries the current mode,
map and difficulty with fresh lives, score and speed progression. Returning to
the main menu allows a new normal or Endless run.

Coins are banked once across pause, defeat, victory continuation and menu changes.
Extra lives retain the five-life cap and minimum spawn interval. Normal runs offer
at most two extra lives per run; Endless renews that offer budget every 60 seconds
of active play, allowing rare recovery opportunities during long sessions.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
