/*
 * ==========================================
 * Fly's Plugin v1.4.1
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.utils

import de.doetchen.projects.Flys
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class MessageUtils(private val plugin: Flys) {

    fun parse(message: String): String = ChatColor.translateAlternateColorCodes('&', message)

    fun sendMessage(sender: CommandSender, configPath: String, vararg placeholders: Pair<String, String>) {
        sender.sendMessage(parse(plugin.configManager.getMessage(configPath, *placeholders)))
    }

    fun sendActionBar(player: Player, configPath: String, vararg placeholders: Pair<String, String>) {
        val message = plugin.configManager.getMessage(configPath, *placeholders)
        sendActionBarMessage(player, message)
    }

    fun sendActionBarMessage(player: Player, message: String) {
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(parse(message)))
    }
}
