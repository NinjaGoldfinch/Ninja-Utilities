package com.ninjagoldfinch.nz.ninja_utils.api.publicapi.dto

data class ElectionResponse(
    val success: Boolean,
    val lastUpdated: Long,
    val mayor: MayorData?,
    val current: ElectionCurrent?
)

data class MayorData(
    val key: String,
    val name: String,
    val perks: List<MayorPerk>?,
    val minister: MinisterData?
)

data class MinisterData(
    val key: String,
    val name: String,
    val perk: MayorPerk?
)

data class MayorPerk(
    val name: String,
    val description: String?
)

data class ElectionCurrent(
    val year: Int,
    val candidates: List<ElectionCandidate>?
)

data class ElectionCandidate(
    val key: String,
    val name: String,
    val perks: List<MayorPerk>?,
    val votes: Int
)
