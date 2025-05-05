import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class MainViewModel(private val dao: DeliveryDao) : ViewModel() {

    fun saveQuantity(date: String, quantity: Int) {
        viewModelScope.launch {
            val record = DeliveryRecord(date = date, quantity = quantity)
            dao.insertRecord(record)
        }
    }

    fun loadQuantity(date: String, onResult: (DeliveryRecord?) -> Unit) {
        viewModelScope.launch {
            val record = dao.getRecordByDate(date)
            onResult(record)
        }
    }
}
