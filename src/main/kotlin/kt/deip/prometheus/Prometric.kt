// This Source Code Form is subject to the terms of the Mozilla Public
// License, v. 2.0. If a copy of the MPL was not distributed with this
// file, You can obtain one at https://mozilla.org/MPL/2.0/.

package kt.deip.prometheus

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.exporter.HTTPServer.Builder
import org.bukkit.Bukkit.getMaxPlayers
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.Statistic
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.plugin.java.JavaPlugin

class Prometric : JavaPlugin() {
    private lateinit var metricsServer: HTTPServer

    val onlinePlayers: Gauge = Gauge.build()
        .name("mc_online_players_total").help("Online players total").register()

    val onlinePlayer: Gauge = Gauge.build()
        .name("mc_online_player")
        .labelNames("uuid", "name")
        .help("An online player").register()

    val blocksPlaced: Counter = Counter.build()
        .name("mc_blocks_placed")
        .labelNames("uuid", "name")
        .help("Placed blocks by a player").register()

    val blocksBroken: Counter = Counter.build()
        .name("mc_blocks_broken")
        .labelNames("uuid", "name")
        .help("Broken blocks by a player").register()

    val playerDeaths: Gauge = Gauge.build()
        .name("mc_player_deaths")
        .labelNames("uuid", "name")
        .help("Player death count").register()

    val playerKills: Gauge = Gauge.build()
        .name("mc_player_kills")
        .labelNames("uuid", "name")
        .help("Player kill count").register()

    val playerTimeSinceLastDeath: Gauge = Gauge.build()
        .name("mc_player_timesld")
        .labelNames("uuid", "name")
        .help("Player kill count").register()

    private val maxPlayers: Gauge = Gauge.build()
        .name("mc_max_players").help("Max players").register()

    private val eventListener = object : Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        fun onPlayerJoin(e: PlayerJoinEvent) {
            onlinePlayers.set(getOnlinePlayers().size.toDouble())

            val player = e.player
            val playerUuid = player.uniqueId.toString()
            val playerName = player.name
            onlinePlayer.labels(playerUuid, playerName).set(e.player.ping.toDouble())
            playerDeaths.labels(playerUuid, playerName)
                .set(player.getStatistic(Statistic.DEATHS).toDouble())
            playerKills.labels(playerUuid, playerName)
                .set(player.getStatistic(Statistic.PLAYER_KILLS).toDouble())
            playerTimeSinceLastDeath.labels(playerUuid, playerName)
                // `TIME_SINCE_DEATH` is expressed in tick, 1 tick <=> 0.05s
                .set(player.getStatistic(Statistic.TIME_SINCE_DEATH).toDouble() * 0.05)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onPlayerConnectionClose(e: PlayerConnectionCloseEvent) {
            onlinePlayers.set(getOnlinePlayers().size.toDouble())
            onlinePlayer.remove(e.playerUniqueId.toString(), e.playerName)
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onPlayerMove(e: PlayerMoveEvent) {
            // update some player stats
            for (p in server.onlinePlayers) {
                onlinePlayer.labels(p.uniqueId.toString(), p.name).set(e.player.ping.toDouble())
                playerTimeSinceLastDeath.labels(p.uniqueId.toString(), p.name)
                    .set(p.getStatistic(Statistic.TIME_SINCE_DEATH).toDouble() * 0.05)
            }
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onBlockPlace(e: BlockPlaceEvent) {
            blocksPlaced.labels(e.player.uniqueId.toString(), e.player.name).inc()
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onBlockBreak(e: BlockBreakEvent) {
            blocksBroken.labels(e.player.uniqueId.toString(), e.player.name).inc()
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onPlayerDeath(e: PlayerDeathEvent) {
            playerDeaths.labels(e.player.uniqueId.toString(), e.player.name).inc()

            // update some player stats
            for (p in server.onlinePlayers) {
                playerKills.labels(p.uniqueId.toString(), p.name)
                    .set(p.getStatistic(Statistic.PLAYER_KILLS).toDouble())
                playerTimeSinceLastDeath.labels(p.uniqueId.toString(), p.name)
                    .set(p.getStatistic(Statistic.TIME_SINCE_DEATH).toDouble() * 0.05)
            }
        }
    }

    override fun onEnable() {
        saveDefaultConfig()
        server.pluginManager.registerEvents(eventListener, this)
        maxPlayers.set(getMaxPlayers().toDouble())

        val port = config.getString("port")?.toInt() ?: 9190

        // starts an HTTP server to expose metrics
        metricsServer = Builder()
            .withPort(port).build()

        logger.info("metrics exposed on $port")
    }

    override fun onDisable() {
        onlinePlayer.clear()
        metricsServer.close()
    }
}