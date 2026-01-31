# Model Context Protocol (MCP) & Autonomous Agents

This document explains the architecture of the **Autonomous Agent** system used in this project and how it leverages the **Model Context Protocol (MCP)** to interact with your code.

## 1. The Autonomous Agent Concept

In this environment, "Junie" acts as an **Autonomous Agent** rather than a traditional reactive chatbot.

| Traditional Chatbot | Autonomous Agent (Junie) |
| :--- | :--- |
| **Reactive**: Waits for user input to provide text. | **Proactive**: Takes a high-level goal and generates a plan. |
| **Information-Limited**: Only knows what you paste into the chat. | **Environment-Aware**: Can explore the project filesystem and structure. |
| **Advisory**: Tells you how to fix code. | **Operational**: Executes tools to fix, build, and verify code directly. |

### The "Loop" of Autonomy
1.  **Observe**: Read error logs or user requests.
2.  **Explore**: Use tools to find relevant code, dependencies, or configurations.
3.  **Plan**: Create a multi-step strategy to resolve the issue.
4.  **Execute**: Apply code changes and run terminal commands.
5.  **Verify**: Run tests or builds to ensure the fix works.

## 2. MCP: The Standardized Interface

**Model Context Protocol (MCP)** is the communication standard that enables the AI's "brain" to talk to the "hands" (the tools and the environment).

### Why MCP?
Before MCP, every AI integration (for IntelliJ, VS Code, or custom scripts) required a unique, hard-coded "wiring." MCP provides a standardized protocol, allowing the same AI logic to work across different IDEs and modules seamlessly.

### How it works in this Project
When Junie works on the **Android module**, **Backend**, or **Frontend**, it uses MCP-compliant tools provided by the **Environment**.

#### A. The Environment (The Sandbox)
The environment is a secure, virtualized workspace containing:
*   A copy of your project files.
*   An operating system (Windows/PowerShell).
*   An **MCP Client** that hosts the AI session.

#### B. The MCP Servers (The Toolbox)
Inside the sandbox, several MCP Servers run as micro-services, providing specialized tools:

*   **File System Server**: Provides `open`, `create`, `list_dir`, and `search_project`. It indexes the files so the AI can "see" the project structure.
*   **Language Parser Server**: Provides `get_file_structure`. It uses **Tree-sitter** to parse languages like Kotlin, Java, and TypeScript, allowing the AI to understand class hierarchies and method signatures without reading the whole file.
*   **Terminal/Shell Server**: Provides the `bash` (PowerShell) tool to execute commands like `./gradlew` or `mvn`.

## 3. Case Study: Working on the Android Module

Even though the Android module is a standalone Gradle project, it is fully accessible to Junie because the **Environment's MCP tools are language-agnostic**.

1.  **Context Injection**: Junie calls the `search_project` MCP tool. The MCP Server searches the filesystem and finds Kotlin files in `android/app/src/main/java/`.
2.  **Structural Analysis**: Junie calls `get_file_structure` on `MainActivity.kt`. The Language Parser (MCP Server) uses its Kotlin parser to return a list of Composable functions.
3.  **Tool Execution**: Junie calls the Terminal MCP tool to run `$env:JAVA_HOME = "..." ; ./gradlew assembleDebug`. The Shell Server executes this in the sandbox and streams the logs back to Junie.

## 4. Relationship with Custom MCP Servers

There is a distinction between the **Built-in MCP Tools** (used by Junie natively) and **Project-Specific MCP Servers** (documented in `mcp-server.md`).

*   **Built-in (Infrastructure)**: Focuses on "Generic Engineering Tasks" (File I/O, Terminal, Parsing). These are active right now.
*   **Custom (Application)**: Focuses on "Domain Tasks" (Querying the running Task database, reading Action Logs via API).

For a detailed analysis of when to implement a custom MCP server, see [mcp-analysis.md](./mcp-analysis.md).
