package com.example.trackrdata.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.trackrdata.data.entities.Location
import com.example.trackrdata.data.models.LocationSummary
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {
    @Query("SELECT * FROM location ORDER BY name")
    fun observeAll(): Flow<List<Location>>

    @Delete
    suspend fun delete(location: Location)

    @Insert
    suspend fun insertLocations(vararg locations: Location)

    @Query("SELECT * FROM location WHERE uid = :id")
    fun observedById(id: Int): Flow<Location?>

    @Update
    suspend fun update(location: Location)

    @Query("SELECT * FROM location WHERE uid = :id")
    suspend fun getById(id: Int): Location?

    @Query("""
        SELECT
            l.*,
            COUNT(v.uid) AS visit_count,
            MAX(v.arrived_at) AS last_visited_at
        FROM location AS l
        LEFT JOIN visit AS v 
            ON v.location_uid = l.uid
        GROUP BY l.uid
        ORDER BY
            visit_count DESC,
            last_visited_at DESC,
            l.name COLLATE NOCASE ASC,
            l.uid ASC
    """)
    fun observeLocationSummaries(): Flow<List<LocationSummary>>
}