package com.example.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val appDao: AppDao) {

    val allBillRecords: Flow<List<BillRecord>> = appDao.getAllBillRecords()
    val allAppliances: Flow<List<Appliance>> = appDao.getAllAppliances()

    suspend fun insertBillRecord(record: BillRecord) {
        appDao.insertBillRecord(record)
    }

    suspend fun deleteBillRecord(id: Int) {
        appDao.deleteBillRecord(id)
    }

    suspend fun insertAppliance(appliance: Appliance) {
        appDao.insertAppliance(appliance)
    }

    suspend fun deleteAppliance(id: Int) {
        appDao.deleteAppliance(id)
    }

    suspend fun prepopulateAppliancesIfEmpty(currentCount: Int) {
        if (currentCount == 0) {
            val defaults = listOf(
                Appliance(name = "Air Conditioner (AC)", wattage = 1500.0, dailyHours = 6.0, quantity = 1),
                Appliance(name = "Refrigerator", wattage = 150.0, dailyHours = 24.0, quantity = 1),
                Appliance(name = "LED Smart TV", wattage = 100.0, dailyHours = 4.0, quantity = 2),
                Appliance(name = "Water Heater", wattage = 3000.0, dailyHours = 1.0, quantity = 1),
                Appliance(name = "Microwave Oven", wattage = 1200.0, dailyHours = 0.5, quantity = 1),
                Appliance(name = "Washing Machine", wattage = 500.0, dailyHours = 1.0, quantity = 1),
                Appliance(name = "LED Bulb Light", wattage = 12.0, dailyHours = 7.0, quantity = 10),
                Appliance(name = "Desktop Gaming PC", wattage = 400.0, dailyHours = 3.0, quantity = 1)
            )
            for (appliance in defaults) {
                appDao.insertAppliance(appliance)
            }
        }
    }
}
