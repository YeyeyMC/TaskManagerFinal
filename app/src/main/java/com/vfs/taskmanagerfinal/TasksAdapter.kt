package com.vfs.taskmanagerfinal

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.collections.get


class TasksViewHolder (rootView: LinearLayout): RecyclerView.ViewHolder(rootView)
{
    lateinit var taskNameTextView: TextView
    lateinit var taskCompletedCheckBox: CheckBox
    lateinit var taskDividerView: View
    lateinit var ownerGroup: Group

    init
    {
        taskNameTextView = itemView.findViewById<TextView>(R.id.taskTextView_id)
        taskCompletedCheckBox = itemView.findViewById<CheckBox>(R.id.taskCompletionCheckBox_id)
        taskDividerView = itemView.findViewById<View>(R.id.tasksDividerView_id)
    }

    fun bind (task: Task, hideDivider: Boolean, listener: TaskListener)
    {
        taskNameTextView.text = task.name
        taskCompletedCheckBox.isChecked = task.completed
        if (task.completed)
        {
            taskNameTextView.paintFlags = taskNameTextView.paintFlags or
                    Paint.STRIKE_THRU_TEXT_FLAG
            itemView.setBackgroundColor(Color.GRAY)

            listener.taskStatusChanged(task)
        }
        else
        {
            taskNameTextView.paintFlags = taskNameTextView.paintFlags and
                    Paint.STRIKE_THRU_TEXT_FLAG.inv()
            itemView.setBackgroundColor(Color.TRANSPARENT)

            listener.taskStatusChanged(task)
        }
        taskDividerView.visibility = View.VISIBLE
        if (hideDivider)
            taskDividerView.visibility = View.GONE



    }
}

// Adapter for the tasks RecyclerView
class TasksAdapter (val listener: TaskListener, val group: Group) : RecyclerView.Adapter <TasksViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): TasksViewHolder {
        val rootLinearLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.task_row,
                parent,
                false) as LinearLayout

        return TasksViewHolder (rootLinearLayout)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int)
    {
        val thisTask = group.tasks[position]
        holder.bind(thisTask, position == group.tasks.count() - 1, listener)

        holder.itemView.setOnLongClickListener {
            listener.taskLongClicked(position, holder.itemView, thisTask)
            true
        }

        holder.itemView.setOnClickListener {
            listener.taskClicked(position)
        }
    }

    override fun getItemCount(): Int = group.tasks.count()
}

// Listener for the tasks RecyclerView
interface TaskListener {
    fun taskLongClicked (index: Int, view: View, task: Task)
    fun taskClicked (index: Int)
    fun taskStatusChanged (task: Task)
}
