## Prometric

Prometheus exporter for PaperMC server

### Metrics

All metrics are prefixed with `mc_*`.

- online players count
- online players pseudo (`name` label)
- total placed blocks by player
- total broken blocks by player
- total player deaths
- total player kills
- player time since last death (in s)

### Config

In the plugin configuration file `plugins/prometric/config.yml`
You can customize the port on which the metrics will be exposed.

```yaml
port: 9190 # default
```

### Dev

Use the `runServer` gradle task to automatically launch a PaperMC server with the plugin installed

```shell
gradlew runServer
```

### Build

Use the `shadowJar` gradle task to build the plugin JAR file, will be located in `build/libs/`

```shell
gradlew shadowJar
```

### TODO

- metrics based on scoreboard bukkit API

### License

MPL 2.0
