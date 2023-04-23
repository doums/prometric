## Prometric

Prometheus exporter for PaperMC server

![grafana_dashboard](https://user-images.githubusercontent.com/6359431/233843185-08b9b6c9-e92e-4cef-b3a6-1ca3a40989e6.png)

### Metrics

All metrics are prefixed with `mc_*`.

- online players count
- online players ping
- total placed blocks by player (during a session)
- total broken blocks by player (during a session)
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

### License

MPL 2.0
