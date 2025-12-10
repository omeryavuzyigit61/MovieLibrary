package com.allmoviedatabase.movielibrary.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.net.toUri
import com.allmoviedatabase.movielibrary.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.NumberFormat
import java.util.Locale

fun Context.showDescriptionDialog(overview: String?) {
    if (overview.isNullOrEmpty()) return

    val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_description, null)
    val dialog = MaterialAlertDialogBuilder(this)
        .setView(dialogView)
        .create()

    dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

    val txtDescription = dialogView.findViewById<TextView>(R.id.dialogDescription)
    val btnClose = dialogView.findViewById<ImageButton>(R.id.btnClose)

    txtDescription.text = overview

    btnClose.setOnClickListener {
        dialog.dismiss()
    }

    dialog.show()
}

// WEB SİTESİ AÇMA (Homepage için)
fun Context.openUrl(url: String?) {
    if (url.isNullOrEmpty()) return
    try {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    } catch (e: Exception) {
        // Hata yönetimi
    }
}

// FACEBOOK
fun Context.openFacebook(pageId: String?) {
    if (pageId.isNullOrEmpty()) return
    val facebookUrl = "https://www.facebook.com/$pageId"
    try {
        // Facebook uygulaması yüklü mü diye kontrol edip fb:// açmak daha complex,
        // direkt web intent'i modern Android'de genelde App Link ile uygulamayı tetikler.
        startActivity(Intent(Intent.ACTION_VIEW, "fb://page/$pageId".toUri()))
    } catch (e: Exception) {
        startActivity(Intent(Intent.ACTION_VIEW, facebookUrl.toUri()))
    }
}

// INSTAGRAM
fun Context.openInstagram(username: String?) {
    if (username.isNullOrEmpty()) return
    val url = "http://instagram.com/_u/$username"
    try {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    } catch (e: Exception) {
        startActivity(Intent(Intent.ACTION_VIEW, "http://instagram.com/$username".toUri()))
    }
}

// TWITTER (X)
fun Context.openTwitter(username: String?) {
    if (username.isNullOrEmpty()) return
    try {
        startActivity(Intent(Intent.ACTION_VIEW, "twitter://user?screen_name=$username".toUri()))
    } catch (e: Exception) {
        startActivity(Intent(Intent.ACTION_VIEW, "https://twitter.com/$username".toUri()))
    }
}

// IMDB (İstersen ekle)
fun Context.openImdb(imdbId: String?) {
    if (imdbId.isNullOrEmpty()) return
    startActivity(Intent(Intent.ACTION_VIEW, "https://www.imdb.com/title/$imdbId".toUri()))
}

fun Context.openYoutubeVideo(videoKey: String) {
    val appIntent = Intent(Intent.ACTION_VIEW, "vnd.youtube:$videoKey".toUri())
    val webIntent = Intent(Intent.ACTION_VIEW, "http://www.youtube.com/watch?v=$videoKey".toUri())
    try {
        startActivity(appIntent)
    } catch (_: Exception) {
        startActivity(webIntent)
    }
}

fun Long?.formatCurrency(): String {
    if (this == null || this <= 0) {
        return "-"
    }
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.maximumFractionDigits = 0
    return format.format(this)
}

// 4. DİL FORMATLAMA (String üzerine eklenti)
fun String?.formatLanguage(): String {
    return when (this) {
        "en" -> "İngilizce"
        "tr" -> "Türkçe"
        "ja" -> "Japonca"
        "ko" -> "Korece"
        "it" -> "İtalyanca"
        "fr" -> "Fransızca"
        "es" -> "İspanyolca"
        "de" -> "Almanca"
        else -> this?.uppercase() ?: "-"
    }
}