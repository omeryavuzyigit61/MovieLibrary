// file: com/allmoviedatabase/movielibrary/adapter/FullCastAdapter.kt
package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.ItemFullCastMemberBinding
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class FullCastAdapter : ListAdapter<CastMember, FullCastAdapter.ViewHolder>(DiffCallback()) {

    // ViewHolder sınıfı, item layout'undaki view'ları tutar
    class ViewHolder(private val binding: ItemFullCastMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(castMember: CastMember) {
            binding.actorNameTextView.text = castMember.name
            binding.characterNameTextView.text = castMember.character

            val imageUrl = "https://image.tmdb.org/t/p/w200${castMember.profilePath}"
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.search) // Yüklenirken gösterilecek resim
                .error(R.drawable.search)       // Hata durumunda gösterilecek resim
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.profileImageView)
        }

        // Bu companion object, ViewHolder oluşturmayı daha temiz hale getirir
        companion object {
            fun from(parent: ViewGroup): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemFullCastMemberBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding)
            }
        }
    }

    // Yeni bir ViewHolder oluşturulduğunda çağrılır
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent)
    }

    // ViewHolder'ı verilerle doldurmak için çağrılır
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val castMember = getItem(position)
        holder.bind(castMember)
    }

    // ListAdapter'ın listeler arasındaki farkı verimli bir şekilde bulmasını sağlar
    class DiffCallback : DiffUtil.ItemCallback<CastMember>() {
        override fun areItemsTheSame(oldItem: CastMember, newItem: CastMember): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: CastMember, newItem: CastMember): Boolean {
            return oldItem == newItem
        }
    }
}