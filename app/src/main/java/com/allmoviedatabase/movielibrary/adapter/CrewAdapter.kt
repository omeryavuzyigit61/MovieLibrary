package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.ItemCrewHeaderBinding
import com.allmoviedatabase.movielibrary.databinding.ItemCrewMemberBinding
import com.allmoviedatabase.movielibrary.model.Credits.CrewMember
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

// View tiplerini ayırt etmek için sabitler
private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_MEMBER = 1

class CrewAdapter : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

    // Kategori başlığı (Departman adı) için ViewHolder
    class HeaderViewHolder(private val binding: ItemCrewHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(department: String) {
            binding.departmentTextView.text = department
        }

        companion object {
            fun from(parent: ViewGroup): HeaderViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCrewHeaderBinding.inflate(layoutInflater, parent, false)
                return HeaderViewHolder(binding)
            }
        }
    }

    // Ekip üyesi için ViewHolder
    class MemberViewHolder(private val binding: ItemCrewMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(crewMember: CrewMember) {
            binding.crewNameTextView.text = crewMember.name
            binding.jobTextView.text = crewMember.job
            val imageUrl = "https://image.tmdb.org/t/p/w200${crewMember.profilePath}"
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.profileImageView)
        }

        companion object {
            fun from(parent: ViewGroup): MemberViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ItemCrewMemberBinding.inflate(layoutInflater, parent, false)
                return MemberViewHolder(binding)
            }
        }
    }

    // Pozisyondaki öğenin türüne göre (String mi CrewMember mı) view tipini döndürür
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is String -> VIEW_TYPE_HEADER
            is CrewMember -> VIEW_TYPE_MEMBER
            else -> throw IllegalArgumentException("Invalid type of data at position $position")
        }
    }

    // Gelen view tipine göre uygun ViewHolder'ı oluşturur
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            VIEW_TYPE_MEMBER -> MemberViewHolder.from(parent)
            else -> throw IllegalArgumentException("Invalid viewType: $viewType")
        }
    }

    // ViewHolder'ı gelen veriyle doldurur
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val departmentName = getItem(position) as String
                holder.bind(departmentName)
            }
            is MemberViewHolder -> {
                val crewMember = getItem(position) as CrewMember
                holder.bind(crewMember)
            }
        }
    }

    // ListAdapter'ın farkları bulmasını sağlar.
    // Listemiz <Any> olduğu için içerik kontrolünü daha dikkatli yapmalıyız.
    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return when (oldItem) {
                is CrewMember if newItem is CrewMember -> {
                    oldItem.id == newItem.id && oldItem.creditId == newItem.creditId
                }

                is String if newItem is String -> {
                    oldItem == newItem
                }

                else -> {
                    false
                }
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            // Tipleri kontrol ederek içerik karşılaştırması yap
            return when (oldItem) {
                is CrewMember if newItem is CrewMember -> {
                    // data class olduğu için '==' içerikleri karşılaştırır
                    oldItem == newItem
                }

                is String if newItem is String -> {
                    // String'ler için '==' içerikleri karşılaştırır
                    oldItem == newItem
                }

                else -> {
                    false
                }
            }
        }
    }
}
