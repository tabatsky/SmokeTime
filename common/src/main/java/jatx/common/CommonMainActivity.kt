@file:OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)

package jatx.common

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi

abstract class CommonMainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels { MainViewModel.Factory }

    abstract val appearance: Appearance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.appearance = appearance
    }

    override fun onResume() {
        super.onResume()
        viewModel.updateFromDB()
    }


}

