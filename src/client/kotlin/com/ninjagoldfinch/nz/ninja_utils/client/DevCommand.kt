package com.ninjagoldfinch.nz.ninja_utils.client

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.ninjagoldfinch.nz.ninja_utils.api.modapi.ModApiManager
import com.ninjagoldfinch.nz.ninja_utils.api.publicapi.BackendClient
import com.ninjagoldfinch.nz.ninja_utils.api.publicapi.HypixelApiClient
import com.ninjagoldfinch.nz.ninja_utils.config.ApiCategory
import com.ninjagoldfinch.nz.ninja_utils.config.GeneralCategory
import com.ninjagoldfinch.nz.ninja_utils.config.HudCategory
import com.ninjagoldfinch.nz.ninja_utils.core.CoinChangeEvent
import com.ninjagoldfinch.nz.ninja_utils.core.EventBus
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.core.RareDropEvent
import com.ninjagoldfinch.nz.ninja_utils.features.stats.PingTracker
import com.ninjagoldfinch.nz.ninja_utils.util.SkyBlockItemUtils
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SkillTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SkillXpChecker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.TPSTracker
import com.ninjagoldfinch.nz.ninja_utils.logging.PerformanceMonitor
import com.ninjagoldfinch.nz.ninja_utils.hud.elements.SkillProgressHud
import com.ninjagoldfinch.nz.ninja_utils.parsers.ChatParser
import com.ninjagoldfinch.nz.ninja_utils.parsers.ScoreboardParser
import com.ninjagoldfinch.nz.ninja_utils.parsers.TabListParser
import java.text.NumberFormat
import java.util.Locale
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource
import net.minecraft.client.MinecraftClient
import net.minecraft.scoreboard.ScoreboardDisplaySlot
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Util

object DevCommand {

