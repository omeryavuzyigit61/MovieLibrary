package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemCastMemberBinding
import com.allmoviedatabase.movielibrary.databinding.ItemShowMoreBinding
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

private const val VIEW_TYPE_CAST = 1
private const val VIEW_TYPE_SHOW_MORE = 2

class CastAdapter(
    private val isTvShow: Boolean = false,
    private val onCastMemberClicked: (Int) -> Unit,
    private val onShowMoreClicked: () -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var castList: List<CastMember> = emptyList()

    fun submitList(list: List<CastMember>) {
        castList = list.take(10)
        notifyDataSetChanged()
    }

    inner class CastViewHolder(val binding: ItemCastMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(castMember: CastMember) {
            binding.castNameTextView.text = castMember.name

            // --- KARAKTER ADINI BULMA MANTIĞI ---
            // 1. Önce normal 'character' alanına bak (Film için)
            // 2. Boşsa 'roles' listesinin ilk elemanına bak (Dizi için)
            val characterName = if (!castMember.character.isNullOrEmpty()) {
                castMember.character
            } else if (!castMember.roles.isNullOrEmpty()) {
                castMember.roles[0].character
            } else {
                ""
            }
            // -------------------------------------

            if (isTvShow) {
                // --- DİZİ MODU ---
                val episodeCount = castMember.totalEpisodeCount ?: 0

                if (episodeCount > 0) {
                    // Karakter adı varsa ve dizi ise:
                    // Eleven
                    // (42 Bölüm)
                    binding.castCharacterNameTextView.text = "$characterName\n($episodeCount Bölüm)"
                } else {
                    binding.castCharacterNameTextView.text = characterName
                }
            } else {
                // --- FİLM MODU ---
                binding.castCharacterNameTextView.text = characterName ?: "-"
            }

            // ... (Resim yükleme ve click listener aynı kalacak) ...
            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + castMember.profilePath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .into(binding.castImageView)

            itemView.setOnClickListener {
                castMember.id?.let { personId -> onCastMemberClicked(personId) }
            }
        }
    }

    inner class ShowMoreViewHolder(val binding: ItemShowMoreBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener { onShowMoreClicked() }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 10 && castList.size >= 10) VIEW_TYPE_SHOW_MORE else VIEW_TYPE_CAST
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CAST -> CastViewHolder(ItemCastMemberBinding.inflate(inflater, parent, false))
            VIEW_TYPE_SHOW_MORE -> ShowMoreViewHolder(ItemShowMoreBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is CastViewHolder) {
            holder.bind(castList[position])
        }
    }

    override fun getItemCount(): Int {
        // Eğer liste 10 veya daha fazlaysa, 10 kişi + 1 "Daha Fazla" butonu = 11 item döner
        return if (castList.size >= 10) 11 else castList.size
    }
}