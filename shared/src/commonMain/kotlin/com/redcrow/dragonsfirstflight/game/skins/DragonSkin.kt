package com.redcrow.dragonsfirstflight.game.skins

enum class DragonSkin(
    val displayName: String,
    val spritePath: String,
    val nyan: NyanSkin?,
) {
    BRASS(displayName = "Brass", spritePath = "sprites/dragon/YoungBrassDragon.png", nyan = null),
    LDA(displayName = "LDA", spritePath = "sprites/dragon/skins/dragon_lda.png", nyan = NyanSkin.LDA),
    SAPRISSA(
        displayName = "Saprissa",
        spritePath = "sprites/dragon/skins/dragon_saprissa.png",
        nyan = NyanSkin.SAPRISSA,
    ),
    HEREDIA(displayName = "Heredia", spritePath = "sprites/dragon/skins/dragon_heredia.png", nyan = NyanSkin.HEREDIA),
    CARTAGO(displayName = "Cartago", spritePath = "sprites/dragon/skins/dragon_cartago.png", nyan = NyanSkin.CARTAGO),
    ;

    companion object {
        val DEFAULT: DragonSkin = BRASS
    }
}
