/*
 * ==========================================
 * Fly's Plugin v1.0
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */
package de.doetchen.projects.commands

import de.doetchen.projects.Flys
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender

class FlyReloadCommand(private val plugin: Flys) : CommandExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        val permission = plugin.configManager.getString("permissions.flyreload").takeIf { it.isNotEmpty() } ?: "flys.flyreload"
        if (!sender.hasPermission(permission)) {
            plugin.messageUtils.sendMessage(sender, "errors.no-permission")
            if (plugin.configManager.getBoolean("general.enable-sounds")) {
                if (sender is org.bukkit.entity.Player) {
                    sender.playSound(sender.location, "block.note_block.bass", 1.0f, 1.0f)
                }
            }
            return true
        }

        try {
            plugin.configManager.reloadConfig()
            val prefix = plugin.configManager.getString("messages.prefix")
            sender.sendMessage(plugin.messageUtils.parse("$prefix&a&l✔ &aConfig reloaded! &7(&ev${plugin.description.version}&7)"))
            plugin.logger.info("Config reloaded by ${sender.name}")
        } catch (e: Exception) {
            plugin.messageUtils.sendMessage(sender, "reload.error")
            plugin.logger.severe("Error reloading config: ${e.message}")
        }

        return true
    }
}
