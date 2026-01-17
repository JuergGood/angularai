package ch.goodone.angularai.testclient.command

import ch.goodone.angularai.testclient.client.ApiClient
import ch.goodone.angularai.testclient.model.ActionLogDTO
import ch.goodone.angularai.testclient.model.TaskDTO
import ch.goodone.angularai.testclient.model.UserDTO
import ch.goodone.angularai.testclient.service.DataService
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

class LoadCommand : Command {
    private val mapper = ObjectMapper().registerKotlinModule().registerModule(JavaTimeModule())

    companion object {
        private const val COUNT_PARAM = "--count"
        private const val FILE_PARAM = "--file"
        private const val TASKS_API = "/api/tasks"
        private const val ADMIN_LOGS_API = "/api/admin/logs"
        private const val ADMIN_USERS_API = "/api/admin/users"
    }

    override fun execute(client: ApiClient, args: List<String>) {
        if (args.isEmpty()) {
            println("Usage: load [all|tasks|users|logs|paging-data|paging|custom]")
            return
        }

        when (args[0]) {
            "all" -> loadAll(client)
            "tasks" -> loadTasks(client)
            "users" -> loadUsers(client)
            "logs" -> loadLogs(client)
            "paging-data", "paging" -> {
                if (args.getOrNull(1) == "all") {
                    loadPagingAll(client, args.drop(2))
                } else {
                    loadPagingData(client, args.drop(1))
                }
            }
            "custom" -> loadCustom(client, args.drop(1))
            else -> println("Unknown load target: ${args[0]}")
        }
    }

    private fun loadAll(client: ApiClient) {
        println("Loading all sample data...")
        loadUsers(client)
        loadLogs(client)
        loadTasks(client)
        println("Done loading all sample data.")
    }

    private fun loadLogs(client: ApiClient) {
        println("Loading sample logs...")
        DataService.createSampleLogs().forEach { log ->
            // Note: We need an endpoint to post logs if we want to load them manually.
            // Currently backend might only create them via interceptors or services.
            // If there's no POST /api/admin/logs, this will fail.
            try {
                client.post(ADMIN_LOGS_API, log, ActionLogDTO::class.java)
                println("Created log: ${log.action} at ${log.timestamp}")
            } catch (e: Exception) {
                println("Failed to create log: ${e.message}")
            }
        }
    }

    private fun loadTasks(client: ApiClient) {
        println("Loading sample tasks...")
        DataService.createSampleTasks().forEach { task ->
            client.post(TASKS_API, task, TaskDTO::class.java)
            println("Created task: ${task.title}")
        }
    }

    private fun loadUsers(client: ApiClient) {
        println("Loading sample users...")
        DataService.createSampleUsers().forEach { user ->
            client.post(ADMIN_USERS_API, user, UserDTO::class.java)
            println("Created user: ${user.login}")
        }
    }

    private fun loadPagingData(client: ApiClient, args: List<String>) {
        val count = args.getOrNull(args.indexOf(COUNT_PARAM) + 1)?.toIntOrNull() ?: 50
        val target = args.getOrNull(0) ?: "tasks"

        when (target) {
            "tasks" -> {
                println("Loading $count paging tasks...")
                DataService.createPagingTasks(count).forEach { task ->
                    client.post(TASKS_API, task, TaskDTO::class.java)
                }
                println("Done loading paging tasks.")
            }
            "logs" -> {
                println("Loading $count paging logs...")
                DataService.createPagingLogs(count).forEach { log ->
                    client.post(ADMIN_LOGS_API, log, ActionLogDTO::class.java)
                }
                println("Done loading paging logs.")
            }
            else -> println("Unknown paging target: $target. Use 'tasks' or 'logs'.")
        }
    }

    private fun loadPagingAll(client: ApiClient, args: List<String>) {
        val count = args.getOrNull(args.indexOf(COUNT_PARAM) + 1)?.toIntOrNull() ?: 50
        println("Loading all paging data (count=$count)...")
        loadPagingData(client, listOf("tasks", COUNT_PARAM, count.toString()))
        loadPagingData(client, listOf("logs", COUNT_PARAM, count.toString()))
        println("Done loading all paging data.")
    }

    private fun loadCustom(client: ApiClient, args: List<String>) {
        val filePath = args.getOrNull(args.indexOf(FILE_PARAM) + 1) ?: return println("Usage: load custom --file <path>")
        val file = File(filePath)
        if (!file.exists()) return println("File not found: $filePath")

        println("Loading custom data from $filePath...")
        // Detect type from file content or filename? Let's try to parse as TaskDTO list first
        try {
            val tasks: List<TaskDTO> = mapper.readValue(file, mapper.typeFactory.constructCollectionType(List::class.java, TaskDTO::class.java))
            tasks.forEach { client.post(TASKS_API, it, TaskDTO::class.java) }
            println("Loaded ${tasks.size} tasks.")
        } catch (e: Exception) {
            println("Failed to load custom data: ${e.message}")
        }
    }
}
