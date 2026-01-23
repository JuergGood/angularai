# Proposal: Centralized Version Management

## 1. Problem Statement
Currently, the project version (e.g., `$NewVersion`) is hardcoded in over 70 locations across multiple file types:
- Maven `pom.xml` files (4 locations)
- Frontend `package.json` (1 location)
- Deployment scripts `deploy-aws.ps1` (1 location)
- AWS ECS Task Definitions (2 locations)
- Documentation `.md` files (Multiple locations in `doc/ai/aws/`, etc.)
- Android App `build.gradle` (1 location)

This makes version updates error-prone and tedious.

## 2. Proposed Solution
We propose making the **Root `pom.xml`** the single source of truth for the project version. All other files will be synchronized based on this value.

### 2.1 Implementation Strategy

#### A. Maven Modules
Update child modules to inherit the version from the parent without restating it.
- **Root `pom.xml`**: Keep `<version>1.x.y</version>`.
- **Sub-modules (`backend`, `frontend`, `test-client`)**: Remove the redundant `<version>` tag from their `pom.xml`.

#### B. Synchronization Script (`scripts/sync-version.sh`)
Create a Bash script that performs the following:
1.  **Read** the current version from the root `pom.xml`.
2.  **Update** `frontend/package.json`.
3.  **Update** `scripts/deploy-aws.sh` (`VERSION="..."`).
4.  **Update** AWS Task Definitions (`deploy/aws/*.json`).
5.  **Update** Documentation files in `doc/ai/` using regex to replace version patterns.
6.  **Update** `frontend/.gitignore` (specifically the `.jar` exclusion).
7.  **Update** `android/app/build.gradle` (`versionName`).

#### C. Optional: Build-time Synchronization
For files needed during the build (like `package.json`), we can use the `exec-maven-plugin` in the root POM to trigger the sync script automatically during the `initialize` phase.

## 3. Workflow for Developers
1.  Open the root `pom.xml`.
2.  Update the `<version>` value.
3.  Run `./scripts/sync-version.sh` (or let Maven do it during build).
4.  Commit the changes.

## 4. Benefits
- **Consistency**: No more version mismatches between backend, frontend, and deployment tags.
- **Efficiency**: Version updates take seconds instead of minutes.
- **Reliability**: Automation reduces the risk of forgetting a file.

## 5. Script Preview (Concept)
```bash
# scripts/sync-version.sh
VERSION=$(grep -m 1 "<version>" pom.xml | sed -E 's/.*<version>(.*)<\/version>.*/\1/')

echo "Syncing version $VERSION across project..."

# 1. Update package.json
sed -i "s/\"version\": \".*\"/\"version\": \"$VERSION\"/" frontend/package.json

# 2. Update deploy-aws.sh
sed -i "s/VERSION=\".*\"/VERSION=\"$VERSION\"/" scripts/deploy-aws.sh

# 3. Update Markdown Docs (Regex based)
find doc/ai/ -name "*.md" -exec sed -i "s/1\.0\.3/$VERSION/g" {} +

# 4. Update Android build.gradle
sed -i "s/versionName \".*\"/versionName \"$VERSION\"/" android/app/build.gradle
```
