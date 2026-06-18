package com.stickerapk.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class StickerRepository(
    private val context: Context,
) {
    private val catalog: StickerCatalog by lazy { loadCatalog() }

    fun getAllSheets(): List<StickerSheet> = catalog.sheets

    fun getAllStickers(): List<Sticker> = catalog.stickers

    fun getSheet(sheetId: String): StickerSheet? = catalog.sheetsById[sheetId]

    fun getSticker(stickerId: String): Sticker? = catalog.stickersById[stickerId]

    fun pickRandomSheets(count: Int): List<StickerSheet> =
        catalog.sheets.shuffled().take(count.coerceAtMost(catalog.sheets.size))

    fun rollSheets(count: Int = 3): List<StickerSheet> = pickRandomSheets(count)

    fun stickersForSheets(sheets: List<StickerSheet>) =
        sheets.flatMap { sheet -> catalog.stickersForSheet(sheet.id) }

    fun assetExists(assetPath: String): Boolean {
        if (assetPath.isBlank()) return false
        return runCatching {
            context.assets.open(assetPath).use { }
            true
        }.getOrDefault(false)
    }

    private fun loadCatalog(): StickerCatalog {
        return runCatching {
            context.assets.open(CATALOG_ASSET).use { stream ->
                parseCatalog(stream.readBytes().decodeToString())
            }
        }.getOrElse { buildFallbackCatalog() }
    }

    private fun parseCatalog(json: String): StickerCatalog {
        val root = JSONObject(json)
        val sheetsArray = root.getJSONArray("sheets")
        val stickersArray = root.getJSONArray("stickers")

        val sheets = buildList {
            for (index in 0 until sheetsArray.length()) {
                val sheet = sheetsArray.getJSONObject(index)
                add(
                    StickerSheet(
                        id = sheet.getString("id"),
                        name = sheet.getString("name"),
                        assetPath = sheet.getString("assetPath"),
                        stickerIds = sheet.getJSONArray("stickerIds").toStringList(),
                        sheetNumber = sheet.optInt("sheetNumber").takeIf { sheet.has("sheetNumber") },
                    ),
                )
            }
        }

        val stickers = buildList {
            for (index in 0 until stickersArray.length()) {
                val sticker = stickersArray.getJSONObject(index)
                add(
                    Sticker(
                        id = sticker.getString("id"),
                        name = sticker.getString("name"),
                        sheetId = sticker.getString("sheetId"),
                        assetPath = sticker.optString("assetPath", ""),
                        accentHue = sticker.optDouble("accentHue", (index * 37 % 360).toDouble()).toFloat(),
                        vowels = sticker.optInt("vowels", 0),
                    ),
                )
            }
        }

        return StickerCatalog(sheets = sheets, stickers = stickers)
    }

    private fun buildFallbackCatalog(): StickerCatalog {
        val sheetNames = listOf("Arena", "Legends", "Neon", "Retro", "Cosmic", "Pulse")
        val sheets = sheetNames.mapIndexed { index, name ->
            val sheetId = "sheet_${index + 1}"
            StickerSheet(
                id = sheetId,
                name = name,
                assetPath = "stickers/sheets/${sheetId}.png",
                stickerIds = (1..3).map { "sticker_${index * 3 + it}" },
            )
        }

        val stickers = sheets.flatMap { sheet ->
            sheet.stickerIds.mapIndexed { stickerIndex, stickerId ->
                Sticker(
                    id = stickerId,
                    name = "${sheet.name} #${stickerIndex + 1}",
                    sheetId = sheet.id,
                    assetPath = "stickers/items/$stickerId.png",
                    accentHue = (sheetNames.indexOf(sheet.name) * 55f + stickerIndex * 18f) % 360f,
                )
            }
        }

        return StickerCatalog(sheets = sheets, stickers = stickers)
    }

    private fun JSONArray.toStringList(): List<String> = buildList {
        for (index in 0 until length()) {
            add(getString(index))
        }
    }

    companion object {
        const val CATALOG_ASSET = "stickers/catalog.json"
    }
}
