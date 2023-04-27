import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobilproje.AnnouncementListAdapter
import com.example.mobilproje.R
import com.example.mobilproje.databinding.FragmentAnnouncementListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class AnnouncementListFragment : Fragment() {
    val database = FirebaseDatabase.getInstance().reference
    private var _binding: FragmentAnnouncementListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private val announcementList = mutableListOf<Announcement>()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAnnouncementListBinding.inflate(inflater, container, false)
        recyclerView = binding.list
        binding.addAnnouncementButton.setOnClickListener {
            findNavController().navigate(R.id.action_announcementListFragment_to_addAnnouncement)
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        announcementList.addAll(getAllAnnouncements())
        val adapter = AnnouncementListAdapter(announcementList)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }


    private fun getAllAnnouncements(): MutableList<Announcement>{
        val announcementRef = database.child("announcement")
        val announcementList = mutableListOf<Announcement>()

        announcementRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (snapshot in dataSnapshot.children) {
                    val announcement = snapshot.getValue(Announcement::class.java)
                    if(!compareDates(announcement!!.date))
                        announcementList.add(announcement)
                    else{
                        announcementRef.child(snapshot.key!!).removeValue()
                    }
                }
                // Burada announcementList içindeki verileri kullanarak adapter'ınızı oluşturabilirsiniz
                // Örneğin:
                val adapter = AnnouncementListAdapter(announcementList)
                recyclerView.adapter = adapter
            }

            override fun onCancelled(error: DatabaseError) {
                // Hata durumunda burası çalışır
                Log.e("AnnouncementFragment", "Failed to read announcements", error.toException())
            }
        })
    return announcementList
    }

    fun compareDates(firstDate: String): Boolean {
        val formatter = SimpleDateFormat("dd-MM-yyyy")
        val currentDate = formatter.format(Date())
        return formatter.parse(firstDate)?.before(formatter.parse(currentDate)) ?: false
    }

}
