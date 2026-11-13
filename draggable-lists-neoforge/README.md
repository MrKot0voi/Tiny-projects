# Draggable Lists — NeoForge 1.21.11

NeoForge port of [Draggable Lists](https://github.com/mrmelon54/draggable_lists) by MrMelon54.

Draggable resource packs, datapacks, servers and worlds in their in-game lists.

## Requirements

- Minecraft 1.21.11
- NeoForge 21.11.42 or newer for Minecraft 1.21.11
- Architectury API for NeoForge 19.0.1 or newer
- Cloth Config API for NeoForge 21.11.153 or newer

## Build

The project uses Java 21 and ModDevGradle. In CI it is built with Gradle 9.8.2:

```bash
gradle build --stacktrace --no-daemon
```

The distributable JAR is written to `build/libs/`.

## License

LGPL-3.0-only. See [LICENSE.md](LICENSE.md) and the upstream project for attribution.
