package com.cs407.badgerbuzz

import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import android.widget.TextView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class MapsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var googleMap: GoogleMap
    private val db = FirebaseFirestore.getInstance()

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        val Madison = LatLng(43.07340550591327, -89.40070146109815)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Madison, 15f))

        map.setOnMarkerClickListener { marker ->
            markersMap.forEach { entry ->
                if (entry.value.title == marker.title) {
                    db.collection("events")
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            for (document in querySnapshot) {
                                if (document.id == entry.key) {
                                    val bundle = Bundle()
                                    bundle.putString("eventName", document.getString("eventName"))
                                    bundle.putString(
                                        "description",
                                        document.getString("description")
                                    )
                                    bundle.putString("imageUrl", document.getString("imageUrl"))
                                    val location = document.getGeoPoint("location")
                                    if (location != null) {
                                        bundle.putDouble("latitude", location.latitude)
                                        bundle.putDouble("longitude", location.longitude)
                                    }
                                    val start =
                                        document.getTimestamp("startTime")?.toDate().toString()
                                    bundle.putString("startTime", start)
                                    val end = document.getTimestamp("endTime")?.toDate().toString()
                                    bundle.putString("endTime", end)
                                    parentFragmentManager.beginTransaction()
                                        .replace(R.id.fragmentContainerView, ViewEventFragment.newInstance(bundle), null)
                                        .setReorderingAllowed(true)
                                        .addToBackStack("viewing event")
                                        .commitAllowingStateLoss()
                                }
                            }
                        }
                        .addOnFailureListener { exception ->
                            exception.printStackTrace()
                        }
                }
            }
            true
        }

        addMarkers()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
    }

    private val markersMap = mutableMapOf<String, com.google.android.gms.maps.model.Marker>()
    private fun addMarkers() {
        db.collection("events")
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot) {
                    val eventId = document.id
                    val eventName = document.getString("eventName")
                    val location = document.getGeoPoint("location")

                    if (location != null) {
                        val latLng = LatLng(location.latitude, location.longitude)

                        val marker = googleMap.addMarker(
                            MarkerOptions()
                                .position(latLng)
                                .title(eventName)
                        )
                        if (marker != null) {
                            markersMap[eventId] = marker
                        }
                    }
                }
            }
            .addOnFailureListener { exception ->
                exception.printStackTrace()
            }
    }

    private fun listenForEventRemovals() {
        db.collection("events")
            .addSnapshotListener { snapshots, error ->
                for (change in snapshots!!.documentChanges) {
                    when (change.type) {
                        com.google.firebase.firestore.DocumentChange.Type.REMOVED -> {
                            val eventId = change.document.id
                            val marker = markersMap[eventId]
                            marker?.remove()
                            markersMap.remove(eventId)
                        }


                        else -> {}
                    }
                }
            }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val viewList = view.findViewById<TextView>(R.id.viewAllEvents)
        viewList.setOnClickListener() {
            navigateToFragment(EventListFragment::class.java, "showing list of Events")

        }
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        listenForEventRemovals()


        val menuIcon: ImageView = view.findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener { v ->
            val popupMenu = PopupMenu(requireContext(), v)
            popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_logout -> {
                        Firebase.auth.signOut()
                        clearBackStack()
                        navigateToFragment(LoginFragment::class.java, null)
                        true
                    }

                    else -> false
                }
            }
            popupMenu.show()
        }


        val spinner: Spinner = view.findViewById(R.id.filterTests)
        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.filter_array,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }

        val fab = view.findViewById<FloatingActionButton>(R.id.fab)
        fab.setOnClickListener {
            navigateToFragment(LocationFragment::class.java, "showing location")
        }


    }

    private fun navigateToFragment(fragmentClass: Class<out Fragment>, backStackName: String?) {

        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragmentClass, null)
            .setReorderingAllowed(true)
            .addToBackStack(backStackName)
            .commitAllowingStateLoss()
    }

    private fun clearBackStack() {
        val fragmentManager = parentFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            for (i in 0 until fragmentManager.backStackEntryCount) {
                fragmentManager.popBackStack()
            }
        }
    }


}