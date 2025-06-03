/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.compose.jetchat

import ShakeDetector
import android.hardware.Sensor
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.viewinterop.AndroidViewBinding
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.example.compose.jetchat.components.JetchatDrawer
import com.example.compose.jetchat.databinding.ContentMainBinding
import kotlinx.coroutines.launch
import android.hardware.SensorManager
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.compose.jetchat.conversation.ConversationContent
import com.example.compose.jetchat.conversation.ConversationUiState
import com.example.compose.jetchat.data.exampleUiState
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


/**
 * Main activity for the app.
 */
class NavActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val sensorManager: SensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }
    private var shakeDetector: ShakeDetector? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        //Shake-to-clear executor
        shakeDetector = ShakeDetector(
            onDelete = { runOnUiThread { mainViewModel.removeCurrentWord() } },
            onRedo = { runOnUiThread { mainViewModel.redoLastDelete() } }
        )

        setContent {
            var isAuthenticated by remember { mutableStateOf(false) }
            var showAuthError by remember { mutableStateOf<String?>(null) }
            var triedAuth by remember { mutableStateOf(false) }

            // Show the biometric prompt as soon as the UI is loaded
            LaunchedEffect(triedAuth) {
                if (!isAuthenticated && !triedAuth) {
                    triedAuth = true
                    showBiometricPrompt(
                        activity = this@NavActivity,
                        onAuthSuccess = { isAuthenticated = true },
                        onAuthError = { error -> showAuthError = error }
                    )
                }
            }

            if (isAuthenticated) {
                // Show your main app/chat UI
                AppRoot(
                    mainViewModel = mainViewModel,
                    uiState = exampleUiState, // get this from ViewModel or wherever you manage state
                    navigateToProfile = { /* handle profile navigation */ }
                )

                val drawerState = rememberDrawerState(initialValue = Closed)
                val drawerOpen by mainViewModel.drawerShouldBeOpened
                    .collectAsStateWithLifecycle()

                var selectedMenu by remember { mutableStateOf("composers") }
                if (drawerOpen) {
                    // Open drawer and reset state in VM.
                    LaunchedEffect(Unit) {
                        // wrap in try-finally to handle interruption whiles opening drawer
                        try {
                            drawerState.open()
                        } finally {
                            mainViewModel.resetOpenDrawerAction()
                        }
                    }
                }

                val scope = rememberCoroutineScope()

                JetchatDrawer(
                    drawerState = drawerState,
                    selectedMenu = selectedMenu,
                    onChatClicked = {
                        findNavController().popBackStack(R.id.nav_home, false)
                        scope.launch {
                            drawerState.close()
                        }
                        selectedMenu = it
                    },
                    onProfileClicked = {
                        val bundle = bundleOf("userId" to it)
                        findNavController().navigate(R.id.nav_profile, bundle)
                        scope.launch {
                            drawerState.close()
                        }
                        selectedMenu = it
                    }
                ) {
                    AndroidViewBinding(ContentMainBinding::inflate)
                }
            } else if (showAuthError != null) {
                // Show error and allow retry
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Authentication failed: $showAuthError")
                        Button(onClick = {
                            showAuthError = null
                            triedAuth = false // will trigger prompt again
                        }) {
                            Text("Retry")
                        }
                    }
                }
            } else {
                // --- Splash/Loading/Lock screen ---
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.Center
                ) {
                    // Replace with your app logo or any splash animation
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "Welcome to JetChat",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(Modifier.height(24.dp))
                        CircularProgressIndicator()
                        Spacer(Modifier.height(24.dp))
                        Text("Please authenticate to continue")
                    }
                }
            }
        }
    }

    @Composable
    fun AppRoot(
        mainViewModel: MainViewModel,
        uiState: ConversationUiState,
        navigateToProfile: (String) -> Unit,
        onNavIconPressed: () -> Unit = {},
    ) {
        ConversationContent(
            uiState = uiState,
            navigateToProfile = navigateToProfile,
            onNavIconPressed = onNavIconPressed,
            mainViewModel = mainViewModel
        )
    }

    private fun showBiometricPrompt(
        activity: FragmentActivity,
        onAuthSuccess: () -> Unit,
        onAuthError: (String) -> Unit
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Fingerprint Authentication")
            .setSubtitle("Authenticate to continue")
            .setNegativeButtonText("Cancel")
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onAuthSuccess()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onAuthError(errString.toString())
                }
            })

        biometricPrompt.authenticate(promptInfo)
    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController().navigateUp() || super.onSupportNavigateUp()
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(
            shakeDetector,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_UI
        )
    }

    override fun onPause() {
        super.onPause()
        shakeDetector?.let {
            sensorManager.unregisterListener(it)
        }
    }

    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
}
