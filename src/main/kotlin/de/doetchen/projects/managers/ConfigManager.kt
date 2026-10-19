/*
 * ==========================================
 * Fly's Plugin v1.5
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
import java.io.IOException
import java.io.Reader
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class ConfigManager(private val plugin: Flys) {

    private lateinit var config: FileConfiguration
    private lateinit var configFile: File
    private var configReadable = true
    private val languageFileMigrator = LanguageFileMigrator(plugin)
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
        val loaded = YamlConfiguration()
        configReadable = try {
            configFile.reader(Charsets.UTF_8).use { loaded.load(it) }
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to read config.yml, using built-in defaults until it is fixed: ${e.message}")
            false
        }
        config = loaded

        val bundled = readBundledConfig() ?: return
        config.setDefaults(bundled)
        if (configReadable) addMissingOptions(bundled)
    }

    private fun readBundledConfig(): YamlConfiguration? {
        val stream = plugin.getResource("config.yml") ?: return null
        return stream.reader(Charsets.UTF_8).use { YamlConfiguration.loadConfiguration(it) }
    }

    private fun addMissingOptions(bundled: YamlConfiguration) {
        val missingPaths = bundled.getKeys(true).filterNot { config.contains(it, true) }
        if (missingPaths.isEmpty()) return
        if (!backUpConfigFile()) return

        missingPaths.forEach { copyBundledValue(bundled, it) }
        missingPaths.forEach { copyBundledComments(bundled, it) }

        if (saveConfig()) {
            val addedOptions = missingPaths.filterNot { bundled.isConfigurationSection(it) }
            plugin.logger.info("Added ${addedOptions.size} new option(s) to config.yml: ${addedOptions.joinToString()}")
        }
    }

    private fun copyBundledValue(bundled: YamlConfiguration, path: String) {
        if (bundled.isConfigurationSection(path)) {
            if (!config.isConfigurationSection(path)) config.createSection(path)
        } else {
            config.set(path, bundled.get(path))
        }
    }

    private fun copyBundledComments(bundled: YamlConfiguration, path: String) {
        config.setComments(path, bundled.getComments(path))
        config.setInlineComments(path, bundled.getInlineComments(path))
    }

    private fun backUpConfigFile(): Boolean = try {
        Files.copy(
            configFile.toPath(),
            File(plugin.dataFolder, CONFIG_BACKUP_NAME).toPath(),
            StandardCopyOption.REPLACE_EXISTING
        )
        true
    } catch (e: IOException) {
        plugin.logger.warning("Could not back up config.yml, new options are not added to the file: ${e.message}")
        false
    }

    private fun ensureLanguageFilesExist() {
        LANGUAGES.forEach { (code, name) ->
            val resourcePath = messagesResourcePath(code)
            if (File(plugin.dataFolder, resourcePath).exists()) {
                languageFileMigrator.addMissingMessages(resourcePath)
            } else {
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

    fun getInt(path: String): Int = config.getInt(path)

    fun getLong(path: String): Long = config.getLong(path)

    fun getDouble(path: String, defaultValue: Double = 0.0): Double = config.getDouble(path, defaultValue)

    fun getStringList(path: String): List<String> = config.getStringList(path)

    fun setStringList(path: String, value: List<String>) {
        config.set(path, value)
        refreshWorldCache()
    }

    fun saveConfig(): Boolean {
        if (!configReadable) {
            plugin.logger.severe("config.yml could not be read, not saving to avoid overwriting it. Fix the file and use /flyreload.")
            return false
        }

        return try {
            config.save(configFile)
            plugin.logger.info("Config saved successfully!")
            true
        } catch (e: Exception) {
            plugin.logger.severe("Failed to save config: ${e.message}")
            false
        }
    }

    private companion object {
        const val DEFAULT_PREFIX = "&8[&b&lFlys&8] "
        const val CONFIG_BACKUP_NAME = "config.yml.bak"

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
            "flight.disabled-by-other",
            "flight.timed-enabled-self",
            "flight.timed-enabled-by-other",
            "flight.timed-expired",
            "flight.timed-warning",
            "flight.combat-disabled"
        )

        fun messagesResourcePath(language: String) = "messages/messages_$language.json"
    }
}
