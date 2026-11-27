package com.redcrow.dragonsfirstflight.game.audio

import com.redcrow.dragonsfirstflight.ui.components.createMedievalImageButton
import korlibs.image.color.Colors
import korlibs.image.format.readBitmap
import korlibs.io.file.std.resourcesVfs
import korlibs.korge.Korge
import korlibs.korge.service.storage.InmemoryStorage
import korlibs.korge.view.Views
import korlibs.korge.view.addUpdater
import korlibs.korge.view.text
import korlibs.math.geom.Size

/** Developer soundboard, launched separately from the game with :desktopApp:previewAudio. */
suspend fun launchAudioPreview(beforePreview: suspend (Views) -> Unit = {}) {
    Korge(
        windowSize = Size(1280, 720),
        virtualSize = Size(1280, 720),
        title = "Dragon's First Flight - Audio preview",
        backgroundColor = Colors["#101827"],
    ).start {
        beforePreview(views)
        val audio = GameAudio.load(views.coroutineContext, InmemoryStorage())
        audio.bindWindow(views.gameWindow)
        val button = resourcesVfs["ui/buttons/button_normal.png"].readBitmap()
        text("Audio preview", textSize = 32.0, color = Colors["#F8D66D"]) {
            x = 24.0
            y = 18.0
        }
        val status =
            text("Choose a sound. Charge and music can loop.", textSize = 20.0) {
                x = 290.0
                y = 665.0
            }
        var musicOn = false
        var bossOn = false
        var chargeOn = false
        createMedievalImageButton(
            410.0,
            12.0,
            200.0,
            48.0,
            button,
            "Stage music",
            onClick = {
                musicOn = !musicOn || bossOn
                bossOn = false
                audio.stopEffects()
                status.text = "Music: ${if (musicOn) "On" else "Off"}"
            },
        )
        createMedievalImageButton(
            620.0,
            12.0,
            200.0,
            48.0,
            button,
            "Boss music",
            onClick = {
                musicOn = !musicOn || !bossOn
                bossOn = true
                audio.stopEffects()
                status.text = "Boss music: ${if (musicOn) "On" else "Off"}"
            },
        )
        createMedievalImageButton(
            830.0,
            12.0,
            200.0,
            48.0,
            button,
            "Intro music",
            onClick = {
                musicOn = false
                chargeOn = false
                audio.music(active = false, paused = false, boss = false)
                audio.charge(active = false, paused = false)
                audio.previewIntro()
                status.text = "Intro cinematic (12.5 seconds)"
            },
        )
        createMedievalImageButton(
            1040.0,
            12.0,
            210.0,
            48.0,
            button,
            "Stop all",
            onClick = {
                musicOn = false
                chargeOn = false
                audio.stopEffects()
                status.text = "Stopped"
            },
        )
        GameSound.entries.forEachIndexed { index, sound ->
            createMedievalImageButton(
                x = 24.0 + (index % 3) * 416.0,
                y = 80.0 + (index / 3) * 62.0,
                width = 400.0,
                height = 48.0,
                texture = button,
                label = sound.fileName.replace('_', ' '),
                textSize = 22.0,
                onClick = {
                    audio.stopEffects()
                    if (sound == GameSound.CATAPULT_CHARGE) {
                        chargeOn = !chargeOn
                    } else {
                        chargeOn = false
                        audio.play(sound)
                    }
                    status.text = "Selected: ${sound.fileName}"
                },
            )
        }
        createMedievalImageButton(
            x = 24.0,
            y = 650.0,
            width = 240.0,
            height = 48.0,
            texture = button,
            label = "Intro effects",
            onClick = {
                musicOn = false
                chargeOn = false
                audio.music(active = false, paused = false, boss = false)
                audio.charge(active = false, paused = false)
                audio.previewIntroEffects()
                status.text = "Synced intro effects (12.5 seconds)"
            },
        )
        addUpdater {
            audio.music(active = musicOn, paused = false, boss = bossOn)
            audio.charge(active = chargeOn, paused = false)
        }
    }
}
