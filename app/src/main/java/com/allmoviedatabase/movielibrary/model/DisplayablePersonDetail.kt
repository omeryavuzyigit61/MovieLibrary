package com.allmoviedatabase.movielibrary.model

import com.allmoviedatabase.movielibrary.model.PersonDetail.PersonDetail

data class DisplayablePersonDetail(
    val person: PersonDetail,
    val showEnglishSourceWarning: Boolean
)