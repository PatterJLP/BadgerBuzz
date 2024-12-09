package com.cs407.badgerbuzz


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"
private lateinit var auth: FirebaseAuth
private lateinit var googleMap: GoogleMap



/**
 * A simple [Fragment] subclass.
 * Use the [EventListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

data class Event(
    val title: String,
    val date: String,
    val location: String
)


class EventListFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private val db = FirebaseFirestore.getInstance()
    private val list = mutableListOf<Event>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retrieveFromDB()
        auth = Firebase.auth
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?

    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.event_list_fragment, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
    private fun retrieveFromDB(){
        db.collection("events")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val eventId = document.id
                    val eventName = document.getString("eventName")
                    val location = document.getGeoPoint("location")

                    val eventDetails = Event(eventId, eventName.toString(), location.toString())
                    list.add(eventDetails)
                    addEvent(eventDetails)


                }


            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun addEvent(eventDetails: Event){
        val eventContainer: LinearLayout = requireView().findViewById(R.id.eventContainer)
        val eventView = TextView(requireContext())
        eventView.text = "Event: ${eventDetails.title}\nDate: (${eventDetails.date},\n Location: ${eventDetails.location})"
        eventView.textSize = 16f
        eventView.setPadding(16, 16, 16, 16)

        // Add the TextView to the LinearLayout
        eventContainer.addView(eventView)



    }

}
