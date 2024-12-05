import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.type.LatLng

class SharedViewModel : ViewModel() {
    private val _selectedLocation = MutableLiveData<LatLng>()
    val selectedLocation: LiveData<LatLng> get() = _selectedLocation

    fun setSelectedLocation(location: LatLng) {
        _selectedLocation.value = location
    }
}
