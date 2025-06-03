package com.example.compose.jetchat.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubble
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color

val JetChatBlue = Color(0xFF1752F3)
val JetChatBlueLight = Color(0xFF5087F8)
@Composable
fun AuthenticationFailedScreen(
    errorMessage: String,
    onRetry: () -> Unit
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(JetChatBlueLight),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(min = 280.dp, max = 340.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.Fingerprint,
                    contentDescription = "Fingerprint",
                    modifier = Modifier.size(64.dp).alpha(0.7f),
                    tint = JetChatBlue
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Authentication Failed",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Red,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp),
                    maxLines = 2
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onRetry,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JetChatBlue,
                        contentColor = Color.White
                    )
                ) {
                    Icon(Icons.Outlined.Fingerprint, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Try Again")
                }
            }
        }
    }
}

@Composable
fun JetchatSplashScreen() {
    Box(
        Modifier
            .fillMaxSize()
            .background(JetChatBlueLight), // Use your custom blue here!
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .padding(32.dp)
                .widthIn(min = 280.dp, max = 340.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                Modifier.padding(horizontal = 36.dp, vertical = 36.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.ChatBubble,
                    contentDescription = "JetChat Logo",
                    modifier = Modifier.size(64.dp).alpha(0.8f),
                    tint = JetChatBlue // Also use the blue for the icon!
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Welcome to JetChat!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontSize = 28.sp),
                    color = JetChatBlue,
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Please authenticate to continue",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(32.dp))
                CircularProgressIndicator(
                    color = JetChatBlue,
                    strokeWidth = 4.dp,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}

