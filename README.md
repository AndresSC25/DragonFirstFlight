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

### Game introduction

Android and Desktop play `shared/src/commonMain/resources/video/intro.mp4` before
creating the main menu. The bundled introduction is a silent, 12.5-second H.264
video (1536 x 1024, 24 fps). Playback preserves its aspect ratio with black bands
where needed. It runs once per game launch; starting or retrying a run does not
replay it. A playback error falls back to the menu.

Android uses the system VideoView. Desktop decodes video frames with JavaFX and
renders them as a KorGE texture; Gradle selects native JavaFX libraries for the build
machine. Build distributables on their target OS/architecture.
Android includes `kotlinx-coroutines-android` to provide the main-thread dispatcher
required by the video player and Activity integration.

The Skip button ends playback and opens the menu once. Desktop also accepts Esc,
Space and Enter. Playback pauses when Desktop loses focus or is minimized, and
when Android enters the background. Returning resumes from the saved position.
Android saves the intro position/completion flag across Activity recreation.
Neither platform persists an "already watched" preference: a fresh app launch
shows the introduction again. Decoder startup stalls time out after 15 seconds;
playback stalls time out after 5 seconds without progress. Background time does
not count toward these limits. End, skip, error and cancellation remove listeners,
stop the decoder and dispose temporary resources.

This introduction is implemented for Android and Desktop.

Manual acceptance checklist (run separately on Desktop and an Android device):

| Check | Expected result |
|---|---|
| Fresh launch | Intro appears before the menu; the complete video lasts 12.5 seconds. |
| Image | No blue screen, distortion or cropping; black side bands are expected on 16:9. |
| Natural end | Main menu appears once, and gameplay starts only after Play. |
| Skip early/midway/near end | Menu opens once, with no frozen frame or background playback. |
| Rapid repeated Skip | No duplicate menus and no accidental Play activation. |
| Desktop keyboard | Esc, Space and Enter each skip the intro. |
| Desktop focus/minimize | Video pauses; returning resumes at the same moment. |
| Android Home/lock | Video pauses; returning resumes without a second player. |
| Long interruption | Staying away for over 30 seconds does not trigger a timeout. |
| Android rotation/recreation | Intro resumes near its saved position; after the menu it does not replay. |
| Screen sizes | Full image and Skip remain visible, including Android cutouts/insets. |
| Retry/menu navigation | Intro does not replay after defeat, victory, retry, shop, inventory or Back to menu. |
| Full close and reopen | A new intro starts from the beginning. |
| Offline launch | Intro and menu work without network access. |
| Close during intro | App exits normally; a later launch works and no decoder remains active. |
| Missing/corrupt video (development only) | Menu opens instead of trapping the player; restore the asset afterward. |

Automated checks include startup ordering/cancellation, background-aware timeout
rules and texture upload/color conversion. Real MP4 decode/pause/resume/completion
and corrupt-file fallback use a separate `:desktopApp:verifyIntroPlayback` task,
which needs a graphical environment; ordinary `:desktopApp:test` remains headless.
For the full validation set, run:

```powershell
.\gradlew.bat ktlintCheck :shared:jvmTest :shared:testAndroidHostTest :desktopApp:test :desktopApp:verifyIntroPlayback :desktopApp:compileKotlin :androidApp:assembleDebug --console=plain
```

Validation recorded on November 20, 2026:

- Shared JVM and Android host suites: 307 tests passed.
- Desktop texture tests: 3 passed; native playback and corrupt-video checks: 2 passed.
- Android Debug APK generation, `ktlintCheck` and `:androidApp:lintDebug` passed.
  Android lint reported no errors and 14 warnings.
- A Pixel 6 emulator running API 37 verified introduction playback, natural
  completion, repeated Skip presses without accidental Play activation, resuming
  after 31 seconds in the background, and starting and advancing a game with Play.

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html)…
