package de.doetchen.projects.utils

import de.doetchen.projects.Flys
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MessageUtils(private val plugin: Flys) {

    fun parse(message: String): String {
        return ChatColor.translateAlternateColorCodes('&', message)
    }

    fun sendMessage(player: Player, configPath: String, vararg placeholders: Pair<String, String>) {
        val message = plugin.configManager.getString(configPath, *placeholders)
        player.sendMessage(parse(message))
    }

    fun sendMessage(sender: CommandSender, configPath: String, vararg placeholders: Pair<String, String>) {
        val message = plugin.configManager.getString(configPath, *placeholders)
        sender.sendMessage(parse(message))
    }

    fun sendMessage(sender: CommandSender, message: String) {
        sender.sendMessage(parse(message))
    }

    fun sendActionBar(player: Player, configPath: String, vararg placeholders: Pair<String, String>) {
        val message = plugin.configManager.getString(configPath, *placeholders)
        sendActionBarMessage(player, message)
    }

    fun sendActionBarMessage(player: Player, message: String) {
        val parsedMessage = parse(message)

        try {
            val method = player::class.java.getMethod("sendActionBar", String::class.java)
            method.invoke(player, parsedMessage)
        } catch (e: Exception) {
            try {
                player.sendTitle("", parsedMessage, 0, 60, 10)
            } catch (e2: Exception) {
                player.sendMessage("&7[ActionBar] $parsedMessage")
            }
        }
    }

    companion object {
        const val PREFIX = "&8[&b&lFlys&8] "
        const val SUCCESS = "&a+ "
        const val ERROR = "&cX "
        const val WARNING = "&e! "
        const val INFO = "&9i "
        const val NO_PERMISSION = "${PREFIX}${ERROR}&cDu hast keine Berechtigung für diesen Befehl!"
    }
}