## Description

Implemented extra-life rewards for the first three zones. Extra lives are rare, limited to a maximum of two per run, excluded from the boss battle, and integrated with gameplay, HUD, pause, transitions, restart, victory, and defeat states.

## What Changed

- Added configurable extra-life spawning and collection.
- Limited extra lives to two per run.
- Added a maximum of five total lives.
- Prevented collection when the player already has the maximum number of lives.
- Added animated `extra_life` sprite rendering using an 8-frame atlas.
- Updated the HUD layout:
  - Points and coins are displayed on the left.
  - Three default life heads remain on the right.
  - Additional active dragon heads represent collected extra lives.
- Updated the life indicators when lives are gained or lost.
- Cleared extra lives on restart and zone transitions.
- Disabled extra-life spawning during the boss battle.

## Changed Files

- `shared/src/commonMain/kotlin/com/redcrow/dragonsfirstflight/game/engine/GameEngine.kt`
- `shared/src/commonMain/kotlin/com/redcrow/dragonsfirstflight/game/engine/GameEngineConfig.kt`
- `shared/src/commonMain/kotlin/com/redcrow/dragonsfirstflight/game/engine/GameSnapshot.kt`
- `shared/src/commonMain/kotlin/com/redcrow/dragonsfirstflight/game/engine/ExtraLife.kt`
- `shared/src/commonMain/kotlin/com/redcrow/dragonsfirstflight/game/engine/ExtraLifeField.kt`
- `shared/src/commonMain/kotlin/com/redcrow/dragonsfirstflight/ui/components/GameplayHud.kt`
- `shared/src/commonMain/kotlin/com/redcrow/dragonsfirstflight/ui/screens/GameplayScreen.kt`
- `shared/src/commonMain/kotlin/com/redcrow/dragonsfirstflight/ui/gameplay/ExtraLifeView.kt`
- `shared/src/commonMain/resources/sprites/power-ups/extra_life.png`

## Tests

- `:shared:compileKotlinJvm`
- `:desktopApp:compileKotlin`
- `:androidApp:assembleDebug`
- `:shared:jvmTest`
- `git diff --check`

All validations passed.
