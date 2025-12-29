package com.example.cleantrack.util

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

@Composable
fun StylizedConversation(message: String) {
    val annotatedString = buildAnnotatedString {
        val lines = message.split("\n")
        lines.forEachIndexed { index, line ->
            val trimmedLine = line.trim()

            when {
                // If it's an Admin message, keep it Green
                trimmedLine.startsWith("[Admin @") -> {
                    withStyle(style = SpanStyle(color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)) {
                        append(line.substringBefore("]:") + "]:")
                    }
                    append(line.substringAfter("]:"))
                }

                // If the line starts with '[' and contains ' @', assume it's the User's name
                trimmedLine.startsWith("[") && trimmedLine.contains(" @") -> {
                    withStyle(style = SpanStyle(color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)) {
                        append(line.substringBefore("]:") + "]:")
                    }
                    append(line.substringAfter("]:"))
                }
                else -> append(line)
            }
            if (index < lines.size - 1) append("\n")
        }
    }

    Text(
        text = annotatedString,
        style = TextStyle(fontSize = 14.sp, color = Color.Black, lineHeight = 20.sp)
    )
}