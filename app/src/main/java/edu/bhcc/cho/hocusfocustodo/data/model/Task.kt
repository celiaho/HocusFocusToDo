package edu.bhcc.cho.hocusfocustodo.data.model

data class Task(
    val id: String,
    var text: String,
    var isCompleted: Boolean = false,
    val quadrant: String,
    var dueDate: String? = null
)