import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilproje.R
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.gallery_item.view.*
import kotlinx.android.synthetic.main.user_item.view.*

class GalleryItemAdapter(private val galleryItems: List<GalleryItem>,private val fragment: Fragment
, private val userName: String) : RecyclerView.Adapter<GalleryItemAdapter.GalleryItemViewHolder>() {

    val database = FirebaseDatabase.getInstance().reference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.gallery_item, parent, false)
        return GalleryItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryItemViewHolder, position: Int) {
        holder.bind()
    }

    override fun getItemCount(): Int {
        return galleryItems.size
    }

    inner class GalleryItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val thumbnailView: ImageView = itemView.findViewById(R.id.thumbnail_view)

        fun bind() {
            itemView.thumbnail_view.setOnClickListener {
                val bundle = bundleOf("userName" to userName)
                fragment.findNavController().navigate(R.id.action_galleryFragment_to_viewAndDeletePhoto, bundle)
            }
        }
    }

}
