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

import android.util.Log
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Used to communicate between screens.
 */
class MainViewModel : ViewModel() {

    private val _drawerShouldBeOpened = MutableStateFlow(false)
    val drawerShouldBeOpened = _drawerShouldBeOpened.asStateFlow()
    private val _inputText = MutableStateFlow(TextFieldValue())
    val inputText: StateFlow<TextFieldValue> = _inputText

    fun openDrawer() {
        _drawerShouldBeOpened.value = true
    }

    fun resetOpenDrawerAction() {
        _drawerShouldBeOpened.value = false
    }

    fun updateInputText(newValue: TextFieldValue) {
        Log.d("JETCHAT_DEBUG", "updateInputText.value: ${newValue.text}")
        _inputText.value = newValue
    }

    fun clearInput() {
        Log.d("JETCHAT_DEBUG", "clearInput called")
        _inputText.value = TextFieldValue()
    }

    fun removeCurrentWord() {
        val tf = _inputText.value
        val cursor = tf.selection.start
        val text = tf.text
        Log.d("JETCHAT_DEBUG", "InputText.value: ${text}")

        if (text.isEmpty()) {
            Log.d("JETCHAT_DEBUG", "Nothing to remove. Text was empty.")
            return
        }
        if (cursor == 0) {
            Log.d("JETCHAT_DEBUG", "Nothing to remove. cursor at start.")
            return
        }

        // If cursor is in the middle of a word or at the end, still works
        val before = text.substring(0, cursor).lastIndexOf(' ').let { if (it == -1) 0 else it + 1 }
        val after = text.indexOf(' ', cursor).let { if (it == -1) text.length else it }
        if (before >= after) {
            // Should not happen, but just in case
            Log.d("JETCHAT_DEBUG", "No word found to remove. before >= after ($before >= $after)")
            return
        }
        val newText = text.removeRange(before, after)
        val newCursor = before

        Log.d("JETCHAT_DEBUG", "removeCurrentWord called. Before: [$text], After: [$newText], Cursor: $cursor -> $newCursor")

        _inputText.value = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }

}