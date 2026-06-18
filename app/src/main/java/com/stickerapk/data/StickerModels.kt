package com.stickerapk.data

data class StickerSheet(
    val id: String,
    val name: String,
    val assetPath: String,
    val stickerIds: List<String>,
    val sheetNumber: Int? = null,
)

data class Sticker(
    val id: String,
    val name: String,
    val sheetId: String,
    val assetPath: String,
    val accentHue: Float,
    val vowels: Int = 0,
)

data class StickerCatalog(
    val sheets: List<StickerSheet>,
    val stickers: List<Sticker>,
) {
    val stickersById: Map<String, Sticker> by lazy { stickers.associateBy { it.id } }
    val sheetsById: Map<String, StickerSheet> by lazy { sheets.associateBy { it.id } }

    fun stickersForSheet(sheetId: String): List<Sticker> =
        stickers.filter { it.sheetId == sheetId }
}
