package com.example.cleantrack.view.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.cleantrack.model.UserModel
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.AccentRed
import com.example.cleantrack.ui.theme.BackgroundLightGray
import com.example.cleantrack.ui.theme.CleanTrackTheme
import com.example.cleantrack.ui.theme.PrimaryGreen
import com.example.cleantrack.viewmodel.UserViewModel
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.lazy.items


class UserManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UserManagementScreen()
        }
    }
}

@Composable
fun UserManagementScreen() {

    val userViewModel = remember { UserViewModel(UserRepoImpl()) }
    // Start listening to users collection
    LaunchedEffect(Unit) {
        userViewModel.getAllUsers()
    }

    val allUsers by userViewModel.allUsers.observeAsState(initial = null)

    val loading by userViewModel.loading.observeAsState(initial = false)

    val total = allUsers?.size ?: 0




    var editingUser by remember { mutableStateOf<UserModel?>(null) }
    var showEditDialog by remember { mutableStateOf(false) }

    var userToDeleteId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { UserHeader(userCount = total) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(BackgroundLightGray)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text(
                    "Registered Users ($total)",
                    style = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    val userList = allUsers ?: emptyList()
                    items(userList, key = { it.userId }) { user ->
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

            // Edit Dialog
            if (showEditDialog && editingUser != null) {
                EditUserDialog(
                    user = editingUser!!,
                    onDismiss = {
                        showEditDialog = false
                        editingUser = null
                    },
                    onSave = { updated ->
                        val id = updated.userId   ?: ""
                        userViewModel.editUserProfile(id,updated) { success, err ->
                            // optional: show result via snackbar/toast
                            showEditDialog = false
                            editingUser = null
                        }
                    }
                )
            }

            // Delete confirmation dialog
            if (showDeleteDialog && userToDeleteId != null) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false; userToDeleteId = null },
                    title = { Text("Confirm Deletion") },
                    text = { Text("Are you sure you want to delete user ?") },
                    confirmButton = {
                        Button(onClick = {
                            val id = userToDeleteId
                            if (!id.isNullOrBlank()) {
                                userViewModel.deleteUser(id) { success, err ->
                                    // optional: show result via snackbar/toast
                                }
                            }
                            showDeleteDialog = false
                            userToDeleteId = null
                        }, colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)) {
                            Text("Yes", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showDeleteDialog = false
                            userToDeleteId = null
                        }) {
                            Text("No", color = Color.Gray)
                        }
                    }
                )
            }

            // Optional: show a simple loading indicator (you can replace with a nicer one)
            if (loading == true) {
                // Very simple center text — replace with CircularProgressIndicator if you want
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading...", color = Color.Gray)
                }
            }



        }
    }
}

@Composable
fun UserHeader(userCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(64.dp)
            .background(PrimaryGreen)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Settings,
                contentDescription = "Settings",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    "User Management",
                    style = TextStyle(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                )
                Text(
                    "Registered Users ($userCount)",
                    style = TextStyle(
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp
                    )
                )
            }
        }
    }
}

@Composable
fun UserCard(user: UserModel, onEdit: (UserModel) -> Unit, onDelete: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${user.fullname} (${user.role})",
                    style = TextStyle(
                        color = Color.Black,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    user.email,
                    style = TextStyle(color = Color.Gray, fontSize = 14.sp)
                )
                Text(
                    "Phone: ${user.number}",
                    style = TextStyle(color = Color.Gray, fontSize = 14.sp)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Edit
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onEdit(user) }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Edit,
                        contentDescription = "Edit User",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Delete
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onDelete(user.userId ?: "") }
                        .padding(8.dp)
                ) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Delete User",
                        tint = AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

//private fun Unit.height(dp: Dp): Modifier {}

@Composable
fun EditUserDialog(user: UserModel, onDismiss: () -> Unit, onSave: (UserModel) -> Unit) {
    var name by remember { mutableStateOf(user.fullname) }
    var email by remember { mutableStateOf(user.email) }
    var phone by remember { mutableStateOf(user.number) }
    var role by remember { mutableStateOf(user.role) }

    // Dropdown state
    val roles = listOf("USER", "DRIVER", "ADMIN")
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Edit User: ${user.fullname}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone") },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )

                // Role dropdown: admin can only pick from USER, DRIVER, ADMIN
                Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    OutlinedTextField(
                        value = role,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                Icons.Filled.ArrowDropDown,
                                contentDescription = "Role dropdown",
                                modifier = Modifier.clickable { expanded = !expanded }
                            )
                        },
                        label = { Text("Role") },
                        modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { r ->
                            DropdownMenuItem(text = { Text(r) }, onClick = {
                                role = r
                                expanded = false
                            })
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = Color.Gray)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val updated = user.copy(
                                fullname = name,
                                email = email,
                                number = phone,
                                role = role
                            )
                            onSave(updated)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save Changes", color = Color.White)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserManagementPreview() {
    CleanTrackTheme {
        Box(modifier = Modifier.fillMaxSize()) {
            Text("Preview - run on device/emulator")
        }
    }
}