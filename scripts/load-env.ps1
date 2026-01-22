# Helper script to load .env variables into the current PowerShell session

function Load-Env {
    param (
        [string]$EnvFile = ".env"
    )

    if (Test-Path $EnvFile) {
        Write-Host "Loading environment variables from $EnvFile..." -ForegroundColor Gray
        Get-Content $EnvFile | ForEach-Object {
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
    } else {
        Write-Warning "Environment file $EnvFile not found."
    }
}

# Run the function
Load-Env
