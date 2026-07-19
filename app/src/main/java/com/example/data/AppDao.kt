package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    // Bill Records Queries
    @Query("SELECT * FROM bill_records ORDER BY billingDate DESC")
    fun getAllBillRecords(): Flow<List<BillRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBillRecord(record: BillRecord): Long

    @Query("DELETE FROM bill_records WHERE id = :id")
    suspend fun deleteBillRecord(id: Int)

    // Appliances Queries
    @Query("SELECT * FROM appliances ORDER BY id ASC")
    fun getAllAppliances(): Flow<List<Appliance>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppliance(appliance: Appliance): Long

    @Query("DELETE FROM appliances WHERE id = :id")
    suspend fun deleteAppliance(id: Int)

    @Query("DELETE FROM appliances")
    suspend fun deleteAllAppliances()
}
