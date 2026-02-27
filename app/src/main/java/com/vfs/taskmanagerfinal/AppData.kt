package com.vfs.taskmanagerfinal

// Data for individual tasks
data class Task(
    var name: String = "",
    var completed: Boolean = false
)

data class Group(
    var name: String = "",
    var tasks: MutableList<Task> = mutableListOf()
)

// Initial data for the application
class AppData
{
    companion object
    {
        var groups: MutableList<Group> = mutableListOf()

        fun initialize ()
        {
            val task_1 = Task("Click to Toggle", false)
            val task_2 = Task("Long Click to Delete", false)
            val task_3 = Task("Ballons", false)


            val group_1 = Group ("Click to see the Items", mutableListOf(task_1, task_2, task_3))
            val group_2 = Group ("Long Click to Delete", mutableListOf(task_1, task_2))
            val group_3 = Group ("Gym", mutableListOf(task_3, task_2))

            groups = mutableListOf(group_1, group_2, group_3)
        }
    }
}