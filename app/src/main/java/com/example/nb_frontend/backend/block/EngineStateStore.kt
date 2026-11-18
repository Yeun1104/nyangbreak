package com.example.nb_frontend.backend.block

import android.content.Context
import androidx.core.content.edit
import org.json.JSONObject

class EngineStateStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("engine_state", Context.MODE_PRIVATE)

    fun loadBreaks(): Map<String, GatekeeperEngine.ActiveBreak> = runCatching {
        val o = JSONObject(prefs.getString(KEY, null) ?: return emptyMap())
        o.keys().asSequence().associateWith { pkg ->
            val b = o.getJSONObject(pkg)
            GatekeeperEngine.ActiveBreak(
                until = b.getLong("until"),
                breakEventId = if (b.has("id")) b.getLong("id") else null,
            )
        }
    }.getOrDefault(emptyMap())

    fun saveBreaks(breaks: Map<String, GatekeeperEngine.ActiveBreak>) {
        val o = JSONObject()
        for ((pkg, b) in breaks) {
            o.put(pkg, JSONObject().put("until", b.until).apply { b.breakEventId?.let { put("id", it) } })
        }
        prefs.edit { putString(KEY, o.toString()) }
    }

    private companion object {
        const val KEY = "breaks"
    }
}
