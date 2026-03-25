package com.ninjagoldfinch.nz.ninja_utils.client

import com.ninjagoldfinch.nz.ninja_utils.NinjaUtils
import com.ninjagoldfinch.nz.ninja_utils.api.modapi.ModApiManager
import com.ninjagoldfinch.nz.ninja_utils.api.publicapi.BackendClient
import com.ninjagoldfinch.nz.ninja_utils.api.publicapi.HypixelApiClient
import com.ninjagoldfinch.nz.ninja_utils.config.DebugCategory
import com.ninjagoldfinch.nz.ninja_utils.config.ModConfig
import com.ninjagoldfinch.nz.ninja_utils.core.HypixelState
import com.ninjagoldfinch.nz.ninja_utils.config.SkyblockCategory
import com.ninjagoldfinch.nz.ninja_utils.features.stats.InventorySnapshotTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.ItemTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SkillTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SkillXpChecker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.SlayerTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.PingTracker
import com.ninjagoldfinch.nz.ninja_utils.features.stats.TPSTracker
import com.ninjagoldfinch.nz.ninja_utils.features.warnings.WarningManager
import com.ninjagoldfinch.nz.ninja_utils.hud.HudConfigScreen
import com.ninjagoldfinch.nz.ninja_utils.hud.HudManager
import com.ninjagoldfinch.nz.ninja_utils.logging.ModLogger
import com.ninjagoldfinch.nz.ninja_utils.logging.PerformanceMonitor
import com.ninjagoldfinch.nz.ninja_utils.parsers.ChatParser
import com.ninjagoldfinch.nz.ninja_utils.parsers.ScoreboardParser
import com.ninjagoldfinch.nz.ninja_utils.parsers.ScreenInterceptor
import com.ninjagoldfinch.nz.ninja_utils.parsers.TabListParser
import com.teamresourceful.resourcefulconfig.api.client.ResourcefulConfigScreen
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.screen.Screen

object NinjaUtilsClient : ClientModInitializer {
    private var tickCounter = 0
    private var pingCounter = 0
    private var invSnapshotCounter = 0

    @Volatile
    @JvmField
    var pendingScreen: Screen? = null

    override fun onInitializeClient() {
        // 1. Config
        ModConfig.CONFIGURATOR.register(ModConfig::class.java)
        ModLogger.level = DebugCategory.logLevel
        ModLogger.info("Config", "Configuration loaded")

        // 2. Hypixel Mod API subscriptions
        ModApiManager.initialize()

        // 3. Connection handlers
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            WarningManager.onJoinServer()
        }

        ClientPlayConnectionEvents.DISCONNECT.register { _, _ ->
            HypixelState.reset()
            SlayerTracker.reset()
            SkillTracker.reset()
            SkillXpChecker.reset()
            TPSTracker.reset()
            PingTracker.reset()
            ItemTracker.reset()
            InventorySnapshotTracker.reset()
            HypixelApiClient.invalidateAll()
            BackendClient.invalidateAll()
            WarningManager.onDisconnect()
            PerformanceMonitor.reset()
            tickCounter = 0
            ModLogger.info("Connection", "Disconnected — state reset")
        }

        // 4. Command registration — single tree with aliases
        ClientCommandRegistrationCallback.EVENT.register { dispatcher, _ ->
            val commandTree = ClientCommandManager.literal("ninja_utils")
                .executes { ctx ->
                    // Base command shows help
                    val source = ctx.source
                    DevCommand.header(source, "Ninja Utilities")
                    DevCommand.msg(source, "/ninja_utils config \u00a77— Open settings")
                    DevCommand.msg(source, "/ninja_utils hud \u00a77— HUD position editor")
                    DevCommand.msg(source, "/ninja_utils dev \u00a77— Dev/debug commands")
                    DevCommand.msg(source, "\u00a77Aliases: /nu, /ninja, /ninja_utilities")
                    1
                }
                .then(ClientCommandManager.literal("config").executes { _ ->
                    pendingScreen = ResourcefulConfigScreen
                        .make(ModConfig.CONFIGURATOR, ModConfig::class.java)
                        .withParent(null)
                        .build()
                    1
                })
                .then(ClientCommandManager.literal("hud").executes { _ ->
                    pendingScreen = HudConfigScreen()
                    1
                })
                .then(DevCommand.buildDevSubtree())

            dispatcher.register(commandTree)

            // Aliases — redirect to the same tree
            for (alias in listOf("nu", "ninja", "ninja_utilities")) {
                dispatcher.register(
                    ClientCommandManager.literal(alias)
                        .redirect(dispatcher.root.getChild("ninja_utils"))
                )
            }
        }

        // 5. Inventory snapshot tracker (subscribes to IslandChangeEvent)
        InventorySnapshotTracker.initialize()

        // 6. Screen interceptor (reads SkyBlock menus)
        ScreenInterceptor.initialize()

        // 6. HUD system
        HudManager.initialize()

        // 7. Parsers — scoreboard & tab list on tick, and pending screen opener
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            PerformanceMonitor.beginTick()

            // TPS tracking — count ticks that received server packets
            TPSTracker.onClientTick()

            // Open pending config screen on render thread
            pendingScreen?.let { screen ->
                pendingScreen = null
                client.setScreen(screen)
            }

            // Always run scoreboard parser — it detects SkyBlock from the scoreboard title
            // as a fallback when the Mod API hasn't sent packets yet
            ScoreboardParser.parse()

            // Tab list parsing and warnings are less frequent
            tickCounter++
            if (tickCounter >= 20) {
                tickCounter = 0
                TabListParser.parse()
                WarningManager.tick()
            }

            // Send ping packet every 5 seconds (100 ticks)
            pingCounter++
            if (pingCounter >= 100) {
                pingCounter = 0
                PingTracker.sendPing()
            }

            // Inventory snapshot every 0.5 seconds (10 ticks)
            if (SkyblockCategory.trackItemGains && SkyblockCategory.trackInventoryGains) {
                invSnapshotCounter++
                if (invSnapshotCounter >= 10) {
                    invSnapshotCounter = 0
                    InventorySnapshotTracker.tick()
                }
            }

            PerformanceMonitor.endTick()
        }

        // 8. Chat parser
        ChatParser.registerDefaultHandlers()
        ClientReceiveMessageEvents.GAME.register { message, overlay ->
            ChatParser.onChatMessage(message, overlay)
        }

        ModLogger.info("Init", "Ninja Utilities initialized (${NinjaUtils.MOD_ID})")
    }
}
