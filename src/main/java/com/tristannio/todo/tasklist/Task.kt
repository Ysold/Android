package com.tristannio.todo.tasklist

import kotlinx.serialization.Serializable

@Serializable
data class Task(val id : String, val title : String, val description : String = "Message par default"):java.io.Serializable;
