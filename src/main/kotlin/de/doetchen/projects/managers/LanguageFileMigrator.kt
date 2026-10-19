/*
 * ==========================================
 * Fly's Plugin v1.5
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.managers

import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.doetchen.projects.Flys
import java.io.File
import java.io.IOException
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class LanguageFileMigrator(private val plugin: Flys) {

    private val prettyWriter = GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create()

    fun addMissingMessages(resourcePath: String) {
        val file = File(plugin.dataFolder, resourcePath)
        val bundled = readBundledMessages(resourcePath) ?: return
        val current = readServerMessages(file) ?: return

        val addedPaths = mergeMissingEntries(bundled, current, "")
        if (addedPaths.isEmpty()) return
        if (!backUpMessageFile(file)) return

        if (writeMessageFile(file, current)) {
            plugin.logger.info("Added ${addedPaths.size} new message(s) to ${file.name}: ${addedPaths.joinToString()}")
        }
    }

    private fun mergeMissingEntries(source: JsonObject, target: JsonObject, parentPath: String): List<String> {
        val addedPaths = mutableListOf<String>()

        source.entrySet().forEach { (key, value) ->
            val path = if (parentPath.isEmpty()) key else "$parentPath.$key"
            val existing = target.get(key)

            when {
                existing == null -> {
                    target.add(key, value.deepCopy())
                    addedPaths += collectLeafPaths(path, value)
                }
                existing.isJsonObject && value.isJsonObject ->
                    addedPaths += mergeMissingEntries(value.asJsonObject, existing.asJsonObject, path)
            }
        }
        return addedPaths
    }

    private fun collectLeafPaths(path: String, value: JsonElement): List<String> {
        if (!value.isJsonObject) return listOf(path)
        return value.asJsonObject.entrySet().flatMap { (key, child) -> collectLeafPaths("$path.$key", child) }
    }

    private fun readBundledMessages(resourcePath: String): JsonObject? {
        val stream = plugin.getResource(resourcePath) ?: return null
        return stream.reader(Charsets.UTF_8).use { JsonParser.parseReader(it).asJsonObject }
    }

    private fun readServerMessages(file: File): JsonObject? {
        if (!file.exists()) return null

        return try {
            file.reader(Charsets.UTF_8).use { JsonParser.parseReader(it) }
                .takeIf { it.isJsonObject }?.asJsonObject
                ?: run {
                    plugin.logger.warning("${file.name} is not a JSON object, new messages are not added to it.")
                    null
                }
        } catch (e: Exception) {
            plugin.logger.warning("${file.name} is not valid JSON, new messages are not added to it: ${e.message}")
            null
        }
    }

    private fun backUpMessageFile(file: File): Boolean = try {
        Files.copy(file.toPath(), File(file.parentFile, "${file.name}.bak").toPath(), StandardCopyOption.REPLACE_EXISTING)
        true
    } catch (e: IOException) {
        plugin.logger.warning("Could not back up ${file.name}, new messages are not added to it: ${e.message}")
        false
    }

    private fun writeMessageFile(file: File, messages: JsonObject): Boolean {
        val tempFile = File(file.parentFile, "${file.name}.tmp")

        return try {
            tempFile.writeText(prettyWriter.toJson(messages) + "\n", Charsets.UTF_8)
            moveReplacing(tempFile, file)
            true
        } catch (e: IOException) {
            tempFile.delete()
            plugin.logger.severe("Failed to update ${file.name}: ${e.message}")
            false
        }
    }

    private fun moveReplacing(source: File, destination: File) {
        try {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            Files.move(source.toPath(), destination.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
