/*
 * ==========================================
 * Fly's Plugin v1.4
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.commands

import de.doetchen.projects.Flys
import org.bukkit.command.Command
import org.bukkit.command.CommandSender

class FlyReloadCommand(plugin: Flys) : BaseCommand(plugin) {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!checkPermission(sender, "permissions.flyreload", "flys.flyreload")) return true

        try {
            plugin.configManager.reloadConfig()
            plugin.messageUtils.sendMessage(sender, "reload.success")
            plugin.logger.info("Config reloaded by ${sender.name}")
        } catch (e: Exception) {
            plugin.messageUtils.sendMessage(sender, "reload.error")
            plugin.logger.severe("Error reloading config: ${e.message}")
        }

        return true
    }
}
