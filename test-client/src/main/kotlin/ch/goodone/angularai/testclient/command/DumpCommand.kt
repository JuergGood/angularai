package ch.goodone.angularai.testclient.command

import ch.goodone.angularai.testclient.client.ApiClient
import ch.goodone.angularai.testclient.model.PageResponse
import ch.goodone.angularai.testclient.model.TaskDTO
import ch.goodone.angularai.testclient.model.UserDTO
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

class DumpCommand : Command {
    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .enable(SerializationFeature.INDENT_OUTPUT)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)

    override fun execute(client: ApiClient, args: List<String>) {
        println("Dumping database content to JSON files...")
        
        dumpTasks(client)
        dumpUsers(client)
        dumpLogs(client)
        
        println("Dump complete.")
    }

    private fun dumpTasks(client: ApiClient) {
        val tasks = client.get("/api/tasks", Array<TaskDTO>::class.java)
        File("dump_tasks.json").writeText(mapper.writeValueAsString(tasks))
        println("Dumped ${tasks.size} tasks to dump_tasks.json")
    }

    private fun dumpUsers(client: ApiClient) {
        val users = client.get("/api/admin/users", Array<UserDTO>::class.java)
        File("dump_users.json").writeText(mapper.writeValueAsString(users))
        println("Dumped ${users.size} users to dump_users.json")
    }

    private fun dumpLogs(client: ApiClient) {
        // Logs are paged, so we might need to fetch all pages or just the first few
        // For a dump, let's try to get a large number of logs
        try {
            val response = client.get("/api/admin/logs?size=1000", PageResponse::class.java)
            // Note: deserialization of PageResponse with generic T is tricky with Jackson in Kotlin 
            // without providing type reference. For dump we can just write the raw response if we handle it correctly.
            // But since we want to be sure, let's fetch content.
            File("dump_logs.json").writeText(mapper.writeValueAsString(response.content))
            println("Dumped ${response.content.size} logs to dump_logs.json")
        } catch (e: Exception) {
            println("Failed to dump logs: ${e.message}")
        }
    }
}
