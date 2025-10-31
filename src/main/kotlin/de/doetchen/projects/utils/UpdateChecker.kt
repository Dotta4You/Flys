/*
 * ==========================================
 * Fly's Plugin v1.0
 * Made by Dötchen with <3
 * https://github.com/Dotta4You/Flys
 * ==========================================
 */
package de.doetchen.projects.utils

import de.doetchen.projects.Flys
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

class UpdateChecker(private val plugin: Flys) : Listener {

    private var latestVersion: String? = null
    private var updateAvailable = false
    private var lastCheck = 0L

    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/repos/Dotta4You/Flys/releases/latest"
        private const val CHECK_INTERVAL = 3600000L // 1 hour
        private const val UPDATE_PERMISSION = "flys.updatenotify"
    }

    fun checkForUpdates(): CompletableFuture<Boolean> {
        return CompletableFuture.supplyAsync {
            try {
                if (System.currentTimeMillis() - lastCheck < CHECK_INTERVAL) {
                    return@supplyAsync updateAvailable
                }

                val url = URL(GITHUB_API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = 5000
                connection.readTimeout = 5000
                connection.setRequestProperty("User-Agent", "Flys-Plugin-UpdateChecker")

                if (connection.responseCode == 200) {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
                    val response = reader.readText()
                    reader.close()

                    val tagNameStart = response.indexOf("\"tag_name\":\"") + 12
                    if (tagNameStart > 11) {
                        val tagNameEnd = response.indexOf("\"", tagNameStart)
                        if (tagNameEnd != -1) {
                            latestVersion = response.substring(tagNameStart, tagNameEnd)

                            val currentVersion = plugin.description.version
                            updateAvailable = isNewerVersion(latestVersion!!, currentVersion)
                            lastCheck = System.currentTimeMillis()

                            if (updateAvailable) {
                                plugin.logger.info("Update available! Current: v$currentVersion, Latest: v$latestVersion")
                            }

                            return@supplyAsync updateAvailable
                        }
                    }
                }
            } catch (e: Exception) {
                plugin.logger.warning("Could not check for updates: ${e.message}")
            }
            false
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        try {
                val latestParts = latest.replace("v", "").split(".")
                val currentParts = current.replace("v", "").split(".")

                for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
                    val latestPart = latestParts.getOrNull(i)?.toIntOrNull() ?: 0
                    val currentPart = currentParts.getOrNull(i)?.toIntOrNull() ?: 0

                    when {
                        latestPart > currentPart -> return true
                        latestPart < currentPart -> return false
                    }
                }
            } catch (_: Exception) {
                return false
            }
        return false
    }

    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        val player = event.player

        if (player.isOp || player.hasPermission(UPDATE_PERMISSION) || player.hasPermission("*")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
                checkForUpdates().thenAccept { hasUpdate ->
                    if (hasUpdate) {
                        Bukkit.getScheduler().runTask(plugin, Runnable {
                            sendUpdateNotification(player)
                        })
                    }
                }
            }, 40L) // 2 seconds delay
        }
    }

    private fun sendUpdateNotification(player: Player) {
        val prefix = plugin.configManager.getString("messages.prefix")
        val currentVersion = plugin.description.version

        player.sendMessage("")
        player.sendMessage(plugin.messageUtils.parse("${prefix}&e&l⚡ UPDATE AVAILABLE!"))
        player.sendMessage(plugin.messageUtils.parse("${prefix}&7Current Version: &c$currentVersion"))
        player.sendMessage(plugin.messageUtils.parse("${prefix}&7Latest Version: &a$latestVersion"))
        player.sendMessage(plugin.messageUtils.parse("${prefix}&7Download: &bhttps://modrinth.com/plugin/flys/changelog"))
        player.sendMessage("")

        player.playSound(player.location, "block.note_block.pling", 1.0f, 1.5f)
    }

    fun performInitialCheck() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            checkForUpdates()
        })
    }
}
