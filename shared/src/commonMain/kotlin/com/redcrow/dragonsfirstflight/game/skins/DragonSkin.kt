package com.redcrow.dragonsfirstflight.game.skins

/** Dragon skins with their sprite sheet, optional matching [NyanSkin] and shop price in coins (0 is free). */
enum class DragonSkin(
    val displayName: String,
    val spritePath: String,
    val nyan: NyanSkin?,
    val price: Int,
) {
    BRASS(displayName = "Brass", spritePath = "sprites/dragon/YoungBrassDragon.png", nyan = null, price = 0),
    LDA(displayName = "LDA", spritePath = "sprites/dragon/skins/dragon_lda.png", nyan = NyanSkin.LDA, price = 5000),
    SAPRISSA(
        displayName = "Saprissa",
        spritePath = "sprites/dragon/skins/dragon_saprissa.png",
        nyan = NyanSkin.SAPRISSA,
        price = 5000,
    ),
    HEREDIA(
        displayName = "Heredia",
        spritePath = "sprites/dragon/skins/dragon_heredia.png",
        nyan = NyanSkin.HEREDIA,
        price = 5000,
    ),
    CARTAGO(
        displayName = "Cartago",
        spritePath = "sprites/dragon/skins/dragon_cartago.png",
        nyan = NyanSkin.CARTAGO,
        price = 5000,
    ),
    ;

    init {
        require(price >= 0) { "Skin price must not be negative: $price" }
    }

    companion object {
        /** Skin that is always unlocked and equipped by default. */
        val DEFAULT: DragonSkin = BRASS
    }
}
