package com.machina.ppb_kelompok_1

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import coil.request.CachePolicy
import com.machina.ppb_kelompok_1.databinding.ItemPostBinding
import com.machina.ppb_kelompok_1.network.Post

class PostAdapter(
) : RecyclerView.Adapter<ItemPost>() {

    private val diffCallback = object : DiffUtil.ItemCallback<Post>() {
        override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
            return oldItem.id == newItem.id &&
                    oldItem.path == newItem.path &&
                    oldItem.name == newItem.name
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    var dataSet: List<Post>
        get() = differ.currentList
        set(value) = differ.submitList(value)



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemPost {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemPostBinding.inflate(inflater, parent, false)

        return ItemPost(binding)
    }

    override fun onBindViewHolder(holder: ItemPost, position: Int) {
        holder.onBind(dataSet[position])
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}

class ItemPost(
    private val binding: ItemPostBinding,
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(data: Post) {
        with(binding) {
            itemPostName.text = data.name
            itemPostDisplayPicture.load("https://i.pravatar.cc/300")
            itemPostImage.load("http://${data.path}") {
                memoryCachePolicy(CachePolicy.DISABLED)
            }
        }
    }
}