package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.IncidentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Query("SELECT * FROM incident_logs ORDER BY timestamp DESC")
    fun getAllIncidents(): Flow<List<IncidentEntity>>

    @Query("SELECT * FROM incident_logs ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentIncidents(limit: Int): Flow<List<IncidentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: IncidentEntity): Long

    @Query("DELETE FROM incident_logs")
    suspend fun clearAll()

    @Query("DELETE FROM incident_logs WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM incident_logs")
    fun getCount(): Flow<Int>
}
