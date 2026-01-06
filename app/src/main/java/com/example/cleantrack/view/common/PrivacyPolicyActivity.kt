package com.example.cleantrack.view.common

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.cleantrack.repository.PrivacyPolicyRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.viewmodel.PrivacyPolicyViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor

class PrivacyPolicyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userViewModel = remember { UserViewModel(UserRepoImpl()) }
            val userModel by userViewModel.user.observeAsState()

            LaunchedEffect(Unit) {
                val currentId = userViewModel.getCurrentUserId()
                if (currentId != null) {
                    userViewModel.getUserById(currentId)
                }
            }

            MaterialTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()) {
                        when {
                            userModel == null -> {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                            }

                            userModel?.role == "ADMIN" -> {
                                AdminPrivacyPolicyScreen()
                            }

                            else -> {
                                PrivacyPolicyScreen()
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- ADMIN SCREEN (EDITOR) ---

@Composable
fun AdminPrivacyPolicyScreen() {
    val viewModel = remember { PrivacyPolicyViewModel(PrivacyPolicyRepoImpl()) }
    val currentPrivacyPolicy by viewModel.privacypolicy.collectAsState()
    val message by viewModel.message.collectAsState()
    val context = LocalContext.current
    val activity = context as Activity

    // Rich Text State
    val state = rememberRichTextState()
    val titleSize = MaterialTheme.typography.displaySmall.fontSize
    val subtitleSize = MaterialTheme.typography.titleLarge.fontSize

    LaunchedEffect(Unit) { viewModel.loadPrivacyPolicy() }

    // Update editor content when data is fetched from Firebase
    LaunchedEffect(currentPrivacyPolicy) {
        if (currentPrivacyPolicy.isNotEmpty()) {
            state.setHtml(currentPrivacyPolicy)
        }
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {
        Text("Admin: Edit Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        // Editor Toolbar
        EditorControls(
            modifier = Modifier.weight(2.5f),
            state = state,
            onBoldClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
            onItalicClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
            onUnderlineClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
            onTitleClick = { state.toggleSpanStyle(SpanStyle(fontSize = titleSize)) },
            onSubtitleClick = { state.toggleSpanStyle(SpanStyle(fontSize = subtitleSize)) },
            onTextColorClick = { state.toggleSpanStyle(SpanStyle(color = Color.Red)) },
            onStartAlignClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start)) },
            onEndAlignClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End)) },
            onCenterAlignClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center)) },
            onExportClick = { Log.d("Editor", state.toHtml()) }
        )

        // The Rich Editor
        RichTextEditor(
            modifier = Modifier
                .fillMaxWidth()
                .weight(6.5f)
                .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp)),
            state = state,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.postPrivacyPolicy(state.toHtml()) { s, m -> if(s) {
                    Toast.makeText(context, m, Toast.LENGTH_SHORT)
                    activity.finish()
                }

                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text( "Update Policy")
        }
    }
}

// --- USER SCREEN (VIEWER) ---

@Composable
fun PrivacyPolicyScreen() {
    val viewModel = remember { PrivacyPolicyViewModel(PrivacyPolicyRepoImpl()) }
    val guidelines by viewModel.privacypolicy.collectAsState()
    val state = rememberRichTextState()

    LaunchedEffect(Unit) { viewModel.loadPrivacyPolicy() }

    LaunchedEffect(guidelines) {
        state.setHtml(guidelines)
    }

    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {
        Text("Privacy Policy", fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(modifier = Modifier.padding(20.dp)) {
                if (guidelines.isEmpty()) {
                    Text("Loading policy...")
                } else {
                    // RichText component renders the HTML correctly
                    RichText(state = state)
                }
            }
        }
    }
}