    fun buildDevSubtree(): LiteralArgumentBuilder<FabricClientCommandSource> {
        return literal("dev").apply {
            // /ninja_utils dev — show all state
            executes { ctx ->
                sendAll(ctx.source)
                1
            }

            then(literal("state").executes { ctx ->
                sendState(ctx.source)
                1
            })

            then(literal("scoreboard").executes { ctx ->
                sendScoreboard(ctx.source)
                1
            })

            then(literal("hud").executes { ctx ->
                sendHud(ctx.source)
                1
            })

            then(literal("stats").executes { ctx ->
                sendStats(ctx.source)
                1
            })

            then(literal("tablist").executes { ctx ->
                sendTabList(ctx.source)
                1
            })

            then(literal("toggle").executes { ctx ->
                GeneralCategory.skyblockOnly = !GeneralCategory.skyblockOnly
                msg(ctx.source, "skyblockOnly = ${GeneralCategory.skyblockOnly}", Formatting.YELLOW)
                1
            })

            then(literal("slayer").executes { ctx ->
                sendSlayer(ctx.source)
                1
            })

            then(literal("party").executes { ctx ->
                ModApiManager.requestPartyInfo()
                msg(ctx.source, "Requested party info...", Formatting.YELLOW)
                sendParty(ctx.source)
                1
            })

            then(literal("api").executes { ctx ->
                sendApi(ctx.source)
                1
            })

            then(literal("mayor").executes { ctx ->
                msg(ctx.source, "Fetching election data...", Formatting.YELLOW)
                HypixelApiClient.fetchElection().thenAccept { response ->
                    if (response?.mayor != null) {
                        val mayor = response.mayor
                        header(ctx.source, "Current Mayor")
                        msg(ctx.source, "name: ${mayor.name}")
                        msg(ctx.source, "key: ${mayor.key}")
                        mayor.perks?.forEach { perk ->
                            msg(ctx.source, "  perk: ${perk.name}", Formatting.AQUA)
                            perk.description?.let { msg(ctx.source, "    $it", Formatting.GRAY) }
                        }
                        val minister = mayor.minister
                        if (minister != null) {
                            header(ctx.source, "Minister")
                            msg(ctx.source, "name: ${minister.name}")
                            msg(ctx.source, "key: ${minister.key}")
                            minister.perk?.let { perk ->
                                msg(ctx.source, "  perk: ${perk.name}", Formatting.AQUA)
                                perk.description?.let { msg(ctx.source, "    $it", Formatting.GRAY) }
                            }
                        }
                    } else {
                        msg(ctx.source, "Failed to fetch election data", Formatting.RED)
                    }
                }
                1
            })

            then(literal("perf").executes { ctx ->
                sendPerf(ctx.source)
                1
            })

            then(literal("ping").executes { ctx ->
                sendPing(ctx.source)
                1
            })

            then(literal("skills").executes { ctx ->
                sendSkillChecker(ctx.source)
                1
            })

            then(literal("item").executes { ctx ->
                sendItemInfo(ctx.source)
                1
            })

            then(SearchCommand.buildSearchSubtree())

            then(literal("colors").apply {
                executes { ctx ->
                    sendColorsAll(ctx.source)
                    1
                }
                then(literal("chat").executes { ctx ->
                    sendColorChat(ctx.source)
                    1
                })
                then(literal("scoreboard").executes { ctx ->
                    sendColorScoreboard(ctx.source)
                    1
                })
                then(literal("tablist").executes { ctx ->
                    sendColorTabList(ctx.source)
                    1
                })
            })

            then(literal("simulate").apply {
                then(literal("skill").executes { ctx ->
                    simulateSkillXp(ctx.source)
                    1
                })
                then(literal("slayer").executes { ctx ->
                    simulateSlayer(ctx.source)
                    1
                })
                then(literal("ping").executes { ctx ->
                    simulatePing(ctx.source)
                    1
                })
                then(literal("coins").executes { ctx ->
                    simulateCoins(ctx.source)
                    1
                })
                then(literal("drop").executes { ctx ->
                    simulateDrop(ctx.source)
                    1
                })
                then(literal("reset").executes { ctx ->
                    simulateReset(ctx.source)
                    1
                })
            })

        }
    }

    private fun sendAll(source: FabricClientCommandSource) {
        header(source, "Ninja Utilities Dev Info")
        sendState(source)
        sendScoreboard(source)
        sendHud(source)
    }

    private fun sendState(source: FabricClientCommandSource) {
        header(source, "HypixelState")
        val s = HypixelState
        msg(source, "isOnHypixel: ${colored(s.isOnHypixel)}")
        msg(source, "environment: ${s.environment ?: "null"}")
        msg(source, "serverType: ${s.serverType ?: "null"}")
        msg(source, "serverName: ${s.serverName ?: "null"}")
        msg(source, "mode: ${s.mode ?: "null"}")
        msg(source, "map: ${s.map ?: "null"}")
        msg(source, "lobbyName: ${s.lobbyName ?: "null"}")
        msg(source, "isInSkyBlock: ${colored(s.isInSkyBlock)}")
        msg(source, "currentIsland: ${s.currentIsland?.displayName ?: "null"}")
        msg(source, "currentArea: ${s.currentArea ?: "null"}")
        msg(source, "purse: ${s.purse}")
        msg(source, "bits: ${s.bits}")
        msg(source, "skyblockDetectedViaScoreboard: ${colored(s.skyblockDetectedViaScoreboard)}")

        val helloAgo = if (s.lastHelloPacketTime > 0) "${(System.currentTimeMillis() - s.lastHelloPacketTime) / 1000}s ago" else "never"
        val locAgo = if (s.lastLocationPacketTime > 0) "${(System.currentTimeMillis() - s.lastLocationPacketTime) / 1000}s ago" else "never"
        msg(source, "lastHelloPacket: $helloAgo")
        msg(source, "lastLocationPacket: $locAgo")

        header(source, "Raw Packets")
        msg(source, "Hello: ${ModApiManager.lastHelloRaw ?: "none received"}")
        msg(source, "Location: ${ModApiManager.lastLocationRaw ?: "none received"}")
    }

