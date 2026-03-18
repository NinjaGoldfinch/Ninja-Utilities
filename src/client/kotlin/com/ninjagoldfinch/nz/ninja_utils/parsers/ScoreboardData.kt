package com.ninjagoldfinch.nz.ninja_utils.parsers

data class ScoreboardData(
    val title: String,
    val purse: Long?,
    val bits: Int?,
    val location: String?,
    val slayerQuest: String?,
    val slayerProgress: String?,
    val slayerBossSpawned: Boolean,
    val objective: String?,
    val rawLines: List<String>
)
