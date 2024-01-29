package jatx.common

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "smoking")
data class SmokeEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val time: Long
)
