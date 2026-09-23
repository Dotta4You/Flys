/*
 * ==========================================
 * Fly's Plugin v1.4
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects

import de.doetchen.projects.commands.BaseCommand
import de.doetchen.projects.commands.FlyCommand
import de.doetchen.projects.commands.FlyReloadCommand
import de.doetchen.projects.commands.FlySpeedCommand
import de.doetchen.projects.commands.FlysCommand
import de.doetchen.projects.hooks.PlaceholderAPIHook
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
    private var metrics: Metrics? = null

    override fun onEnable() {
        configManager = ConfigManager(this)
        configManager.initialize()

        messageUtils = MessageUtils(this)
        flightManager = FlightManager(this)
        updateChecker = UpdateChecker(this)

        server.pluginManager.registerEvents(flightManager, this)
        server.pluginManager.registerEvents(updateChecker, this)

        registerCommand("fly", FlyCommand(this))
        registerCommand("flys", FlysCommand(this))
        registerCommand("flyspeed", FlySpeedCommand(this))
        registerCommand("flyreload", FlyReloadCommand(this))

        registerPlaceholderHook()
        registerMetrics()

        updateChecker.performInitialCheck()

        logger.info("Fly's loaded!")
        logger.info("Version: ${description.version} by ${description.authors}")
    }

    override fun onDisable() {
        metrics?.shutdown()
        logger.info("Fly's unloaded!")
    }

    private fun registerCommand(name: String, command: BaseCommand) {
        getCommand(name)?.setExecutor(command)
    }

    private fun registerPlaceholderHook() {
        if (server.pluginManager.getPlugin("PlaceholderAPI") == null) return

        try {
            PlaceholderAPIHook(this).register()
            logger.info("PlaceholderAPI hook registered successfully!")
        } catch (e: Exception) {
            logger.warning("PlaceholderAPI found but hook registration failed: ${e.message}")
        }
    }

    private fun registerMetrics() {
        val metrics = Metrics(this, BSTATS_ID)
        this.metrics = metrics

        metrics.addCustomChart(SimplePie("particles_enabled") {
            if (configManager.getBoolean("general.enable-particles")) "enabled" else "disabled"
        })

        metrics.addCustomChart(SimplePie("sounds_enabled") {
            if (configManager.getBoolean("general.enable-sounds")) "enabled" else "disabled"
        })

        metrics.addCustomChart(SimplePie("language") {
            configManager.getString("language.language").ifEmpty { "en" }
        })

        metrics.addCustomChart(SimplePie("world_restrictions") {
            when {
                configManager.disabledWorlds.isNotEmpty() -> "has_disabled_worlds"
                configManager.allowedWorlds.isNotEmpty() -> "has_allowed_worlds"
                else -> "all_worlds_allowed"
            }
        })
    }

    private companion object {
        const val BSTATS_ID = 24086
    }
}
