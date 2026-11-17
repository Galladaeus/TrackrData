package com.example.trackrdata.data.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.trackrdata.data.entities.LocationSample;

import kotlinx.coroutines.flow.Flow;

@Dao
interface LocationSampleDao {
    @Insert
    suspend fun insert(sample: LocationSample): Long

    @Query("""
        SELECT * FROM location_samples
        WHERE session_id = :sessionId
        ORDER BY recorded_at ASC, id ASC
    """)
    fun observeForSession(
        sessionId: Long
    ): Flow<List<LocationSample>>

    @Query("""
        SELECT COUNT(*) FROM location_samples
        WHERE session_id = :sessionId
    """)
    fun observeCountForSession(
        sessionId: Long
    ): Flow<Int>
}
