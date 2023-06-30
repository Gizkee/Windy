package app.windy.windyflow

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {

    private val _viewState: MutableStateFlow<MainScreenViewState> = MutableStateFlow(
        MainScreenViewState(
            input = "",
            items = emptyList()
        )
    )
    val viewState = _viewState.asStateFlow()

    private var sumJob: Job? = null

    fun onInputChange(input: String) {
        _viewState.update { state ->
            state.copy(input = input.filter { it.isDigit() })
        }
    }

    fun onStartClick() {
        sumJob?.cancel()
        _viewState.update { state ->
            state.copy(items = emptyList())
        }

        val input = viewState.value.input
        if (input.isEmpty()) return

        val n = input.toInt()
        if (n < 1) return

        /*
         * Результирующий Flow должен суммировать значения всех N Flow.
         * Суммирующий Flow должен возвращать значение после обновления каждого из N Flow
         */
        val sumFlow = channelFlow {
            val latestValues = arrayOfNulls<Int>(n)

            createFlowArray(size = n).forEachIndexed { index, flow ->
                launch(Dispatchers.IO) {
                    flow.collect {
                        latestValues[index] = it
                        send(latestValues.sumOfNotNull())
                    }
                }
            }
        }

        /*
         * Результат работы нужно вывести в текстовое поле.
         * Каждое обновление должно находиться на новой строчке.
         */
        sumJob = viewModelScope.launch {
            sumFlow.collect { sum ->
                _viewState.update { state ->
                    state.copy(
                        items = if (state.items.isEmpty()) {
                            listOf(sum.toString())
                        } else {
                            state.items + (state.items.last() + " $sum")
                        }
                    )
                }
            }
        }
    }

    /*
     * Необходимо создать массив Flow<Int>, количества N, каждый из которых после задержки
     * в (index + 1) * 100, эмитит значение index + 1. Т.е. Flow с индексом 0 с задержкой 100
     * эмитит значение 1, Flow с индексом 1 с задержкой 200 эмитит значение 2
     */
    private fun createFlowArray(size: Int): Array<Flow<Int>> =
        Array(size) { index ->
            flow {
                delay((index + 1) * INTERVAL_MILLIS)
                emit(index + 1)
            }
        }

    private fun Array<Int?>.sumOfNotNull() = sumOf { it ?: 0 }

    private companion object {
        const val INTERVAL_MILLIS = 100L
    }
}