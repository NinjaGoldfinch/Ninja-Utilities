# Ninja Utilities

![Discord](https://img.shields.io/discord/1194919292437807163)
![GitHub Downloads (all assets, all releases)](https://img.shields.io/github/downloads/NinjaGoldfinch/Ninja-Utilities/total)
![Modrinth Downloads](https://img.shields.io/modrinth/dt/pVY1CRc2)

A client-side Fabric mod for Hypixel SkyBlock that provides a configurable HUD, stat tracking, and quality-of-life features.

## Features

### HUD Elements
- **Location** — Current island and area
- **Coin Purse** — Live purse/piggy bank balance
- **Bits** — Bit balance from scoreboard
- **Skill Progress** — XP gains with XP/min rate tracking
- **Slayer** — Active slayer quest and boss status
- **Mayor** — Current mayor and minister (fetched from Hypixel API)
- **Pet** — Active pet from tab list
- **Cookie Timer** — Cookie buff duration from tab list
- **SkyBlock Date** — In-game date from scoreboard

### HUD Customization
- Draggable panel positioning (`/ninja_utils dev hud_config`)
- Configurable scale (0.5x–3x)
- Adjustable background opacity
- Per-element toggle switches

### Tracking & Stats
- Skill XP rate calculator (60-second sliding window)
- Slayer quest tracker with boss alerts
- Session kill counter

### API Integration
- Hypixel public API (election/bazaar data, no key required)
- Backend proxy support for key-required endpoints (profile data)
- TTL-based caching with configurable durations
- Retry with exponential backoff

### Warnings
- Mod API not detected after joining Hypixel
- Unknown location detection
- Unknown island mode logging

### Debug Tools
- Debug overlay with full state inspection, performance metrics, and memory monitoring
- Dev commands for inspecting scoreboard, tab list, API state, and more
- Configurable logging verbosity per subsystem

## Requirements

- Minecraft **1.21.10** or **1.21.11**
- [Fabric Loader](https://fabricmc.net/) 0.18.4+
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [Fabric Language Kotlin](https://modrinth.com/mod/fabric-language-kotlin)
- [ResourcefulConfig](https://modrinth.com/mod/resourceful-config)

**Recommended:**
- [Hypixel Mod API](https://modrinth.com/mod/hypixel-mod-api) — Enables accurate location detection and party info

## Installation

1. Install Fabric Loader for your Minecraft version
2. Download and place the required dependencies into your `mods/` folder:
   - Fabric API
   - Fabric Language Kotlin
   - ResourcefulConfig
3. Download the Ninja Utilities `.jar` for your MC version from releases
4. Place it in your `mods/` folder
5. Launch the game

## Commands

All commands are available under `/ninja_utils` with aliases `/nu`, `/ninja`, `/ninja_utilities`.

| Command | Description |
|---------|-------------|
| `/ninja_utils` | Show help |
| `/ninja_utils config` | Open the settings screen |
| `/ninja_utils dev` | Show all debug state |
| `/ninja_utils dev state` | Hypixel connection state |
| `/ninja_utils dev scoreboard` | Raw scoreboard data |
| `/ninja_utils dev tablist` | Raw tab list data |
| `/ninja_utils dev stats` | SkyBlock stats and XP tracking |
| `/ninja_utils dev slayer` | Slayer tracker state |
| `/ninja_utils dev party` | Party info |
| `/ninja_utils dev api` | API client status and cache state |
| `/ninja_utils dev mayor` | Fetch and display election data |
| `/ninja_utils dev perf` | Performance and memory stats |
| `/ninja_utils dev hud` | HUD configuration state |
| `/ninja_utils dev hud_config` | Open HUD position editor |
| `/ninja_utils dev toggle` | Toggle SkyBlock-only mode |

## Configuration

Open the config screen with `/ninja_utils config`. Settings are organized into categories:

- **General** — Master enable, SkyBlock-only mode, warnings, chat prefix
- **HUD** — Toggle individual elements, scale, opacity, position
- **SkyBlock** — Scoreboard cleaning, skill/coin/drop tracking, slayer settings
- **API** — Backend proxy URL, cache TTL settings
- **Debug** — Log level, per-subsystem logging toggles, debug overlay

## Building from Source

Requires JDK 21.

```bash
./gradlew build
```

Output JARs are in `versions/1.21.10/build/libs/` and `versions/1.21.11/build/libs/`.

## License

[LGPL-3.0](LICENSE.txt)
