/*
 * ==========================================
 * Fly's Plugin v1.0
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */
package de.doetchen.projects

import de.doetchen.projects.commands.FlyCommand
import de.doetchen.projects.commands.FlyReloadCommand
import de.doetchen.projects.commands.FlySpeedCommand
import de.doetchen.projects.managers.ConfigManager
import de.doetchen.projects.managers.FlightManager
import de.doetchen.projects.utils.MessageUtils
import de.doetchen.projects.utils.UpdateChecker
import org.bstats.bukkit.Metrics
import org.bstats.charts.SimplePie
import org.bukkit.plugin.java.JavaPlugin

class Flys : JavaPlugin() {

    lateinit var configManager: ConfigManager
        private set

    lateinit var messageUtils: MessageUtils
        private set

    lateinit var flightManager: FlightManager
        private set

    private lateinit var updateChecker: UpdateChecker

    override fun onEnable() {
        configManager = ConfigManager(this)
        configManager.initialize()

        messageUtils = MessageUtils(this)
        flightManager = FlightManager(this)
        updateChecker = UpdateChecker(this)

        getCommand("fly")?.setExecutor(FlyCommand(this))
        getCommand("flyspeed")?.setExecutor(FlySpeedCommand(this))
        getCommand("flyreload")?.setExecutor(FlyReloadCommand(this))

        getCommand("fly")?.tabCompleter = FlyCommand(this)
        getCommand("flyspeed")?.tabCompleter = FlySpeedCommand(this)

        server.pluginManager.registerEvents(flightManager, this)
        server.pluginManager.registerEvents(updateChecker, this)

        val metrics = Metrics(this, 27336)
        setupMetrics(metrics)

        updateChecker.performInitialCheck()

        logger.info("Fly's loaded!")
        logger.info("Version: ${description.version} by ${description.authors}")
    }

    private fun setupMetrics(metrics: Metrics) {
        metrics.addCustomChart(SimplePie("particles_enabled") {
            if (configManager.getBoolean("general.enable-particles")) "enabled" else "disabled"
        })

        metrics.addCustomChart(SimplePie("sounds_enabled") {
            if (configManager.getBoolean("general.enable-sounds")) "enabled" else "disabled"
        })

        metrics.addCustomChart(SimplePie("language") {
            configManager.getString("language.language").takeIf { it.isNotEmpty() } ?: "en"
        })

        metrics.addCustomChart(SimplePie("world_restrictions") {
            val allowedWorlds = configManager.getStringList("worlds.allowed-worlds")
            val disabledWorlds = configManager.getStringList("worlds.disabled-worlds")

            when {
                disabledWorlds.isNotEmpty() -> "has_disabled_worlds"
                allowedWorlds.isNotEmpty() -> "has_allowed_worlds"
                else -> "all_worlds_allowed"
            }
        })
    }

    override fun onDisable() {
        logger.info("Fly's unloaded!")
    }
}