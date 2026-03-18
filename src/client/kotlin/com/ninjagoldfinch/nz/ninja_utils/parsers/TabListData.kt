package com.ninjagoldfinch.nz.ninja_utils.parsers

data class TabListData(
    val skills: String?,
    val stats: List<String>,
    val profile: String?,
    val pet: String?,
    val cookie: String?,
    val slayerBossSpawned: Boolean,
    val rawLines: List<String>
)
