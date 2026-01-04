package com.allmoviedatabase.movielibrary.model.Badge

import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_ACTION
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_ADVENTURE
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_ANIMATION
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_COMEDY
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_CRIME
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_DOCUMENTARY
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_DRAMA
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_FAMILY
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_FANTASY
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_HISTORY
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_HORROR
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_MUSIC
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_MYSTERY
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_ROMANCE
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_SCIFI
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_THRILLER
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_WAR
import com.allmoviedatabase.movielibrary.util.Constants.GENRE_WESTERN
import java.util.concurrent.TimeUnit

// Rozet Tipleri
enum class BadgeType {
    GENRE,          // Tür Bazlı (Korku, Aksiyon vb.)
    INTERACTION,    // Etkileşim (Yorum, Beğeni)
    LOYALTY         // Sadakat (Süre)
}

// Rozet Seviyeleri
enum class BadgeTier {
    BRONZE, SILVER, GOLD, PLATINUM, SPECIAL
}

// Rozet Tanımı
data class BadgeDefinition(
    val id: String,
    val name: String,
    val description: String,
    val iconResId: Int,      // Kendi resimlerinle değiştireceğin yer
    val type: BadgeType,
    val tier: BadgeTier,
    val threshold: Int,      // Gereken sayı (Film sayısı veya Gün sayısı)
    val relatedGenreId: Int? = null
)

object BadgeManager {

