package com.edson.todolist.repositories

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.edson.todolist.models.Task
import java.util.*

class TaskRepository {
    private val mDatabase = FirebaseDatabase.getInstance()

    fun create(task: Task, callback: ((Task) -> Unit)?): String
    {
        val taskRef = mDatabase.getReference("tasks").push()
        val taskMap = mutableMapOf<String, Any>()

        taskMap.put("title", task.title)
        taskMap.put("description", task.desc)
        taskMap.put("position", task.position)
        taskMap.put("done", task.done)
        taskMap.put("inCalendar", task.inCalendar)

        task.obs?.let { taskMap.put("obs", it) }
        task.color?.let { taskMap.put("color", it) }
        task.icon?.let { taskMap.put("icon", it) }
        task.date?.let { taskMap.put("date", it.time) }
        task.hasTime?.let { taskMap.put("hasTime", it) }

        taskRef
            .setValue(taskMap)
            .addOnSuccessListener {
                if (callback != null) {
                    callback(task.copy(taskRef.key))
                }
            }
        return taskRef.key!!
    }

    fun getAll(callback: ((List<Task>) -> Unit)?)
    {
        val taskRef = mDatabase.getReference("tasks").get()
        taskRef.addOnSuccessListener {
            if (callback != null) {
                val tasks = mutableListOf<Task>()
                it.children.forEach {
                    tasks.add(readTask(it))
                }
                callback(tasks)
            }
        }
    }

    fun get(key: String, callback: ((Task) -> Unit)?)
    {
        val taskRef = mDatabase.getReference("tasks/$key").get()
        taskRef.addOnSuccessListener {
            if (callback != null) {
                callback(readTask(it))
            }
        }
    }

    fun update(task: Task, callback: (Task) -> Unit)
    {
        val taskMap = mutableMapOf<String, Any>()

        taskMap.put("title", task.title)
        taskMap.put("description", task.desc)
        taskMap.put("position", task.position)
        taskMap.put("done", task.done)
        taskMap.put("inCalendar", task.inCalendar)

        task.obs?.let { taskMap.put("obs", it) }
        task.color?.let { taskMap.put("color", it) }
        task.icon?.let { taskMap.put("icon", it) }
        task.date?.let { taskMap.put("date", it.time) }
        task.hasTime?.let { taskMap.put("hasTime", it) }

        mDatabase.getReference("tasks/${task.id}")
            .updateChildren(taskMap)
            .addOnSuccessListener {
                callback(task)
            }
            .addOnFailureListener {
                throw it
            }
    }

    fun delete(task: Task, callback: () -> Unit)
    {
        mDatabase.getReference("tasks/${task.id}")
            .removeValue(object:DatabaseReference.CompletionListener {
                override fun onComplete(error: DatabaseError?, ref: DatabaseReference) {
                    if (error == null)
                    {
                        callback()
                    }
                }
            })
    }

    private fun readTask(snap: DataSnapshot): Task
    {
        val task = Task(
            snap.key,
            snap.child("title").value.toString(),
            snap.child("description").value.toString(),
            snap.child("position").value.toString().toInt()
        )

        if (snap.hasChild("obs"))
        {
            task.obs = snap.child("obs").value.toString()
        }

        if (snap.hasChild("date"))
        {
            task.date = Date(snap.child("date").value.toString().toLong())
        }

        if (snap.hasChild("color"))
        {
            task.color = snap.child("color").value.toString().toInt()
        }

        if (snap.hasChild("icon"))
        {
            task.icon = snap.child("icon").value.toString().toInt()
        }

        if (snap.hasChild("done"))
        {
            task.done = snap.child("done").value.toString().toBoolean()
        }

        if (snap.hasChild("inCalendar"))
        {
            task.inCalendar = snap.child("inCalendar").value.toString().toBoolean()
        }

        if (snap.hasChild("hasTime"))
        {
            task.hasTime = snap.child("hasTime").value.toString().toBoolean()
        }
        return task
    }
}