    private fun sendScoreboard(source: FabricClientCommandSource) {
        header(source, "Scoreboard")
        val data = ScoreboardParser.parse()
        if (data == null) {
            msg(source, "No scoreboard data available", Formatting.RED)
            return
        }
        msg(source, "title: ${data.title}")
        msg(source, "purse: ${data.purse ?: "null"}")
        msg(source, "bits: ${data.bits ?: "null"}")
        msg(source, "location: ${data.location ?: "null"}")
        msg(source, "slayerQuest: ${data.slayerQuest ?: "null"}")
        msg(source, "objective: ${data.objective ?: "null"}")
        msg(source, "rawLines (${data.rawLines.size}):")
        data.rawLines.forEachIndexed { i, line ->
            msg(source, "  [$i] $line", Formatting.GRAY)
        }
    }

    private fun sendHud(source: FabricClientCommandSource) {
        header(source, "HUD Config")
        msg(source, "general.enabled: ${colored(GeneralCategory.enabled)}")
        msg(source, "general.skyblockOnly: ${colored(GeneralCategory.skyblockOnly)}")
        msg(source, "hud.showLocation: ${colored(HudCategory.showLocation)}")
        msg(source, "hud.showPurse: ${colored(HudCategory.showPurse)}")
        msg(source, "hud.showSkillProgress: ${colored(HudCategory.showSkillProgress)}")
        msg(source, "hudScale: ${HudCategory.hudScale}")
        msg(source, "hudBgOpacity: ${HudCategory.hudBackgroundOpacity}")

        val shouldRender = !GeneralCategory.skyblockOnly || HypixelState.isInSkyBlock
        msg(source, "HUD will render: ${colored(shouldRender)}")
        msg(source, "Use \u00a7e/ninja_utils hud\u00a7r to reposition HUD elements")
    }

    private fun sendStats(source: FabricClientCommandSource) {
        val fmt = NumberFormat.getNumberInstance(Locale.US)
        val s = HypixelState

        header(source, "SkyBlock Stats")
        msg(source, "isInSkyBlock: ${colored(s.isInSkyBlock)}")
        msg(source, "island: ${s.currentIsland?.displayName ?: "null"} (mode=${s.mode ?: "null"})")
        msg(source, "area: ${s.currentArea ?: "null"}")

        header(source, "Scoreboard Stats")
        val sb = ScoreboardParser.parse()
        msg(source, "purse: ${sb?.purse?.let { fmt.format(it) } ?: "null"} (HypixelState: ${fmt.format(s.purse)})")
        msg(source, "bits: ${sb?.bits?.let { fmt.format(it) } ?: "null"} (HypixelState: ${fmt.format(s.bits)})")
        msg(source, "location: ${sb?.location ?: "null"}")
        msg(source, "slayerQuest: ${sb?.slayerQuest ?: "null"}")
        msg(source, "objective: ${sb?.objective ?: "null"}")

        header(source, "Tab List Stats")
        val tab = TabListParser.parse()
        if (tab != null) {
            msg(source, "skills: ${tab.skills ?: "null"}")
            msg(source, "profile: ${tab.profile ?: "null"}")
            msg(source, "pet: ${tab.pet ?: "null"}")
            msg(source, "cookie: ${tab.cookie ?: "null"}")
            if (tab.stats.isNotEmpty()) {
                msg(source, "stats (${tab.stats.size}):")
                tab.stats.forEach { msg(source, "  $it", Formatting.GRAY) }
            } else {
                msg(source, "stats: none parsed")
            }
        } else {
            msg(source, "No tab list data available", Formatting.RED)
        }

        header(source, "Skill XP Tracker")
        val skill = SkillProgressHud.currentSkill
        if (skill != null) {
            val elapsed = (System.currentTimeMillis() - SkillProgressHud.lastUpdateTime) / 1000
            msg(source, "skill: $skill")
            msg(source, "lastXpGain: +${String.format("%.1f", SkillProgressHud.lastXpGain)}")
            msg(source, "progress: ${fmt.format(SkillProgressHud.currentXp)}/${fmt.format(SkillProgressHud.requiredXp)}")
            msg(source, "lastUpdate: ${elapsed}s ago")

            val rate = SkillTracker.getRate(skill)
            if (rate != null) {
                msg(source, "xpPerMinute: ${String.format("%.0f", rate.xpPerMinute)}")
                msg(source, "totalGained (1min window): ${String.format("%.0f", rate.totalGained)}")
            }
        } else {
            msg(source, "No skill XP recorded yet", Formatting.GRAY)
        }

        val allRates = SkillTracker.getAllRates()
        if (allRates.isNotEmpty()) {
            header(source, "All Skill Rates (XP/min)")
            allRates.forEach { (name, rate) ->
                msg(source, "$name: ${String.format("%.0f", rate.xpPerMinute)} XP/min (${rate.entryCount} gains)")
            }
        }
    }

