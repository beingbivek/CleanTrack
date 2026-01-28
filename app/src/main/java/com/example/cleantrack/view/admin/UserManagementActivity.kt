package com.example.cleantrack.view.admin

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.PrimaryGreen
import com.example.cleantrack.viewmodel.UserViewModel

class UserManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserManagementScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen() {
    val context = LocalContext.current
    val activity = context as? Activity
    val userViewModel = remember { UserViewModel(UserRepoImpl()) }

    // UI states
    var isSaving by remember { mutableStateOf(false) }
    var editingUser by remember { mutableStateOf<UserModel?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }
    var userToDeleteId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Fetch users on start
    LaunchedEffect(Unit) {
        userViewModel.getAllUsers()
    }

    val allUsers by userViewModel.allUsers.observeAsState(initial = emptyList())
    val loading by userViewModel.loading.observeAsState(initial = false)
    val total = allUsers?.size ?: 0

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, Color.White),
                    startY = 0f,
                    endY = 1000f
                )
            )
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "User Management",
                            style = TextStyle(color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { activity?.finish() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            // Use a Box here so the Loader can overlay the content
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Text(
                        "Registered Users ($total)",
                        style = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )

                    // Only show list if not loading and list isn't empty
                    if (!loading) {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                start = 16.dp,
                                top = 8.dp,
                                end = 16.dp,
                                bottom = 100.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(allUsers ?: emptyList(), key = { it.userId ?: "" }) { user ->
                                UserCard(
                                    user = user,
                                    onEdit = {
                                        editingUser = user
                                        showEditDialog = true
                                    },
                                    onDelete = { uid ->
                                        userToDeleteId = uid
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                // Center the loader in the middle of the Scaffold area
                if (loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen,
                        strokeWidth = 4.dp
                    )
                }
            }
        }

        // Edit Dialog
        if (showEditDialog && editingUser != null) {
            EditUserDialog(
                user = editingUser!!,
                isLoading = isSaving,
                onDismiss = { if (!isSaving) showEditDialog = false },
                onSave = { updated ->
                    isSaving = true
                    userViewModel.editUserProfile(updated.userId ?: "", updated) { success, msg ->
                        isSaving = false
                        Toast.makeText(context, msg ?: "Profile Updated", Toast.LENGTH_SHORT).show()
                        if (success) {
                            showEditDialog = false
                            userViewModel.getAllUsers()
                        }
                    }
                }
            )
        }

        // Delete Dialog
        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { if (!isSaving) showDeleteDialog = false },
                shape = RoundedCornerShape(20.dp),
                title = { Text("Confirm Deletion", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to remove this user?") },
                confirmButton = {
                    Button(
                        onClick = {
                            userToDeleteId?.let { id ->
                                isSaving = true
                                userViewModel.deleteUser(id) { success, msg ->
                                    isSaving = false
                                    Toast.makeText(context, msg ?: "User Deleted", Toast.LENGTH_SHORT).show()
                                    if (success) userViewModel.getAllUsers()
                                }
                            }
                            showDeleteDialog = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        enabled = !isSaving
                    ) {
                        Text("Delete", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }, enabled = !isSaving) {
                        Text("Cancel", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun UserCard(user: UserModel, onEdit: (UserModel) -> Unit, onDelete: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(PrimaryGreen.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryGreen)
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    user.fullname ?: "No Name",
                    style = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                )
                Text(
                    user.role ?: "USER",
                    style = TextStyle(fontSize = 12.sp, color = PrimaryGreen, fontWeight = FontWeight.Medium)
                )
                Text(
                    user.email ?: "",
                    style = TextStyle(color = Color.Gray, fontSize = 13.sp)
                )
            }

            Row {
                IconButton(onClick = { onEdit(user) }) {
                    Icon(Icons.Filled.Edit, contentDescription = "Edit", tint = PrimaryGreen)
                }
                IconButton(onClick = { onDelete(user.userId ?: "") }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(
    user: UserModel,
    isLoading: Boolean,
    onDismiss: () -> Unit,
    onSave: (UserModel) -> Unit
) {
    var name by remember { mutableStateOf(user.fullname) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.number) }
    var role by remember { mutableStateOf(user.role) }
    var expanded by remember { mutableStateOf(false) }
    val roles = listOf("USER", "DRIVER", "ADMIN")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(12.dp),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Edit User Details", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, color = PrimaryGreen)
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name ?: "", onValueChange = { name = it },
                    label = { Text("Name") }, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(), enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = email ?: "", onValueChange = { email = it },
                    label = { Text("Email") }, shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(), enabled = !isLoading
                )
                Spacer(modifier = Modifier.height(12.dp))

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = role ?: "USER", onValueChange = {}, readOnly = true,
                        label = { Text("Role") }, shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        trailingIcon = {
                            IconButton(onClick = { if (!isLoading) expanded = true }) {
                                Icon(Icons.Filled.ArrowDropDown, null)
                            }
                        }
                    )
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = {
                                role = r
                                expanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) { Text("Cancel", color = Color.Gray) }

                    Button(
                        onClick = { onSave(user.copy(fullname = name, email = email, number = phone, role = role)) },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Text("Save", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}