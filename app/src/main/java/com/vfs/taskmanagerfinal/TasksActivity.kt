package com.vfs.taskmanagerfinal

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.PopupMenu
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class TasksActivity : AppCompatActivity(), TaskListener
{
    lateinit var thisGroup: Group
    lateinit var tasksAdapter: TasksAdapter

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.tasks_layout)

        val index = intent.getIntExtra("index", 0)
        thisGroup = AppData.groups[index]

        val grpTextView = findViewById<TextView>(R.id.grpNameTextView_id)
        grpTextView.text = thisGroup.name

        val tasksRv = findViewById<RecyclerView>(R.id.tasksRv_id)
        tasksRv.layoutManager = LinearLayoutManager(this)

        tasksAdapter = TasksAdapter (this, thisGroup)
        tasksRv.adapter = tasksAdapter
    }


    // Adds a new task to the group
    fun addNewTask(v: View)
    {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("New Task")
        builder.setMessage("Enter a name for your new task")

        val nameEditText = EditText(this)
        builder.setView(nameEditText)

        builder.setPositiveButton("Add") {_, _ ->
            val newTask = Task (nameEditText.text.toString(),
                false)
            thisGroup.tasks.add(newTask)
            tasksAdapter.notifyDataSetChanged()
            writeTasks(newTask, thisGroup)
        }

        builder.setNegativeButton("Cancel") { _, _ -> }

        val dialog = builder.create()
        dialog.show()
    }


    // Shows a pop-up menu for tasks
    override fun taskLongClicked(index: Int, view: View, task: Task)
    {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.popup_menu, popupMenu.menu)

        val editItem = popupMenu.menu.findItem(R.id.editTextView_id)
        val deleteItem = popupMenu.menu.findItem(R.id.deleteTextView_id)

        editItem.title = "Edit Task"
        deleteItem.title = "Delete Task"

        popupMenu.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId)
            {
                R.id.editTextView_id ->
                {
                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("Edit Task")
                    builder.setMessage("Enter the new name for your task")

                    val nameEditText = EditText(this)
                    builder.setView(nameEditText)

                    builder.setPositiveButton("Add") {_, _ ->
                        val oldTaskName = task.name

                        task.name = nameEditText.text.toString()
                        tasksAdapter.notifyDataSetChanged()
                        editTasks(task, thisGroup, oldTaskName)
                    }

                    builder.setNegativeButton("Cancel") { _, _ -> }

                    val dialog = builder.create()
                    dialog.show()

                    true
                }
                R.id.deleteTextView_id ->
                {
                    thisGroup.tasks.removeAt(index)
                    tasksAdapter.notifyDataSetChanged()
                    deleteTasks(task, thisGroup)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // Updates the task status (Checked/Unchecked)
    override fun taskClicked(index: Int)
    {
        thisGroup.tasks[index].completed = !thisGroup.tasks[index].completed
        tasksAdapter.notifyDataSetChanged()
    }

    override fun taskStatusChanged(task: Task) {
        updateCheckBoxTask(task, thisGroup)
    }
}