    private fun sendSlayer(source: FabricClientCommandSource) {
        header(source, "Slayer Tracker")
        msg(source, "activeQuest: ${SlayerTracker.activeQuest ?: "none"}")
        msg(source, "bossSpawned: ${colored(SlayerTracker.bossSpawned)}")
        msg(source, "completionsThisSession: ${SlayerTracker.completionsThisSession}")
        if (SlayerTracker.questStartTime > 0) {
            val elapsed = (System.currentTimeMillis() - SlayerTracker.questStartTime) / 1000
            msg(source, "questStarted: ${elapsed}s ago")
        }
        if (SlayerTracker.lastCompletionTime > 0) {
            val elapsed = (System.currentTimeMillis() - SlayerTracker.lastCompletionTime) / 1000
            msg(source, "lastCompletion: ${elapsed}s ago")
        }
    }

    private fun sendParty(source: FabricClientCommandSource) {
        header(source, "Party Info")
        val s = HypixelState
        msg(source, "partyLeader: ${s.partyLeader ?: "none"}")
        if (s.partyMembers.isNotEmpty()) {
            msg(source, "members (${s.partyMembers.size}):")
            s.partyMembers.forEach { msg(source, "  $it", Formatting.GRAY) }
        } else {
            msg(source, "members: none")
        }
        msg(source, "playerRank: ${s.playerRank ?: "null"}")
        msg(source, "Raw party: ${ModApiManager.lastPartyRaw ?: "none received"}")
        msg(source, "Raw playerInfo: ${ModApiManager.lastPlayerInfoRaw ?: "none received"}")
    }

    private fun sendPerf(source: FabricClientCommandSource) {
        val perf = PerformanceMonitor
        header(source, "Performance")
        msg(source, "avgTickTime: ${String.format("%.1f", perf.avgTickTimeUs)} \u00b5s")
        msg(source, "maxTickTime: ${String.format("%.1f", perf.maxTickTimeUs)} \u00b5s")
        msg(source, "avgRenderTime: ${String.format("%.1f", perf.avgRenderTimeUs)} \u00b5s")

        header(source, "Memory")
        msg(source, "used: ${perf.usedMemoryMb} MB")
        msg(source, "max: ${perf.maxMemoryMb} MB")
        msg(source, "usage: ${perf.memoryPercent}%")

        val runtime = Runtime.getRuntime()
        msg(source, "totalAllocated: ${runtime.totalMemory() / (1024 * 1024)} MB")
        msg(source, "processors: ${runtime.availableProcessors()}")
    }

