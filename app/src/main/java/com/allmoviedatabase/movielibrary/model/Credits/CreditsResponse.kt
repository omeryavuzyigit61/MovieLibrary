package com.allmoviedatabase.movielibrary.model.Credits

data class CreditsResponse(
    val id: Int,
    val cast: List<CastMember>,
    val crew: List<CrewMember>
)
