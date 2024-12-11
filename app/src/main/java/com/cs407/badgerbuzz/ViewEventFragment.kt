package com.cs407.badgerbuzz

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ViewEventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ViewEventFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        return inflater.inflate(R.layout.fragment_view_event, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ViewEventFragment.
         */
        @JvmStatic
        fun newInstance(bundle: Bundle) =
            ViewEventFragment().apply {
                arguments = bundle
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bundle = arguments
        val eventTitleTextView = view.findViewById<TextView>(R.id.eventTitleTextView)
        val eventImageView = view.findViewById<ImageView>(R.id.eventImageView)
        val eventDescriptionTextView = view.findViewById<TextView>(R.id.eventDescriptionTextView)
        val eventStartTextView = view.findViewById<TextView>(R.id.eventStartTextView)
        val eventEndTextView = view.findViewById<TextView>(R.id.eventEndTextView)
        if (bundle != null) {
            val storage = Firebase.storage
            val storageRef = storage.reference
            val pathReference = storageRef.child(bundle.getString("imageUrl").toString())
            var bytes = ByteArray(0)
            pathReference.getBytes(1024*1024).addOnSuccessListener { byteArray ->
                bytes = byteArray
                eventImageView.setImageBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.size))
            }.addOnFailureListener {
                bytes = ByteArray(0)
            }
            eventTitleTextView.text = bundle.getString("eventName")
            eventDescriptionTextView.text = bundle.getString("description")
            eventStartTextView.text = "Starts: ${bundle.getString("startTime")}"
            eventEndTextView.text = "Ends: ${bundle.getString("endTime")}"
        }
    }
}