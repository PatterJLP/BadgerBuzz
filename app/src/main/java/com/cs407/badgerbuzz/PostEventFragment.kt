package com.cs407.badgerbuzz

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import android.text.format.DateFormat
import android.util.Log
import android.widget.Button
import android.widget.DatePicker
import android.widget.ImageButton
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.Calendar
import java.util.Date
import kotlin.math.log

class PostEventFragment : Fragment(), DatePickerFragment.OnDateSelectedListener {
    val db = FirebaseFirestore.getInstance()
    private var startDate: String? = null
    private var endDate: String? = null



    private fun addEvent(eventName: String,description: String, imageUrl: String, latitude: Double, longitude: Double, startTime: Date, endTime: Date){
        val event = hashMapOf(
            "eventName" to eventName,
            "description" to description,
            "imageUrl" to imageUrl,
            "location" to GeoPoint(latitude, longitude),
            "startTime" to Timestamp(startTime),
            "endTime" to Timestamp(endTime)
        )
        db.collection("events")
            .add(event)
            .addOnSuccessListener { documentReference ->
                Log.d("Firestore", "Event added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Error adding event", e)
            }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_event, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set listeners for date pickers
        requireView().findViewById<ImageButton>(R.id.eventDateButton).setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.setOnDateSelectedListener(this)  // Set the listener
            datePickerFragment.show(parentFragmentManager, "startDatePicker")
        }

        requireView().findViewById<ImageButton>(R.id.eventEndDateButton).setOnClickListener {
            val datePickerFragment = DatePickerFragment()
            datePickerFragment.setOnDateSelectedListener(this)  // Set the listener
            datePickerFragment.show(parentFragmentManager, "endDatePicker")
        }

        // Handle the post event button logic
        requireView().findViewById<Button>(R.id.postEventButton).setOnClickListener {
            navigateToFragment(MapsFragment::class.java, "showing Map")
        }
    }


    override fun onDateSelected(date: String) {
        if (startDate == null) {
            startDate = date
            Log.d("PostEventFragment", "Start Date: $startDate")
        } else {
            endDate = date
            Log.d("PostEventFragment", "End Date: $endDate")
        }
    }

    private fun navigateToFragment(fragmentClass: Class<out Fragment>, backStackName: String) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainerView, fragmentClass, null)
            .setReorderingAllowed(true)
            .addToBackStack(backStackName)
            .commitAllowingStateLoss()
    }
}




    class TimePickerFragment : DialogFragment(), TimePickerDialog.OnTimeSetListener {

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker.
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Create a new instance of TimePickerDialog and return it.
            return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            // Do something with the time the user picks.
        }
    }


class DatePickerFragment : DialogFragment(), DatePickerDialog.OnDateSetListener {
    private var listener: OnDateSelectedListener? = null

    interface OnDateSelectedListener {
        fun onDateSelected(date: String)
    }

    fun setOnDateSelectedListener(listener: OnDateSelectedListener) {
        this.listener = listener
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker.
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        // Create a new instance of DatePickerDialog and return it.
        return DatePickerDialog(requireContext(), this, year, month, day)

    }

    override fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int) {
        val selectedDate = "$month/$day/$year"
        listener?.onDateSelected(selectedDate)
    }

}