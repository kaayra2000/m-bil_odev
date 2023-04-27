package com.example.mobilproje

import Announcement
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_announcement.view.*
import kotlinx.android.synthetic.main.fragment_announcement.view.imageView
import kotlinx.android.synthetic.main.user_item.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AnnouncementListAdapter (private val annList: List<Announcement>) :
    RecyclerView.Adapter<AnnouncementListAdapter.AnnouncementListViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementListViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fragment_announcement, parent, false)
        return AnnouncementListViewHolder(view)
    }



    override fun onBindViewHolder(holder: AnnouncementListViewHolder, position: Int) {
        val user = annList[position]
        holder.bind(user)
    }

    override fun getItemCount(): Int {
        return annList.size
    }

    inner class AnnouncementListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(announcement: Announcement) = GlobalScope.launch {
            withContext(Dispatchers.Main){
                itemView.imageView.setImageBitmap(announcement.photo.let { convertStringToBitmap(it) })
                itemView.imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                itemView.upperLinearText.text = announcement.message
                itemView.lowerLinearText.text = announcement.date
                itemView.titleText.text = announcement.title
            }


        }

    }




    fun convertStringToBitmap(encodedString: String): Bitmap? {
        return try {
            val encodeByte = Base64.decode(encodedString, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.size)
        } catch (e: Exception) {
            e.message?.let { Log.d("Error", it) }
            null
        }
    }
}