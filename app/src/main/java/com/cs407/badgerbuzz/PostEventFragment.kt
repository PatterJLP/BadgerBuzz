package com.cs407.badgerbuzz

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.graphics.Bitmap
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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview
import com.cs407.badgerbuzz.DatePickerFragment.OnDateSelectedListener
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.TimeZone
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
        val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")


        val startEvent = fixDate("$startDate $startTime")
        val endEvent = fixDate("$endDate $endTime")
        Log.d("timetest","$startEvent")



        val startDateParsed: Date = format.parse(startEvent) ?: Date()
        val endDateParsed: Date = format.parse(endEvent) ?: Date()



        val utcTimeZone = TimeZone.getTimeZone("UTC")
        val calendarStart = Calendar.getInstance(utcTimeZone)
        calendarStart.time = startDateParsed
        val calendarEnd = Calendar.getInstance(utcTimeZone)
        calendarEnd.time = endDateParsed

        // Convert to Firebase Timestamp (in UTC)
        val startTimestamp = Timestamp(calendarStart.time)
        val endTimestamp = Timestamp(calendarEnd.time)

        Log.d("time", "$startTimestamp")
        Log.d("timeend", "$endTimestamp")


        val event = hashMapOf(
            "eventName" to eventName,
            "description" to description,
            "imageUrl" to imageUrl,
            "location" to GeoPoint(latitude, longitude),
            "startTime" to startTimestamp,
            "endTime" to endTimestamp,
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

    fun fixDate(date: String): String {
        val parts = date.split(" ")
        val dateParts = parts[0].split("-")
        val timeParts = parts[1].split(":")

        val year = dateParts[0]
        val month = dateParts[1].padStart(2, '0')
        val day = dateParts[2].padStart(2, '0')


        val hour = timeParts[0].padStart(2, '0')
        val minute = timeParts[1].padStart(2, '0')

        return "$year-$month-$day $hour:$minute:00"
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
        requireView().findViewById<ImageButton>(R.id.currentLocationButton).setOnClickListener {
            navigateToFragment(LocationFragment::class.java, "showing location")
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

        val storage = Firebase.storage
        // Create a storage reference from our app
        val storageRef = storage.reference

        // Create a reference to "mountains.jpg"
        val imageRef = storageRef.child("mountains.jpg")

        // Create a reference to 'images/mountains.jpg'
        val mountainImagesRef = storageRef.child("images/mountains.jpg")

        // While the file names are the same, the references point to different files
        //mountainsRef.name == mountainImagesRef.name // true
        //mountainsRef.path == mountainImagesRef.path // false

        val eventImage = requireView().findViewById<ImageView>(R.id.eventImage)
        val takePicturePreview =
            registerForActivityResult(TakePicturePreview()) { thumbnail: Bitmap? ->
                eventImage.setImageBitmap(thumbnail)
            }
        requireView().findViewById<Button>(R.id.addImageButton).setOnClickListener {
            takePicturePreview.launch(null)
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
        val selectedDate = "$year-$month-$day"
        listener?.onDateSelected(selectedDate)
    }

}