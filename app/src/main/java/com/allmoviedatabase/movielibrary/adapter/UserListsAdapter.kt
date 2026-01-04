package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemUserListBinding
import com.allmoviedatabase.movielibrary.model.UserList
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserListsAdapter(
    private val onListClick: (UserList) -> Unit
) : ListAdapter<UserList, UserListsAdapter.ListViewHolder>(ListDiffCallback()) {

    inner class ListViewHolder(private val binding: ItemUserListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: UserList) {
            // Liste Adı
            binding.tvListName.text = item.listName

            // Öğe Sayısı (Örn: "0 İçerik" veya "5 İçerik")
            binding.tvItemCount.text = "${item.itemCount} İçerik"

            // Tarih Formatlama (Long -> String)
            val date = Date(item.createdAt)
            val format = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            binding.tvCreatedDate.text = format.format(date)

            // Tıklama Olayı
            binding.root.setOnClickListener {
                onListClick(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemUserListBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListDiffCallback : DiffUtil.ItemCallback<UserList>() {
        override fun areItemsTheSame(oldItem: UserList, newItem: UserList) = oldItem.listId == newItem.listId
        override fun areContentsTheSame(oldItem: UserList, newItem: UserList) = oldItem == newItem
    }
}