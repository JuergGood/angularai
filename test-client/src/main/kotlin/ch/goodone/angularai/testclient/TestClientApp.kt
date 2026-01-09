package ch.goodone.angularai.testclient

import ch.goodone.angularai.testclient.client.ApiClient
import ch.goodone.angularai.testclient.command.ClearCommand
import ch.goodone.angularai.testclient.command.Command
import ch.goodone.angularai.testclient.command.DumpCommand
import ch.goodone.angularai.testclient.command.LoadCommand
import java.util.Base64

fun main(args: Array<String>) {
    if (args.isEmpty()) {
        printUsage()
        return
    }

    val options = mutableMapOf<String, String>()
    val commands = mutableListOf<String>()
    
    var i = 0
    while (i < args.size) {
        if (args[i].startsWith("--")) {
            if (i + 1 < args.size && !args[i+1].startsWith("--")) {
                options[args[i]] = args[i+1]
                i += 2
            } else {
                options[args[i]] = "true"
                i++
            }
        } else {
            commands.add(args[i])
            i++
        }
    }

    if (commands.isEmpty()) {
        printUsage()
        return
    }

    val env = options["--env"] ?: "local"
    val baseUrl = when (env) {
        "local" -> "http://localhost:8080"
        "aws" -> "https://www.goodone.ch/api"
        else -> env // allow direct URL
    }

    val username = options["--user"] ?: System.getenv("TC_USERNAME") ?: "admin"
    val password = options["--pass"] ?: System.getenv("TC_PASSWORD") ?: "admin123"
    val auth = Base64.getEncoder().encodeToString("$username:$password".toByteArray())

    val client = ApiClient(baseUrl, auth)
    val commandDispatcher = mapOf<String, Command>(
        "load" to LoadCommand(),
        "clear" to ClearCommand(),
        "dump" to DumpCommand()
    )

    val commandName = commands[0]
    val command = commandDispatcher[commandName]

    if (command != null) {
        try {
            command.execute(client, commands.drop(1))
        } catch (e: Exception) {
            println("Error executing command '$commandName': ${e.message}")
            // e.printStackTrace()
        }
    } else {
        println("Unknown command: $commandName")
        printUsage()
    }
}

fun printUsage() {
    println("""
        AngularAI TestClient CLI
        
        Usage: testclient [options] <command> [args]
        
        Options:
          --env <local|aws|url>   Environment (default: local)
          --user <username>       Username (default: admin or TC_USERNAME env)
          --pass <password>       Password (default: admin123 or TC_PASSWORD env)
          
        Commands:
          load tasks              Load sample tasks
          load users              Load sample users
          load paging-data        Load extensive dataset for paging
            --count <number>      Number of tasks to load (default: 50)
          load custom             Load custom data from JSON
            --file <path>         Path to JSON file
            
          clear tasks             Delete all tasks
          clear logs              Delete all logs
          clear users             Delete all users
            --keep-defaults       Keep 'admin' and 'user' accounts
            
          dump                    Dump all tables to JSON files
    """.trimIndent())
}
