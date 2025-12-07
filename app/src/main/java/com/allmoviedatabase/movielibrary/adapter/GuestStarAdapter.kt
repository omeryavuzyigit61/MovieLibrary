package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.ItemGuestStarBinding
import com.allmoviedatabase.movielibrary.model.SeasonDetail.GuestStar
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.bumptech.glide.Glide

class GuestStarAdapter(private val guestStars: List<GuestStar>) : RecyclerView.Adapter<GuestStarAdapter.GuestViewHolder>() {

    inner class GuestViewHolder(val binding: ItemGuestStarBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(guest: GuestStar) {
            binding.guestNameTextView.text = guest.name
            binding.guestCharacterTextView.text = guest.character

            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + guest.profilePath)
                .placeholder(R.drawable.user) // Placeholder
                .centerCrop()
                .into(binding.guestImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestViewHolder {
        val binding =
            ItemGuestStarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuestViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuestViewHolder, position: Int) {
        holder.bind(guestStars[position])
    }

    override fun getItemCount() = guestStars.size
}