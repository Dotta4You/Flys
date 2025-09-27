/*
 * ==========================================
 * Fly's Plugin v1.0
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */
package de.doetchen.projects.managers

import de.doetchen.projects.Flys
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.*
import java.nio.charset.StandardCharsets

class ConfigManager(private val plugin: Flys) {

    private lateinit var config: FileConfiguration
    private lateinit var configFile: File
    private lateinit var messages: Map<String, Any>
    private var currentLanguage = "en"

    fun initialize() {
        configFile = File(plugin.dataFolder, "config.yml")

        if (!configFile.exists()) {
            plugin.saveDefaultConfig()
        }

        loadConfigWithUTF8()
        ensureAllLanguageFilesExist()
        loadLanguageMessages()
        plugin.logger.info("Config loaded!")
    }

    private fun loadConfigWithUTF8() {
        try {
            val reader = InputStreamReader(FileInputStream(configFile), StandardCharsets.UTF_8)
            config = YamlConfiguration.loadConfiguration(reader)
            reader.close()
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load config with UTF-8: ${e.message}")
            config = plugin.config
        }
    }

    private fun ensureAllLanguageFilesExist() {
        val messagesDir = File(plugin.dataFolder, "messages")
        if (!messagesDir.exists()) {
            messagesDir.mkdirs()
        }

        val englishFile = File(messagesDir, "messages_en.json")
        val germanFile = File(messagesDir, "messages_de.json")

        if (!englishFile.exists()) {
            plugin.logger.info("Creating English messages file...")
            createMessagesFile(englishFile, getEnglishMessages())
        }

        if (!germanFile.exists()) {
            plugin.logger.info("Creating German messages file...")
            createMessagesFile(germanFile, getGermanMessages())
        }
    }

    private fun createMessagesFile(file: File, messages: Map<String, Any>) {
        try {
            val gson = Gson().newBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create()
            val json = gson.toJson(messages)

            val writer = OutputStreamWriter(FileOutputStream(file), StandardCharsets.UTF_8)
            writer.write(json)
            writer.close()

            plugin.logger.info("Created messages file: ${file.name}")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to create messages file ${file.name}: ${e.message}")
        }
    }

    private fun loadLanguageMessages() {
        currentLanguage = config.getString("language.language", "en") ?: "en"

        val messagesDir = File(plugin.dataFolder, "messages")
        val messagesFile = File(messagesDir, "messages_$currentLanguage.json")

        try {
            val reader = InputStreamReader(FileInputStream(messagesFile), StandardCharsets.UTF_8)
            val gson = Gson()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            messages = gson.fromJson(reader, type)
            reader.close()
            plugin.logger.info("Language messages loaded: $currentLanguage")
        } catch (e: Exception) {
            plugin.logger.severe("Failed to load language messages: ${e.message}")
            plugin.logger.info("Using fallback messages...")
            messages = when (currentLanguage) {
                "de" -> getGermanMessages()
                else -> getEnglishMessages()
            }
        }
    }

    private fun getEnglishMessages(): Map<String, Any> {
        return mapOf(
            "flight" to mapOf(
                "enabled-self" to "&a&l✔ Flight enabled",
                "disabled-self" to "&c&l✘ Flight disabled",
                "enabled-other" to "&a&l✔ &aFlight enabled for &e{PLAYER}&a!",
                "disabled-other" to "&c&l✘ &cFlight disabled for &e{PLAYER}&4!",
                "enabled-by-other" to "&b&lFlight was enabled by &e{SENDER}",
                "disabled-by-other" to "&e&lFlight was disabled by &e{SENDER}"
            ),
            "errors" to mapOf(
                "player-only" to "&c✘ Only players can use this command!",
                "no-permission" to "&c✘ You don't have permission for this command!",
                "player-not-found" to "&c✘ Player &e{PLAYER} &cwas not found!",
                "usage" to "&e&lℹ &eUsage: &f/fly [player]",
                "world-not-allowed" to "&c✘ Flying is not allowed in this world!"
            ),
            "flyspeed" to mapOf(
                "current" to "&e&lℹ &eCurrent flight speed: &b{SPEED}&e/10",
                "set" to "&a&l+ &aFlight speed set to &b{SPEED}&a/10!",
                "set-other" to "&a&l+ &aFlight speed for &e{PLAYER} &aset to &b{SPEED}&a/10!",
                "set-by-other" to "&e&lℹ &eYour flight speed was set to &b{SPEED}&e/10 by &b{SENDER}&e!",
                "invalid-range" to "&c✘ Invalid speed! Use values from 1-10.",
                "flight-not-enabled" to "&c✘ You must enable flight first!",
                "target-flight-not-enabled" to "&c✘ Player &e{PLAYER} &cdoesn't have flight enabled!",
                "usage" to "&e&lℹ &eUsage: &f/flyspeed [1-10] &eor &f/flyspeed [player] [1-10]"
            ),
            "reload" to mapOf(
                "success" to "&a&l+ &aConfiguration successfully reloaded!",
                "error" to "&c✘ Error while reloading configuration!"
            )
        )
    }

