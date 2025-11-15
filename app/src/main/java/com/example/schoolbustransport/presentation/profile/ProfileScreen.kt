package com.example.schoolbustransport.presentation.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val user by viewModel.user.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var name by remember { mutableStateOf(user?.name ?: "") }
    var phone by remember { mutableStateOf(user?.phone ?: "") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Profile") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            // Profile image
            if (user?.image != null) {
                Image(
                    painter = rememberAsyncImagePainter(user!!.image),
                    contentDescription = "Profile Image",
                    modifier = Modifier.size(96.dp).clickable {
                        // TODO: Pick new image and call viewModel.uploadProfileImage
                    }
                )
                TextButton(onClick = { user?.id?.let { viewModel.deleteProfileImage(it) } }) { Text("Remove Photo") }
            } else {
                Box(
                    modifier = Modifier.size(96.dp).clickable {
                        // TODO: Pick new image and call viewModel.uploadProfileImage
                    },
                    contentAlignment = Alignment.Center
                ) {
                    Text("Add Photo", color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(onClick = { user?.id?.let { viewModel.updateProfile(it, name, phone) } }, modifier = Modifier.fillMaxWidth()) {
                    Text("Save Changes")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            val showDeleteDialog = remember { mutableStateOf(false) }
            if (showDeleteDialog.value) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog.value = false },
                    title = { Text("Delete Account") },
                    text = { Text("Are you sure you want to delete your account? This action cannot be undone.") },
                    confirmButton = {
                        Button(onClick = {
                            user?.id?.let {
                                viewModel.deleteAccount(it) {
                                    // TODO: Navigate to login or splash after deletion
                                }
                            }
                            showDeleteDialog.value = false
                        }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                            Text("Delete", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteDialog.value = false }) { Text("Cancel") }
                    }
                )
            }
            Button(onClick = { showDeleteDialog.value = true }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), modifier = Modifier.fillMaxWidth()) {
                Text("Delete Account", color = Color.White)
            }
            if (error != null) {
                Text(error!!, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
