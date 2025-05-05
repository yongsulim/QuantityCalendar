import androidx.room.*

@Dao
interface DeliveryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: DeliveryRecord)

    @Query("SELECT * FROM delivery_records WHERE date = :date")
    suspend fun getRecordByDate(date: String): DeliveryRecord?

    @Query("SELECT * FROM delivery_records")
    suspend fun getAllRecords(): List<DeliveryRecord>
}
