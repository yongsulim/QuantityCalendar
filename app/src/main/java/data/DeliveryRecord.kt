import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery_records")
data class DeliveryRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,  // 날짜 예: "2025-05-03"
    val quantity: Int  // 배송한 수량
)
