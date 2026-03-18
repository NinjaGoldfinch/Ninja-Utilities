package com.ninjagoldfinch.nz.ninja_utils.api.publicapi.dto

data class ProfilesResponse(
    val success: Boolean,
    val profiles: List<SkyBlockProfile>?
)

data class SkyBlockProfile(
    val profile_id: String,
    val cute_name: String?,
    val selected: Boolean?,
    val members: Map<String, ProfileMember>?
)

data class ProfileMember(
    val player_data: PlayerData?
)

data class PlayerData(
    val experience: Map<String, Double>?
)
