package com.redcrow.dragonsfirstflight.game.audio

internal enum class GameSound(
    val fileName: String,
    val volume: Double = 0.65,
    val minimumIntervalSeconds: Double = 0.08,
) {
    COIN("coin_collect", 0.45),
    MENU_CLICK("menu_click", 0.45),
    MENU_BACK("menu_back", 0.45),
    MAP_SELECT("map_select", 0.5),
    TRANSITION("screen_transition", 0.5),
    START("game_start", 0.55),
    PAUSE("game_pause", 0.45),
    RESUME("game_resume", 0.45),
    VICTORY("victory"),
    GAME_OVER("game_over", 0.6),
    BOSS_INTRO("boss_intro"),
    CATAPULT_CHARGE("catapult_charge", 0.35),
    CATAPULT_FIRE("catapult_fire", 0.65, 0.15),
    CATAPULT_WARNING("catapult_warning", 0.75),
    CATAPULT_DESTROY("catapult_destroy"),
    PROJECTILE("projectile_fly", 0.45, 0.2),
    DAMAGE("dragon_damage"),
    DEFEAT("dragon_defeat"),
    FLAP_ONE("dragon_flap_01", 0.22),
    FLAP_TWO("dragon_flap_02", 0.22),
    FLAP_THREE("dragon_flap_03", 0.22),
    ENDLESS("endless_continue", 0.6),
    EXTRA_LIFE("extra_life", 0.6),
    RUSH("rush_warning", 0.75, 0.3),
    PURCHASE("shop_purchase", 0.55),
    INSUFFICIENT_COINS("shop_insufficient_coins", 0.5, 0.25),
    EQUIP("skin_equip", 0.5),
    ;

    val path: String get() = "audio/sfx/$fileName.wav"
}

internal interface GameAudioOutput {
    fun play(
        sound: GameSound,
        delaySeconds: Double = 0.0,
    )

    fun music(
        active: Boolean,
        paused: Boolean,
        boss: Boolean,
    )

    fun charge(
        active: Boolean,
        paused: Boolean,
    )

    fun stopEffects()
}
