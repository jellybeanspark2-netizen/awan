package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "appliances")
data class Appliance(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val wattage: Double, // in Watts
    val dailyHours: Double, // hours used per day
    val quantity: Int = 1
) {
    // Calculated property: Monthly consumption in kWh
    val monthlyKwh: Double
        get() = (wattage * dailyHours * quantity * 30.0) / 1000.0
}
