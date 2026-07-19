package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Appliance
import com.example.data.BillRecord
import com.example.data.AppRepository
import com.example.api.GeminiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BillViewModel(private val repository: AppRepository) : ViewModel() {

    val billRecords: StateFlow<List<BillRecord>> = repository.allBillRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appliances: StateFlow<List<Appliance>> = repository.allAppliances
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val aiResponse = MutableStateFlow("")
    val isAiLoading = MutableStateFlow(false)

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

    fun askAiForRecommendations(customPrompt: String? = null) {
        viewModelScope.launch {
            isAiLoading.value = true
            aiResponse.value = "Consulting the Awan Aaram AI Energy expert..."

            // Build dynamic context prompt based on the user's appliance database
            val currentAppliances = appliances.value
            val applianceContext = if (currentAppliances.isEmpty()) {
                "No appliances defined yet."
            } else {
                currentAppliances.joinToString("\n") { 
                    "- ${it.name}: ${it.wattage} Watts, used ${it.dailyHours} hours/day, qty: ${it.quantity} (Est. ${String.format("%.2f", it.monthlyKwh)} kWh/month)"
                }
            }

            val recentBills = billRecords.value.take(3)
            val billContext = if (recentBills.isEmpty()) {
                "No billing history recorded yet."
            } else {
                recentBills.joinToString("\n") {
                    "- Date: ${java.text.DateFormat.getDateInstance().format(it.billingDate)}, Consumption: ${it.consumptionKwh} kWh, Cost: $${String.format("%.2f", it.totalBill)}"
                }
            }

            val systemContext = """
                You are "Awan Aaram AI", a helpful, elite AI energy expert. Your goal is to guide homeowners to optimize their electricity consumption, save money, and live sustainably.
                
                Here is the user's current household appliance inventory:
                $applianceContext
                
                Here is the user's recent billing history:
                $billContext
                
                Please respond to their question:
                ${customPrompt ?: "Give me a thorough analysis of my appliance energy consumption, identify the top 3 power consumers, and give me 5 extremely specific, practical tips to lower my next electricity bill."}
                
                Make your response highly actionable, friendly, structured in clear Markdown, and formatted with emojis.
            """.trimIndent()

            val responseText = GeminiClient.getEnergyAdvice(systemContext)
            aiResponse.value = responseText
            isAiLoading.value = false
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
