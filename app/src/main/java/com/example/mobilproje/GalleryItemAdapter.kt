import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.mobilproje.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.gallery_item.view.*
import kotlinx.android.synthetic.main.toast_message.view.*
import kotlinx.android.synthetic.main.user_item.view.*

class GalleryItemAdapter(private val galleryItems: List<GalleryItem>,private val fragment: Fragment
, private val userName: String) : RecyclerView.Adapter<GalleryItemAdapter.GalleryItemViewHolder>() {

    val database = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
        return GalleryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryItemViewHolder, position: Int) {
        val galleryItem = galleryItems[position]
        holder.bind(galleryItem)
    }

    override fun getItemCount(): Int {
        return galleryItems.size
    }

    inner class GalleryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(galleryItem: GalleryItem) {
            itemView.date.text = "Upload Date: "+galleryItem.date
            Glide.with(itemView)
                .load(galleryItem.downloadUrl)
                .apply(RequestOptions().centerCrop())
                .into(itemView.thumbnail_view)





            itemView.thumbnail_view.setOnClickListener {
                var bundle = bundleOf("isOwner" to false)
                if(userName.equals(galleryItem.ownerUserName)){
                    bundle = bundleOf("isOwner" to true)
                }
                bundle.putString("url",galleryItem.downloadUrl)
                bundle.putString("mediaID",galleryItem.id)
                fragment.findNavController().navigate(R.id.action_galleryFragment_to_viewAndDeletePhoto, bundle)
            }
        }
    }

}
