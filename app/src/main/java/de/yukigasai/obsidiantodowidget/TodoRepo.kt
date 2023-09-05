package de.yukigasai.obsidiantodowidget

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TodoRepo {
    private var _currentTodos = MutableStateFlow<List<TodoItem>>(emptyList())
    val currentTodos: StateFlow<List<TodoItem>> get() = _currentTodos
    fun updateTodos(config: WidgetConfig) {
        if(!config.isConfigured())return

        _currentTodos.value = FsHelper().getTodosFromFile(config)
    }
}