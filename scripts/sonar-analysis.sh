#!/bin/bash

# SYNOPSIS: Runs SonarCloud analysis locally.
# DESCRIPTION: This script runs a full Maven verify followed by a SonarCloud scan.

# Load .env file if it exists
if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
fi

TOKEN=${1:-$SONAR_TOKEN}

if [ -z "$TOKEN" ]; then
  echo "Error: SONAR_TOKEN is not set. Please provide it as an argument or set it in .env"
  exit 1
fi

echo "Starting Local SonarCloud Analysis..."

# 1. Frontend: Ensure coverage report is generated
echo "Step 1: Running Frontend Tests with Coverage..."
cd frontend
npm install --legacy-peer-deps
npm test
cd ..

# 2. Root: Run Maven verify and Sonar scan
echo "Step 2: Running Maven Verify and Sonar Scan..."
mvn verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.token="$TOKEN" \
  -Dsonar.projectKey=JuergGood_angularai \
  -Dsonar.organization=juerggood \
  -Dsonar.javascript.lcov.reportPaths=frontend/coverage/lcov.info \
  -Dsonar.coverage.jacoco.xmlReportPaths=backend/target/site/jacoco/jacoco.xml,test-client/target/site/jacoco/jacoco.xml,android/app/build/reports/jacoco/testDebugUnitTest/testDebugUnitTest.xml

if [ $? -eq 0 ]; then
    echo "SonarCloud Analysis completed successfully!"
    echo "Check your results at: https://sonarcloud.io/project/dashboard?id=JuergGood_angularai"
else
    echo "SonarCloud Analysis failed"
    exit 1
fi
