package com.ericho.myhospital.data.source.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ericho.myhospital.model.HospitalEntity

@Dao
interface HospitalDao {
    @Query("SELECT * FROM hospitals")
    suspend fun getAll(): List<HospitalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<HospitalEntity>)

    @Query("DELETE FROM hospitals")
    suspend fun clearAll()
}
