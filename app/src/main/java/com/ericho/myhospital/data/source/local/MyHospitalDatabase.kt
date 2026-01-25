package com.ericho.myhospital.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ericho.myhospital.model.HospitalEntity

@Database(
    entities = [HospitalEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class MyHospitalDatabase : RoomDatabase() {
    abstract fun hospitalDao(): HospitalDao
}
