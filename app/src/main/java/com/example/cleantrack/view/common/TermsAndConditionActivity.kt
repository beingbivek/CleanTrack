package com.example.cleantrack.view.common

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
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
import com.example.cleantrack.model.NotificationPayload
import com.example.cleantrack.repository.NotificationRepoImpl
import com.example.cleantrack.repository.TermsAndConditionRepoImpl
import com.example.cleantrack.repository.UserRepoImpl
import com.example.cleantrack.ui.theme.* import com.example.cleantrack.viewmodel.NotificationViewModel
import com.example.cleantrack.viewmodel.TermsAndConditionViewModel
import com.example.cleantrack.viewmodel.UserViewModel
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichText
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditorDefaults

class TermsAndConditionActivity : ComponentActivity() {
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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(Blue, Green, Color.White), endY = 1100f))
            ) {
                when {
                    userModel == null -> TermsScreen()
                    userModel?.role == "ADMIN" -> AdminTermsScreen()
                    else -> TermsScreen()
                }
            }
        }
    }
}

// --- SHARED TOP BAR (Privacy Policy Style) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TermsTopBar(title: String) {
    val activity = LocalActivity.current
    CenterAlignedTopAppBar(
        title = { Text(title, fontWeight = FontWeight.ExtraBold, color = Color.White) },
        navigationIcon = {
            IconButton(onClick = { activity?.finish() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
    )
}

// --- ADMIN SCREEN (EDITOR) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTermsScreen() {
    val viewModel = remember { TermsAndConditionViewModel(TermsAndConditionRepoImpl()) }
    val notificationVM = remember { NotificationViewModel(NotificationRepoImpl(), UserRepoImpl()) }
    val currentTerms by viewModel.termsAndCondition.observeAsState()
    val context = LocalContext.current
    val activity = LocalActivity.current

    val state = rememberRichTextState()
    val titleSize = 24.sp
    val subtitleSize = 18.sp

    LaunchedEffect(Unit) { viewModel.loadTermsAndCondition() }

    LaunchedEffect(currentTerms) {
        state.setHtml(currentTerms ?: "")
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { TermsTopBar("Edit Terms & Conditions") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TermsEditorControls(
                        state = state,
                        onBoldClick = { state.toggleSpanStyle(SpanStyle(fontWeight = FontWeight.Bold)) },
                        onItalicClick = { state.toggleSpanStyle(SpanStyle(fontStyle = FontStyle.Italic)) },
                        onUnderlineClick = { state.toggleSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline)) },
                        onTitleClick = { state.toggleSpanStyle(SpanStyle(fontSize = titleSize)) },
                        onSubtitleClick = { state.toggleSpanStyle(SpanStyle(fontSize = subtitleSize)) },
                        onTextColorClick = { state.toggleSpanStyle(SpanStyle(color = Color.Red)) },
                        onStartAlignClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Start)) },
                        onEndAlignClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.End)) },
                        onCenterAlignClick = { state.toggleParagraphStyle(ParagraphStyle(textAlign = TextAlign.Center)) }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(8.dp))

                    RichTextEditor(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        colors = RichTextEditorDefaults.richTextEditorColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.postTermsAndCondition(state.toHtml()) { s, m ->
                        if (s) {
                            Toast.makeText(context, m, Toast.LENGTH_SHORT).show()
                            notificationVM.notifyAllRecipients(
                                NotificationPayload(
                                    title = "Terms updated",
                                    message = "Please review the updated terms and conditions.",
                                    type = "policy",
                                    actionType = "terms"
                                )
                            )
                            activity?.finish()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Green),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Publish Update", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// --- USER SCREEN (VIEWER) ---

@Composable
fun TermsScreen() {
    val viewModel = remember { TermsAndConditionViewModel(TermsAndConditionRepoImpl()) }
    val content by viewModel.termsAndCondition.observeAsState()
    val state = rememberRichTextState()

    LaunchedEffect(Unit) { viewModel.loadTermsAndCondition() }

    LaunchedEffect(content) {
        state.setHtml(content ?: "")
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = { TermsTopBar("Terms & Conditions") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().weight(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (content.isNullOrEmpty()) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = Green
                        )
                    } else {
                        RichText(state = state)
                    }
                }
            }
        }
    }
}

