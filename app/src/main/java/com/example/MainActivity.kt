package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppDatabase
import com.example.data.AppRepository
import com.example.data.Appliance
import com.example.data.BillRecord
import com.example.ui.BillViewModel
import com.example.ui.BillViewModelFactory
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.GreenAccent
import com.example.ui.theme.AmberAccent
import com.example.ui.theme.RedAccent
import java.text.DateFormat
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext)
        val repository = AppRepository(database.appDao())

        setContent {
            MyApplicationTheme {
                val viewModel: BillViewModel by viewModels {
                    BillViewModelFactory(repository)
                }
                MainScreen(viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: BillViewModel) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabTitles = listOf("Calculator", "Appliances", "History", "AI expert")
    val tabIcons = listOf(
        Icons.Default.Calculate,
        Icons.Default.ElectricalServices,
        Icons.Default.History,
        Icons.Default.AutoAwesome
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Awan Aaram",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "Cloud Comfort • Smart Domestic Utilities",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                actions = {
                    IconButton(
                        onClick = { viewModel.askAiForRecommendations() },
                        modifier = Modifier.testTag("appbar_quick_ai_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Quick AI Recommendation",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Material 3 Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                        icon = { Icon(tabIcons[index], contentDescription = title, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("tab_${title.lowercase()}")
                    )
                }
            }

            // Tab Content
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                when (selectedTab) {
                    0 -> CalculatorTab(viewModel)
                    1 -> AppliancesTab(viewModel)
                    2 -> HistoryTab(viewModel)
                    3 -> AiExpertTab(viewModel)
                }
            }
        }
    }
}

// ==================== CALCULATOR TAB ====================
@Composable
fun CalculatorTab(viewModel: BillViewModel) {
    var previousReadingInput by remember { mutableStateOf("0") }
    var currentReadingInput by remember { mutableStateOf("250") }
    var fixedChargeInput by remember { mutableStateOf("15.00") }
    var taxPercentInput by remember { mutableStateOf("5.0") }
    var flatRateInput by remember { mutableStateOf("0.15") }
    var notesInput by remember { mutableStateOf("") }
    
    var tariffType by remember { mutableStateOf("Tiered (Slab)") }
    var expandedDropdown by remember { mutableStateOf(false) }

    val previous = previousReadingInput.toDoubleOrNull() ?: 0.0
    val current = currentReadingInput.toDoubleOrNull() ?: 0.0
    val consumption = (current - previous).coerceAtLeast(0.0)

    val flatRate = flatRateInput.toDoubleOrNull() ?: 0.15
    val fixedCharge = fixedChargeInput.toDoubleOrNull() ?: 15.00
    val taxPercent = taxPercentInput.toDoubleOrNull() ?: 5.0

    val calculatedCost = if (tariffType == "Tiered (Slab)") {
        viewModel.calculateSlabCost(consumption)
    } else {
        consumption * flatRate
    }

    val taxAmount = (calculatedCost + fixedCharge) * (taxPercent / 100.0)
    val totalBill = calculatedCost + fixedCharge + taxAmount

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Welcome and Header
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Awan Aaram Bill Estimator",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Enter your current and previous meter readings to calculate your cost with either flat-rate or tiered domestic slab policies.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }

        // Inputs Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Meter Readings & Tariff Configuration",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = previousReadingInput,
                        onValueChange = { previousReadingInput = it },
                        label = { Text("Previous (kWh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("previous_reading_input")
                    )

                    OutlinedTextField(
                        value = currentReadingInput,
                        onValueChange = { currentReadingInput = it },
                        label = { Text("Current (kWh)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("current_reading_input")
                    )
                }

                // Tariff Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = tariffType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Tariff Policy") },
                        trailingIcon = {
                            IconButton(onClick = { expandedDropdown = !expandedDropdown }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expandedDropdown = !expandedDropdown }
                            .testTag("tariff_type_selector")
                    )
                    DropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tiered (Slab) Pricing") },
                            onClick = {
                                tariffType = "Tiered (Slab)"
                                expandedDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Flat-Rate Pricing") },
                            onClick = {
                                tariffType = "Flat Rate"
                                expandedDropdown = false
                            }
                        )
                    }
                }

                // Policy Specific Input
                AnimatedVisibility(visible = tariffType == "Flat Rate") {
                    OutlinedTextField(
                        value = flatRateInput,
                        onValueChange = { flatRateInput = it },
                        label = { Text("Rate per kWh ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("flat_rate_input")
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = fixedChargeInput,
                        onValueChange = { fixedChargeInput = it },
                        label = { Text("Fixed Charge ($)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("fixed_charge_input")
                    )

                    OutlinedTextField(
                        value = taxPercentInput,
                        onValueChange = { taxPercentInput = it },
                        label = { Text("Surcharge/Tax (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("tax_percent_input")
                    )
                }

                OutlinedTextField(
                    value = notesInput,
                    onValueChange = { notesInput = it },
                    label = { Text("Billing Period Notes (e.g. July 2026)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("billing_notes_input")
                )
            }
        }

        // Live calculation results
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Live Calculation Summary",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Net Consumption:")
                    Text("${String.format("%.1f", consumption)} kWh", fontWeight = FontWeight.Bold)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Base Energy cost:")
                    Text("$${String.format("%.2f", calculatedCost)}")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Fixed Service Charge:")
                    Text("$${String.format("%.2f", fixedCharge)}")
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Surcharges & Taxes (${taxPercent}%):")
                    Text("$${String.format("%.2f", taxAmount)}")
                }

                Divider(modifier = Modifier.padding(vertical = 4.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Estimated Total Bill:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = "$${String.format("%.2f", totalBill)}",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.testTag("total_bill_text")
                    )
                }

                // Custom Graphical Gauge showing consumption tier
                Spacer(modifier = Modifier.height(12.dp))
                ConsumptionGauge(consumption = consumption)
                Spacer(modifier = Modifier.height(4.dp))

                if (tariffType == "Tiered (Slab)") {
                    Text(
                        text = "Slabs used: 100 units @ $0.10, next 200 @ $0.15, next 200 @ $0.20, rest @ $0.25",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        viewModel.addBillRecord(
                            previous = previous,
                            current = current,
                            flatRate = flatRate,
                            fixedCharge = fixedCharge,
                            taxPercent = taxPercent,
                            tariffType = tariffType,
                            notes = notesInput.ifEmpty { "Metered: ${consumption.toInt()} kWh" }
                        )
                        // Reset inputs on success
                        notesInput = ""
                        previousReadingInput = currentReadingInput
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_bill_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Save Bill")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save to Billing History")
                }
            }
        }
    }
}

@Composable
fun ConsumptionGauge(consumption: Double) {
    // Determine colors & percentage
    val maxProgress = 600.0 // standard domestic maximum cap
    val fraction = (consumption / maxProgress).coerceIn(0.0, 1.0).toFloat()
    val indicatorColor = when {
        consumption <= 150.0 -> GreenAccent
        consumption <= 350.0 -> AmberAccent
        else -> RedAccent
    }

    val label = when {
        consumption <= 150.0 -> "Green Consumption Zone (Optimized)"
        consumption <= 350.0 -> "Medium Consumption Zone (Average)"
        else -> "High Consumption Zone (Heavy Usage)"
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Consumption Level Rating:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = indicatorColor)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(Color.LightGray.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction)
                    .clip(RoundedCornerShape(5.dp))
                    .background(indicatorColor)
            )
        }
    }
}

