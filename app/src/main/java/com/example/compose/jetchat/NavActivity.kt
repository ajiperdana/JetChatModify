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
import android.content.Context
import android.hardware.Sensor
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.material3.DrawerValue.Closed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
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
import android.util.Log
import androidx.compose.runtime.Composable
import com.example.compose.jetchat.conversation.ConversationContent
import com.example.compose.jetchat.conversation.ConversationUiState
import com.example.compose.jetchat.data.exampleUiState

/**
 * Main activity for the app.
 */
class NavActivity : AppCompatActivity() {
    private val mainViewModel: MainViewModel by viewModels()
    private val sensorManager: SensorManager by lazy {
        getSystemService(SENSOR_SERVICE) as SensorManager
    }
    private var shakeDetector: ShakeDetector? = null

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets -> insets }

        //ShakeDetector
        shakeDetector = ShakeDetector {
            // Called when shake is detected!
            runOnUiThread {
                mainViewModel.removeCurrentWord()
            }
        }

        setContentView(
            ComposeView(this).apply {
                consumeWindowInsets = false
                setContent {
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
                }
            }
        )
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

    /**
     * See https://issuetracker.google.com/142847973
     */
    private fun findNavController(): NavController {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        return navHostFragment.navController
    }
}
