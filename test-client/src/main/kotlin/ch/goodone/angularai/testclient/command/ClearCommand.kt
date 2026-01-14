package ch.goodone.angularai.testclient.command

import ch.goodone.angularai.testclient.client.ApiClient
import ch.goodone.angularai.testclient.model.TaskDTO
import ch.goodone.angularai.testclient.model.UserDTO

class ClearCommand : Command {
    override fun execute(client: ApiClient, args: List<String>) {
        if (args.isEmpty()) {
            println("Usage: clear [all|tasks|logs|users]")
            return
        }

        when (args[0]) {
            "all" -> clearAll(client)
            "tasks" -> clearTasks(client)
            "logs" -> clearLogs(client)
            "users" -> clearUsers(client, args.contains("--keep-defaults"))
            else -> println("Unknown clear target: ${args[0]}")
        }
    }

    private fun clearAll(client: ApiClient) {
        println("Clearing all sample data...")
        clearTasks(client)
        clearLogs(client)
        clearUsers(client, true) // Keep defaults for 'all' to avoid locking self out easily
        println("Done clearing all sample data.")
    }

    private fun clearTasks(client: ApiClient) {
        println("Clearing all tasks...")
        val tasks = client.get("/api/tasks", Array<TaskDTO>::class.java)
        tasks.forEach { task ->
            client.delete("/api/tasks/${task.id}")
        }
        println("Cleared ${tasks.size} tasks.")
    }

    private fun clearLogs(client: ApiClient) {
        println("Clearing all logs...")
        client.delete("/api/admin/logs")
        println("Logs cleared.")
    }

    private fun clearUsers(client: ApiClient, keepDefaults: Boolean) {
        println("Clearing users (keepDefaults=$keepDefaults)...")
        val users = client.get("/api/admin/users", Array<UserDTO>::class.java)
        var count = 0
        users.forEach { user ->
            if (keepDefaults && (user.login == "admin" || user.login == "user" || user.login == "admin-read")) {
                println("Skipping default user: ${user.login}")
            } else {
                try {
                    client.delete("/api/admin/users/${user.id}")
                    count++
                } catch (e: Exception) {
                    println("Failed to delete user ${user.login}: ${e.message}")
                }
            }
        }
        println("Cleared $count users.")
    }
}
