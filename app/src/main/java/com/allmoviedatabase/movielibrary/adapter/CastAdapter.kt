// adapter/CastAdapter.kt
package com.allmoviedatabase.movielibrary.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemCastMemberBinding
import com.allmoviedatabase.movielibrary.databinding.ItemShowMoreBinding
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

// View tiplerini ayırt etmek için sabitler
private const val VIEW_TYPE_CAST = 1
private const val VIEW_TYPE_SHOW_MORE = 2

class CastAdapter(
    private val onCastMemberClicked: (Int) -> Unit,
    private val onShowMoreClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var castList: List<CastMember> = emptyList()

    fun submitList(list: List<CastMember>) {
        // En fazla 10 oyuncu alıyoruz
        castList = list.take(10)
        notifyDataSetChanged()
    }

    // Oyuncu kartı için ViewHolder
    inner class CastViewHolder(val binding: ItemCastMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(castMember: CastMember) {
            binding.castNameTextView.text = castMember.name
            binding.castCharacterNameTextView.text = castMember.character
            val imageUrl = "https://image.tmdb.org/t/p/w200${castMember.profilePath}"
            Log.i("dataimage", "bind: "+castMember.profilePath)
            Glide.with(itemView.context)
                .load(imageUrl)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.castImageView)
            itemView.setOnClickListener {
                castMember.id?.let { personId ->
                    onCastMemberClicked(personId)
                }
            }
        }
    }

    // "Daha Fazla Göster" için ViewHolder
    inner class ShowMoreViewHolder(val binding: ItemShowMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                onShowMoreClicked()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        // Eğer pozisyon listenin sonundaysa (ve liste 10 elemanlıysa) "Daha Fazla Göster" view'ını göster
        return if (position == 10) {
            VIEW_TYPE_SHOW_MORE
        } else {
            VIEW_TYPE_CAST
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CAST -> {
                val binding = ItemCastMemberBinding.inflate(inflater, parent, false)
                CastViewHolder(binding)
            }
            VIEW_TYPE_SHOW_MORE -> {
                val binding = ItemShowMoreBinding.inflate(inflater, parent, false)
                ShowMoreViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CastViewHolder) {
            holder.bind(castList[position])
        }
        // ShowMoreViewHolder için özel bir bind işlemi gerekmiyor, tıklama olayı init'te halledildi.
    }

    override fun getItemCount(): Int {
        // Eğer 10 oyuncu varsa, bir de "Daha Fazla Göster" butonu için +1 ekliyoruz.
        return if (castList.size == 10) 11 else castList.size
    }
}