// ==================== APPLIANCES TAB ====================
@Composable
fun AppliancesTab(viewModel: BillViewModel) {
    val appliances by viewModel.appliances.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    // State for Add Appliance
    var applianceName by remember { mutableStateOf("") }
    var wattageInput by remember { mutableStateOf("") }
    var dailyHoursInput by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("1") }

    val totalEstimatedMonthlyKwh = appliances.sumOf { it.monthlyKwh }
    // Average flat rate calculation of $0.15/kWh for rapid domestic preview
    val estimatedMonthlyCost = totalEstimatedMonthlyKwh * 0.15

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Summary Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Appliance Consumption Simulator",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Simulate how much power different household electronic appliances consume. Double check their average daily load below.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Estimated monthly usage:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(
                            "${String.format("%.1f", totalEstimatedMonthlyKwh)} kWh",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Est. monthly Cost (@$0.15):", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                        Text(
                            "$${String.format("%.2f", estimatedMonthlyCost)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Appliance Inventory (${appliances.size})",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall
            )
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("add_appliance_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add", fontSize = 12.sp)
            }
        }

        // Appliance List
        if (appliances.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Your inventory is empty. Tap 'Add' to insert household devices.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(appliances) { item ->
                    val percentage = if (totalEstimatedMonthlyKwh > 0.0) {
                        (item.monthlyKwh / totalEstimatedMonthlyKwh).toFloat()
                    } else {
                        0f
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.name} (${item.quantity}x)",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        text = "${item.wattage.toInt()}W • ${item.dailyHours} hrs/day",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${String.format("%.1f", item.monthlyKwh)} kWh/mo",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = "Est. $${String.format("%.2f", item.monthlyKwh * 0.15)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.deleteAppliance(item.id) },
                                    modifier = Modifier.testTag("delete_appliance_${item.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Appliance",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Custom proportional usage bar chart
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(percentage)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Share of overall home usage",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                                Text(
                                    text = "${String.format("%.1f", percentage * 100)}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Appliance Dialog
    if (showAddDialog) {
        Dialog(onDismissRequest = { showAddDialog = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Add Household Appliance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    OutlinedTextField(
                        value = applianceName,
                        onValueChange = { applianceName = it },
                        label = { Text("Appliance Name") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appliance_name_input"),
                        placeholder = { Text("e.g., Living Room Fan") }
                    )

                    OutlinedTextField(
                        value = wattageInput,
                        onValueChange = { wattageInput = it },
                        label = { Text("Power Rating (Watts)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appliance_wattage_input"),
                        placeholder = { Text("e.g., 75") }
                    )

                    OutlinedTextField(
                        value = dailyHoursInput,
                        onValueChange = { dailyHoursInput = it },
                        label = { Text("Daily Usage (Hours)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appliance_hours_input"),
                        placeholder = { Text("e.g., 6") }
                    )

                    OutlinedTextField(
                        value = quantityInput,
                        onValueChange = { quantityInput = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("appliance_quantity_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = { showAddDialog = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val watts = wattageInput.toDoubleOrNull() ?: 100.0
                                val hours = dailyHoursInput.toDoubleOrNull() ?: 4.0
                                val qty = quantityInput.toIntOrNull() ?: 1

                                if (applianceName.isNotBlank()) {
                                    viewModel.addAppliance(
                                        name = applianceName,
                                        wattage = watts,
                                        dailyHours = hours,
                                        quantity = qty
                                    )
                                    // Reset & close
                                    applianceName = ""
                                    wattageInput = ""
                                    dailyHoursInput = ""
                                    quantityInput = "1"
                                    showAddDialog = false
                                }
                            },
                            modifier = Modifier.testTag("submit_appliance_button")
                        ) {
                            Text("Add Device")
                        }
                    }
                }
            }
        }
    }
}

// ==================== HISTORY TAB ====================
@Composable
fun HistoryTab(viewModel: BillViewModel) {
    val history by viewModel.billRecords.collectAsState()
    var selectedRecordForDetail by remember { mutableStateOf<BillRecord?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Saved Billing History",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (history.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No historical billing records found. Save calculated bills from the 'Calculator' tab to monitor changes over time.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(24.dp)
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(history) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedRecordForDetail = record },
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = record.notes.ifEmpty { "Billing Record" },
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(record.billingDate)),
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%.2f", record.totalBill)}",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "${record.consumptionKwh.toInt()} kWh consumed",
                                        fontSize = 11.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Policy: ${record.tariffType}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Text(
                                    text = "Tap to view details • Hold to delete",
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Record Detail Dialog with complete Breakdown & Delete functionality
    selectedRecordForDetail?.let { record ->
        Dialog(onDismissRequest = { selectedRecordForDetail = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Bill Breakdown Detail",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(onClick = { selectedRecordForDetail = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Divider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Record Date:")
                        Text(DateFormat.getDateTimeInstance().format(Date(record.billingDate)), fontWeight = FontWeight.Bold)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Meter Previous Reading:")
                        Text("${record.previousReading} kWh")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Meter Current Reading:")
                        Text("${record.currentReading} kWh")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Net Consumption:")
                        Text("${record.consumptionKwh} kWh", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Calculation Policy:")
                        Text(record.tariffType)
                    }

                    Divider()

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Base Energy Cost:")
                        Text("$${String.format("%.2f", record.calculatedCost)}")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Fixed Charges:")
                        Text("$${String.format("%.2f", record.fixedCharge)}")
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Surcharges & Taxes:")
                        Text("$${String.format("%.2f", record.taxAmount)}")
                    }

                    Divider()

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Final Bill Paid:", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(
                            "$${String.format("%.2f", record.totalBill)}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.deleteBillRecord(record.id)
                                selectedRecordForDetail = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("delete_record_button")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Record")
                        }

                        Button(
                            onClick = { selectedRecordForDetail = null },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Dismiss")
                        }
                    }
                }
            }
        }
    }
}