// --- EDITOR CONTROLS (Privacy Policy Style) ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TermsEditorControls(
    state: RichTextState,
    onBoldClick: () -> Unit,
    onItalicClick: () -> Unit,
    onUnderlineClick: () -> Unit,
    onTitleClick: () -> Unit,
    onSubtitleClick: () -> Unit,
    onTextColorClick: () -> Unit,
    onStartAlignClick: () -> Unit,
    onEndAlignClick: () -> Unit,
    onCenterAlignClick: () -> Unit
) {
    var boldSelected by rememberSaveable { mutableStateOf(false) }
    var italicSelected by rememberSaveable { mutableStateOf(false) }
    var underlineSelected by rememberSaveable { mutableStateOf(false) }
    var alignmentSelected by rememberSaveable { mutableIntStateOf(0) }
    var showLinkDialog by remember { mutableStateOf(false) }

    if (showLinkDialog) {
        TermsLinkDialog(
            onDismissRequest = { showLinkDialog = false },
            onConfirmation = { text, link ->
                state.addLink(text = text, url = link)
                showLinkDialog = false
            }
        )
    }

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TermsControlWrapper(selected = boldSelected, onChangeClick = { boldSelected = it }, onClick = onBoldClick) {
            Icon(Icons.Default.FormatBold, "Bold", tint = if (boldSelected) Color.White else Green)
        }
        TermsControlWrapper(selected = italicSelected, onChangeClick = { italicSelected = it }, onClick = onItalicClick) {
            Icon(Icons.Default.FormatItalic, "Italic", tint = if (italicSelected) Color.White else Green)
        }
        TermsControlWrapper(selected = underlineSelected, onChangeClick = { underlineSelected = it }, onClick = onUnderlineClick) {
            Icon(Icons.Default.FormatUnderlined, "Underline", tint = if (underlineSelected) Color.White else Green)
        }
        TermsControlWrapper(selected = false, onChangeClick = {}, onClick = onTitleClick) {
            Icon(Icons.Default.Title, "Title", tint = Green)
        }
        TermsControlWrapper(selected = false, onChangeClick = { showLinkDialog = true }, onClick = { showLinkDialog = true }) {
            Icon(Icons.Default.AddLink, "Link", tint = Green)
        }
        TermsControlWrapper(selected = alignmentSelected == 0, onChangeClick = { alignmentSelected = 0 }, onClick = onStartAlignClick) {
            Icon(Icons.Default.FormatAlignLeft, "Left", tint = if (alignmentSelected == 0) Color.White else Green)
        }
        TermsControlWrapper(selected = alignmentSelected == 1, onChangeClick = { alignmentSelected = 1 }, onClick = onCenterAlignClick) {
            Icon(Icons.Default.FormatAlignCenter, "Center", tint = if (alignmentSelected == 1) Color.White else Green)
        }
    }
}

@Composable
fun TermsControlWrapper(
    selected: Boolean,
    onChangeClick: (Boolean) -> Unit,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) Green else Color.Transparent)
            .border(1.dp, Green, RoundedCornerShape(8.dp))
            .clickable {
                onClick()
                onChangeClick(!selected)
            },
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
fun TermsLinkDialog(onDismissRequest: () -> Unit, onConfirmation: (String, String) -> Unit) {
    var linkText by remember { mutableStateOf("") }
    var linkUrl by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(onClick = { onConfirmation(linkText, linkUrl) }) { Text("Confirm", color = Green) }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) { Text("Cancel", color = Color.Gray) }
        },
        title = { Text("Add Link", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = linkText, onValueChange = { linkText = it }, label = { Text("Text") })
                OutlinedTextField(value = linkUrl, onValueChange = { linkUrl = it }, label = { Text("URL") })
            }
        }
    )
}