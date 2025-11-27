package com.allmoviedatabase.movielibrary.model.PersonDetail

import com.google.gson.annotations.SerializedName

data class PersonDetail(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("biography")
    val biography: String?,
    @SerializedName("birthday")
    val birthday: String?, // "1958-10-16"
    @SerializedName("gender")
    val gender: Int?, // 2 for Male, 1 for Female
    @SerializedName("known_for_department")
    val knownForDepartment: String?,
    @SerializedName("place_of_birth")
    val placeOfBirth: String?,
    @SerializedName("profile_path")
    val profilePath: String?
)
