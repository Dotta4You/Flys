package de.doetchen.projects

import de.doetchen.projects.commands.FlyCommand
import de.doetchen.projects.managers.ConfigManager
import de.doetchen.projects.managers.FlightManager
import de.doetchen.projects.utils.MessageUtils
import org.bukkit.plugin.java.JavaPlugin

class Flys : JavaPlugin() {

    lateinit var configManager: ConfigManager
        private set

    lateinit var messageUtils: MessageUtils
        private set

    lateinit var flightManager: FlightManager
        private set

    override fun onEnable() {
        configManager = ConfigManager(this)
        configManager.initialize()

        messageUtils = MessageUtils(this)
        flightManager = FlightManager(this)

        getCommand("fly")?.setExecutor(FlyCommand(this))

        server.pluginManager.registerEvents(flightManager, this)

        logger.info("Fly's loaded!")
        logger.info("Version: ${description.version} by ${description.authors}")
    }

    override fun onDisable() {
        logger.info("Fly's unloaded!")
    }
}