package edu.rosehulman.catchandkit

import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot

class ThumbnailAdapter(
    val context: Context,
    private val listener: ThumbnailGridFragment.OnThumbnailListener?
) : RecyclerView.Adapter<ThumbnailViewHolder>() {
    private val thumbnails = ArrayList<Thumbnail>()

    val thumbnailRef = FirebaseFirestore
        .getInstance()
        .collection(Constants.THUMBNAIL_COLLECTION)

    // TODO: Add storage ref.


    init {
        thumbnailRef.addSnapshotListener { snapshot: QuerySnapshot?, exception: FirebaseFirestoreException? ->
            Log.d(Constants.TAG, "Adding snapshot listener")
            if (exception != null) {
                Log.e(Constants.TAG, "Firebase error: $exception")
            }
            processThumbnailDiffs(snapshot!!)
        }
    }

    private fun processThumbnailDiffs(snapshot: QuerySnapshot) {
        for (documentChange in snapshot.documentChanges) {
            val thumbnail = Thumbnail.fromSnapshot(documentChange.document)
            when (documentChange.type) {
                DocumentChange.Type.ADDED -> {
                    if (thumbnail.url.isNotEmpty()) {
                        thumbnails.add(0, thumbnail)
                        notifyItemInserted(0)
                    }
                }
                DocumentChange.Type.REMOVED -> {
                    val index = thumbnails.indexOfFirst { it.id == thumbnail.id }
                    thumbnails.removeAt(index)
                    notifyItemRemoved(index)
                }
                DocumentChange.Type.MODIFIED -> {
                    val index = thumbnails.indexOfFirst { it.id == thumbnail.id }
                    thumbnails[index] = thumbnail
                    notifyItemChanged(index)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ThumbnailViewHolder {
        val itemView = LayoutInflater.from(context).inflate(R.layout.thumbnail_item, parent, false)
        return ThumbnailViewHolder(itemView, context, this)
    }

    override fun getItemCount() = thumbnails.size

    override fun onBindViewHolder(viewHolder: ThumbnailViewHolder, position: Int) {
        viewHolder.bind(thumbnails[position])
    }

    fun onThumbnailSelected(position: Int) {
        listener?.onThumbnailSelected(thumbnails[position])
    }

    fun add(localPath: String) {
        // TODO: You'll want to wait to add this to Firetore until after you have a Storage download URL.
        // Move this line of code there.
        thumbnailRef.add(Thumbnail(localPath))
        ImageRescaleTask(localPath).execute()
    }

    // Could save a smaller version to Storage to save time on the network.
    // But if too small, recognition accuracy can suffer.
    inner class ImageRescaleTask(val localPath: String) : AsyncTask<Void, Void, Bitmap>() {
        override fun doInBackground(vararg p0: Void?): Bitmap? {
            // Reduces length and width by a factor (currently 2).
            val ratio = 2
            return BitmapUtils.rotateAndScaleByRatio(context, localPath, ratio)
        }

        override fun onPostExecute(bitmap: Bitmap?) {
            // TODO: Write and call a new storageAdd() method with the path and bitmap
            // that uses Firebase storage.
            // https://firebase.google.com/docs/storage/android/upload-files
        }
    }

}