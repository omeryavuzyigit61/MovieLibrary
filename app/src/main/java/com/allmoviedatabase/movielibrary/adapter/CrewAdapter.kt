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

private const val VIEW_TYPE_HEADER = 0
private const val VIEW_TYPE_MEMBER = 1

// DÜZELTME: Constructor'a 'onPersonClick' eklendi.
class CrewAdapter(
    private val onPersonClick: (Int) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(DiffCallback()) {

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

    // Inner class yaptık ki 'onPersonClick'e erişebilsin
    inner class MemberViewHolder(private val binding: ItemCrewMemberBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(crewMember: CrewMember) {
            binding.crewNameTextView.text = crewMember.name
            binding.jobTextView.text = crewMember.job

            // TIKLAMA OLAYI
            itemView.setOnClickListener {
                crewMember.id?.let { id -> onPersonClick(id) }
            }

            val imageUrl = "https://image.tmdb.org/t/p/w200${crewMember.profilePath}"
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.profileImageView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is String -> VIEW_TYPE_HEADER
            is CrewMember -> VIEW_TYPE_MEMBER
            else -> throw IllegalArgumentException("Invalid type of data")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_HEADER -> HeaderViewHolder.from(parent)
            VIEW_TYPE_MEMBER -> MemberViewHolder(ItemCrewMemberBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid viewType: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> holder.bind(getItem(position) as String)
            is MemberViewHolder -> holder.bind(getItem(position) as CrewMember)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Any>() {
        override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
            return if (oldItem is CrewMember && newItem is CrewMember) {
                oldItem.id == newItem.id && oldItem.creditId == newItem.creditId
            } else if (oldItem is String && newItem is String) {
                oldItem == newItem
            } else {
                false
            }
        }

        override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
            return oldItem == newItem
        }
    }
}