// --- SHARED COMPONENTS (CONTROLS, DIALOGS) ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorControls(
    modifier: Modifier = Modifier,
    state: RichTextState,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onTitleClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onTextColorClick: () -> Unit,
    onStartAlignClick: () -> Unit,
    onEndAlignClick: () -> Unit,
    onCenterAlignClick: () -> Unit,
    onExportClick: () -> Unit,
) {
    var boldSelected by rememberSaveable { mutableStateOf(false) }
    var italicSelected by rememberSaveable { mutableStateOf(false) }
    var underlineSelected by rememberSaveable { mutableStateOf(false) }
    var titleSelected by rememberSaveable { mutableStateOf(false) }
    var subtitleSelected by rememberSaveable { mutableStateOf(false) }
    var textColorSelected by rememberSaveable { mutableStateOf(false) }
    var linkSelected by rememberSaveable { mutableStateOf(false) }
    var alignmentSelected by rememberSaveable { mutableIntStateOf(0) }
    var showLinkDialog by remember { mutableStateOf(false) }

    if (showLinkDialog) {
        LinkDialog(
            onDismissRequest = {
                showLinkDialog = false
                linkSelected = false
            },
            onConfirmation = { linkText, link ->
                state.addLink(text = linkText, url = link)
                showLinkDialog = false
                linkSelected = false
            }
        )
    }

    FlowRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ControlWrapper(
            selected = boldSelected,
            onChangeClick = { boldSelected = it },
            onClick = onBoldClick
        ) {
            Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = Color.White)
        }
        ControlWrapper(
            selected = italicSelected,
            onChangeClick = { italicSelected = it },
            onClick = onItalicClick
        ) {
            Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = Color.White)
        }
        ControlWrapper(
            selected = underlineSelected,
            onChangeClick = { underlineSelected = it },
            onClick = onUnderlineClick
        ) {
            Icon(
                Icons.Default.FormatUnderlined,
                contentDescription = "Underline",
                tint = Color.White
            )
        }
        ControlWrapper(
            selected = titleSelected,
            onChangeClick = { titleSelected = it },
            onClick = onTitleClick
        ) {
            Icon(Icons.Default.Title, contentDescription = "Title", tint = Color.White)
        }
        ControlWrapper(
            selected = subtitleSelected,
            onChangeClick = { subtitleSelected = it },
            onClick = onSubtitleClick
        ) {
            Icon(Icons.Default.FormatSize, contentDescription = "Subtitle", tint = Color.White)
        }
        ControlWrapper(
            selected = textColorSelected,
            onChangeClick = { textColorSelected = it },
            onClick = onTextColorClick
        ) {
            Icon(Icons.Default.FormatColorText, contentDescription = "Color", tint = Color.White)
        }
        ControlWrapper(
            selected = linkSelected,
            onChangeClick = { linkSelected = it },
            onClick = { showLinkDialog = true }) {
            Icon(Icons.Default.AddLink, contentDescription = "Link", tint = Color.White)
        }
        ControlWrapper(
            selected = alignmentSelected == 0,
            onChangeClick = { alignmentSelected = 0 },
            onClick = onStartAlignClick
        ) {
            Icon(Icons.Default.FormatAlignLeft, contentDescription = "Left", tint = Color.White)
        }
        ControlWrapper(
            selected = alignmentSelected == 1,
            onChangeClick = { alignmentSelected = 1 },
            onClick = onCenterAlignClick
        ) {
            Icon(Icons.Default.FormatAlignCenter, contentDescription = "Center", tint = Color.White)
        }
        ControlWrapper(
            selected = alignmentSelected == 2,
            onChangeClick = { alignmentSelected = 2 },
            onClick = onEndAlignClick
        ) {
            Icon(Icons.Default.FormatAlignRight, contentDescription = "Right", tint = Color.White)
        }
    }
}

@Composable
fun ControlWrapper(
    selected: Boolean,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.inversePrimary,
    onChangeClick: (Boolean) -> Unit,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(size = 6.dp))
            .clickable {
                onClick()
                onChangeClick(!selected)
            }
            .background(if (selected) selectedColor else unselectedColor)
            .border(width = 1.dp, color = Color.LightGray, shape = RoundedCornerShape(size = 6.dp))
            .padding(all = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun LinkDialog(onDismissRequest: () -> Unit, onConfirmation: (String, String) -> Unit) {
    var linkText by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Add Link") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = linkText,
                    onValueChange = { linkText = it },
                    label = { Text("Display Text") })
                TextField(
                    value = linkUrl,
                    onValueChange = { linkUrl = it },
                    label = { Text("URL") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onConfirmation(
                    linkText,
                    linkUrl
                )
            }) { Text("Confirm") }
        },
        dismissButton = { TextButton(onClick = onDismissRequest) { Text("Dismiss") } }
    )
}