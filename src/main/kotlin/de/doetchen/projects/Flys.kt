/*
 * ==========================================
 * Fly's Plugin v1.2
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects

import de.doetchen.projects.commands.FlyCommand
import de.doetchen.projects.commands.FlyReloadCommand
import de.doetchen.projects.commands.FlySpeedCommand
import de.doetchen.projects.commands.FlysCommand
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
    private lateinit var metrics: Metrics

    override fun onEnable() {
        configManager = ConfigManager(this)
        configManager.initialize()

        messageUtils = MessageUtils(this)
        flightManager = FlightManager(this)
        updateChecker = UpdateChecker(this)
        metrics = Metrics(this, 24086)

        server.pluginManager.registerEvents(flightManager, this)
        server.pluginManager.registerEvents(updateChecker, this)

        getCommand("fly")?.setExecutor(FlyCommand(this))
        getCommand("flys")?.setExecutor(FlysCommand(this))
        getCommand("flyspeed")?.setExecutor(FlySpeedCommand(this))
        getCommand("flyreload")?.setExecutor(FlyReloadCommand(this))

        getCommand("fly")?.tabCompleter = FlyCommand(this)
        getCommand("flys")?.tabCompleter = FlysCommand(this)
        getCommand("flyspeed")?.tabCompleter = FlySpeedCommand(this)
        getCommand("flyreload")?.tabCompleter = FlyReloadCommand(this)

        if (server.pluginManager.getPlugin("PlaceholderAPI") != null) {
            try {
                val hookClass = Class.forName("de.doetchen.projects.hooks.PlaceholderAPIHook")
                val constructor = hookClass.getConstructor(Flys::class.java)
                val hook = constructor.newInstance(this) as me.clip.placeholderapi.expansion.PlaceholderExpansion
                hook.register()
                logger.info("PlaceholderAPI hook registered successfully!")
            } catch (e: Exception) {
                logger.warning("PlaceholderAPI found but hook registration failed: ${e.message}")
            }
        }

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

        updateChecker.performInitialCheck()

        logger.info("Fly's loaded!")
        logger.info("Version: ${description.version} by ${description.authors}")
    }

    override fun onDisable() {
        logger.info("Fly's unloaded!")
    }
}