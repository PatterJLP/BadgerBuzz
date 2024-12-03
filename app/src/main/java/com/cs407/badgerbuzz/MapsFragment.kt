package com.cs407.badgerbuzz

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



class MapsFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    private val callback = OnMapReadyCallback { googleMap ->

        val Madison = LatLng(43.07340550591327, -89.40070146109815)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Madison, 15f))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
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
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)


        val menuIcon: ImageView = view.findViewById(R.id.menu_icon)
        menuIcon.setOnClickListener { v ->
            val popupMenu = PopupMenu(requireContext(), v)
            popupMenu.menuInflater.inflate(R.menu.menu_main, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_logout -> {
                        Firebase.auth.signOut()
                        clearBackStack()
                        navigateToFragment(LoginFragment::class.java,null)
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
            navigateToFragment(PostEventFragment::class.java, "showing Post Event")
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