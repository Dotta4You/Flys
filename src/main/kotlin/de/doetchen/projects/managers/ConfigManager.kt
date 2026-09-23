/*
 * ==========================================
 * Fly's Plugin v1.4
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.managers

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import de.doetchen.projects.Flys
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File
import java.io.Reader

class ConfigManager(private val plugin: Flys) {

    private lateinit var config: FileConfiguration
    private lateinit var configFile: File
    private var messages: Map<String, String> = emptyMap()
    private var prefix = DEFAULT_PREFIX

    var allowedWorlds: Set<String> = emptySet()
        private set

    var disabledWorlds: Set<String> = emptySet()
        private set

    fun initialize() {
        configFile = File(plugin.dataFolder, "config.yml")

        if (!configFile.exists()) {
            plugin.saveDefaultConfig()
        }

        load()
        plugin.logger.info("Config loaded!")
    }

    fun reloadConfig() {
        load()
        plugin.logger.info("Config reloaded!")
    }

    private fun load() {
        loadConfig()
        ensureLanguageFilesExist()
        loadLanguageMessages()
        prefix = config.getString("messages.prefix") ?: DEFAULT_PREFIX
        refreshWorldCache()
    }

    private fun loadConfig() {
        config = try {
            configFile.reader(Charsets.UTF_8).use { YamlConfiguration.loadConfiguration(it) }
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load config with UTF-8: ${e.message}")
            plugin.config
        }
    }

    private fun ensureLanguageFilesExist() {
        LANGUAGES.forEach { (code, name) ->
            val resourcePath = messagesResourcePath(code)
            if (!File(plugin.dataFolder, resourcePath).exists()) {
                plugin.logger.info("Creating $name messages file...")
                plugin.saveResource(resourcePath, false)
            }
        }
    }

    private fun loadLanguageMessages() {
        val language = config.getString("language.language", "en") ?: "en"
        val file = File(plugin.dataFolder, messagesResourcePath(language))

        val bundledEnglish = readBundledMessages("en")
        val bundledLanguage = if (language == "en") emptyMap() else readBundledMessages(language)
        val custom = if (file.exists()) {
            try {
                file.reader(Charsets.UTF_8).use { parseMessages(it) }
            } catch (e: Exception) {
                plugin.logger.severe("Failed to load language messages: ${e.message}")
                plugin.logger.info("Using fallback messages...")
                emptyMap()
            }
        } else {
            plugin.logger.warning("Language file ${file.name} not found, using fallback messages...")
            emptyMap()
        }

        messages = bundledEnglish + bundledLanguage + custom
        plugin.logger.info("Language messages loaded: $language")
    }

    private fun readBundledMessages(language: String): Map<String, String> {
        val stream = plugin.getResource(messagesResourcePath(language)) ?: return emptyMap()
        return stream.reader(Charsets.UTF_8).use { parseMessages(it) }
    }

    private fun parseMessages(reader: Reader): Map<String, String> {
        val result = HashMap<String, String>()
        flatten("", JsonParser.parseReader(reader).asJsonObject, result)
        return result
    }

    private fun flatten(parentPath: String, node: JsonObject, target: MutableMap<String, String>) {
        node.entrySet().forEach { (key, value) ->
            val path = if (parentPath.isEmpty()) key else "$parentPath.$key"
            when {
                value.isJsonObject -> flatten(path, value.asJsonObject, target)
                value.isJsonPrimitive -> target[path] = value.asString
            }
        }
    }

    private fun refreshWorldCache() {
        allowedWorlds = config.getStringList("worlds.allowed-worlds").toSet()
        disabledWorlds = config.getStringList("worlds.disabled-worlds").toSet()
    }

    fun getMessage(path: String, vararg placeholders: Pair<String, String>): String {
        val message = messages[path] ?: run {
            plugin.logger.warning("Message path '$path' not found!")
            "&cMessage Error: $path"
        }

        var finalMessage = if (path in UNPREFIXED_PATHS) message else "$prefix$message"

        placeholders.forEach { (placeholder, value) ->
            finalMessage = finalMessage.replace("{$placeholder}", value)
        }

        return finalMessage
    }

    fun getString(path: String): String = config.getString(path) ?: ""

    fun getBoolean(path: String): Boolean = config.getBoolean(path)

    fun getDouble(path: String, defaultValue: Double = 0.0): Double = config.getDouble(path, defaultValue)

    fun getStringList(path: String): List<String> = config.getStringList(path)

    fun setStringList(path: String, value: List<String>) {
        config.set(path, value)
        refreshWorldCache()
    }

    fun saveConfig() {
        try {
            config.save(configFile)
            plugin.logger.info("Config saved successfully!")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save config: ${e.message}")
        }
    }

    private companion object {
        const val DEFAULT_PREFIX = "&8[&b&lFlys&8] "

        val LANGUAGES = linkedMapOf(
            "en" to "English",
            "de" to "German",
            "es" to "Spanish",
            "fr" to "French",
            "ru" to "Russian",
            "pl" to "Polish"
        )

        val UNPREFIXED_PATHS = setOf(
            "flight.enabled-self",
            "flight.disabled-self",
            "flight.enabled-by-other",
            "flight.disabled-by-other"
        )

        fun messagesResourcePath(language: String) = "messages/messages_$language.json"
    }
}