    // --- TÜM ROZETLER LİSTESİ ---
    val allBadges = listOf(
        // ==========================================
        // 1. SADAKAT (SÜRE) ROZETLERİ (3 SEVİYE)
        // ==========================================
        BadgeDefinition("loyalty_new", "Yeni Üye", "Aramıza hoş geldin!", R.drawable.time_fast, BadgeType.LOYALTY, BadgeTier.BRONZE, 0, null),
        BadgeDefinition("loyalty_1month", "Müdavim", "1 aydır bizimlesin. Alıştın artık.", R.drawable.time_fast, BadgeType.LOYALTY, BadgeTier.SILVER, 30, null),
        BadgeDefinition("loyalty_1year", "Eskimeyen Dost", "1 yıldır buradasın. Demirbaş oldun!", R.drawable.time_fast, BadgeType.LOYALTY, BadgeTier.GOLD, 365, null),

        // ==========================================
        // 2. ETKİLEŞİM ROZETLERİ
        // ==========================================
        BadgeDefinition("comment_bronze", "Ses Veren", "İlk yorumunu yaptın.", android.R.drawable.ic_menu_edit, BadgeType.INTERACTION, BadgeTier.BRONZE, 1, null),
        BadgeDefinition("comment_silver", "Yazar", "10 yorum yaptın. Klavyene sağlık.", android.R.drawable.ic_menu_sort_alphabetically, BadgeType.INTERACTION, BadgeTier.SILVER, 10, null),
        BadgeDefinition("comment_gold", "Sinema Otoritesi", "50 yorum! Herkes seni dinliyor.", android.R.drawable.ic_menu_save, BadgeType.INTERACTION, BadgeTier.GOLD, 50, null),

        // ==========================================
        // 3. TÜR (GENRE) ROZETLERİ
        // ==========================================

        // --- AKSİYON (28) ---
        BadgeDefinition("action_bronze", "Hızlı", "5 Aksiyon filmi izledin.", R.drawable.ic_left, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_ACTION),
        BadgeDefinition("action_silver", "Dublör", "20 Aksiyon filmi. Patlamaları seversin.", android.R.drawable.ic_media_ff, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_ACTION),
        BadgeDefinition("action_gold", "Yenilmez", "50 Aksiyon filmi. Tek kişilik ordusun.", android.R.drawable.ic_menu_manage, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_ACTION),

        // --- KORKU (27) ---
        BadgeDefinition("horror_bronze", "Gece Lambası", "5 Korku filmi. Işıkları açık bırak.", R.drawable.horror, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_HORROR),
        BadgeDefinition("horror_silver", "Korkusuz", "20 Korku filmi. Alıştın artık.", R.drawable.horror, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_HORROR),
        BadgeDefinition("horror_gold", "Çelik Sinir", "50 Korku filmi. Seni hiçbir şey korkutamaz.", R.drawable.horror, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_HORROR),

        // --- MACERA (12) ---
        BadgeDefinition("adventure_bronze", "Gezgin", "5 Macera filmi.", R.drawable.adventure, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_ADVENTURE),
        BadgeDefinition("adventure_silver", "Kaşif", "20 Macera filmi. Yeni dünyalar keşfettin.", R.drawable.adventure, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_ADVENTURE),
        BadgeDefinition("adventure_gold", "Indiana Jones", "50 Macera filmi. Hazinenin peşindesin.", R.drawable.adventure, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_ADVENTURE),

        // --- ANİMASYON (16) ---
        BadgeDefinition("anim_bronze", "Çizgi Sever", "5 Animasyon.", android.R.drawable.ic_menu_gallery, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_ANIMATION),
        BadgeDefinition("anim_silver", "Hayalperest", "20 Animasyon. Renkli dünyaları seviyorsun.", android.R.drawable.ic_menu_crop, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_ANIMATION),
        BadgeDefinition("anim_gold", "Ruhun Genç", "50 Animasyon. Asla büyümeyeceksin!", android.R.drawable.ic_menu_camera, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_ANIMATION),

        // --- KOMEDİ (35) ---
        BadgeDefinition("comedy_bronze", "Gülümseyen", "5 Komedi filmi.", android.R.drawable.ic_menu_view, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_COMEDY),
        BadgeDefinition("comedy_silver", "Şakacı", "20 Komedi filmi. Hayat sana güzel.", android.R.drawable.sym_action_chat, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_COMEDY),
        BadgeDefinition("comedy_gold", "Kahkaha Makinesi", "50 Komedi filmi. Ortamın neşesisin!", android.R.drawable.sym_call_incoming, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_COMEDY),

        // --- SUÇ (80) ---
        BadgeDefinition("crime_bronze", "Dedektif", "5 Suç filmi.", R.drawable.crime, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_CRIME),
        BadgeDefinition("crime_silver", "Suç Ortağı", "20 Suç filmi. Olayları çözdün.", R.drawable.crime, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_CRIME),
        BadgeDefinition("crime_gold", "Godfather", "50 Suç filmi. Yeraltı dünyası senden sorulur.", R.drawable.crime, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_CRIME),

        // --- BELGESEL (99) ---
        BadgeDefinition("doc_bronze", "Öğrenci", "5 Belgesel.", R.drawable.documentary, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_DOCUMENTARY),
        BadgeDefinition("doc_silver", "Bilge", "20 Belgesel. Kültür seviyen artıyor.", R.drawable.documentary, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_DOCUMENTARY),
        BadgeDefinition("doc_gold", "Ansiklopedi", "50 Belgesel. Yürüyen bilgi kaynağısın.", R.drawable.documentary, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_DOCUMENTARY),

        // --- DRAM (18) ---
        BadgeDefinition("drama_bronze", "Düşünceli", "5 Dram filmi.", R.drawable.drama, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_DRAMA),
        BadgeDefinition("drama_silver", "Melankolik", "20 Dram filmi. Gözyaşları sel oldu.", R.drawable.drama, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_DRAMA),
        BadgeDefinition("drama_gold", "Oscar Adayı", "50 Dram filmi. Hayatın kendisi bir film.", R.drawable.drama, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_DRAMA),

        // --- AİLE (10751) ---
        BadgeDefinition("family_bronze", "Ev Kuşu", "5 Aile filmi.", R.drawable.family, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_FAMILY),
        BadgeDefinition("family_silver", "Aile Babası", "20 Aile filmi. Pazar sineması keyfi.", R.drawable.family, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_FAMILY),
        BadgeDefinition("family_gold", "Bizim Aile", "50 Aile filmi. En önemlisi ailedir.", R.drawable.family, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_FAMILY),

        // --- FANTASTİK (14) ---
        BadgeDefinition("fantasy_bronze", "Büyücü", "5 Fantastik film.", android.R.drawable.star_off, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_FANTASY),
        BadgeDefinition("fantasy_silver", "Ejderha Terbiyecisi", "20 Fantastik film. Hayal gücün sınır tanımıyor.", android.R.drawable.star_on, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_FANTASY),
        BadgeDefinition("fantasy_gold", "Orta Dünya Sakini", "50 Fantastik film. Efsaneler gerçek oldu.", android.R.drawable.btn_star_big_on, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_FANTASY),

        // --- TARİH (36) ---
        BadgeDefinition("history_bronze", "Tarihçi", "5 Tarih filmi.", android.R.drawable.ic_menu_month, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_HISTORY),
        BadgeDefinition("history_silver", "Zaman Yolcusu", "20 Tarih filmi. Geçmişe tanıklık ettin.", android.R.drawable.ic_menu_today, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_HISTORY),
        BadgeDefinition("history_gold", "İmparator", "50 Tarih filmi. Tarih tekerrürden ibarettir.", android.R.drawable.ic_menu_week, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_HISTORY),

        // --- MÜZİK (10402) ---
        BadgeDefinition("music_bronze", "Dinleyici", "5 Müzikal film.", android.R.drawable.ic_media_pause, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_MUSIC),
        BadgeDefinition("music_silver", "Solist", "20 Müzikal film. Ritim tutmaya başladın.", android.R.drawable.ic_media_play, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_MUSIC),
        BadgeDefinition("music_gold", "Maestro", "50 Müzikal film. Sahne senin!", android.R.drawable.ic_media_next, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_MUSIC),

        // --- GİZEM (9648) ---
        BadgeDefinition("mystery_bronze", "Şüpheci", "5 Gizem filmi.", android.R.drawable.ic_menu_help, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_MYSTERY),
        BadgeDefinition("mystery_silver", "Sherlock", "20 Gizem filmi. Detaylar gözünden kaçmıyor.", android.R.drawable.ic_search_category_default, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_MYSTERY),
        BadgeDefinition("mystery_gold", "Çözülmeyen Sır", "50 Gizem filmi. Her bilmeceyi çözersin.", android.R.drawable.ic_menu_zoom, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_MYSTERY),

        // --- ROMANTİK (10749) ---
        BadgeDefinition("romance_bronze", "Duygusal", "5 Romantik film.", android.R.drawable.star_off, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_ROMANCE),
        BadgeDefinition("romance_silver", "Aşık", "20 Romantik film. Aşkı arıyorsun.", android.R.drawable.star_on, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_ROMANCE),
        BadgeDefinition("romance_gold", "Casanova", "50 Romantik film. Kalp hırsızı!", android.R.drawable.btn_star_big_on, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_ROMANCE),

        // --- BİLİM KURGU (878) ---
        BadgeDefinition("scifi_bronze", "Meraklı", "5 Bilim Kurgu filmi.", R.drawable.science_fiction, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_SCIFI),
        BadgeDefinition("scifi_silver", "Astronot", "20 Bilim Kurgu filmi. Gelecekten geldin.", android.R.drawable.ic_menu_compass, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_SCIFI),
        BadgeDefinition("scifi_gold", "Galaktik İmparator", "50 Bilim Kurgu. Uzay senin çöplüğün.", android.R.drawable.stat_sys_data_bluetooth, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_SCIFI),

        // --- GERİLİM (53) ---
        BadgeDefinition("thriller_bronze", "Gergin", "5 Gerilim filmi.", android.R.drawable.ic_lock_idle_alarm, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_THRILLER),
        BadgeDefinition("thriller_silver", "Soğukkanlı", "20 Gerilim filmi. Nabzın hiç yükselmiyor.", android.R.drawable.ic_lock_idle_charging, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_THRILLER),
        BadgeDefinition("thriller_gold", "Buz Adam", "50 Gerilim filmi. Sinirlerin alınmış.", android.R.drawable.ic_lock_power_off, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_THRILLER),

        // --- SAVAŞ (10752) ---
        BadgeDefinition("war_bronze", "Er", "5 Savaş filmi.", R.drawable.war, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_WAR),
        BadgeDefinition("war_silver", "Gazi", "20 Savaş filmi. Cepheden döndün.", android.R.drawable.ic_menu_send, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_WAR),
        BadgeDefinition("war_gold", "Komutan", "50 Savaş filmi. Strateji ustasısın.", android.R.drawable.ic_menu_upload, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_WAR),

        // --- WESTERN (37) ---
        BadgeDefinition("western_bronze", "Kovboy", "5 Western filmi.", R.drawable.western, BadgeType.GENRE, BadgeTier.BRONZE, 5, GENRE_WESTERN),
        BadgeDefinition("western_silver", "Şerif", "20 Western filmi. Kasabada düzen senden sorulur.", android.R.drawable.ic_menu_mylocation, BadgeType.GENRE, BadgeTier.SILVER, 20, GENRE_WESTERN),
        BadgeDefinition("western_gold", "Vahşi Batı Efsanesi", "50 Western filmi. En hızlı silah çeken sensin.", android.R.drawable.ic_menu_mapmode, BadgeType.GENRE, BadgeTier.GOLD, 50, GENRE_WESTERN)
    )

