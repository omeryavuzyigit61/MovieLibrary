package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemListSelectionBinding
import com.allmoviedatabase.movielibrary.model.UserList

class UserListSelectionAdapter(private val onItemClick: (UserList) -> Unit) :
    ListAdapter<UserList, UserListSelectionAdapter.ListViewHolder>(DiffCallback) {

    class ListViewHolder(private val binding: ItemListSelectionBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: UserList, onItemClick: (UserList) -> Unit) {
            binding.tvListName.text = item.listName
            binding.tvListCount.text = "${item.itemCount} Film"

            // Eğer istersen item.isPublic vs gibi ikonlar da koyabilirsin.

            binding.root.setOnClickListener { onItemClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemListSelectionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<UserList>() {
        override fun areItemsTheSame(oldItem: UserList, newItem: UserList): Boolean = oldItem.listId == newItem.listId
        override fun areContentsTheSame(oldItem: UserList, newItem: UserList): Boolean = oldItem == newItem
    }
}