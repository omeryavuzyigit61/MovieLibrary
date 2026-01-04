package com.allmoviedatabase.movielibrary.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.ItemProfileBadgeBinding
import com.allmoviedatabase.movielibrary.model.Badge.BadgeDefinition
import com.allmoviedatabase.movielibrary.model.Badge.BadgeTier

class BadgeAdapter(private val badges: List<BadgeDefinition>) : RecyclerView.Adapter<BadgeAdapter.BadgeViewHolder>() {

    class BadgeViewHolder(val binding: ItemProfileBadgeBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BadgeViewHolder {
        val binding = ItemProfileBadgeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BadgeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BadgeViewHolder, position: Int) {
        val badge = badges[position]
        val context = holder.itemView.context

        // 1. GENİŞLİĞİ SABİTLEME (100dp)
        // XML yerine buradan müdahale ediyoruz. DP'yi piksele çevirmemiz lazım.
        val density = context.resources.displayMetrics.density
        val widthInPx = (100 * density).toInt() // 100dp karşılığı piksel

        val layoutParams = holder.binding.rootLayout.layoutParams
        layoutParams.width = widthInPx
        layoutParams.height = widthInPx
        holder.binding.rootLayout.layoutParams = layoutParams

        // 2. İÇERİK SET ETME
        holder.binding.tvBadgeName.text = badge.name
        holder.binding.ivBadgeIcon.setImageResource(badge.iconResId)

        // 3. ARKA PLAN SEÇİMİ (Aynen korudum)
        val backgroundRes = when (badge.tier) {
            BadgeTier.BRONZE -> R.drawable.bg_badge_bronze
            BadgeTier.SILVER -> R.drawable.bg_badge_silver
            BadgeTier.GOLD -> R.drawable.bg_badge_gold
            else -> R.drawable.bg_badge_bronze
        }
        holder.binding.rootLayout.setBackgroundResource(backgroundRes)

        // 4. RENK AYARLARI (Yazı Beyaz Olsun Dedin)
        // Yazıyı direkt Beyaz yapıyoruz
        holder.binding.tvBadgeName.setTextColor(Color.WHITE)

        // İkon rengi için hala Tier'a göre koyu ton mu istiyorsun yoksa o da mı beyaz olsun?
        // Eğer ikonu da beyaz istersen: holder.binding.ivBadgeIcon.setColorFilter(Color.WHITE)
        // Eğer ikonu eski mantıkla (koyu renk) bırakacaksan aşağıdaki blok kalsın:

        val contentColorRes = when (badge.tier) {
            BadgeTier.BRONZE -> R.color.badge_icon_bronze
            BadgeTier.SILVER -> R.color.badge_icon_silver
            BadgeTier.GOLD -> R.color.badge_icon_gold
            else -> R.color.black
        }
        val darkColor = ContextCompat.getColor(context, contentColorRes)

        // İkonu kategori rengine boyuyoruz (Yazı beyaz, ikon renkli kalır böylece)
        holder.binding.ivBadgeIcon.setColorFilter(darkColor)
    }

    override fun getItemCount() = badges.size
}