    fun getBadgeById(id: String) = allBadges.find { it.id == id }

    // Rozet Kontrol Fonksiyonu
    // NOT: Bu fonksiyonu çağırırken 'registerDate' bilgisini de göndermeyi unutma!
    fun checkNewBadges(
        currentStats: Map<String, Long>,
        ownedBadges: List<String>,
        registerDate: Long // Kullanıcının kayıt tarihi
    ): List<BadgeDefinition> {
        val newBadges = mutableListOf<BadgeDefinition>()

        // Üyelik süresini gün cinsinden hesapla
        val daysMember = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - registerDate)

        allBadges.forEach { badge ->
            if (ownedBadges.contains(badge.id)) return@forEach

            val count = when (badge.type) {
                BadgeType.INTERACTION -> {
                    if (badge.id.contains("comment")) currentStats["total_comments"] ?: 0
                    else 0
                }
                BadgeType.GENRE -> {
                    // İlgili türün sayacına bak (örn: "genre_28")
                    currentStats["genre_${badge.relatedGenreId}"] ?: 0
                }
                BadgeType.LOYALTY -> {
                    // Kayıt tarihine göre gün sayısı
                    daysMember
                }
            }

            if (count >= badge.threshold) {
                newBadges.add(badge)
            }
        }
        return newBadges
    }
}