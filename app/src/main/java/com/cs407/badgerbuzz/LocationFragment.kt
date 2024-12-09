package com.cs407.badgerbuzz

import SharedViewModel
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Spinner
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase




class LocationFragment : Fragment() {
    private lateinit var googleMap: GoogleMap

    private val callback = OnMapReadyCallback { map ->
        googleMap = map

        val Madison = LatLng(43.07340550591327, -89.40070146109815)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Madison, 15f))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        val confirmButton: Button = view.findViewById(R.id.locationButton)
        confirmButton.setOnClickListener {
                val centerLatLng = googleMap.cameraPosition.target
                val bundle = Bundle().apply {
                    putDouble("latitude",centerLatLng.latitude)
                    putDouble("longitude",centerLatLng.longitude)
                }


                parentFragmentManager.setFragmentResult("locationKey",bundle)
            navigateToFragment(PostEventFragment::class.java, "showing location")
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