    private fun getGermanMessages(): Map<String, Any> {
        return mapOf(
            "flight" to mapOf(
                "enabled-self" to "&a&l✔ Fliegen aktiviert",
                "disabled-self" to "&c&l✘ Fliegen deaktiviert",
                "enabled-other" to "&a&l✔ &aFliegen für &e{PLAYER} &aaktiviert!",
                "disabled-other" to "&c&l✘ &cFliegen für &e{PLAYER} &4deaktiviert!",
                "enabled-by-other" to "&b&lFliegen wurde von &e{SENDER} &b&laktiviert",
                "disabled-by-other" to "&e&lFliegen wurde von &e{SENDER} &e&ldeaktiviert"
            ),
            "errors" to mapOf(
                "player-only" to "&c✘ Nur Spieler können diesen Befehl verwenden!",
                "no-permission" to "&c✘ Du hast keine Berechtigung für diesen Befehl!",
                "player-not-found" to "&c✘ Spieler &e{PLAYER} &cwurde nicht gefunden!",
                "usage" to "&e&lℹ &eVerwendung: &f/fly [spieler]",
                "world-not-allowed" to "&c✘ Fliegen ist in dieser Welt nicht erlaubt!"
            ),
            "flyspeed" to mapOf(
                "current" to "&e&lℹ &eAktuelle Fluggeschwindigkeit: &b{SPEED}&e/10",
                "set" to "&a&l+ &aFluggeschwindigkeit auf &b{SPEED}&a/10 gesetzt!",
                "set-other" to "&a&l+ &aFluggeschwindigkeit für &e{PLAYER} &aauf &b{SPEED}&a/10 gesetzt!",
                "set-by-other" to "&e&lℹ &eDeine Fluggeschwindigkeit wurde von &b{SENDER} &eauf &b{SPEED}&e/10 gesetzt!",
                "invalid-range" to "&c✘ Ungültige Geschwindigkeit! Verwende Werte von 1-10.",
                "flight-not-enabled" to "&c✘ Du musst zuerst das Fliegen aktivieren!",
                "target-flight-not-enabled" to "&c✘ Spieler &e{PLAYER} &chat das Fliegen nicht aktiviert!",
                "usage" to "&e&lℹ &eVerwendung: &f/flyspeed [1-10] &eoder &f/flyspeed [spieler] [1-10]"
            ),
            "reload" to mapOf(
                "success" to "&a&l+ &aKonfiguration erfolgreich neu geladen!",
                "error" to "&c✘ Fehler beim Neuladen der Konfiguration!"
            )
        )
    }

    fun reloadConfig() {
        loadConfigWithUTF8()
        ensureAllLanguageFilesExist()
        loadLanguageMessages()
        plugin.logger.info("Config reloaded!")
    }

    fun getString(path: String, vararg placeholders: Pair<String, String>): String {
        val message = getMessageFromPath(path) ?: run {
            plugin.logger.warning("Message path '$path' not found!")
            when (path) {
                "flight.enabled-self" -> "&a&l✔ Flight enabled"
                "flight.disabled-self" -> "&c&l✘ Flight disabled"
                "flight.enabled-by-other" -> "&b&lFlight was enabled by &e{SENDER}"
                "flight.disabled-by-other" -> "&e&lFlight was disabled by &e{SENDER}"
                "errors.player-only" -> "&cOnly players can use this command!"
                "errors.no-permission" -> "&cYou don't have permission for this command!"
                "errors.player-not-found" -> "&cPlayer {PLAYER} was not found!"
                "errors.usage" -> "&eUsage: /fly [player]"
                else -> "&cMessage Error: $path"
            }
        }

        val needsPrefix = !path.startsWith("flight.enabled-self") &&
                !path.startsWith("flight.disabled-self") &&
                !path.startsWith("flight.enabled-by-other") &&
                !path.startsWith("flight.disabled-by-other")

        var finalMessage = if (needsPrefix) {
            val prefix = config.getString("messages.prefix") ?: "&8[&b&lFlys&8] "
            "$prefix$message"
        } else {
            message
        }

        placeholders.forEach { (placeholder, value) ->
            finalMessage = finalMessage.replace("{$placeholder}", value)
        }

        return finalMessage
    }

    private fun getMessageFromPath(path: String): String? {
        val parts = path.split(".")
        var current: Any? = messages

        for (part in parts) {
            current = when (current) {
                is Map<*, *> -> current[part]
                else -> null
            }
            if (current == null) break
        }

        return current as? String
    }

    fun getString(path: String): String {
        return config.getString(path) ?: ""
    }

    fun getBoolean(path: String): Boolean = config.getBoolean(path)

    fun getDouble(path: String, defaultValue: Double = 0.0): Double = config.getDouble(path, defaultValue)

    fun getLong(path: String, defaultValue: Long = 0L): Long = config.getLong(path, defaultValue)

    fun getStringList(path: String): List<String> = config.getStringList(path) ?: emptyList()
}