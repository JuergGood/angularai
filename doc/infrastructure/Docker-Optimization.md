# Docker Build Optimization

This document explains the strategies used to optimize Docker build times and ensure reliable dependency management in the AngularAI project.

## Dependency Caching Strategy

The primary optimization involves leveraging **Docker Layer Caching** to avoid re-downloading Maven and NPM packages on every build.

### 1. The Problem: Cache Invalidation
Docker builds images in layers. Each instruction in a `Dockerfile` creates a new layer. Docker caches these layers; however, if the files copied in a `COPY` instruction change, that layer **and all subsequent layers** are invalidated and must be rebuilt.

Previously, the `Dockerfile` copied the entire `backend/` directory (including source code) before running the Maven build. Consequently:
*   Any change to a Java file invalidated the cache.
*   Maven was forced to download all dependencies again because the downloading step followed the invalidated `COPY` step.

### 2. The Solution: Selective Copying, Dependency Pre-fetching & Cache Mounts
The optimized `Dockerfile` separates **dependency resolution** from **source code compilation** and utilizes **Docker BuildKit cache mounts** for persistent local repositories.

#### Backend (Maven) Optimization
We copy only the `pom.xml` files first to create a layer that represents the project's dependencies. We use `--mount=type=cache,target=/root/.m2` to ensure the Maven local repository is persisted across builds, even when layers are invalidated.

```bash
# Step 1: Copy ONLY pom.xml files (The "Blueprint")
COPY pom.xml .
COPY backend/pom.xml backend/
COPY test-client/pom.xml test-client/

# Step 2: Resolve and Cache Dependencies
# BuildKit cache mounts persist the .m2 folder across builds.
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B || true
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f backend/pom.xml dependency:go-offline -B || true

# Step 3: Copy volatile assets and source code
COPY --from=frontend-build /app/frontend/dist /app/frontend/dist
COPY backend/src backend/src

# Final package step also uses the cache mount
RUN --mount=type=cache,target=/root/.m2 \
    mvn -f backend/pom.xml clean package -DskipTests -Dcheckstyle.skip
```

#### Frontend (NPM) Optimization
Similarly, for the Angular frontend, we use cache mounts for the NPM cache.

```bash
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm install --legacy-peer-deps
```

### 3. Key Improvements

*   **Cache Mounts (BuildKit)**: Persists `~/.m2` and `~/.npm` across builds, preventing re-downloads even if `pom.xml` or `package.json` changes or previous layers are invalidated.
*   **Selective File Copying**: By copying only configuration files first, we create layers that change infrequently.
*   **Pre-fetching**: Using `mvn dependency:go-offline` ensures most artifacts are available before the source code is copied.
*   **Delayed Configuration Copying**: Secondary files like `dependency-check-suppressions.xml` are copied after the dependencies are cached.
*   **Multi-Module Support**: The root `pom.xml` is copied to ensure Maven understands the project structure during the `go-offline` phase.

## Comparative Analysis

| Feature | Unoptimized Build | Optimized Build |
| :--- | :--- | :--- |
| **Source code change** | Re-downloads all packages (~5-10 mins) | Re-uses cached packages (~1-2 mins) |
| **Dependency change** | Re-downloads all packages | Re-downloads (expected) |
| **Build Reliability** | Vulnerable to network glitches | Dependencies are safely pre-fetched |

## Best Practices
1.  **Always use `.dockerignore`**: Ensure large or unnecessary files (like local `node_modules`, `target`, or `.git` folders) are excluded from the Docker context.
2.  **Order matters**: Place instructions that change frequently (like `COPY src/`) as late as possible in the `Dockerfile`.
3.  **Clean up in the same layer**: If you install system packages (e.g., `apk add`), clean up caches in the same `RUN` command to keep image sizes small.
