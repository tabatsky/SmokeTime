package jatx.smoketime.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import jatx.common.CommonMainActivity
import jatx.common.Content

class MainActivity : CommonMainActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content(appearance = AppearanceImpl())
        }
    }
}
