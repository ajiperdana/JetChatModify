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
import kotlinx.coroutines.flow.asStateFlow


/**
 * Used to communicate between screens.
 */
class MainViewModel : ViewModel() {

    private val _inputText = MutableStateFlow(TextFieldValue())
    val inputText = _inputText.asStateFlow()

    private var lastDeletedWord: String? = null
    private var lastDeletedCursor: Int = 0



    private val _drawerShouldBeOpened = MutableStateFlow(false)
    val drawerShouldBeOpened = _drawerShouldBeOpened.asStateFlow()

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
        val cursor = tf.selection.end
        val text = tf.text
        if (text.isEmpty() || cursor == 0) return

        val before = text.substring(0, cursor).lastIndexOf(' ').let { if (it == -1) 0 else it + 1 }
        val after = text.indexOf(' ', cursor).let { if (it == -1) text.length else it }
        if (before >= after) return

        // Save deleted word for undo
        lastDeletedWord = text.substring(before, after)
        lastDeletedCursor = before

        val newText = text.removeRange(before, after)
        val newCursor = before

        _inputText.value = TextFieldValue(
            text = newText,
            selection = TextRange(newCursor)
        )
    }


    fun redoLastDelete() {
        val word = lastDeletedWord ?: return
        val cursor = lastDeletedCursor
        val currentText = _inputText.value.text

        // Restore the deleted word
        val newText = StringBuilder(currentText).insert(cursor, word).toString()
        _inputText.value = TextFieldValue(
            text = newText,
            selection = TextRange(cursor + word.length)
        )

        // Optionally, clear redo state so it can't redo twice
        lastDeletedWord = null
    }
}