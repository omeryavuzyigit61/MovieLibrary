package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemGuestStarBinding
import com.allmoviedatabase.movielibrary.model.SeasonDetail.GuestStar
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.bumptech.glide.Glide

class GuestStarAdapter(
    private val guestStars: List<GuestStar>,
    private val onGuestClick: (Int) -> Unit // YENİ: Tıklama Callback'i
) : RecyclerView.Adapter<GuestStarAdapter.GuestStarViewHolder>() {

    inner class GuestStarViewHolder(val binding: ItemGuestStarBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(guest: GuestStar) {
            binding.guestNameTextView.text = guest.name
            binding.guestCharacterTextView.text = guest.character

            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + guest.profilePath)
                .centerCrop()
                .into(binding.guestImageView)

            // Tıklama olayı
            itemView.setOnClickListener {
                guest.id?.let { id -> onGuestClick(id) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GuestStarViewHolder {
        val binding = ItemGuestStarBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return GuestStarViewHolder(binding)
    }

    override fun onBindViewHolder(holder: GuestStarViewHolder, position: Int) {
        holder.bind(guestStars[position])
    }

    override fun getItemCount(): Int = guestStars.size
}