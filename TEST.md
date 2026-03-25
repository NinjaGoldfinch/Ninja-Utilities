# Testing Guide — Ninja Utilities

## Running Tests

### IntelliJ
Use the **Run Tests** run configuration from the run dropdown. This runs `./gradlew test --info` and shows results in the test runner panel.

### Command Line
```bash
./gradlew test
```

## Dev Commands

All dev commands are accessed via `/ninja_utils dev <subcommand>` in-game.

### Inspection Commands

| Command | Description |
|---------|-------------|
| `/ninja_utils dev` | Show all state (HypixelState + scoreboard + HUD config) |
| `/ninja_utils dev state` | Hypixel connection state, island, packets |
| `/ninja_utils dev scoreboard` | Raw scoreboard data and parsed values |
| `/ninja_utils dev tablist` | Raw tab list entries |
| `/ninja_utils dev hud` | HUD config toggles and render status |
| `/ninja_utils dev stats` | SkyBlock stats, skill XP rates, tab list data |
| `/ninja_utils dev slayer` | Slayer tracker state (quest, boss, completions, timing) |
| `/ninja_utils dev party` | Party info from Mod API |
| `/ninja_utils dev api` | API cache status, backend config, error counts |
| `/ninja_utils dev mayor` | Fetch and display full mayor/minister data with perks |
| `/ninja_utils dev perf` | Performance metrics (tick/render time, memory) |
| `/ninja_utils dev ping` | Current ping, average ping, sample history |
| `/ninja_utils dev skills` | Skill XP checker — compares tracked gains vs menu baseline |
| `/ninja_utils dev item` | Dump held item's SkyBlock ID and full ExtraAttributes NBT |
| `/ninja_utils dev search` | Show held item's SkyBlock ID and attributes |
| `/ninja_utils dev search <id>` | Search inventory + open container for items by Hypixel item ID |
| `/ninja_utils dev items` | Item gain tracker — rates and totals per item |
| `/ninja_utils dev colors` | Show raw formatting codes for scoreboard, tab list, and chat |
| `/ninja_utils dev colors chat` | Chat formatting codes only |
| `/ninja_utils dev colors scoreboard` | Scoreboard formatting codes only |
| `/ninja_utils dev colors tablist` | Tab list formatting codes only |

### Simulation Commands

These commands inject fake data to test HUD elements and trackers without needing to trigger real in-game events.

| Command | Description |
|---------|-------------|
| `/ninja_utils dev simulate skill` | Simulate a Mining XP gain (+1,250 XP). Triggers SkillProgressHud and SkillTracker. Run multiple times quickly to see XP/min rate build up. |
| `/ninja_utils dev simulate slayer` | Simulate full slayer cycle: quest start → boss spawn (2s) → boss slain (4s). Watch SlayerHud and chat alerts. |
| `/ninja_utils dev simulate ping` | Inject 10 fake ping samples (80-200ms) to populate average ping display. |
| `/ninja_utils dev simulate coins` | Fire a CoinChangeEvent (+50,000 coins from "Bazaar Sale"). |
| `/ninja_utils dev simulate drop` | Fire a RareDropEvent ("Wither Chestplate"). |
| `/ninja_utils dev simulate item` | Fire 2 fake ItemGainEvents (64x Enchanted Diamond from inventory, 128x Wheat from sack). |
| `/ninja_utils dev simulate reset` | Reset all trackers (ping, TPS, slayer, skill, items) to initial state. |

## Testing Individual Features

### Ping HUD
1. Enable: Config screen → HUD → `showPing = true`
2. Join any Hypixel server and wait ~5 seconds for first ping reading
3. The HUD shows current ping (color-coded) with average in grey brackets
4. Use `/ninja_utils dev ping` to inspect current values and history
5. Use `/ninja_utils dev simulate ping` to test with fake data

### Item Gain Tracking
1. Enable: Config screen → SkyBlock → `trackItemGains = true`
2. Enable: Config screen → HUD → `showItemGains = true`
3. Use `/ninja_utils dev simulate item` to test with fake data — HUD should show 2 items for 10 seconds
4. Use `/ninja_utils dev items` to see tracked item rates
5. For inventory tracking: enable `trackInventoryGains`, pick up items from ground or mob drops — gains appear on HUD
6. For sack tracking: enable `trackSackGains`, add items to a sack — chat messages trigger gain events
7. Open a container and kill a mob (or have lootshare) — inventory gains should still be tracked
8. Switch islands — verify no false positives from inventory reset
9. Use `/ninja_utils dev simulate reset` to clear all trackers

### Item Detection & Search
1. Hold any SkyBlock item and run `/ninja_utils dev search` to see its Hypixel item ID
2. Run `/ninja_utils dev item` to see full ExtraAttributes NBT data for the held item
3. Run `/ninja_utils dev search ASPECT_OF_THE_DRAGONS` (or any item ID) to find it in your inventory
4. Open a container (chest, ender chest, backpack) and run the search again — it searches both inventory and the open container
5. Vanilla items (no SkyBlock ID) will show "no SkyBlock ID" when inspected