// ==================== AI EXPERT TAB ====================
@Composable
fun AiExpertTab(viewModel: BillViewModel) {
    val aiResponse by viewModel.aiResponse.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    var userPrompt by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Welcome Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "Awan Aaram AI Expert",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Your intelligent cloud-saving consultant. Get specialized household energy plans.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // AI Response display card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                if (aiResponse.isEmpty() && !isAiLoading) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Need tips to reduce your utility bills?",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap 'Analyze' to scan your custom appliance inventory and generate a detailed savings action plan.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.askAiForRecommendations() },
                            modifier = Modifier.testTag("analyze_appliances_ai_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Analyze Home Energy")
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Consultation Response:", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp))
                        
                        // Scrollable response text
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = aiResponse,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier
                                    .padding(bottom = 16.dp)
                                    .testTag("ai_response_text")
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom user question input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                placeholder = { Text("Ask AI for energy advice...", fontSize = 14.sp) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("ai_custom_input"),
                maxLines = 2,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )

            IconButton(
                onClick = {
                    if (userPrompt.isNotBlank()) {
                        viewModel.askAiForRecommendations(userPrompt)
                        userPrompt = ""
                    }
                },
                enabled = userPrompt.isNotBlank() && !isAiLoading,
                modifier = Modifier
                    .background(
                        color = if (userPrompt.isNotBlank() && !isAiLoading) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .testTag("send_ai_prompt_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send prompt",
                    tint = Color.White
                )
            }
        }
    }
}