    private fun sendApi(source: FabricClientCommandSource) {
        header(source, "API Status")
        msg(source, "backend.enabled: ${colored(ApiCategory.enabled)}")
        msg(source, "backend.url: ${ApiCategory.backendUrl.ifBlank { "(not set)" }}")
        msg(source, "backend.errors: ${BackendClient.consecutiveErrors}")
        msg(source, "profileCacheTtl: ${ApiCategory.profileCacheTtl}s")
        msg(source, "bazaarCacheTtl: ${ApiCategory.bazaarCacheTtl}s")

        header(source, "Public API Cache")
        msg(source, "election: ${if (HypixelApiClient.electionCache.isFresh) "\u00a7acached" else "\u00a77stale/empty"}")
        msg(source, "bazaar: ${if (HypixelApiClient.bazaarCache.isFresh) "\u00a7acached" else "\u00a77stale/empty"}")
        msg(source, "publicApi.errors: ${HypixelApiClient.consecutiveErrors}")

        val election = HypixelApiClient.electionCache.get()
        if (election?.mayor != null) {
            msg(source, "mayor: ${election.mayor.name} (${election.mayor.perks?.size ?: 0} perks)")
        }
    }

    private fun sendTabList(source: FabricClientCommandSource) {
        header(source, "Tab List Raw")
        val tab = TabListParser.parse()
        if (tab == null) {
            msg(source, "No tab list data available", Formatting.RED)
            return
        }
        msg(source, "Total entries: ${tab.rawLines.size}")
        tab.rawLines.forEachIndexed { i, line ->
            msg(source, "  [$i] $line", Formatting.GRAY)
        }
    }

    // ── Color formatting inspection ──────────────────────────────────────

    /** Replace § with & so the formatting codes are visible in chat instead of being rendered. */
    private fun escapeFormatting(text: String): String =
        text.replace("\u00a7", "&")

    private fun sendColorsAll(source: FabricClientCommandSource) {
        sendColorScoreboard(source)
        sendColorTabList(source)
        sendColorChat(source)
    }

    private fun sendColorScoreboard(source: FabricClientCommandSource) {
        header(source, "Scoreboard Colors")
        val scoreboard = MinecraftClient.getInstance().world?.scoreboard
        val objective = scoreboard?.getObjectiveForSlot(ScoreboardDisplaySlot.SIDEBAR)
        if (scoreboard == null || objective == null) {
            msg(source, "No scoreboard data available", Formatting.RED)
            return
        }

        msg(source, "title: ${escapeFormatting(objective.displayName.string)}", Formatting.GRAY)
        val entries = scoreboard.getScoreboardEntries(objective)
            .sortedByDescending { it.value }
        entries.forEachIndexed { i, entry ->
            val raw = entry.display?.string
                ?: scoreboard.getScoreHolderTeam(entry.owner)
                    ?.decorateName(Text.literal(entry.owner))?.string
                ?: entry.owner
            msg(source, "  [$i] ${escapeFormatting(raw)}", Formatting.GRAY)
        }
    }

    private fun sendColorTabList(source: FabricClientCommandSource) {
        header(source, "Tab List Colors")
        val networkHandler = MinecraftClient.getInstance().networkHandler
        if (networkHandler == null) {
            msg(source, "No tab list data available", Formatting.RED)
            return
        }
        val entries = networkHandler.playerList.sortedBy { it.listOrder }
        val lines = entries.mapNotNull { it.displayName?.string }
            .filter { it.isNotBlank() }
        msg(source, "Total entries: ${lines.size}")
        lines.forEachIndexed { i, line ->
            msg(source, "  [$i] ${escapeFormatting(line)}", Formatting.GRAY)
        }
    }

    private fun sendColorChat(source: FabricClientCommandSource) {
        header(source, "Recent Chat Colors")
        val messages = ChatParser.getRecentFormatted()
        if (messages.isEmpty()) {
            msg(source, "No recent chat messages", Formatting.RED)
            return
        }
        msg(source, "Last ${messages.size} messages:")
        messages.forEachIndexed { i, line ->
            msg(source, "  [$i] ${escapeFormatting(line)}", Formatting.GRAY)
        }
    }

