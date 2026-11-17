package com.example.trackrdata.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.trackrdata.data.entities.Visit
import kotlinx.coroutines.flow.Flow

@Dao
interface VisitDao {
    @Query("SELECT * FROM visit")
    suspend fun getAll(): List<Visit>

    @Query("""
        SELECT * FROM visit
        ORDER BY arrived_at DESC, uid DESC
    """)
    fun observeAll(): Flow<List<Visit>>

    @Query("SELECT * FROM visit WHERE uid IN (:visitIds)")
    suspend fun loadAllByIds(visitIds: IntArray): List<Visit>

    @Insert
    suspend fun insertVisits(vararg visits: Visit)

    @Delete
    suspend fun delete(visit: Visit)

    @Query("""
        SELECT * FROM visit
        WHERE location_uid = :locationId
        ORDER BY arrived_at DESC, uid DESC
    """)
    fun observeVisitsForLocation(locationId: Int): Flow<List<Visit>>

    @Query("""
        UPDATE visit
        SET departed_at = :departedAt
        WHERE uid = :visitId
            AND departed_at IS NULL
            AND arrived_at < :departedAt
    
    """)
    suspend fun endVisit(visitId: Int, departedAt: Long): Int

    @Query("SELECT * FROM visit WHERE departed_at IS NULL")
    suspend fun getActiveVisits(): List<Visit>

    @Query("""
        UPDATE visit
        SET departed_at = :departedAt
        WHERE departed_at IS NULL
    """)
    suspend fun endActiveVisits(departedAt: Long): Int
}