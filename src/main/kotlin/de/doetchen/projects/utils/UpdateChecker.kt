/*
 * ==========================================
 * Fly's Plugin v1.4.1
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */

package de.doetchen.projects.utils

import com.google.gson.JsonParser
import de.doetchen.projects.Flys
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.net.HttpURLConnection
import java.net.URI

class UpdateChecker(private val plugin: Flys) : Listener {

    @Volatile
    private var latestVersion: String? = null

    @Volatile
    private var updateAvailable = false

    @Volatile
    private var lastCheck = 0L

    private fun checkForUpdates(): Boolean {
        if (System.currentTimeMillis() - lastCheck < CHECK_INTERVAL) return updateAvailable

        val connection = URI.create(GITHUB_API_URL).toURL().openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "GET"
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT
            connection.setRequestProperty("User-Agent", "Flys-Plugin-UpdateChecker")

            if (connection.responseCode != 200) return false

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val tag = JsonParser.parseString(body).asJsonObject.get("tag_name")?.asString ?: return false

            val currentVersion = plugin.description.version
            latestVersion = tag
            updateAvailable = isNewerVersion(tag, currentVersion)
            lastCheck = System.currentTimeMillis()

            if (updateAvailable) {
                plugin.logger.info("Update available! Current: v$currentVersion, Latest: v$tag")
            }
            return updateAvailable
        } catch (e: Exception) {
            plugin.logger.warning("Could not check for updates: ${e.message}")
            return false
        } finally {
            connection.disconnect()
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.removePrefix("v").split(".")
        val currentParts = current.removePrefix("v").split(".")

        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val latestPart = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
            val currentPart = currentParts.getOrNull(i)?.toIntOrNull() ?: 0

            if (latestPart != currentPart) return latestPart > currentPart
        }
        return false
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        if (!plugin.configManager.getBoolean("update-checker.enabled")) return

        val player = event.player
        if (!(player.isOp || player.hasPermission(UPDATE_PERMISSION) || player.hasPermission("*"))) return

        plugin.server.scheduler.runTaskLaterAsynchronously(plugin, Runnable {
            if (checkForUpdates()) {
                plugin.server.scheduler.runTask(plugin, Runnable {
                    if (player.isOnline) sendUpdateNotification(player)
                })
            }
        }, 40L)
    }

    private fun sendUpdateNotification(player: Player) {
        val messages = plugin.messageUtils

        player.sendMessage("")
        messages.sendMessage(player, "update.available")
        messages.sendMessage(player, "update.current-version", "VERSION" to plugin.description.version)
        messages.sendMessage(player, "update.latest-version", "VERSION" to (latestVersion ?: "Unknown"))
        messages.sendMessage(player, "update.download")
        player.sendMessage("")

        if (plugin.configManager.getBoolean("general.enable-sounds")) {
            player.playSound(player.location, "block.note_block.pling", 1.0f, 1.5f)
        }
    }

    fun performInitialCheck() {
        if (!plugin.configManager.getBoolean("update-checker.enabled")) return

        plugin.server.scheduler.runTaskAsynchronously(plugin, Runnable { checkForUpdates() })
    }

    private companion object {
        const val GITHUB_API_URL = "https://api.github.com/repos/Dotta4You/Flys/releases/latest"
        const val CHECK_INTERVAL = 3_600_000L
        const val TIMEOUT = 5000
        const val UPDATE_PERMISSION = "flys.updatenotify"
    }
}