    // ── Ping inspection ────────────────────────────────────────────────

    private fun sendPing(source: FabricClientCommandSource) {
        header(source, "Ping Tracker")
        val ping = PingTracker.ping
        val display = PingTracker.displayPing
        val avg = PingTracker.averagePing
        msg(source, "latest: ${if (ping > 0) "${ping}ms" else "no data"}")
        msg(source, "display: ${if (display > 0) "${display}ms" else "no data"} (updates every 2.5s or on spike)")
        msg(source, "average: ${if (avg > 0) "${avg}ms" else "no data"}")
    }

    // ── Item inspection ────────────────────────────────────────────────

    private fun sendItemInfo(source: FabricClientCommandSource) {
        val player = MinecraftClient.getInstance().player
        if (player == null) {
            msg(source, "Not in-game", Formatting.RED)
            return
        }
        header(source, "Held Item Debug")
        val stack = player.mainHandStack
        if (stack.isEmpty) {
            msg(source, "Not holding an item", Formatting.GRAY)
            return
        }
        msg(source, "name: ${stack.name.string}")
        msg(source, "mcId: ${stack.item}", Formatting.GRAY)

        val itemId = SkyBlockItemUtils.getItemId(stack)
        msg(source, "skyblockId: ${itemId ?: "none"}")

        // Dump all components on the item
        msg(source, "Components:", Formatting.GRAY)
        for (component in stack.components) {
            val type = component.type()
            val value = component.value()
            msg(source, "  ${type}: ${value?.javaClass?.simpleName}", Formatting.DARK_GRAY)
        }

        // Dump custom data NBT
        val customNbt = SkyBlockItemUtils.getCustomData(stack)
        if (customNbt != null) {
            msg(source, "CustomData keys: ${customNbt.keys.joinToString(", ")}", Formatting.GRAY)
            msg(source, "CustomData raw: $customNbt", Formatting.DARK_GRAY)
        } else {
            msg(source, "No custom data", Formatting.GRAY)
        }
    }

    // ── Skill XP checker ──────────────────────────────────────────────────

    private fun sendSkillChecker(source: FabricClientCommandSource) {
        val fmt = NumberFormat.getNumberInstance(Locale.US)
        val lastRead = SkillXpChecker.lastMenuReadTime
        if (lastRead == 0L) {
            header(source, "Skill XP Checker")
            msg(source, "No skill menu data yet. Open /skills in-game to capture XP values.", Formatting.GRAY)

            // Still show tracked gains if any
            val allRates = SkillTracker.getAllRates()
            if (allRates.isNotEmpty()) {
                header(source, "Tracked Gains (no menu baseline)")
                allRates.forEach { (skill, rate) ->
                    msg(source, "$skill: +${fmt.format(rate.totalGained.toLong())} XP (${String.format("%.0f", rate.xpPerMinute)} XP/min)")
                }
            }
            return
        }

        val ageSeconds = (System.currentTimeMillis() - lastRead) / 1000
        header(source, "Skill XP Checker (menu read ${ageSeconds}s ago)")

        val comparisons = SkillXpChecker.compare()
        if (comparisons.isEmpty()) {
            msg(source, "No data to compare", Formatting.GRAY)
            return
        }

        for (comp in comparisons) {
            val menuStr = if (comp.menuXp != null) {
                "${fmt.format(comp.menuXp)}/${fmt.format(comp.menuRequired)} (${String.format("%.1f", comp.menuPercent)}%)"
            } else {
                "not in menu"
            }
            val gainStr = if (comp.trackedGain > 0) {
                "+${fmt.format(comp.trackedGain.toLong())} (${String.format("%.0f", comp.trackedRate)} XP/min)"
            } else {
                "no gains tracked"
            }

            msg(source, "\u00a7e${comp.skill}:", Formatting.YELLOW)
            msg(source, "  Menu: $menuStr", Formatting.GRAY)
            msg(source, "  Tracked: $gainStr", Formatting.GRAY)
        }

        msg(source, "\u00a77Tip: Open /skills again to refresh the baseline", Formatting.GRAY)
    }

