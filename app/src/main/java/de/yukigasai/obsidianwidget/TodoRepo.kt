package de.yukigasai.obsidianwidget

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.Instant

object TodoRepo {
    private const val TIMEOUT = 10L

    private var _currentTodos = MutableStateFlow<List<TodoItem>>(emptyList())
    val currentTodos: StateFlow<List<TodoItem>> get() = _currentTodos

    private var lastRun: Instant = Instant.EPOCH
    private val mutex = Mutex()

    fun updateTodos(config: WidgetConfig) {
        if(!config.isConfigured())return

        _currentTodos.value = FsHelper().getTodosFromFile(config.getFullPath())
    }
}