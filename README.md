<h1 align="center">
  Ninja Utilities

Minecraft Mod for Hypixel SkyBlock

<img src="https://raw.githubusercontent.com/NinjaGoldfinch/Ninja-Utilities/refs/heads/development/src/main/resources/assets/ninja-utils/icon.png" alt="ninja-utils" width="200">
</h1>

<div align="center">

[![Discord](https://img.shields.io/discord/1194919292437807163?label=discord&color=7289da&logo=discord&style=for-the-badge)](https://discord.gg/cegdEA3h77)
[![Downloads](https://img.shields.io/github/downloads/NinjaGoldfinch/Ninja-Utilities/total?label=downloads&color=055ca3&logo=github&style=for-the-badge)](https://github.com/NinjaGoldfinch/Ninja-Utilities/releases)
[![Modrinth](https://img.shields.io/modrinth/dt/pVY1CRc2?style=for-the-badge&logo=Modrinth&color=17ad56)](https://modrinth.com/mod/pVY1CRc2)
</div>

## What it does

Ninja Utilities is a Fabric Mod for Minecraft 1.21.10-1.21.11 that adds a configurable HUD, stat tracking, and quality-of-life features for [Hypixel SkyBlock](https://wiki.hypixel.net/).

* **Customizable HUD:** Location, purse, bits, skill XP, slayer, mayor, pet, cookie timer, ping, TPS, and more
* **Stat Tracking:** Skill XP rates, slayer quest tracking with boss alerts, ping and TPS monitoring, and session counters
* **Island-Specific Features:** Garden copper/sowdust/pest tracking, with more islands planned
* **Item Utilities:** SkyBlock item ID detection and inventory searching
* **And much more!**

## Join the Discord!

Give feedback or just chat with others on our Discord.

* **Bug Reports:** Use the `#bug-reporting` channel when you find broken stuff.
* **Feature Suggestions:** Feel free to share your ideas in `#suggestions` for new features and improvements.
* **Chatting:** Chat with other Ninja Utilities users in `#general`.

---

## Features

### HUD Elements

All HUD elements are draggable, individually toggleable, and persist across restarts.

#### General
- **Location** — Current island and area (includes garden plot number)
- **Coin Purse** — Live purse/piggy bank balance
- **Bits** — Bit balance from scoreboard
- **Skill Progress** — XP gains with progress bar and XP/min rate tracking
- **Slayer** — Active slayer quest with [BOSS] indicator and spawn time
- **Mayor** — Current mayor and minister (fetched from Hypixel API)
- **Pet** — Active pet from tab list
- **Cookie Timer** — Cookie buff duration from tab list
- **SkyBlock Date** — In-game date from scoreboard
- **Ping** — Current latency with rolling average (color-coded thresholds)
- **TPS** — Server ticks per second (10-second rolling average)
- **Item Gains** — Recent item gains from inventory and sacks (auto-hides after 10s)

#### Garden Island
- **Copper** — Copper count (Garden only)
- **Sowdust** — Sowdust count (Garden only)
- **Pests** — Pest count (hides when 0, Garden only)

#### Debug
- **Debug Overlay** — 23+ debug lines in a separate panel

### HUD Customization
- Draggable panel positioning (`/ninja_utils hud`)
- Configurable scale (0.5x–3x)
- Adjustable background opacity
- Per-element toggle switches
- Right-click to toggle elements, Shift+drag to disable snap

### Tracking & Stats
- Skill XP rate calculator (60-second sliding window)
- Skill XP checker — compare tracked gains against `/skills` menu baseline
- Slayer quest tracker with boss alerts and spawn time
- Session kill counter
- Ping measurement with rolling average and spike detection
- TPS estimation (10-second rolling average)
- Rare drop logging
- Coin change tracking
- Item gain tracking from inventory changes and sack messages (60-second sliding window)

### Item Utilities
- SkyBlock item ID detection from held items
- Full ExtraAttributes NBT inspection
- Inventory and container search by item ID

### API Integration
- Hypixel public API (election/bazaar data, no key required)
- Hypixel Mod API (location, party info, player rank)
- Backend proxy support for key-required endpoints (profile data)
- TTL-based caching with configurable durations

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
3. Download the Ninja Utilities `.jar` for your MC version from [releases](https://github.com/NinjaGoldfinch/Ninja-Utilities/releases)
4. Place it in your `mods/` folder
5. Launch the game

## Commands

All commands are available under `/ninja_utils` with aliases `/nu`, `/ninja`, `/ninja_utilities`.

### General

| Command | Description |
|---------|-------------|
| `/ninja_utils config` | Open the settings screen |
| `/ninja_utils hud` | Open HUD position editor |

### Dev / Inspection

| Command | Description |
|---------|-------------|
| `/ninja_utils dev` | Show all debug state |
| `/ninja_utils dev state` | Hypixel connection state, island, packets |
| `/ninja_utils dev scoreboard` | Raw scoreboard data and parsed values |
| `/ninja_utils dev tablist` | Raw tab list entries |
| `/ninja_utils dev stats` | SkyBlock stats, skill XP rates, tab list data |
| `/ninja_utils dev slayer` | Slayer tracker state (quest, boss, completions, timing) |
| `/ninja_utils dev party` | Party info from Mod API |
| `/ninja_utils dev api` | API cache status, backend config, error counts |
| `/ninja_utils dev mayor` | Fetch and display full mayor/minister data with perks |
| `/ninja_utils dev perf` | Performance metrics (tick/render time, memory) |
| `/ninja_utils dev ping` | Current ping, average ping, and sample history |
| `/ninja_utils dev skills` | Skill XP checker — compare tracked gains vs menu baseline |
| `/ninja_utils dev item` | Dump held item's SkyBlock ID and ExtraAttributes NBT |
| `/ninja_utils dev search [item_id]` | Search inventory and open container for items by ID |
| `/ninja_utils dev items` | Item gain tracker rates and totals |
| `/ninja_utils dev colors [chat\|scoreboard\|tablist]` | Show raw formatting codes |

### Simulation (Testing)

| Command | Description |
|---------|-------------|
| `/ninja_utils dev simulate skill` | Simulate Mining XP gain (+1,250 XP) |
| `/ninja_utils dev simulate slayer` | Full slayer cycle (quest → boss spawn → boss slain) |
| `/ninja_utils dev simulate ping` | Inject 10 fake ping samples (80–200ms) |
| `/ninja_utils dev simulate coins` | Fire CoinChangeEvent (+50,000 coins) |
| `/ninja_utils dev simulate drop` | Fire RareDropEvent ("Wither Chestplate") |
| `/ninja_utils dev simulate item` | Fire 2 fake item gain events (inventory + sack) |
| `/ninja_utils dev simulate reset` | Reset all trackers to initial state |

## Configuration

Open the config screen with `/ninja_utils config`. Settings are organized into categories:

- **General** — Master enable, SkyBlock-only mode, warnings, chat prefix
- **HUD** — Toggle individual elements, scale, opacity, position
- **SkyBlock** — Scoreboard cleaning, skill/coin/drop/item tracking, slayer settings, tab list parsing
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
