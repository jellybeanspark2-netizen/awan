package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Appliance
import com.example.data.BillRecord
import com.example.data.AppRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BillViewModel(private val repository: AppRepository) : ViewModel() {

    val billRecords: StateFlow<List<BillRecord>> = repository.allBillRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appliances: StateFlow<List<Appliance>> = repository.allAppliances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())



    init {
        // Pre-populate default appliances if the database is empty
        viewModelScope.launch {
            repository.allAppliances.collect { list ->
                repository.prepopulateAppliancesIfEmpty(list.size)
            }
        }
    }

    // Calculate cost based on tiered slabs
    fun calculateSlabCost(kwh: Double): Double {
        if (kwh <= 0) return 0.0
        var cost = 0.0
        var remaining = kwh
        
        // Slab 1: 0 - 100 kWh @ $0.10
        val slab1Limit = 100.0
        val slab1Rate = 0.10
        if (remaining > slab1Limit) {
            cost += slab1Limit * slab1Rate
            remaining -= slab1Limit
        } else {
            cost += remaining * slab1Rate
            return cost
        }
        
        // Slab 2: 101 - 300 kWh @ $0.15 (200 units limit)
        val slab2Limit = 200.0
        val slab2Rate = 0.15
        if (remaining > slab2Limit) {
            cost += slab2Limit * slab2Rate
            remaining -= slab2Limit
        } else {
            cost += remaining * slab2Rate
            return cost
        }
        
        // Slab 3: 301 - 500 kWh @ $0.20 (200 units limit)
        val slab3Limit = 200.0
        val slab3Rate = 0.20
        if (remaining > slab3Limit) {
            cost += slab3Limit * slab3Rate
            remaining -= slab3Limit
        } else {
            cost += remaining * slab3Rate
            return cost
        }
        
        // Slab 4: Above 500 kWh @ $0.25
        val slab4Rate = 0.25
        cost += remaining * slab4Rate
        return cost
    }

    fun addBillRecord(
        previous: Double,
        current: Double,
        flatRate: Double,
        fixedCharge: Double,
        taxPercent: Double,
        tariffType: String,
        notes: String
    ) {
        viewModelScope.launch {
            val consumption = (current - previous).coerceAtLeast(0.0)
            val calculatedCost = if (tariffType == "Tiered (Slab)") {
                calculateSlabCost(consumption)
            } else {
                consumption * flatRate
            }
            val taxAmount = (calculatedCost + fixedCharge) * (taxPercent / 100.0)
            val total = calculatedCost + fixedCharge + taxAmount

            val record = BillRecord(
                previousReading = previous,
                currentReading = current,
                consumptionKwh = consumption,
                calculatedCost = calculatedCost,
                fixedCharge = fixedCharge,
                taxAmount = taxAmount,
                totalBill = total,
                tariffType = tariffType,
                notes = notes
            )
            repository.insertBillRecord(record)
        }
    }

    fun deleteBillRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteBillRecord(id)
        }
    }

    fun addAppliance(name: String, wattage: Double, dailyHours: Double, quantity: Int) {
        viewModelScope.launch {
            val appliance = Appliance(
                name = name,
                wattage = wattage,
                dailyHours = dailyHours,
                quantity = quantity
            )
            repository.insertAppliance(appliance)
        }
    }

    fun deleteAppliance(id: Int) {
        viewModelScope.launch {
            repository.deleteAppliance(id)
        }
    }


}

class BillViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
