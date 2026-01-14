# MCP Server Implementation Plan

This document outlines the plan for implementing a Model Context Protocol (MCP) server for the AngularAI project. The goal is to provide AI assistants with specialized tools to interact with the project's codebase, documentation, and running services.

## 1. Project Overview
The MCP server will be a small, lightweight service that exposes a set of tools to MCP-compatible clients (like IDEs or AI chat interfaces). It will act as a bridge between the AI and the AngularAI ecosystem.

## 2. Proposed Tools (Top 5+)

| Tool Name | Description | Source/Integration |
|-----------|-------------|-------------------|
| `search_tasks` | Search for tasks in the backend database. | Backend REST API (`/api/tasks`) |
| `read_action_logs` | Retrieve recent system logs and user actions. | Backend REST API (`/api/logs`) |
| `get_project_map` | Get a structured overview of the multi-module project (Angular, Spring, Android). | Local File System |
| `run_build_checks` | Run Maven or NPM build/test commands and report status. | Shell / CLI |
| `query_docs` | Search and retrieve content from the `doc/` directory. | Local File System (`doc/**/*.md`) |
| `get_architecture` | Retrieve architectural diagrams and descriptions. | `doc/ai/presentation/` files |

## 3. Technical Stack
- **Language**: Python 3.10+
- **Library**: `mcp` SDK (FastMCP or similar)
- **Communication**: Standard Input/Output (stdio) for IDE integration or HTTP/SSE.
- **Security**: API Key / Token based authentication for backend access.

## 4. Implementation Steps
1. **Setup**: Initialize Python environment and install MCP SDK.
2. **Backend Client**: Implement a small client to communicate with the Spring Boot API.
3. **Tool Implementation**:
    - Implement `search_tasks` and `read_action_logs` using the backend client.
    - Implement `get_project_map` by scanning the project root.
    - Implement `run_build_checks` using `subprocess`.
    - Implement `query_docs` using simple text search or vector embeddings (optional).
4. **Configuration**: Create an `mcp-config.json` for easy client setup.
5. **Testing**: Verify tools using an MCP inspector or compatible IDE.

## 5. Overhead Analysis

### Development Overhead
- **Initial Setup**: 2-4 hours (Environment, SDK setup).
- **Tool Implementation**: 8-12 hours (Mapping tools to logic).
- **Testing & Refinement**: 4-6 hours.
- **Total**: ~2-3 working days.

### Maintenance Overhead
- **Low**: The server only needs updates if the Backend API or project structure changes significantly.
- **Updates**: Estimated 1-2 hours per month for minor adjustments.

### Runtime Overhead
- **CPU**: Negligible (Idle most of the time).
- **Memory**: ~50-100 MB RAM for the Python process.
- **Disk**: < 10 MB for the code and dependencies.

## 6. Future Extensions
- Integration with AWS for cloud resource status.
- Real-time notification tool for build failures.
- Automated PR description generator tool.
