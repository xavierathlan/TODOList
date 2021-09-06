package com.edson.todolist.models

import java.util.*

data class Task(val id: String?, val title: String, val desc: String, var position: Int)
{
    var obs: String? = null
    var date: Date? = null
    var color: Int? = null
    var icon: Int? = null
    var done: Boolean = false
    var inCalendar: Boolean = false
    var hasTime: Boolean = false
}