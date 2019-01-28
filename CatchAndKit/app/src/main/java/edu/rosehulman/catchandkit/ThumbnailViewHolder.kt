package edu.rosehulman.catchandkit

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.thumbnail_item.view.*

class ThumbnailViewHolder(
    itemView: View,
    val context: Context,
    adapter: ThumbnailAdapter
) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView = itemView.thumbnail

    init {
        itemView.setOnClickListener {
            adapter.onThumbnailSelected(adapterPosition)
        }
    }

    fun bind(thumbnail: Thumbnail) {
        Log.d(Constants.TAG, "URL: ${thumbnail.url}")
        val bitmap =
            BitmapUtils.scaleToFit(context, thumbnail.url, 100, 100)
        if (bitmap == null) {
            Picasso.get()
                .load(thumbnail.url)
                .into(imageView)
        }
    }
}