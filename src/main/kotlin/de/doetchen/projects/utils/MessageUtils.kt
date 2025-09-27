/*
 * ==========================================
 * Fly's Plugin v1.0
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
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(parsedMessage))
        } catch (e: Exception) {
            try {
                player.sendTitle("", parsedMessage, 0, 60, 10)
            } catch (e2: Exception) {
                player.sendMessage("&7[ActionBar] $parsedMessage")
            }
        }
    }
}