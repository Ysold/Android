package com.tristannio.todo.tasklist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tristannio.todo.network.Api
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TasksListViewModel : ViewModel() {
    private val webService = Api.tasksWebService

    private val _tasksStateFlow = MutableStateFlow<List<Task>>(emptyList())
    public val tasksStateFlow: StateFlow<List<Task>> = _tasksStateFlow.asStateFlow()

    suspend fun refresh() {
        viewModelScope.launch {
            val response = webService.getTasks()
            if (response.isSuccessful) {
                val fetchedTasks = response.body()!!
                _tasksStateFlow.value = fetchedTasks
            }
        }
    }

    suspend fun update(task: Task) {
        viewModelScope.launch {
            val response = webService.update(task, task.id)
            if (response.isSuccessful) {
                val updateTask = response.body()!!
                _tasksStateFlow.value = _tasksStateFlow.value - task + updateTask
            }
        }
    }

    suspend fun create(task: Task) {
        viewModelScope.launch {
            val response = webService.create(task)
            if (response.isSuccessful) {
                val newTask = response.body()!!
                _tasksStateFlow.value = _tasksStateFlow.value + newTask
            }
        }
    }

    suspend fun delete(task: Task) {
        viewModelScope.launch {
            val response = webService.delete( task.id)
            if (response.isSuccessful) {
                val newTask = response.body()!!
                _tasksStateFlow.value = _tasksStateFlow.value - task
            }
        }
    }
}