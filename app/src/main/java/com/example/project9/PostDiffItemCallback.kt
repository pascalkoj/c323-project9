package com.example.project9

import androidx.recyclerview.widget.DiffUtil
import com.example.project9.model.Post

class PostDiffItemCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post)
            = (oldItem.description == newItem.description)
    override fun areContentsTheSame(oldItem: Post, newItem: Post) = (oldItem == newItem)
}