    // ── Simulation commands ──────────────────────────────────────────────

    private fun simulateSkillXp(source: FabricClientCommandSource) {
        val skill = "Mining"
        val xpGain = 1250.0
        val current = 84_350L
        val required = 100_000L

        SkillTracker.recordXpGain(skill, xpGain)
        SkillProgressHud.recordXpGain(skill, xpGain, current, required)
        msg(source, "Simulated +${xpGain.toLong()} $skill XP ($current/$required)", Formatting.GREEN)
        msg(source, "Run multiple times to build up XP/min rate", Formatting.GRAY)
    }

    private fun simulateSlayer(source: FabricClientCommandSource) {
        msg(source, "Simulating slayer lifecycle...", Formatting.YELLOW)

        SlayerTracker.suppressScoreboardUpdates = true
        SlayerTracker.updateFromScoreboard("Revenant Horror IV")
        SlayerTracker.onQuestStarted()
        msg(source, "Quest started: Revenant Horror IV", Formatting.GREEN)

        CompletableFuture.delayedExecutor(2, TimeUnit.SECONDS).execute {
            MinecraftClient.getInstance().execute {
                SlayerTracker.onBossSpawned()
                msg(source, "Boss spawned! (2s after start)", Formatting.GOLD)
            }
        }

        CompletableFuture.delayedExecutor(4, TimeUnit.SECONDS).execute {
            MinecraftClient.getInstance().execute {
                SlayerTracker.onBossSlain()
                SlayerTracker.suppressScoreboardUpdates = false
                msg(source, "Boss slain! (2s kill time)", Formatting.GREEN)
            }
        }
    }

    private fun simulatePing(source: FabricClientCommandSource) {
        val baseTime = Util.getMeasuringTimeMs()
        val fakePings = listOf(95, 102, 88, 110, 97, 135, 80, 105, 92, 200)
        fakePings.forEach { ping ->
            PingTracker.onPingResult(baseTime - ping)
        }
        msg(source, "Injected ${fakePings.size} ping samples (${fakePings.min()}-${fakePings.max()}ms)", Formatting.GREEN)
        msg(source, "Current: ${PingTracker.ping}ms | Average: ${PingTracker.averagePing}ms", Formatting.GRAY)
    }

    private fun simulateCoins(source: FabricClientCommandSource) {
        val event = CoinChangeEvent(50_000L, "Bazaar Sale")
        EventBus.post(event)
        msg(source, "Fired CoinChangeEvent(+50,000 from Bazaar Sale)", Formatting.GREEN)
    }

    private fun simulateDrop(source: FabricClientCommandSource) {
        val event = RareDropEvent("Wither Chestplate")
        EventBus.post(event)
        msg(source, "Fired RareDropEvent(Wither Chestplate)", Formatting.GREEN)
    }

    private fun simulateReset(source: FabricClientCommandSource) {
        PingTracker.reset()
        TPSTracker.reset()
        SlayerTracker.reset()
        SkillTracker.reset()
        SkillXpChecker.reset()
        msg(source, "All trackers reset to initial state", Formatting.YELLOW)
    }

    private fun colored(value: Boolean): String =
        if (value) "\u00a7atrue" else "\u00a7cfalse"

    fun header(source: FabricClientCommandSource, title: String) {
        source.sendFeedback(
            Text.literal("\u00a76\u00a7l--- $title ---")
        )
    }

    fun msg(source: FabricClientCommandSource, text: String, formatting: Formatting? = null) {
        val t = Text.literal("  $text")
        if (formatting != null) t.formatted(formatting)
        source.sendFeedback(t)
    }
}
