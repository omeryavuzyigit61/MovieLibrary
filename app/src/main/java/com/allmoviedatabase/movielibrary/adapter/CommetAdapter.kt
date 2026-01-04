package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemCommentBinding
import com.allmoviedatabase.movielibrary.model.Comment
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class CommentsAdapter : ListAdapter<Comment, CommentsAdapter.CommentViewHolder>(CommentDiffCallback()) {

    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val binding = ItemCommentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CommentViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class CommentViewHolder(private val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(comment: Comment) {
            binding.apply {
                tvUserName.text = comment.userName
                tvContent.text = comment.content

                // Tarih formatlama
                val date = comment.timestamp?.toDate()
                val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("tr"))
                tvDate.text = if (date != null) format.format(date) else "Az önce"

                // Avatar
                Glide.with(itemView.context)
                    .load(comment.userAvatarUrl)
                    .circleCrop()
                    .into(imgUserAvatar)

                // 1. DURUM KONTROLÜ (Yayında mı, İnceleniyor mu?)
                if (comment.status == 0) {
                    // Eğer yorum onay bekliyorsa ve benim yorumumsa
                    if (comment.userId == currentUserId) {
                        tvStatus.visibility = View.VISIBLE
                        tvStatus.text = "(İnceleniyor)"
                        root.alpha = 0.6f // Biraz silik yap
                    }
                    // Başkasının onaylanmamış yorumunu ViewModel'de zaten filtreleyeceğiz ama
                    // güvenlik için burada da gizleyebilirsin.
                } else {
                    tvStatus.visibility = View.GONE
                    root.alpha = 1.0f
                }

                // 2. SPOILER KONTROLÜ
                if (comment.isSpoiler) {
                    layoutSpoilerWarning.visibility = View.VISIBLE
                    tvContent.visibility = View.GONE // İçeriği gizle

                    btnShowSpoiler.setOnClickListener {
                        tvContent.visibility = View.VISIBLE
                        layoutSpoilerWarning.visibility = View.GONE
                    }
                } else {
                    layoutSpoilerWarning.visibility = View.GONE
                    tvContent.visibility = View.VISIBLE
                }
            }
        }
    }

    class CommentDiffCallback : DiffUtil.ItemCallback<Comment>() {
        override fun areItemsTheSame(oldItem: Comment, newItem: Comment) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Comment, newItem: Comment) = oldItem == newItem
    }
}