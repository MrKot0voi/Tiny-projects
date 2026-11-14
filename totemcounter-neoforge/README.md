# TotemCounter (NeoForge)

NeoForge port of TotemCounter for Minecraft 1.21.11.

The mod displays the number of totem pops above player names and the number of totems in your inventory. It also includes a configurable HUD display, an optional colored XP bar, client commands, and a reset keybind (F10 by default).

## Features

- Pop counter in player names and the player list
- `/resetcounter` and `/resetcounter <player>`
- `/showpops` and `/showpops <player>`
- Automatic counter reset on death and supported round-end chat messages
- Totem inventory counter with configurable HUD position
- Optional colored XP bar
- Configuration screen through the NeoForge Mods menu

## Requirements

- Minecraft 1.21.11
- NeoForge 21.11.48 or newer in the 21.11 line
- Java 21

This NeoForge build has no Fabric API or ukulib dependency. Settings are stored in `config/totemcounter.json`.

## Build

```bash
./gradlew build
```

The built mod is written to `build/libs/`.

## Credits

Original mod by [uku3lig](https://github.com/uku3lig/totemcounter). Icon by [Marlow](https://youtube.com/@Marlowww).

Licensed under MIT. See `LICENSE`.
