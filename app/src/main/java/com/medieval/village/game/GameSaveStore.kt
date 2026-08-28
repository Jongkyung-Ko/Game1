package com.medieval.village.game

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** 저장 슬롯 요약 (목록 UI용) */
data class SaveSlotInfo(
    val slot: Int,
    val empty: Boolean,
    val savedAtMs: Long = 0L,
    val playerName: String = "",
    val level: Int = 0,
    val day: Int = 0,
    val gold: Int = 0,
    val settlementName: String = "",
    val placeLabel: String = "",
)

/**
 * 파일 기반 세이브 (filesDir/saves/slot_N.json).
 * 슬롯은 1..[SLOT_COUNT].
 */
class GameSaveStore(context: Context) {

    companion object {
        const val SLOT_COUNT = 5
        private const val DIR = "saves"
    }

    private val dir: File = File(context.applicationContext.filesDir, DIR).also { it.mkdirs() }

    private fun fileFor(slot: Int): File = File(dir, "slot_${slot.coerceIn(1, SLOT_COUNT)}.json")

    fun slotInfo(slot: Int): SaveSlotInfo {
        val f = fileFor(slot)
        if (!f.exists()) return SaveSlotInfo(slot = slot, empty = true)
        return try {
            val json = JSONObject(f.readText())
            val player = json.getJSONObject("player")
            SaveSlotInfo(
                slot = slot,
                empty = false,
                savedAtMs = json.optLong("savedAtMs", f.lastModified()),
                playerName = player.optString("name", "모험가"),
                level = player.optInt("level", 1),
                day = player.optInt("day", 1),
                gold = player.optInt("gold", 0),
                settlementName = json.optString("settlementName", ""),
                placeLabel = json.optString("placeLabel", ""),
            )
        } catch (_: Throwable) {
            SaveSlotInfo(slot = slot, empty = true)
        }
    }

    fun allSlotInfo(): List<SaveSlotInfo> =
        (1..SLOT_COUNT).map { slotInfo(it) }

    fun write(slot: Int, json: JSONObject) {
        val f = fileFor(slot)
        f.parentFile?.mkdirs()
        f.writeText(json.toString())
    }

    fun read(slot: Int): JSONObject? {
        val f = fileFor(slot)
        if (!f.exists()) return null
        return try {
            JSONObject(f.readText())
        } catch (_: Throwable) {
            null
        }
    }

    fun clear(slot: Int) {
        fileFor(slot).delete()
    }
}

/** JSON 배열 헬퍼 */
fun JSONArray.toStringList(): List<String> =
    buildList {
        for (i in 0 until length()) add(optString(i))
    }

fun JSONArray.toNullableStringList(): List<String?> =
    buildList {
        for (i in 0 until length()) {
            if (isNull(i)) add(null) else add(optString(i))
        }
    }
