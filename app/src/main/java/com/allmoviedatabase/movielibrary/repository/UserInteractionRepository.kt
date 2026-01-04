package com.allmoviedatabase.movielibrary.repository

import com.allmoviedatabase.movielibrary.model.Badge.BadgeManager
import com.allmoviedatabase.movielibrary.model.Comment
import com.allmoviedatabase.movielibrary.model.UserList
import com.allmoviedatabase.movielibrary.util.BadWordFilter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.isNotEmpty
import kotlin.collections.joinToString

class UserInteractionRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    val currentUserId: String? get() = auth.currentUser?.uid

    // Kullanıcı Listelerini Çekme
    suspend fun getUserLists(): List<UserList> {
        val uid = currentUserId ?: return emptyList()
        return try {
            val snapshot = firestore.collection("users").document(uid)
                .collection("created_lists").get().await()
            snapshot.toObjects(UserList::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Listeye Öğe Ekleme
    suspend fun addItemToCustomList(listId: String, item: HashMap<String, Any>): Result<String> {
        val uid = currentUserId ?: return Result.failure(Exception("Kullanıcı yok"))
        return try {
            val listRef = firestore.collection("users").document(uid)
                .collection("created_lists").document(listId)

            firestore.runBatch { batch ->
                val itemRef = listRef.collection("items").document(item["id"].toString())
                batch.set(itemRef, item)
                batch.update(listRef, "itemCount", FieldValue.increment(1))
            }.await()
            Result.success("Başarıyla eklendi")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Like / Watchlist İşlemleri
    suspend fun toggleInteraction(
        collectionName: String, // "favorites" veya "watchlist"
        itemId: String,
        itemData: HashMap<String, Any>,
        isAdding: Boolean
    ): Result<Boolean> {
        val uid = currentUserId ?: return Result.failure(Exception("Giriş yapılmalı"))
        return try {
            val userRef = firestore.collection("users").document(uid)
                .collection(collectionName).document(itemId)

            // İstatistik tablosu referansı
            val statsRef = firestore.collection("movie_stats").document(itemId)

            firestore.runTransaction { transaction ->
                // 1. ÖNCE MEVCUT İSTATİSTİĞİ OKU (Eksiye düşmemek için)
                val statsSnapshot = transaction.get(statsRef)
                // Eğer kayıt yoksa 0 kabul et
                val currentLikes = statsSnapshot.getLong("likeCount") ?: 0L

                if (isAdding) {
                    // A. Kullanıcının listesine ekle
                    transaction.set(userRef, itemData)

                    // B. İstatistik tablosunu güncelle (Meta verilerle birlikte!)
                    // ViewModel'den gelen itemData içindeki başlıkları buraya da ekliyoruz.
                    if (collectionName == "favorites") {
                        val newCount = currentLikes + 1

                        val statsData = hashMapOf(
                            "likeCount" to newCount,
                            "movieId" to (itemData["movieId"] ?: itemData["tvId"] ?: 0), // Hem film hem dizi ID'sini yakalar
                            "title" to (itemData["title"] ?: ""),
                            "originalTitle" to (itemData["originalTitle"] ?: ""),
                            "posterPath" to (itemData["posterPath"] ?: ""),
                        )
                        // merge kullanarak varsa diğer alanları koruyoruz, yoksa oluşturuyoruz
                        transaction.set(statsRef, statsData, SetOptions.merge())
                    }
                } else {
                    // A. Kullanıcının listesinden sil
                    transaction.delete(userRef)

                    // B. İstatistikten düş (Sadece 0'dan büyükse!)
                    if (collectionName == "favorites") {
                        if (currentLikes > 0) {
                            transaction.update(statsRef, "likeCount", currentLikes - 1)
                        } else {
                            // Zaten 0 ise veya negatifse, güvenli olması için 0'a set et
                            transaction.update(statsRef, "likeCount", 0)
                        }
                    }
                }
            }.await()
            Result.success(isAdding)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Yorum Gönderme
    suspend fun sendComment(
        itemId: String,
        mediaType: String,
        content: String,
        isSpoiler: Boolean,
        genreIds: List<Int>
    ): Result<String> {
        val user = auth.currentUser ?: return Result.failure(Exception("Giriş yapmalısınız."))
        if (content.isBlank()) return Result.failure(Exception("Yorum boş olamaz."))
        if (BadWordFilter.containsBadWord(content)) return Result.failure(Exception("Uygunsuz içerik."))

        val commentId = UUID.randomUUID().toString()
        val userDoc = firestore.collection("users").document(user.uid).get().await()
        val nickname = userDoc.getString("nickname") ?: "Anonim"

        val newComment = Comment(
            id = commentId,
            movieId = itemId.toIntOrNull() ?: 0,
            mediaType = mediaType,
            userId = user.uid,
            userName = nickname,
            userAvatarUrl = user.photoUrl?.toString() ?: "", // Null hatası burada çözüldü
            content = content,
            timestamp = com.google.firebase.Timestamp.now(),
            status = 0,
            isSpoiler = isSpoiler
        )

        return try {
            firestore.collection("comments").document(commentId).set(newComment).await()
            val newBadges = updateUserStats(user.uid, incrementComment = true, genreIds = genreIds)
            if (newBadges.isNotEmpty()) {
                val names = newBadges.joinToString { it.name }
                Result.success("TEBRİKLER! Yeni Seviye: $names")
            } else {
                Result.success("SUCCESS")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // YENİ EKLENEN: Bir öğenin favori/watchlist durumunu kontrol eder
    suspend fun checkItemStatus(collectionName: String, itemId: String): Boolean {
        val uid = currentUserId ?: return false
        return try {
            val doc = firestore.collection("users").document(uid)
                .collection(collectionName).document(itemId)
                .get().await()
            doc.exists()
        } catch (e: Exception) {
            false
        }
    }

    // Yorum Sorgusu
    fun getCommentsQuery(itemId: Int): Query {
        return firestore.collection("comments")
            .whereEqualTo("movieId", itemId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
    }

    // İstatistik (Private)
    private suspend fun updateUserStats(userId: String, incrementComment: Boolean, genreIds: List<Int>): List<com.allmoviedatabase.movielibrary.model.Badge.BadgeDefinition> {
        val userRef = firestore.collection("users").document(userId)
        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)
                val currentStatsObj = snapshot.get("stats") as? Map<String, Any> ?: emptyMap()
                val currentStats = currentStatsObj.mapValues { (it.value as? Number)?.toLong() ?: 0L }.toMutableMap()
                val earnedBadges = snapshot.get("earnedBadges") as? MutableList<String> ?: mutableListOf()
                val registerDate = snapshot.getLong("registerDate") ?: System.currentTimeMillis()

                if (incrementComment) currentStats["total_comments"] = (currentStats["total_comments"] ?: 0L) + 1
                genreIds.forEach { id ->
                    val key = "genre_$id"
                    currentStats[key] = (currentStats[key] ?: 0L) + 1
                }

                val newBadges = BadgeManager.checkNewBadges(currentStats, earnedBadges, registerDate)
                if (newBadges.isNotEmpty()) {
                    newBadges.forEach { badge ->
                        earnedBadges.add(badge.id)
                        val baseId = badge.id.substringBeforeLast("_")
                        if (badge.id.endsWith("_silver")) earnedBadges.remove("${baseId}_bronze")
                        else if (badge.id.endsWith("_gold")) {
                            earnedBadges.remove("${baseId}_bronze")
                            earnedBadges.remove("${baseId}_silver")
                        }
                    }
                    transaction.update(userRef, "earnedBadges", earnedBadges)
                }
                transaction.update(userRef, "stats", currentStats)
                newBadges
            }.await()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateGenreStats(
        userId: String,
        genreIds: List<Int>,
        isAdding: Boolean // True ise +1, False ise -1
    ) {
        val userRef = firestore.collection("users").document(userId)

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(userRef)

                // Mevcut istatistikleri al
                val currentStatsObj = snapshot.get("stats") as? Map<String, Any> ?: emptyMap()
                val currentStats = currentStatsObj.mapValues { (it.value as? Number)?.toLong() ?: 0L }.toMutableMap()

                // Eklenecek/Çıkarılacak değer (+1 veya -1)
                val change = if (isAdding) 1L else -1L

                genreIds.forEach { id ->
                    val key = "genre_$id"
                    val currentVal = currentStats[key] ?: 0L

                    // Yeni değer hesapla (0'ın altına düşmesin diye kontrol ekleyelim)
                    val newVal = if (currentVal + change < 0) 0L else currentVal + change

                    currentStats[key] = newVal
                }

                // Sadece istatistikleri güncelle, rozetleri tekrar kontrol etmeye gerek yok
                // (Genelde oyunlaştırmada kazanılan rozet geri alınmaz ama puan düşer)
                transaction.update(userRef, "stats", currentStats)
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getStatsDocument(itemId: String) = firestore.collection("movie_stats").document(itemId)
}