package de.doetchen.projects.managers

import de.doetchen.projects.Flys
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import java.io.*
import java.nio.charset.StandardCharsets

class ConfigManager(private val plugin: Flys) {

    private lateinit var config: FileConfiguration
    private lateinit var configFile: File

    fun initialize() {
        configFile = File(plugin.dataFolder, "config.yml")

        if (!configFile.exists()) {
            plugin.saveDefaultConfig()
        }

        loadConfigWithUTF8()
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

    fun reloadConfig() {
        loadConfigWithUTF8()
        plugin.logger.info("Config reloaded!")
    }

    fun getString(path: String, vararg placeholders: Pair<String, String>): String {
        val rawValue = config.getString(path)

        var message = rawValue ?: run {
            plugin.logger.warning("Config path '$path' not found!")
            // ActionBar Fallback
            when (path) {
                "flight.enabled-self" -> "&a&l✔ Fliegen aktiviert"
                "flight.disabled-self" -> "&c&l✘ Fliegen deaktiviert"
                "flight.enabled-by-other" -> "&9&lFliegen wurde von &e{SENDER} &9&laktiviert"
                "flight.disabled-by-other" -> "&e&lFliegen wurde von &e{SENDER} &e&ldeaktiviert"
                "errors.player-only" -> "&cNur Spieler können diesen Befehl verwenden!"
                "errors.no-permission" -> "&cDu hast keine Berechtigung für diesen Befehl!"
                "errors.player-not-found" -> "&cSpieler {PLAYER} wurde nicht gefunden!"
                "errors.usage" -> "&eVerwendung: /fly [spieler]"
                else -> "&cConfig Error: $path"
            }
        }

        message = message.replace("{PREFIX}", config.getString("messages.prefix") ?: "&8[&b&lFlys&8] ")
        message = message.replace("{SUCCESS}", config.getString("messages.success-icon") ?: "&a+ ")
        //message = message.replace("{ERROR}", config.getString("messages.error-icon") ?: "&c✘ ")
        message = message.replace("{WARNING}", config.getString("messages.warning-icon") ?: "&e! ")
        message = message.replace("{INFO}", config.getString("messages.info-icon") ?: "&9i ")

        placeholders.forEach { (placeholder, value) ->
            message = message.replace("{$placeholder}", value)
        }

        return message
    }
    fun getBoolean(path: String): Boolean = config.getBoolean(path)
}