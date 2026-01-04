package com.allmoviedatabase.movielibrary.model

data class UserList(
    val listId: String = "",
    val listName: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val itemCount: Int = 0
)
