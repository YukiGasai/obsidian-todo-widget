package de.yukigasai.obsidiantodowidget.todo

import de.yukigasai.obsidiantodowidget.WidgetConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object TodoRepo {
    private var _currentTodos = MutableStateFlow<List<TodoItem>>(emptyList())
    val currentTodos: StateFlow<List<TodoItem>> get() = _currentTodos
    fun updateTodos(config: WidgetConfig) {
        if(!config.isConfigured())return

        _currentTodos.value = TodoFactory(config).getTodosFromFile()
    }
}