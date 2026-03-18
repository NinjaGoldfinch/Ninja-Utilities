# Hypixel SkyBlock Fabric Mod — Comprehensive Development Plan

**Target:** Minecraft 1.21.10 / 1.21.11 · Fabric Loader · Kotlin  
**Scope:** Hypixel SkyBlock only (no other game modes)  
**Document Version:** 1.0 — March 2026

---

## Table of Contents

1. [Project Setup & Dependencies](#1-project-setup--dependencies)
2. [Architecture Overview](#2-architecture-overview)
3. [Core Systems — Parsers & Detection](#3-core-systems--parsers--detection)
4. [Hypixel Mod API Integration](#4-hypixel-mod-api-integration)
5. [Hypixel Public API (Backend-Proxied)](#5-hypixel-public-api-backend-proxied)
6. [HUD Features](#6-hud-features)
7. [Config Design — ResourcefulConfig](#7-config-design--resourcefulconfig)
8. [Logging & Debug System](#8-logging--debug-system)
9. [Data Collection — SkyBlock Stats](#9-data-collection--skyblock-stats)
10. [Warning & Alert System](#10-warning--alert-system)
11. [Implementation Phases](#11-implementation-phases)
12. [Appendix — Reference Documentation](#12-appendix--reference-documentation)

---

## 1. Project Setup & Dependencies

### 1.1 Gradle Configuration (Kotlin DSL)

The project uses `build.gradle.kts` with Fabric Loom and Fabric Language Kotlin. Key repositories and dependencies:

```kotlin
plugins {
    kotlin("jvm") version "2.2.20"       // or latest 2.x
    id("fabric-loom") version "1.9-SNAPSHOT"
}

repositories {
    mavenCentral()
    maven("https://maven.fabricmc.net/")
    maven("https://repo.hypixel.net/repository/Hypixel/")   // Hypixel Mod API
    maven("https://maven.teamresourceful.com/repository/maven-public/")  // ResourcefulConfig
}

dependencies {
    // --- Minecraft & Fabric ---
    minecraft("com.mojang:minecraft:1.21.10")
    mappings("net.fabricmc:yarn:1.21.10+build.X:v2")       // pin to latest build
    modImplementation("net.fabricmc:fabric-loader:0.16.X")
    modImplementation("net.fabricmc.fabric-api:fabric-api:X+1.21.10")

    // --- Kotlin ---
    modImplementation("net.fabricmc:fabric-language-kotlin:1.13.6+kotlin.2.2.20")

    // --- Hypixel Mod API (core packet definitions) ---
    implementation("net.hypixel:mod-api:1.0.1")

    // --- ResourcefulConfig ---
    modImplementation("com.teamresourceful:resourcefulconfig-fabric-1.21.10:3.9.1")

    // --- Optional: Hypixel Public API Java wrapper (for backend use) ---
    // implementation("net.hypixel:hypixel-api-transport-reactor:X")

    // --- Logging Utility (SLF4J is bundled with MC, no extra dep needed) ---
}
```

### 1.2 `fabric.mod.json` Essentials

```jsonc
{
  "schemaVersion": 1,
  "id": "yourmod",
  "version": "${version}",
  "environment": "client",
  "entrypoints": {
    "client": [{ "adapter": "kotlin", "value": "com.yourmod.YourModClient" }]
  },
  "depends": {
    "fabricloader": ">=0.16.0",
    "fabric-api": "*",
    "fabric-language-kotlin": ">=1.13.6+kotlin.2.2.20",
    "hypixel-mod-api": ">=1.0.1",
    "minecraft": "~1.21.10"
  }
}
```

### 1.3 Kotlin Entrypoint Pattern

```kotlin
// Kotlin object entrypoint (simplest pattern)
package com.yourmod

import net.fabricmc.api.ClientModInitializer

object YourModClient : ClientModInitializer {
    const val MOD_ID = "yourmod"
    
    override fun onInitializeClient() {
        // Register systems in order:
        // 1. Config (ResourcefulConfig)
        // 2. Logging subsystem
        // 3. Hypixel Mod API event subscriptions
        // 4. Scoreboard / Tab / Chat parsers
        // 5. HUD renderer registrations
        // 6. Command registrations
    }
}
```

---

## 2. Architecture Overview

### 2.1 Package Structure

```
com.yourmod/
├── YourModClient.kt                 # Main entrypoint
├── config/
│   ├── ModConfig.kt                 # @Config root class
│   ├── GeneralCategory.kt           # @Category — general settings
│   ├── HudCategory.kt               # @Category — HUD toggle/position settings
│   ├── SkyblockCategory.kt          # @Category — SkyBlock-specific features
│   ├── ApiCategory.kt               # @Category — backend API URL, toggle
│   └── DebugCategory.kt             # @Category — logging verbosity, debug overlays
├── core/
│   ├── HypixelState.kt              # Singleton: online/offline, current location, game type
│   ├── SkyblockDetector.kt          # Determines if player is in SkyBlock + which island
│   └── EventBus.kt                  # Lightweight internal event bus (or use Fabric Events)
├── parsers/
│   ├── ScoreboardParser.kt          # Reads sidebar scoreboard lines
│   ├── TabListParser.kt             # Reads tab list player entries & widgets
│   └── ChatParser.kt                # Regex-based chat message classifier
├── api/
│   ├── modapi/
│   │   ├── ModApiManager.kt         # Subscribes to Hypixel Mod API packets
│   │   └── LocationTracker.kt       # Processes ClientboundLocationPacket
│   └── publicapi/
│       ├── BackendClient.kt         # HTTP client to YOUR backend (not Hypixel direct)
│       ├── PlayerProfileCache.kt    # Caches profile data with TTL
│       └── dto/                     # Data Transfer Objects for API responses
├── hud/
│   ├── HudManager.kt               # Registers & renders all HUD elements
│   ├── elements/
│   │   ├── LocationHud.kt           # Current island / area display
│   │   ├── CoinPurseHud.kt          # Coins from scoreboard
│   │   ├── SkillProgressHud.kt      # Current skill XP bar
│   │   ├── SlayerHud.kt             # Active slayer quest info
│   │   └── StatsOverviewHud.kt      # Combined stats panel
│   └── HudRenderer.kt              # DrawContext integration for 1.21+
├── features/
│   ├── warnings/
│   │   ├── UnknownLocationWarning.kt
│   │   └── LowHealthWarning.kt
│   └── stats/
│       ├── SkillTracker.kt
│       └── SlayerTracker.kt
├── logging/
│   ├── ModLogger.kt                 # Centralized logger with levels & categories
│   └── DebugOverlay.kt              # In-game debug info (toggle via config)
└── util/
    ├── TextUtils.kt                 # Color stripping, formatting code removal
    ├── RegexPatterns.kt             # Shared compiled patterns
    └── TimeUtils.kt                 # Formatting helpers
```

### 2.2 Data Flow Diagram

```
┌──────────────┐    ┌───────────────────┐    ┌──────────────────┐
│  Hypixel Mod │───▶│  HypixelState     │───▶│ Feature Modules  │
│  API Packets │    │  (location, party, │    │ (HUD, Warnings,  │
└──────────────┘    │   player info)     │    │  Trackers)        │
                    └───────────────────┘    └──────────────────┘
┌──────────────┐              │                       │
│  Scoreboard  │──────────────┤                       │
│  Parser      │              │                       ▼
└──────────────┘              │              ┌──────────────────┐
┌──────────────┐              │              │  HUD Renderer    │
│  Tab List    │──────────────┤              │  (DrawContext)   │
│  Parser      │              │              └──────────────────┘
└──────────────┘              │
┌──────────────┐              │              ┌──────────────────┐
│  Chat        │──────────────┘              │  Your Backend    │
│  Parser      │────────────────────────────▶│  (Hypixel Public │
└──────────────┘  (triggers API lookups)     │   API proxy)     │
                                             └──────────────────┘
```

---

## 3. Core Systems — Parsers & Detection

### 3.1 Scoreboard Parser

The Hypixel SkyBlock sidebar scoreboard contains critical real-time data. Lines are accessed via the vanilla `Scoreboard` object. Key data extractable from scoreboard lines:

**Data available in scoreboard lines (typical patterns):**
- **Server ID** — e.g. `m42CK` (mini server identifier in footer)
- **Date & Time** — `Late Spring 14th` (SkyBlock calendar) and real time
- **Location/Area** — `⏣ The Hub` or `⏣ Deep Caverns` 
- **Purse/Piggy** — `Purse: 1,234,567` or `Piggy: 1,234,567`
- **Bits** — `Bits: 5,432`
- **Motes** — `Motes: 12,345` (in The Rift)
- **Active Event** — `Spooky Festival` / `Mining Fiesta` etc.
- **Slayer Quest** — `Revenant Horror III` + `(2,450/10,000)` progress
- **Objective** — `Objective: Talk to Mort`

**Implementation approach:**

```kotlin
object ScoreboardParser {
    // Called every tick or on scoreboard update event
    fun parse(): ScoreboardData {
        val scoreboard = MinecraftClient.getInstance().world?.scoreboard ?: return empty()
        val objective = scoreboard.getObjectiveForSlot(DisplaySlot.SIDEBAR) ?: return empty()
        
        // Get all visible entries, sorted by score (descending)
        val lines = scoreboard.getScoreboardEntries(objective)
            .sortedByDescending { it.value }
            .map { it.owner }           // raw text
            .map { TextUtils.stripFormatting(it) }  // remove §codes
        
        return ScoreboardData(
            purse = extractPurse(lines),      // regex: "(?:Purse|Piggy): ([\\d,]+)"
            bits = extractBits(lines),         // regex: "Bits: ([\\d,]+)"
            location = extractLocation(lines), // regex: "[⏣ᏜⒸ] (.+)"
            slayer = extractSlayer(lines),     // multi-line parse
            objective = extractObjective(lines),
            rawLines = lines
        )
    }
}
```

**Important regex patterns for scoreboard:**

| Data | Pattern | Notes |
|------|---------|-------|
| Purse/Piggy | `(?:Purse\|Piggy): ([\d,]+)` | Piggy = in bank context |
| Bits | `Bits: ([\d,]+)` | Cookie buff dependent |
| Location | `[⏣ᏜⒸ] (.+)` | Symbol prefix varies by area type |
| Slayer | `([\w ]+) (I{1,4}\|V)` | Followed by XP progress line |
| SB Date | `(Early\|Late)? ?(\w+) (\d+)(st\|nd\|rd\|th)` | SkyBlock calendar |
| Objective | `Objective: (.+)` | Current quest step |
| Event | Known event names list | Match against known strings |

### 3.2 Tab List Parser

The Tab List in Hypixel SkyBlock is divided into 4 columns of 20 rows each (80 entries total). These are configurable via `/widgets` or `/tablist` in-game. The tab list contains structured widget data:

**Column layout (typical):**
- **Column 1:** Player list, area info, server info
- **Column 2:** Skills, stats, profile info  
- **Column 3:** Collection/dungeon info, widgets
- **Column 4:** More widgets, cookie/booster info

**Data extractable from tab list widgets:**
- **Skills** — Current skill being trained, XP, level
- **Stats** — Speed, Strength, Crit Chance, etc.
- **Profile** — Profile name (e.g., "Mango"), game mode (Ironman/Bingo)
- **Pets** — Active pet name and level
- **Soulflow** — Current soulflow amount
- **Island info** — Visitors, minions, etc.
- **Cookie/Booster** — Time remaining on cookie buff
- **Election** — Current mayor/minister

**Implementation approach:**

```kotlin
object TabListParser {
    fun parse(): TabListData {
        val client = MinecraftClient.getInstance()
        val entries = client.networkHandler?.playerList ?: return empty()
        
        // Tab entries are sorted by their display name
        // Hypixel uses fake player entries with formatted text for widget data
        val lines = entries
            .sortedWith(PlayerListEntry.COMPARATOR)
            .mapNotNull { it.displayName?.string }
            .map { TextUtils.stripFormatting(it) }
        
        return TabListData(
            skills = parseSkillWidget(lines),
            stats = parseStatsWidget(lines),
            profile = parseProfileWidget(lines),
            cookie = parseCookieWidget(lines),
            pet = parsePetWidget(lines),
            rawLines = lines
        )
    }
}
```

**Key tab list widget patterns:**

| Widget | Section Header | Data Pattern |
|--------|---------------|--------------|
| Skills | `Skills:` | `Farming 60: 100%` or `+2.5 (123/456)` |
| Stats | `Stats:` | `❤ 12,345/12,345 HP` , `❈ 256 Defense` |
| Profile | `Profile:` | `Profile: Mango` , `Ironman` badge |
| Pet | `Pet:` | `[Lvl 100] Tiger` |
| Cookie | `Cookie Buff:` | `5d 12h remaining` |
| Soulflow | `Soulflow:` | `⸎ 1,234` (must be enabled in /tab) |

### 3.3 Chat Parser

Chat messages are the richest source of game events. Use Fabric's `ClientReceiveMessageEvents` from Fabric API.

**Categories of parsable chat messages:**

| Category | Example Patterns | Use Case |
|----------|-----------------|----------|
| Skill XP | `+12.5 Farming (1,234/5,678)` | XP tracking |
| Coin gain/loss | `+500 coins (Auction)` | Economy tracker |
| Item drops | `RARE DROP! Ender Dragon Pet` | Drop logging |
| Slayer events | `SLAYER QUEST STARTED!` / `SLAYER QUEST COMPLETE!` | Slayer tracker |
| Location change | `Welcome to Hypixel SkyBlock!` | State detection |
| Dungeon events | `[BOSS] Bonzo: ...` | Dungeon features |
| System messages | `You were kicked! Reason: ...` | Error handling |
| Party messages | `Party > [MVP+] Player: ...` | Party features |
| Bazaar/AH | `[Bazaar] Bought 64x ...` | Economy tracking |

**Implementation approach:**

```kotlin
object ChatParser {
    private val handlers = mutableListOf<ChatHandler>()
    
    fun register(handler: ChatHandler) { handlers.add(handler) }
    
    // Called from ClientReceiveMessageEvents.GAME callback
    fun onChatMessage(message: Text, overlay: Boolean) {
        val raw = message.string
        val stripped = TextUtils.stripFormatting(raw)
        
        for (handler in handlers) {
            if (handler.pattern.matches(stripped)) {
                handler.handle(stripped, message)
                ModLogger.debug("ChatParser", "Matched: ${handler.name} -> $stripped")
            }
        }
    }
}

data class ChatHandler(
    val name: String,
    val pattern: Regex,
    val handle: (stripped: String, original: Text) -> Unit
)
```

**Critical regex patterns for chat:**

```kotlin
object RegexPatterns {
    // Skill XP gain: "+12.5 Farming (1,234/5,678)"
    val SKILL_XP = Regex("""^\+?([\d,.]+) (\w+[\w ]*?) \(([\d,.]+)/([\d,.]+)\)$""")
    
    // Coin gain: "+500 coins (Auction)"  
    val COIN_CHANGE = Regex("""^([+-][\d,.]+) coins? \((.+)\)$""")
    
    // Rare drop: "RARE DROP! (✦ Magic Find) Ender Dragon Pet"
    val RARE_DROP = Regex("""^(?:RARE|VERY RARE|CRAZY RARE|INSANE|PRAY RNGESUS) DROP! (?:\(.+?\) )?(.+)$""")
    
    // Slayer boss spawned
    val SLAYER_SPAWNED = Regex("""^SLAYER QUEST STARTED!$""")
    val SLAYER_COMPLETE = Regex("""^\s{3}SLAYER QUEST COMPLETE!$""")
    
    // Hypixel join/leave
    val SKYBLOCK_JOIN = Regex("""^Welcome to Hypixel SkyBlock!$""")
}
```

---

## 4. Hypixel Mod API Integration

The Hypixel Mod API communicates via Minecraft plugin messages (`hypixel:*` channels). It provides server-authoritative data that replaces unreliable methods like `/locraw`.

### 4.1 Available Packets

| Packet | Direction | Type | Data Provided |
|--------|-----------|------|---------------|
| `ClientboundHelloPacket` | Server → Client | Auto (on join) | Server environment (PRODUCTION/STAGING) |
| `ClientboundLocationPacket` | Server → Client | Event (subscribe) | `serverName`, `serverType`, `lobbyName`, `mode`, `map` |
| `ClientboundPartyInfoPacket` | Server → Client | Request/Response | `inParty`, `leader` UUID, `members` list with roles |
| `ClientboundPlayerInfoPacket` | Server → Client | Request/Response | `rank`, `packageRank`, `monthlyPackageRank`, `prefix` |
| `ServerboundPartyInfoPacket` | Client → Server | Request | Triggers party info response |
| `ServerboundPlayerInfoPacket` | Client → Server | Request | Triggers player info response |

### 4.2 Location Packet — Key for SkyBlock Detection

The `ClientboundLocationPacket` is the **primary mechanism** for detecting where the player is. You **subscribe once** during init, and the server pushes updates every time the player changes instances.

**Location packet data fields:**
- `serverName` — Internal server identifier (e.g., `mini123A`)
- `serverType` — The Hypixel GameType (e.g., `SKYBLOCK`, `BEDWARS`, `LOBBY`)
- `lobbyName` — Lobby identifier if in a lobby
- `mode` — Game mode string (for SkyBlock, this is the island type)
- `map` — Map name (if applicable)

**SkyBlock `mode` values (island types):**

| Mode Value | Island/Area | Notes |
|------------|-------------|-------|
| `dynamic` | Private Island | Player's own island |
| `hub` | Hub | Main hub island |
| `farming_1` | The Farming Islands | Barn, Mushroom Desert |
| `foraging_1` | The Park | Birch Park, Spruce Woods |
| `mining_1` | Gold Mine | Early mining area |
| `mining_2` | Deep Caverns | Gunpowder Mines to Obsidian Sanctuary |
| `mining_3` | Dwarven Mines | Commission area |
| `crystal_hollows` | Crystal Hollows | Mining underground |
| `combat_1` | Spider's Den | |
| `combat_2` | Blazing Fortress | |
| `combat_3` | The End | Dragon fight area |
| `crimson_isle` | Crimson Isle | Nether-themed |
| `dungeon` | Dungeons | Catacombs instance |
| `dungeon_hub` | Dungeon Hub | Pre-dungeon lobby |
| `garden` | Garden | Farming feature |
| `rift` | The Rift | Alternate dimension |
| `kuudra` | Kuudra | Boss fight instance |
| `instanced` | Various | Instanced content |
| `winter` | Jerry's Workshop | Seasonal |

### 4.3 Implementation

```kotlin
object ModApiManager {
    private val logger = ModLogger.category("ModAPI")
    
    fun initialize() {
        val api = HypixelModAPI.getInstance()
        
        // 1. Subscribe to location events (REQUIRED — do this ONCE in init)
        api.subscribeToEventPacket(ClientboundLocationPacket::class.java)
        logger.info("Subscribed to ClientboundLocationPacket events")
        
        // 2. Register packet handlers
        api.createHandler(ClientboundHelloPacket::class.java) { packet ->
            val env = packet.environment  // Environment.PRODUCTION or STAGING
            HypixelState.isOnHypixel = true
            HypixelState.environment = env
            logger.info("Connected to Hypixel (env=$env)")
        }
        
        api.createHandler(ClientboundLocationPacket::class.java) { packet ->
            val serverName = packet.serverName       // e.g., "mini123A"
            val serverType = packet.serverType?.name  // e.g., "SKYBLOCK"
            val mode = packet.mode.orElse(null)      // e.g., "hub", "mining_3"
            val map = packet.map.orElse(null)
            val lobbyName = packet.lobbyName.orElse(null)
            
            HypixelState.update(
                serverName = serverName,
                serverType = serverType,
                mode = mode,
                map = map,
                lobbyName = lobbyName
            )
            
            logger.info("Location: server=$serverName type=$serverType mode=$mode map=$map")
            logger.debug("Full packet: lobby=$lobbyName")
        }
        
        api.createHandler(ClientboundPartyInfoPacket::class.java) { packet ->
            val inParty = packet.isInParty
            HypixelState.partyInfo = if (inParty) {
                PartyInfo(
                    leader = packet.leader,
                    members = packet.members
                )
            } else null
            logger.debug("Party: inParty=$inParty members=${packet.members?.size ?: 0}")
        }
        
        api.createHandler(ClientboundPlayerInfoPacket::class.java) { packet ->
            HypixelState.playerRank = packet.packageRank
            logger.debug("PlayerInfo: rank=${packet.packageRank}")
        }
    }
    
    // Request party info (call sparingly — Hypixel rate limits these)
    fun requestPartyInfo() {
        HypixelModAPI.getInstance().sendPacket(ServerboundPartyInfoPacket())
    }
}
```

### 4.4 HypixelState Singleton

```kotlin
object HypixelState {
    var isOnHypixel: Boolean = false
    var environment: Environment? = null
    
    // Location data from Mod API
    var serverName: String? = null
    var serverType: String? = null    // "SKYBLOCK", "BEDWARS", etc.
    var mode: String? = null          // Island type for SkyBlock
    var map: String? = null
    var lobbyName: String? = null
    
    // Derived state
    val isInSkyBlock: Boolean get() = isOnHypixel && serverType == "SKYBLOCK"
    val currentIsland: SkyBlockIsland? get() = mode?.let { SkyBlockIsland.fromMode(it) }
    val isInDungeon: Boolean get() = isInSkyBlock && mode == "dungeon"
    val isOnPrivateIsland: Boolean get() = isInSkyBlock && mode == "dynamic"
    
    // Location UNKNOWN state (for warnings)
    val isLocationUnknown: Boolean get() = isOnHypixel && serverType == null
    
    // Party info
    var partyInfo: PartyInfo? = null
    var playerRank: String? = null
    
    // Scoreboard-derived data (updated by ScoreboardParser)
    var purse: Long = 0
    var bits: Int = 0
    var currentArea: String? = null  // Finer-grained than mode (e.g., "Dwarven Mines" vs "mining_3")
    
    fun update(serverName: String?, serverType: String?, mode: String?, map: String?, lobbyName: String?) {
        val previousIsland = this.currentIsland
        this.serverName = serverName
        this.serverType = serverType
        this.mode = mode
        this.map = map
        this.lobbyName = lobbyName
        
        // Fire island change event if it changed
        if (currentIsland != previousIsland) {
            EventBus.post(IslandChangeEvent(previousIsland, currentIsland))
        }
    }
    
    fun reset() {
        isOnHypixel = false
        environment = null
        serverName = null
        serverType = null
        mode = null
        map = null
        lobbyName = null
        partyInfo = null
        playerRank = null
        purse = 0
        bits = 0
        currentArea = null
    }
}
```

### 4.5 Disconnect / Server-Switch Handling

Register to Fabric's `ClientPlayConnectionEvents.DISCONNECT` to reset state:

```kotlin
ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
    HypixelState.reset()
    ModLogger.info("Disconnected — state reset")
}
```

---

## 5. Hypixel Public API (Backend-Proxied)

### 5.1 Why a Backend is Required

Hypixel's Public API requires developer API keys obtained through https://developer.hypixel.net. Per Hypixel's policy, individual users should **NOT** generate their own keys for mod use — the **developer** holds a single production key. Since client-side mods cannot securely store an API key, you must route all Hypixel API requests through your own backend server.

### 5.2 Architecture

```
┌─────────────┐     HTTPS      ┌──────────────────┐     HTTPS     ┌────────────────┐
│  Your Mod   │───────────────▶│  Your Backend    │──────────────▶│ api.hypixel.net│
│  (Client)   │  player UUID   │  (rate-limited,  │  API-Key      │                │
│             │◀───────────────│   caching proxy) │◀──────────────│                │
│             │  processed data│                  │  raw JSON     │                │
└─────────────┘                └──────────────────┘               └────────────────┘
```

### 5.3 Hypixel SkyBlock API Endpoints (for your backend to proxy)

**No API key required (public endpoints):**

| Endpoint | URL | Data | Use Case |
|----------|-----|------|----------|
| Collections | `/v2/resources/skyblock/collections` | All collection items & tiers | Reference data |
| Skills | `/v2/resources/skyblock/skills` | Skill XP tables, level caps | XP calculation |
| Items | `/v2/resources/skyblock/items` | All SkyBlock items database | Item lookup |
| Election | `/v2/resources/skyblock/election` | Current mayor, perks, candidates | Mayor display |
| Bingo Event | `/v2/resources/skyblock/bingo` | Current bingo card | Bingo helper |
| News | `/v2/resources/skyblock/news` | SkyBlock news/updates | News feed |
| Bazaar | `/v2/skyblock/bazaar` | All bazaar buy/sell prices | Price display |
| Active Auctions | `/v2/skyblock/auctions` | Paginated active auctions | AH search |
| Ended Auctions | `/v2/skyblock/auctions_ended` | Recently ended auctions | Price history |
| Fire Sales | `/v2/skyblock/firesales` | Active/upcoming fire sales | Event alerts |

**API key required (route through your backend):**

| Endpoint | URL | Data | Use Case |
|----------|-----|------|----------|
| Player Profiles | `/v2/skyblock/profiles?uuid=` | All profiles for a player | Profile viewer |
| Single Profile | `/v2/skyblock/profile?profile=` | Specific profile data | Detailed stats |
| Player Bingo | `/v2/skyblock/bingo?uuid=` | Bingo progress for player | Bingo tracker |
| Museum | `/v2/skyblock/museum?profile=` | Museum data for profile | Museum tracker |
| Garden | `/v2/skyblock/garden?profile=` | Garden data for profile | Garden stats |
| Player Data | `/v2/player?uuid=` | Full Hypixel player data | Network stats |
| Status | `/v2/status?uuid=` | Online status & current game | Online check |

### 5.4 Profile Data Structure (Key Fields)

When you fetch `/v2/skyblock/profiles?uuid=<UUID>`, each profile contains a member object per player. Key data within a member object:

**Skills** (`player_data.experience.*`):
- `experience_skill_farming`, `experience_skill_mining`, `experience_skill_combat`, `experience_skill_foraging`, `experience_skill_fishing`, `experience_skill_enchanting`, `experience_skill_alchemy`, `experience_skill_taming`, `experience_skill_carpentry`, `experience_skill_runecrafting`, `experience_skill_social`

**Slayers** (`slayer.slayer_bosses.*`):
- Each slayer type (`zombie`, `spider`, `wolf`, `enderman`, `blaze`, `vampire`) contains: `xp`, `boss_kills_tier_0` through `boss_kills_tier_4`

**Dungeons** (`dungeons.dungeon_types.catacombs`):
- `experience`: Total catacombs XP
- `tier_completions`: Map of floor → completion count
- `best_runs`: Best runs per floor with stats

**Collections** (`collection.*`):
- Map of `ITEM_ID` → amount collected

**Inventories** (base64 gzipped NBT):
- `inv_contents`, `ender_chest_contents`, `wardrobe_contents`, `personal_vault_contents`, `backpack_contents`, `equipment_contents`

### 5.5 Client-Side Implementation (Limited)

```kotlin
object BackendClient {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(5))
        .build()
    
    private var backendUrl: String
        get() = ApiCategory.backendUrl  // from config
    
    // Fetch player profile data from YOUR backend
    suspend fun fetchProfile(uuid: String): ProfileResponse? {
        if (!ApiCategory.enabled) return null
        
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("$backendUrl/v1/skyblock/profile?uuid=$uuid"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build()
            
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                Json.decodeFromString<ProfileResponse>(response.body())
            } else {
                ModLogger.warn("BackendClient", "Profile fetch failed: ${response.statusCode()}")
                null
            }
        } catch (e: Exception) {
            ModLogger.error("BackendClient", "Profile fetch error", e)
            null
        }
    }
    
    // Fetch bazaar prices (can be direct — no key needed)
    suspend fun fetchBazaar(): BazaarResponse? {
        return try {
            val request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.hypixel.net/v2/skyblock/bazaar"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build()
            val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (response.statusCode() == 200) {
                Json.decodeFromString<BazaarResponse>(response.body())
            } else null
        } catch (e: Exception) {
            ModLogger.error("BackendClient", "Bazaar fetch error", e)
            null
        }
    }
}
```

### 5.6 Rate Limiting Awareness

Hypixel API keys have a default rate limit of **300 requests per 5 minutes** (60/min). Response headers provide:
- `RateLimit-Limit` — Max requests per minute
- `RateLimit-Remaining` — Requests left this minute  
- `RateLimit-Reset` — Seconds until limit resets

Your backend should implement caching with sensible TTLs (profiles: 5 min, bazaar: 1 min, static resources: 1 hour).

---

## 6. HUD Features

### 6.1 HUD Rendering in 1.21+

In Fabric 1.21+, HUD rendering uses Fabric API's `HudRenderCallback`:

```kotlin
object HudManager {
    private val elements = mutableListOf<HudElement>()
    
    fun register(element: HudElement) { elements.add(element) }
    
    fun initialize() {
        HudRenderCallback.EVENT.register { drawContext, renderTickCounter ->
            if (!HypixelState.isInSkyBlock) return@register
            
            for (element in elements) {
                if (element.isEnabled()) {
                    element.render(drawContext, renderTickCounter)
                }
            }
        }
    }
}

abstract class HudElement(val id: String) {
    abstract fun isEnabled(): Boolean   // check config toggle
    abstract fun render(context: DrawContext, tickCounter: RenderTickCounter)
    
    fun drawText(context: DrawContext, text: String, x: Int, y: Int, color: Int = 0xFFFFFF) {
        context.drawTextWithShadow(
            MinecraftClient.getInstance().textRenderer,
            text, x, y, color
        )
    }
}
```

### 6.2 Planned HUD Elements

| Element | Data Source | Config Toggle | Default |
|---------|------------|---------------|---------|
| Current Location | Mod API + Scoreboard | `hud.showLocation` | ON |
| Coin Purse | Scoreboard parser | `hud.showPurse` | ON |
| Bits Counter | Scoreboard parser | `hud.showBits` | OFF |
| Skill XP Progress | Chat parser + Tab list | `hud.showSkillProgress` | OFF |
| Active Slayer | Scoreboard parser | `hud.showSlayer` | OFF |
| Stats Overview | Tab list parser | `hud.showStats` | OFF |
| Current Mayor | Public API (cached) | `hud.showMayor` | OFF |
| SkyBlock Date/Time | Scoreboard parser | `hud.showSbTime` | OFF |
| Active Pet | Tab list parser | `hud.showPet` | OFF |
| Cookie Timer | Tab list parser | `hud.showCookieTimer` | OFF |
| FPS & Ping | Vanilla debug data | `hud.showPerf` | OFF |

### 6.3 HUD Position System

Each HUD element needs configurable X/Y position. Store these in config as integer pairs:

```kotlin
@ConfigEntry(type = EntryType.INTEGER, id = "locationHudX")
@ConfigOption.Range(min = 0, max = 3840)
var locationHudX: Int = 5

@ConfigEntry(type = EntryType.INTEGER, id = "locationHudY")  
@ConfigOption.Range(min = 0, max = 2160)
var locationHudY: Int = 5
```

---

## 7. Config Design — ResourcefulConfig

### 7.1 ResourcefulConfig Quick Reference

ResourcefulConfig is annotation-based. Key annotations:

| Annotation | Purpose |
|------------|---------|
| `@Config(value = "modid")` | Root config class |
| `@Category(value = "id")` | Sub-category (acts as a tab) |
| `@ConfigEntry(type, id, translation)` | Individual setting |
| `@ConfigOption.Range(min, max)` | Number bounds |
| `@ConfigOption.Slider` | Render as slider |
| `@ConfigOption.Separator(value, description)` | Visual divider between groups |
| `@ConfigOption.Hidden` | Hide from GUI |
| `@ConfigOption.Color(alpha)` | Color picker for int fields |
| `@Comment(value, translation)` | Description text |
| `@ConfigInfo(title, description, icon, links)` | Config screen header |

### 7.2 Config Structure with Categories (Tabs)

Each `@Category` class becomes a separate tab in the config GUI. Use `@ConfigOption.Separator` for visual grouping within a tab.

```kotlin
@Config(value = "yourmod", categories = [
    GeneralCategory::class,
    HudCategory::class,
    SkyblockCategory::class,
    ApiCategory::class,
    DebugCategory::class
])
@ConfigInfo(
    title = "yourmod.config.title",
    titleTranslation = "yourmod.config.title",
    description = "yourmod.config.description",
    descriptionTranslation = "yourmod.config.description",
    icon = "settings",
    links = [
        ConfigInfo.Link(value = "https://github.com/you/yourmod", icon = "github", title = "GitHub"),
        ConfigInfo.Link(value = "https://discord.gg/yourserver", icon = "message-circle", title = "Discord")
    ]
)
class ModConfig
```

### 7.3 Tab: General Settings

```kotlin
@Category(value = "general", translation = "yourmod.config.general")
class GeneralCategory {
    companion object {
        // ── Mod Behavior ──────────────────────
        @ConfigOption.Separator(value = "yourmod.config.general.behavior",
            description = "yourmod.config.general.behavior.desc")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "enabled", 
                     translation = "yourmod.config.general.enabled")
        @Comment(value = "Master toggle for all mod features")
        @JvmField var enabled: Boolean = true

        @ConfigEntry(type = EntryType.BOOLEAN, id = "skyblockOnly",
                     translation = "yourmod.config.general.skyblockOnly")
        @Comment(value = "Only activate features when in SkyBlock")
        @JvmField var skyblockOnly: Boolean = true
        
        // ── Notifications ─────────────────────
        @ConfigOption.Separator(value = "yourmod.config.general.notifications",
            description = "yourmod.config.general.notifications.desc")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showWarnings",
                     translation = "yourmod.config.general.showWarnings")
        @Comment(value = "Show warning messages for unknown locations etc.")
        @JvmField var showWarnings: Boolean = true
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "chatPrefix",
                     translation = "yourmod.config.general.chatPrefix")
        @Comment(value = "Show [YourMod] prefix on mod messages")
        @JvmField var chatPrefix: Boolean = true
    }
}
```

### 7.4 Tab: HUD Settings

```kotlin
@Category(value = "hud", translation = "yourmod.config.hud")
class HudCategory {
    companion object {
        // ── Basic HUD Elements ────────────────
        @ConfigOption.Separator(value = "yourmod.config.hud.basic",
            description = "Always-useful displays")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showLocation")
        @JvmField var showLocation: Boolean = true
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showPurse")
        @JvmField var showPurse: Boolean = true
        
        // ── Advanced HUD Elements ─────────────
        @ConfigOption.Separator(value = "yourmod.config.hud.advanced",
            description = "Turn on/off detailed information overlays")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showBits")
        @JvmField var showBits: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showSkillProgress")
        @JvmField var showSkillProgress: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showSlayer")
        @JvmField var showSlayer: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showStats")
        @JvmField var showStats: Boolean = false

        @ConfigEntry(type = EntryType.BOOLEAN, id = "showMayor")
        @JvmField var showMayor: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showPet")
        @JvmField var showPet: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showCookieTimer")
        @JvmField var showCookieTimer: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "showSbTime")
        @JvmField var showSbTime: Boolean = false
        
        // ── Appearance ────────────────────────
        @ConfigOption.Separator(value = "yourmod.config.hud.appearance",
            description = "Customize HUD colors and scale")
        
        @ConfigEntry(type = EntryType.FLOAT, id = "hudScale")
        @ConfigOption.Range(min = 0.5, max = 3.0)
        @ConfigOption.Slider
        @JvmField var hudScale: Float = 1.0f
        
        @ConfigEntry(type = EntryType.INTEGER, id = "hudBackgroundColor")
        @ConfigOption.Color(alpha = true)
        @JvmField var hudBackgroundColor: Int = 0x80000000.toInt()
        
        @ConfigEntry(type = EntryType.FLOAT, id = "hudBackgroundOpacity")
        @ConfigOption.Range(min = 0.0, max = 1.0)
        @ConfigOption.Slider
        @JvmField var hudBackgroundOpacity: Float = 0.5f
    }
}
```

### 7.5 Tab: SkyBlock Features

```kotlin
@Category(value = "skyblock", translation = "yourmod.config.skyblock")
class SkyblockCategory {
    companion object {
        // ── Scoreboard Features ───────────────
        @ConfigOption.Separator(value = "yourmod.config.skyblock.scoreboard",
            description = "Modify and enhance the sidebar scoreboard")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "cleanScoreboard")
        @Comment(value = "Remove unnecessary lines from scoreboard")
        @JvmField var cleanScoreboard: Boolean = false

        @ConfigEntry(type = EntryType.BOOLEAN, id = "hideScoreboardNumbers")
        @Comment(value = "Hide the red score numbers on the sidebar")
        @JvmField var hideScoreboardNumbers: Boolean = false
        
        // ── Chat Features ─────────────────────
        @ConfigOption.Separator(value = "yourmod.config.skyblock.chat",
            description = "Chat message filtering and enhancements")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "trackSkillXp")
        @Comment(value = "Track skill XP gains from chat messages")
        @JvmField var trackSkillXp: Boolean = true
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "trackRareDrops")
        @Comment(value = "Log rare drops to debug log")
        @JvmField var trackRareDrops: Boolean = false

        @ConfigEntry(type = EntryType.BOOLEAN, id = "trackCoinChanges")
        @JvmField var trackCoinChanges: Boolean = false
        
        // ── Slayer Features ───────────────────
        @ConfigOption.Separator(value = "yourmod.config.skyblock.slayer",
            description = "Slayer quest tracking and alerts")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "slayerTracker")
        @JvmField var slayerTracker: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "slayerBossAlert")
        @Comment(value = "Alert when slayer boss spawns")
        @JvmField var slayerBossAlert: Boolean = false
        
        // ── Tab List Features ─────────────────
        @ConfigOption.Separator(value = "yourmod.config.skyblock.tablist",
            description = "Tab list parsing and display")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "parseTabWidgets")
        @Comment(value = "Parse tab list widget data for HUD use")
        @JvmField var parseTabWidgets: Boolean = true
    }
}
```

### 7.6 Tab: API Settings

```kotlin
@Category(value = "api", translation = "yourmod.config.api")
class ApiCategory {
    companion object {
        // ── Backend Connection ─────────────────
        @ConfigOption.Separator(value = "yourmod.config.api.backend",
            description = "Settings for the backend API proxy")
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "enabled")
        @Comment(value = "Enable fetching data from the backend API")
        @JvmField var enabled: Boolean = false
        
        @ConfigEntry(type = EntryType.STRING, id = "backendUrl")
        @Comment(value = "URL of your backend API server")
        @JvmField var backendUrl: String = "https://api.yourmod.example.com"
        
        // ── Cache Settings ────────────────────
        @ConfigOption.Separator(value = "yourmod.config.api.cache",
            description = "How long to cache API responses")
        
        @ConfigEntry(type = EntryType.INTEGER, id = "profileCacheTtl")
        @ConfigOption.Range(min = 60, max = 3600)
        @Comment(value = "Profile cache TTL in seconds")
        @JvmField var profileCacheTtl: Int = 300
        
        @ConfigEntry(type = EntryType.INTEGER, id = "bazaarCacheTtl")
        @ConfigOption.Range(min = 30, max = 600)
        @Comment(value = "Bazaar price cache TTL in seconds")
        @JvmField var bazaarCacheTtl: Int = 60
    }
}
```

### 7.7 Tab: Debug Settings

```kotlin
@Category(value = "debug", translation = "yourmod.config.debug")
class DebugCategory {
    companion object {
        @ConfigOption.Separator(value = "yourmod.config.debug.logging",
            description = "Developer and troubleshooting options")
        
        @ConfigEntry(type = EntryType.ENUM, id = "logLevel")
        @Comment(value = "Logging verbosity (INFO for normal use, DEBUG for development)")
        @JvmField var logLevel: LogLevel = LogLevel.INFO
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "logScoreboard")
        @Comment(value = "Log raw scoreboard data every update")
        @JvmField var logScoreboard: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "logTabList")
        @Comment(value = "Log raw tab list data every update")
        @JvmField var logTabList: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "logChatMessages")
        @Comment(value = "Log all incoming chat messages")
        @JvmField var logChatMessages: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "logModApiPackets")
        @Comment(value = "Log all Hypixel Mod API packets")
        @JvmField var logModApiPackets: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "logApiResponses")
        @Comment(value = "Log backend API responses")
        @JvmField var logApiResponses: Boolean = false
        
        @ConfigEntry(type = EntryType.BOOLEAN, id = "debugOverlay")
        @Comment(value = "Show debug info overlay in-game (F3-style)")
        @JvmField var debugOverlay: Boolean = false
    }
}
```

### 7.8 Config Registration & Commands

```kotlin
// In YourModClient.onInitializeClient():
ResourcefulConfig.register(ModConfig::class.java)

// Register command to open config screen
ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
    dispatcher.register(
        ClientCommandManager.literal("yourmod")
            .executes { context ->
                MinecraftClient.getInstance().send {
                    MinecraftClient.getInstance().setScreen(
                        ResourcefulConfigScreen.make(null, "yourmod", null)
                    )
                }
                1
            }
    )
}
```

---

## 8. Logging & Debug System

### 8.1 Centralized Logger

```kotlin
object ModLogger {
    private val logger = LoggerFactory.getLogger("YourMod")
    
    enum class LogLevel { ERROR, WARN, INFO, DEBUG, TRACE }
    
    private val currentLevel: LogLevel get() = DebugCategory.logLevel
    
    fun error(category: String, msg: String, throwable: Throwable? = null) {
        logger.error("[$category] $msg", throwable)
    }
    
    fun warn(category: String, msg: String) {
        logger.warn("[$category] $msg")
    }
    
    fun info(category: String, msg: String) {
        if (currentLevel <= LogLevel.INFO) logger.info("[$category] $msg")
    }
    
    fun debug(category: String, msg: String) {
        if (currentLevel <= LogLevel.DEBUG) logger.debug("[$category] $msg")
    }
    
    fun trace(category: String, msg: String) {
        if (currentLevel <= LogLevel.TRACE) logger.trace("[$category] $msg")
    }
    
    // Shortcut: create a scoped logger for a subsystem
    fun category(name: String) = object {
        fun error(msg: String, t: Throwable? = null) = ModLogger.error(name, msg, t)
        fun warn(msg: String) = ModLogger.warn(name, msg)
        fun info(msg: String) = ModLogger.info(name, msg)
        fun debug(msg: String) = ModLogger.debug(name, msg)
        fun trace(msg: String) = ModLogger.trace(name, msg)
    }
}
```

### 8.2 Conditional Verbose Logging

Each parser has a dedicated debug toggle in the Debug config tab:

```kotlin
// In ScoreboardParser
if (DebugCategory.logScoreboard) {
    ModLogger.debug("Scoreboard", "Raw lines: $lines")
    ModLogger.debug("Scoreboard", "Parsed purse=${data.purse} bits=${data.bits} area=${data.location}")
}
```

### 8.3 Debug Overlay (In-Game)

When `debugOverlay` is enabled, render a transparent panel showing:
- Current HypixelState (isOnHypixel, serverType, mode, serverName)
- Scoreboard parse results (purse, bits, area)
- Tab list parse results (skill, pet, stats) 
- Last chat match (pattern name, timestamp)
- Mod API packet count (received this session)
- Backend API status (last response code, cache hits)

---

## 9. Data Collection — SkyBlock Stats

### 9.1 Client-Side Data (Always Available)

These can be collected purely from in-game data with zero API calls:

| Stat | Source | Parser |
|------|--------|--------|
| Current purse/piggy | Scoreboard | `ScoreboardParser` |
| Current bits | Scoreboard | `ScoreboardParser` |
| Current motes | Scoreboard | `ScoreboardParser` |
| Current area/island | Mod API + Scoreboard | `HypixelState` + `ScoreboardParser` |
| Active slayer quest + progress | Scoreboard | `ScoreboardParser` |
| Current objective | Scoreboard | `ScoreboardParser` |
| Active skill being trained | Tab list | `TabListParser` |
| Player stats (HP, Def, etc.) | Tab list | `TabListParser` |
| Active pet + level | Tab list | `TabListParser` |
| Cookie buff remaining | Tab list | `TabListParser` |
| Profile name & type | Tab list | `TabListParser` |
| Skill XP gains (per action) | Chat messages | `ChatParser` |
| Coin gains/losses | Chat messages | `ChatParser` |
| Rare drops | Chat messages | `ChatParser` |
| Slayer boss events | Chat messages | `ChatParser` |
| Dungeon boss messages | Chat messages | `ChatParser` |

### 9.2 Backend-Dependent Data (Requires API)

| Stat | Endpoint | Notes |
|------|----------|-------|
| Skill levels & total XP | `/v2/skyblock/profiles` | From `experience_skill_*` fields |
| Slayer XP & boss kills | `/v2/skyblock/profiles` | Under `slayer.slayer_bosses` |
| Dungeon stats & completions | `/v2/skyblock/profiles` | Under `dungeons` |
| Collection progress | `/v2/skyblock/profiles` | Under `collection` |
| Minion data | `/v2/skyblock/profiles` | Under `crafted_generators` |
| Banking data | `/v2/skyblock/profiles` | Under `banking` (if API enabled) |
| Current mayor & perks | `/v2/resources/skyblock/election` | No key needed |
| Bazaar prices | `/v2/skyblock/bazaar` | No key needed |
| Active fire sales | `/v2/skyblock/firesales` | No key needed |

### 9.3 Logging for Debugging Data Collection

Every data collection point should have debug logging:

```kotlin
// Example: Skill XP tracking from chat
chatParser.register(ChatHandler(
    name = "SkillXP",
    pattern = RegexPatterns.SKILL_XP
) { stripped, original ->
    val match = RegexPatterns.SKILL_XP.find(stripped) ?: return@ChatHandler
    val xpGain = match.groupValues[1].replace(",", "").toDoubleOrNull() ?: return@ChatHandler
    val skillName = match.groupValues[2]
    val current = match.groupValues[3].replace(",", "").toLongOrNull() ?: 0
    val required = match.groupValues[4].replace(",", "").toLongOrNull() ?: 0
    
    ModLogger.debug("SkillXP", "Gained $xpGain $skillName XP ($current/$required)")
    
    if (DebugCategory.logChatMessages) {
        ModLogger.trace("SkillXP", "Raw match: '${match.value}' groups=${match.groupValues}")
    }
    
    SkillTracker.recordXpGain(skillName, xpGain, current, required)
})
```

---

## 10. Warning & Alert System

### 10.1 Unknown Location Warning

```kotlin
object UnknownLocationWarning {
    private var lastWarningTime = 0L
    private val COOLDOWN_MS = 30_000L  // 30s cooldown
    
    fun tick() {
        if (!GeneralCategory.showWarnings) return
        if (!HypixelState.isOnHypixel) return
        
        val now = System.currentTimeMillis()
        
        if (HypixelState.isLocationUnknown && HypixelState.serverType == null) {
            if (now - lastWarningTime > COOLDOWN_MS) {
                sendModMessage("§eLocation detection unavailable. Some features may not work.")
                sendModMessage("§7Ensure Hypixel Mod API is installed correctly.")
                lastWarningTime = now
                ModLogger.warn("Warning", "Unknown location — Mod API may not be responding")
            }
        }
    }
}
```

### 10.2 Other Warning Conditions

| Warning | Condition | Message | Severity |
|---------|-----------|---------|----------|
| Unknown Location | `isOnHypixel && serverType == null` for >5s | Location detection unavailable | WARN |
| Not on SkyBlock | `isOnHypixel && serverType != "SKYBLOCK"` | Features disabled (not in SkyBlock) | INFO |
| Backend API Down | API request fails 3+ times | Backend API unreachable | WARN |
| Mod API Missing | HelloPacket never received after join | Hypixel Mod API not detected | ERROR |
| New/Unknown Island | `mode` not in `SkyBlockIsland` enum | Unknown island type: {mode} — please report | INFO |
| Low Cookie Buff | Cookie timer < 1 hour | Cookie buff expiring soon! | INFO |

---

## 11. Implementation Phases

### Phase 1 — Foundation (Week 1-2)
- [ ] Gradle project setup with all dependencies
- [ ] Kotlin entrypoint, Fabric mod metadata
- [ ] ResourcefulConfig with all 5 category tabs
- [ ] ModLogger centralized logging system
- [ ] HypixelState singleton
- [ ] Mod API integration (Hello + Location packets)
- [ ] SkyBlockIsland enum with all known mode values
- [ ] Basic `/yourmod` command to open config

### Phase 2 — Parsers (Week 3-4)
- [ ] ScoreboardParser (purse, bits, area, slayer, objective)
- [ ] TabListParser (skills, stats, pet, cookie, profile)
- [ ] ChatParser framework + first handlers (Skill XP, coin changes)
- [ ] TextUtils (color stripping, regex patterns)
- [ ] Debug logging for all parsers (toggled via config)

### Phase 3 — HUD (Week 5-6)
- [ ] HudManager + HudElement base class
- [ ] LocationHud (island name + server ID)
- [ ] CoinPurseHud
- [ ] SkillProgressHud
- [ ] HUD rendering with configurable positions
- [ ] HUD scale and background opacity

### Phase 4 — Features & Warnings (Week 7-8)
- [ ] Warning system (unknown location, mod API missing, etc.)
- [ ] Slayer tracker (quest detection, XP tracking)
- [ ] Skill XP rate tracker
- [ ] Rare drop logging
- [ ] Debug overlay
- [ ] Party info integration (Mod API)

### Phase 5 — API Integration (Week 9-10)
- [ ] Backend API client (HTTP, JSON parsing)
- [ ] Profile data fetch and cache
- [ ] Bazaar price display (direct, no key needed)
- [ ] Mayor/election data
- [ ] Profile stats HUD (from API data)
- [ ] Error handling and retry logic

### Phase 6 — Polish & Testing (Week 11-12)
- [ ] Config screen refinement (translations, tooltips)
- [ ] HUD position editor screen
- [ ] Performance profiling (tick time, memory)
- [ ] Edge case handling (limbo, lobby, server restart)
- [ ] README, installation guide, changelog

---

## 12. Appendix — Reference Documentation

### A. Key URLs

| Resource | URL |
|----------|-----|
| Hypixel Mod API (GitHub) | https://github.com/HypixelDev/ModAPI |
| Hypixel Fabric Mod API | https://github.com/HypixelDev/FabricModAPI |
| Hypixel Mod API (Modrinth) | https://modrinth.com/mod/hypixel-mod-api |
| Hypixel Maven Repository | https://repo.hypixel.net/repository/Hypixel/ |
| Hypixel Public API Docs | https://api.hypixel.net/ |
| Hypixel Developer Dashboard | https://developer.hypixel.net/ |
| Hypixel API Policies | https://developer.hypixel.net/policies |
| SkyBlock API Wiki (Fandom) | https://hypixel-skyblock.fandom.com/wiki/SkyBlock_API |
| ResourcefulConfig Wiki | https://config.wiki.teamresourceful.com/ |
| ResourcefulConfig (Modrinth) | https://modrinth.com/mod/resourceful-config |
| Fabric Language Kotlin | https://github.com/FabricMC/fabric-language-kotlin |
| Fabric Wiki — Kotlin Setup | https://wiki.fabricmc.net/tutorial:kotlin |
| Fabric Template Generator | https://fabricmc.net/develop/template/ |
| HM API (Alternative Lib) | https://github.com/AzureAaron/hm-api |
| SkyHanni (Reference Mod) | https://github.com/hannibal002/SkyHanni |
| Tablist Widgets Wiki | https://wiki.hypixel.net/Tablist_Widgets |

### B. Maven Coordinates Reference

```gradle
// Hypixel Mod API (core)
implementation("net.hypixel:mod-api:1.0.1")

// ResourcefulConfig for Fabric 1.21.10
modImplementation("com.teamresourceful:resourcefulconfig-fabric-1.21.10:3.9.1")

// ResourcefulConfig for Fabric 1.21.11  
modImplementation("com.teamresourceful:resourcefulconfig-fabric-1.21.11:3.11.2")

// Fabric Language Kotlin (1.21.10)
modImplementation("net.fabricmc:fabric-language-kotlin:1.13.6+kotlin.2.2.20")

// Fabric Language Kotlin (1.21.11)
modImplementation("net.fabricmc:fabric-language-kotlin:1.13.9+kotlin.2.3.10")

// HM API (alternative Mod API wrapper for Fabric)
include(modImplementation("net.azureaaron:hm-api:<latest>"))
```

### C. Hypixel Mod API — Packet Reference Summary

```
Clientbound (Server → Client):
├── ClientboundHelloPacket          — auto on join, contains Environment
├── ClientboundLocationPacket       — event (subscribe required), contains:
│   ├── serverName: String
│   ├── serverType: Optional<ServerType>   (SKYBLOCK, BEDWARS, etc.)
│   ├── lobbyName: Optional<String>
│   ├── mode: Optional<String>             (hub, mining_3, dungeon, etc.)
│   └── map: Optional<String>
├── ClientboundPartyInfoPacket      — response to request, contains:
│   ├── inParty: Boolean
│   ├── leader: Optional<UUID>
│   └── members: Map<UUID, PartyRole>
└── ClientboundPlayerInfoPacket     — response to request, contains:
    ├── rank: Optional<String>
    ├── packageRank: Optional<String>
    ├── monthlyPackageRank: Optional<String>
    └── prefix: Optional<String>

Serverbound (Client → Server):
├── ServerboundPartyInfoPacket      — triggers ClientboundPartyInfoPacket
└── ServerboundPlayerInfoPacket     — triggers ClientboundPlayerInfoPacket
```

### D. SkyBlock Scoreboard Line Examples (Raw)

```
§707/14/25 §8m42CK
§7♲ §7Late Spring 14th
§e§lSkyBlock §fHub
⏣ §bVillage
§7§8Purse: §61,234,567
§7§8Bits: §b5,432
§cSlayer Quest
§e  Revenant Horror III
§7  (2,450/10,000) Kills
§ewww.hypixel.net
```

### E. Hypixel Public API — SkyBlock Endpoints Quick Reference

```
No API Key Required:
  GET /v2/resources/skyblock/collections
  GET /v2/resources/skyblock/skills  
  GET /v2/resources/skyblock/items
  GET /v2/resources/skyblock/election
  GET /v2/resources/skyblock/bingo
  GET /v2/skyblock/bazaar
  GET /v2/skyblock/auctions?page={n}
  GET /v2/skyblock/auctions_ended
  GET /v2/skyblock/firesales
  GET /v2/skyblock/news

API Key Required (Header: API-Key):
  GET /v2/skyblock/profiles?uuid={UUID}          → All profiles
  GET /v2/skyblock/profile?profile={PROFILE_ID}  → Single profile
  GET /v2/skyblock/bingo?uuid={UUID}             → Bingo progress
  GET /v2/skyblock/museum?profile={PROFILE_ID}   → Museum data
  GET /v2/skyblock/garden?profile={PROFILE_ID}   → Garden data
  GET /v2/player?uuid={UUID}                     → Full player data
  GET /v2/status?uuid={UUID}                     → Online status
  GET /v2/recentgames?uuid={UUID}                → Recent games

Rate Limits: 300 req / 5 min (default)
Response Headers: RateLimit-Limit, RateLimit-Remaining, RateLimit-Reset
```

---

*End of Planning Document*
