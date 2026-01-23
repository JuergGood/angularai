# Helper script to load .env variables into the current PowerShell session

function Load-Env {
    param (
        [string]$EnvFile = ".env",
        [string]$ExampleFile = ".env.example"
    )

    $FileToLoad = $EnvFile
    if (-not (Test-Path $FileToLoad)) {
        if (Test-Path $ExampleFile) {
            Write-Host "Warning: $EnvFile not found. Falling back to $ExampleFile for dummy values." -ForegroundColor Yellow
            $FileToLoad = $ExampleFile
        } else {
            Write-Warning "Neither $EnvFile nor $ExampleFile found. Environment variables may not be set."
            return
        }
    }

    Write-Host "Loading environment variables from $FileToLoad..." -ForegroundColor Gray
    Get-Content $FileToLoad | ForEach-Object {
            $line = $_.Trim()
            if ($line -and -not $line.StartsWith("#")) {
                $name, $value = $line -split '=', 2
                if ($name -and $value) {
                    $name = $name.Trim()
                    $value = $value.Trim()
                    # Remove surrounding quotes if present
                    if ($value.StartsWith('"') -and $value.EndsWith('"')) {
                        $value = $value.Substring(1, $value.Length - 2)
                    } elseif ($value.StartsWith("'") -and $value.EndsWith("'")) {
                        $value = $value.Substring(1, $value.Length - 2)
                    }
                    [System.Environment]::SetEnvironmentVariable($name, $value, [System.EnvironmentVariableTarget]::Process)
                }
            }
        }
}

# Run the function
Load-Env
