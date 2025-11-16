package com.example.schoolbustransport.presentation.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.schoolbustransport.domain.model.Bus
import com.example.schoolbustransport.domain.model.Route
import com.example.schoolbustransport.domain.model.User

@Composable
fun AdminPanelScreen(navController: NavController, vm: AdminTripsViewModel = hiltViewModel()) {
	LaunchedEffect(Unit) { vm.loadLists() }

	val buses by vm.buses.collectAsState()
	val routes by vm.routes.collectAsState()
	val drivers by vm.drivers.collectAsState()
	val loading by vm.isLoading.collectAsState()
	val error by vm.error.collectAsState()

	var selectedBus by remember { mutableStateOf<Bus?>(null) }
	var selectedRoute by remember { mutableStateOf<Route?>(null) }
	var selectedDriver by remember { mutableStateOf<User?>(null) }
	var tripName by remember { mutableStateOf("") }

	Surface(modifier = Modifier.fillMaxSize()) {
		Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
			Text("Admin - Schedule Trip", style = MaterialTheme.typography.headlineSmall)
			Spacer(Modifier.height(8.dp))
			if (loading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
			if (!error.isNullOrBlank()) Text(error!!, color = MaterialTheme.colorScheme.error)

			// Simple pickers using lists
			Text("Select Bus", style = MaterialTheme.typography.titleMedium)
			SelectionList(items = buses, selected = selectedBus, onSelect = { selectedBus = it }) { it.licensePlate }
			Spacer(Modifier.height(8.dp))
			Text("Select Route", style = MaterialTheme.typography.titleMedium)
			SelectionList(items = routes, selected = selectedRoute, onSelect = { selectedRoute = it }) { it.name }
			Spacer(Modifier.height(8.dp))
			Text("Select Driver", style = MaterialTheme.typography.titleMedium)
			SelectionList(items = drivers, selected = selectedDriver, onSelect = { selectedDriver = it }) { it.name }

			Spacer(Modifier.height(12.dp))
			OutlinedTextField(value = tripName, onValueChange = { tripName = it }, label = { Text("Trip name") }, modifier = Modifier.fillMaxWidth())

			Spacer(Modifier.height(12.dp))
			Button(
				onClick = {
					val bus = selectedBus ?: return@Button
					val route = selectedRoute ?: return@Button
					val driver = selectedDriver ?: return@Button
					vm.createTrip(bus, route, driver, tripName)
				},
				enabled = !loading && selectedBus != null && selectedRoute != null && selectedDriver != null && tripName.isNotBlank(),
				modifier = Modifier.align(Alignment.End)
			) { Text("Create Trip") }
		}
	}
}

@Composable
private fun <T> SelectionList(items: List<T>, selected: T?, onSelect: (T) -> Unit, label: (T) -> String) {
	LazyColumn(modifier = Modifier.heightIn(max = 180.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
		items(items) { item ->
			val isSelected = item == selected
			ElevatedCard(
				modifier = Modifier.fillMaxWidth(),
				colors = CardDefaults.elevatedCardColors(containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
			) {
				Row(
					modifier = Modifier.fillMaxWidth().padding(12.dp),
					horizontalArrangement = Arrangement.SpaceBetween,
					verticalAlignment = Alignment.CenterVertically
				) {
					Text(label(item))
					RadioButton(selected = isSelected, onClick = { onSelect(item) })
				}
			}
		}
	}
}
