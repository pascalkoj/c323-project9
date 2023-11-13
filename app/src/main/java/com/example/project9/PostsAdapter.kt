package com.example.project9

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.project9.databinding.PostItemBinding
import com.example.project9.model.Post
import java.math.BigInteger
import java.security.MessageDigest


class PostsAdapter(val context: Context)
    : ListAdapter<Post, PostsAdapter.PostItemViewHolder>(PostDiffItemCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int)
            : PostItemViewHolder = PostItemViewHolder.inflateFrom(parent)
    override fun onBindViewHolder(holder: PostItemViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, context)
    }

    class PostItemViewHolder(val binding: PostItemBinding)
        : RecyclerView.ViewHolder(binding.root) {

        companion object {
            fun inflateFrom(parent: ViewGroup): PostItemViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = PostItemBinding.inflate(layoutInflater, parent, false)
                return PostItemViewHolder(binding)
            }
        }
        // load the image into our view along with the user's profile icon
        fun bind(post: Post, context: Context) {
            val username = post.user?.email as String
            binding.tvUsername.text = username
            binding.tvDescription.text = post.description
            Glide.with(context).load(post.imageUrl).into(binding.ivPost)
            Glide.with(context).load(getProfileImageUrl(username)).into(binding.ivProfileImage)
            binding.tvRelativeTime.text = DateUtils.getRelativeTimeSpanString(post.creationTimeMs)
            binding.ivPost.setOnClickListener {
                // TODO: display image fullscreen
                val imageView = binding.ivPost
                
            }
        }
        // get random image from gravatar for the user's profile image
        private fun getProfileImageUrl(username: String): String {
            val digest = MessageDigest.getInstance("MD5")
            val hash = digest.digest(username.toByteArray())
            val bigInt = BigInteger(hash)
            val hex = bigInt.abs().toString(16)
            return "https://www.gravatar.com/avatar/$hex?d=identicon"
        }
    }
}