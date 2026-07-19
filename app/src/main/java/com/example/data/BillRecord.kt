package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bill_records")
data class BillRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val previousReading: Double,
    val currentReading: Double,
    val consumptionKwh: Double,
    val calculatedCost: Double,
    val fixedCharge: Double,
    val taxAmount: Double,
    val totalBill: Double,
    val billingDate: Long = System.currentTimeMillis(),
    val notes: String = "",
    val tariffType: String = "Flat"
)
