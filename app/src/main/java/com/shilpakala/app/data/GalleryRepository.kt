package com.shilpakala.app.data

import android.content.Context
import android.graphics.Bitmap
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

private val Context.galleryDataStore: DataStore<Preferences> by preferencesDataStore(name = "shilpakala_gallery")

private val KEY_INDEX = stringPreferencesKey("gallery_items_json")

class GalleryRepository(private val context: Context) {

    private val brandedDir: File
        get() = File(context.filesDir, "branded").apply { mkdirs() }

    val items: Flow<List<GalleryItem>> = context.galleryDataStore.data.map { prefs ->
        parseIndex(prefs[KEY_INDEX] ?: "[]")
    }

    fun fileFor(item: GalleryItem): File = File(brandedDir, item.fileName)

    suspend fun saveBrandedJpeg(bitmap: Bitmap, productName: String, price: String): GalleryItem {
        val id = UUID.randomUUID().toString()
        val fileName = "$id.jpg"
        val file = File(brandedDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
        }
        val item = GalleryItem(
            id = id,
            fileName = fileName,
            productName = productName.trim().ifBlank { "Untitled" },
            price = price.trim(),
            createdAt = System.currentTimeMillis()
        )
        context.galleryDataStore.edit { prefs ->
            val current = parseIndex(prefs[KEY_INDEX] ?: "[]").toMutableList()
            current.add(0, item)
            prefs[KEY_INDEX] = toJson(current)
        }
        return item
    }

    suspend fun delete(item: GalleryItem) {
        fileFor(item).delete()
        context.galleryDataStore.edit { prefs ->
            val current = parseIndex(prefs[KEY_INDEX] ?: "[]").filter { it.id != item.id }
            prefs[KEY_INDEX] = toJson(current)
        }
    }

    private fun parseIndex(json: String): List<GalleryItem> {
        return try {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(
                        GalleryItem(
                            id = o.getString("id"),
                            fileName = o.getString("fileName"),
                            productName = o.getString("productName"),
                            price = o.getString("price"),
                            createdAt = o.getLong("createdAt")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun toJson(items: List<GalleryItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            arr.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("fileName", item.fileName)
                    put("productName", item.productName)
                    put("price", item.price)
                    put("createdAt", item.createdAt)
                }
            )
        }
        return arr.toString()
    }
}
