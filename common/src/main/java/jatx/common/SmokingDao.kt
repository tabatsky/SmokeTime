package jatx.common

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface SmokingDao {
    @Query("SELECT COUNT(*) FROM smoking WHERE time>=:startTime AND time<=:endTime")
    suspend fun getEventCountForTimeInterval(startTime: Long, endTime: Long): Int

    @Query("SELECT * FROM smoking ORDER BY time DESC LIMIT 1")
    suspend fun getLastEvent(): SmokeEventEntity?

    @Query("SELECT * FROM smoking ORDER BY time DESC")
    suspend fun getAllEvents(): List<SmokeEventEntity>

    @Insert
    suspend fun addEvent(smokeEventEntity: SmokeEventEntity)

    @Query("DELETE FROM smoking WHERE id=:id")
    suspend fun deleteById(id: Long)
}