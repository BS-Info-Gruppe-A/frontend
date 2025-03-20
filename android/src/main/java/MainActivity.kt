package eu.bsinfo.android.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.CompositionLocalProvider
import eu.bsinfo.BSInfoApp
import eu.bsinfo.data.Client
import eu.bsinfo.rest.LocalClient

class MainActivity : AppCompatActivity() {
    lateinit var client: Client
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            CompositionLocalProvider(LocalClient provides client) {
                BSInfoApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::client.isInitialized) client.close()
    }
}