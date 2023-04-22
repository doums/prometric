package kt.deip.prometheus

import com.destroystokyo.paper.event.player.PlayerConnectionCloseEvent
import io.prometheus.client.Counter
import io.prometheus.client.Gauge
import io.prometheus.client.exporter.HTTPServer
import io.prometheus.client.exporter.HTTPServer.Builder
import org.bukkit.Bukkit.getMaxPlayers
import org.bukkit.Bukkit.getOnlinePlayers
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin

class Prometric : JavaPlugin() {
    private lateinit var metricsServer: HTTPServer

    val onlinePlayers: Gauge = Gauge.build()
        .name("online_players").help("Online players").register()

    private val maxPlayers: Gauge = Gauge.build()
        .name("max_players").help("Max players").register()

    private val eventListener = object : Listener {
        @EventHandler(priority = EventPriority.MONITOR)
        fun onPlayerJoin(event: PlayerJoinEvent) {
            onlinePlayers.set(getOnlinePlayers().size.toDouble())
        }

        @EventHandler(priority = EventPriority.MONITOR)
        fun onPlayerConnectionClose(event: PlayerConnectionCloseEvent) {
            onlinePlayers.set(getOnlinePlayers().size.toDouble())
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

        logger.info("metrics server listening on $port")
    }

    override fun onDisable() {
        metricsServer.close()
    }
}