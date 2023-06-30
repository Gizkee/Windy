package app.windy.windyflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.windy.windyflow.ui.theme.WindyFlowTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WindyFlowTheme {
                val viewState by viewModel.viewState.collectAsStateWithLifecycle()

                MainScreenView(
                    input = viewState.input,
                    items = viewState.items,
                    onButtonClick = viewModel::onStartClick,
                    onInputChange = viewModel::onInputChange
                )
            }
        }
    }
}

