package com.vfs.taskmanagerfinal

class User
{
    var name: String? = null
    var email: String? = null
    var id: String = ""
    var groups: MutableList<Group> = mutableListOf()

    constructor(n: String?, e: String?, i: String, g: MutableList<Group>)
    {
        this.name = n
        this.email = e
        this.id = i
        this.groups = g
    }

    constructor(map: Map<String, String>)
    {
        map["name"]?.let { this.name = it }
        map["email"]?.let { this.email = it }
        map["id"]?.let { this.id = it }
        map["groups"]?.let { this.groups = it as MutableList<Group> }
    }
}