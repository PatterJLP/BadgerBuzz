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
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import com.cs407.badgerbuzz.DatePickerFragment.OnDateSelectedListener
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import java.util.Calendar
import java.util.Date
import kotlin.math.log
import kotlin.math.min

class PostEventFragment : Fragment(), DatePickerFragment.OnDateSelectedListener,TimePickerFragment.OnTimeSelectedListener {
    val db = FirebaseFirestore.getInstance()
    private var startDate: String? = null
    private var endDate: String? = null
    private var startTime: String? = null
    private var endTime: String? = null
    private var eventName: String? = null
    private var description: String? = null
    private var latitude: Double? = null
    private var longitude: Double? = null
    private var imageURL: String? = null




    private fun addEvent(eventName: String,description: String, imageUrl: String, latitude: Double, longitude: Double, startTime: String, endTime: String, startDate: String, endDate: String){
        val event = hashMapOf(
            "eventName" to eventName,
            "description" to description,
            "imageUrl" to imageUrl,
            "location" to GeoPoint(latitude, longitude),
            "startTime" to startTime,
            "endTime" to endTime,
            "startDate" to startDate,
            "endDate" to endDate

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

        requireView().findViewById<ImageButton>(R.id.eventTimeButton).setOnClickListener {
            val timePickerFragment = TimePickerFragment()
            timePickerFragment.setOnTimeSelectedListener(this)
            timePickerFragment.show(parentFragmentManager, "timePicker")
        }
        requireView().findViewById<ImageButton>(R.id.eventEndTimeButton).setOnClickListener {
            val timePickerFragment = TimePickerFragment()
            timePickerFragment.setOnTimeSelectedListener(this)
            timePickerFragment.show(parentFragmentManager, "timePicker")
        }
        requireView().findViewById<ImageButton>(R.id.currentLocationButton).setOnClickListener{
            navigateToFragment(LocationFragment::class.java,"showing location")
        }
        parentFragmentManager.setFragmentResultListener("locationKey",viewLifecycleOwner){_, bundle ->
            latitude = bundle.getDouble("latitude")
            longitude = bundle.getDouble("longitude")
            val latLng = requireView().findViewById<TextView>(R.id.EventLocationEditText)
            latLng.text = "$latitude $longitude"


        }


        requireView().findViewById<Button>(R.id.postEventButton).setOnClickListener {
            eventName = requireView().findViewById<EditText>(R.id.eventNameEditText).text.toString()
            description = requireView().findViewById<EditText>(R.id.eventDescriptionEditText).text.toString()
            if(eventName == null || description == null || latitude == null || longitude == null || startTime == null || endTime == null || startDate == null || endDate == null){
                Toast.makeText(
                    requireContext(),
                    "Please Make Sure All Fields Are Filled",
                    Toast.LENGTH_SHORT,
                ).show()
            }else {
                if (imageURL == null){
                    imageURL = "https://brand.wisc.edu/content/uploads/2023/09/vert-w-crest-logo-web-digital-color.png"
                }
                addEvent(eventName!!,description!!,imageURL!!,latitude!!,longitude!!,startTime!!,endTime!!,startDate!!,endDate!!)
            }
            navigateToFragment(MapsFragment::class.java, "showing Map")
        }
    }


    override fun onDateSelected(date: String) {
        if (startDate == null) {
            startDate = date
            val startEvent = requireView().findViewById<TextView>(R.id.eventDateEditText)

            startEvent.text = startDate
        } else {
            endDate = date
            val endEvent = requireView().findViewById<TextView>(R.id.eventEndDateEditText)
            endEvent.text = endDate
        }
    }
    override fun onTimeSelected(time: String) {
        if (startTime == null) {
            startTime = time
            val startEvent = requireView().findViewById<TextView>(R.id.eventDateEditText)
            startEvent.text = "$startDate $startTime"
        } else {
            endTime = time
            val endEvent = requireView().findViewById<TextView>(R.id.eventEndDateEditText)
            endEvent.text = "$endDate $endTime"
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
        private var listener: OnTimeSelectedListener? = null

        interface OnTimeSelectedListener {
            fun onTimeSelected(time: String)
        }
        fun setOnTimeSelectedListener(listener: OnTimeSelectedListener) {
            this.listener = listener
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            // Use the current time as the default values for the picker.
            val c = Calendar.getInstance()
            val hour = c.get(Calendar.HOUR_OF_DAY)
            val minute = c.get(Calendar.MINUTE)

            // Create a new instance of TimePickerDialog and return it.
            return TimePickerDialog(activity, this, hour, minute, DateFormat.is24HourFormat(activity))
        }

        override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
            val selectedTime = "$hourOfDay:$minute"
            listener?.onTimeSelected(selectedTime)
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