package jatx.smoketime2.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import jatx.common.CommonMainActivity
import jatx.common.Content

class MainActivity : CommonMainActivity() {

    override val appearance by lazy {
        AppearanceImpl(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Content(appearance = appearance)
        }
    }
}