### TPS HUD
1. Enable: Config screen → HUD → `showTps = true`
2. Join any Hypixel server and wait ~15 seconds (5s ignore + discard + samples)
3. Color thresholds: green ≥19, yellow ≥15, orange ≥10, red <10
4. Use `/ninja_utils dev perf` to check tick timing

### Skill Progress HUD
1. Enable: Config screen → HUD → `showSkillProgress = true`
2. Enable: Config screen → SkyBlock → `trackSkillXp = true`
3. Gain any skill XP in SkyBlock (mine, farm, fish, etc.)
4. HUD shows for 10 seconds after each gain with progress bar and +XP
5. Use `/ninja_utils dev simulate skill` to test without grinding
6. Run it several times rapidly to see XP/min rate in `/ninja_utils dev stats`

### Slayer Tracker
1. Enable: Config screen → SkyBlock → `slayerTracker = true`
2. Optionally enable `slayerBossAlert = true` for chat alerts
3. Start a slayer quest in SkyBlock
4. HUD shows quest name and [BOSS] indicator when spawned
5. Use `/ninja_utils dev simulate slayer` to test the full lifecycle
6. Use `/ninja_utils dev slayer` to inspect tracker state

### Skill XP Checker
1. Enable skill tracking: Config → SkyBlock → `trackSkillXp = true`
2. Open `/skills` in SkyBlock — the mod automatically reads XP values from the menu
3. Grind some XP (mining, farming, etc.)
4. Run `/ninja_utils dev skills` to compare tracked gains against the menu baseline
5. Open `/skills` again to refresh the baseline and see updated values
6. Use `/ninja_utils dev simulate skill` to test without real XP gains

### Location HUD
1. Enable: Config screen → HUD → `showLocation = true` (default)
2. Move between areas — scoreboard parser updates current area
3. In Garden, also shows current plot number
4. Use `/ninja_utils dev state` to verify island/area detection

### Coin Purse HUD
1. Enable: Config screen → HUD → `showPurse = true` (default)
2. Parsed from scoreboard "Purse:" or "Piggy:" line
3. Use `/ninja_utils dev scoreboard` to verify parsing

### Bits HUD
1. Enable: Config screen → HUD → `showBits = true`
2. Parsed from scoreboard "Bits:" line
3. Only visible when bits > 0

### Mayor HUD
1. Enable: Config screen → HUD → `showMayor = true`
2. Fetches from Hypixel public API (no key needed), cached for 1 hour
3. Use `/ninja_utils dev mayor` to see full perk details

### Pet HUD
1. Enable: Config screen → HUD → `showPet = true`
2. Parsed from tab list "Pet:" line
3. Use `/ninja_utils dev tablist` to verify parsing

### Cookie Timer HUD
1. Enable: Config screen → HUD → `showCookieTimer = true`
2. Parsed from tab list "Cookie Buff:" line
3. Only shows when cookie buff is active

### SkyBlock Time HUD
1. Enable: Config screen → HUD → `showSbTime = true`
2. Parsed from scoreboard date line (Early/Late Month Day)

### Garden HUDs (Copper, Sowdust, Pests)
1. Enable respective toggles in Config → HUD
2. Only visible when on Garden island
3. Pests HUD hides when pest count is 0
4. Use `/ninja_utils dev state` to verify garden state

### Scoreboard Cleaning
1. Enable: Config screen → SkyBlock → `cleanScoreboard = true`
2. Removes formatting artifacts from scoreboard display
3. `hideScoreboardNumbers = true` removes the red score numbers

### HUD Positioning
1. Run `/ninja_utils hud` to open the config screen
2. Left-click drag to reposition elements
3. Right-click lines to toggle visibility
4. Hold Shift to disable edge snapping
5. Positions persist across restarts

### Debug Overlay
1. Enable: Config screen → Debug → `debugOverlay = true`
2. Shows 23+ debug lines in a separate panel
3. Use `/ninja_utils dev` commands to cross-reference values

## Verifying Parsers

### Chat Parser
1. Enable tracking options in Config → SkyBlock (`trackSkillXp`, `trackRareDrops`, `trackCoinChanges`)
2. Use `/ninja_utils dev colors chat` to see raw formatting codes of recent messages
3. Check that patterns match by triggering in-game events and inspecting `/ninja_utils dev stats`

### Scoreboard Parser
1. Use `/ninja_utils dev scoreboard` to see all parsed fields
2. Use `/ninja_utils dev colors scoreboard` to see raw formatting codes
3. Compare parsed values against actual scoreboard display

### Tab List Parser
1. Enable: Config screen → SkyBlock → `parseTabWidgets = true` (default)
2. Use `/ninja_utils dev tablist` to see raw entries
3. Use `/ninja_utils dev colors tablist` to see formatting codes
4. Use `/ninja_utils dev stats` to see parsed tab data (skills, profile, pet, cookie, stats)

## API Testing

### Public API (no key needed)
1. Use `/ninja_utils dev api` to check cache status and error counts
2. Use `/ninja_utils dev mayor` to test election endpoint

### Backend API (optional)
1. Set `backendUrl` in Config → API
2. Enable: Config screen → API → `enabled = true`
3. Use `/ninja_utils dev api` to verify connection
