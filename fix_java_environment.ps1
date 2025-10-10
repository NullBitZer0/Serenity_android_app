# Requires PowerShell 5+
param(
    [string]$JdkPath,
    [ValidateSet("User", "System", "Both")]
    [string]$Scope = "User"
)

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] $Message"
}

function Write-Warn {
    param([string]$Message)
    Write-Warning "[WARN] $Message"
}

function Test-JavaHomeCandidate {
    param([string]$Path)

    if (-not $Path) {
        return $false
    }

    try {
        $expanded = [System.IO.Path]::GetFullPath($Path)
    } catch {
        return $false
    }
    $javaExe = Join-Path $expanded "bin\java.exe"

    if (Test-Path -LiteralPath $javaExe) {
        return $expanded
    }

    return $false
}

function Find-JavaHomes {
    $roots = @(
        $env:JAVA_HOME,
        "$env:ProgramFiles\Java",
        "$env:ProgramFiles\Eclipse Adoptium",
        "$env:ProgramFiles\AdoptOpenJDK",
        "$env:ProgramFiles\Zulu",
        "$env:ProgramFiles\Amazon Corretto",
        "$env:ProgramFiles\Microsoft",
        "$env:ProgramFiles\Common Files\Oracle\Java",
        "$env:LOCALAPPDATA\Programs",
        "C:\Program Files\Android\Android Studio\jbr"
    ) | Where-Object { $_ -and (Test-Path -LiteralPath $_) }

    $candidates = New-Object System.Collections.Generic.HashSet[string]

    foreach ($root in $roots) {
        try {
            Get-ChildItem -LiteralPath $root -Directory -ErrorAction Stop | ForEach-Object {
                $candidate = Test-JavaHomeCandidate $_.FullName
                if ($candidate) {
                    $null = $candidates.Add($candidate)
                }

                # check one level deeper for vendors that nest the JDK
                Get-ChildItem -LiteralPath $_.FullName -Directory -ErrorAction SilentlyContinue | ForEach-Object {
                    $nestedCandidate = Test-JavaHomeCandidate $_.FullName
                    if ($nestedCandidate) {
                        $null = $candidates.Add($nestedCandidate)
                    }
                }
            }
        } catch {
            # skip inaccessible directories
        }

        $selfCandidate = Test-JavaHomeCandidate $root
        if ($selfCandidate) {
            $null = $candidates.Add($selfCandidate)
        }
    }

    # fallback: leverage an existing java.exe in PATH, if any
    $javaCommand = Get-Command java.exe -ErrorAction SilentlyContinue
    if ($javaCommand) {
        $exePath = $javaCommand.Source
        $javaHome = Split-Path -Path (Split-Path -Path $exePath -Parent) -Parent
        $javaHomeCandidate = Test-JavaHomeCandidate $javaHome
        if ($javaHomeCandidate) {
            $null = $candidates.Add($javaHomeCandidate)
        }
    }

    return @($candidates) | Where-Object { $_ } | Sort-Object -Unique
}

function Normalize-Path {
    param([string]$Path)

    if ([string]::IsNullOrWhiteSpace($Path)) {
        return $null
    }

    try {
        return [System.IO.Path]::GetFullPath($Path.Trim())
    } catch {
        return $null
    }
}

function Prompt-ForJavaHome {
    param([string[]]$Candidates)

    if (-not $Candidates -or $Candidates.Count -eq 0) {
        return $null
    }

    if ($Candidates.Count -eq 1) {
        Write-Info "Found JDK at '$($Candidates[0])'."
        return $Candidates[0]
    }

    Write-Host "Multiple JDK installations detected. Choose the one you want to use:"
    for ($i = 0; $i -lt $Candidates.Count; $i++) {
        Write-Host ("[{0}] {1}" -f ($i + 1), $Candidates[$i])
    }

    while ($true) {
        $selection = Read-Host "Enter the number of the JDK to use"
        $parsed = 0
        if ([int]::TryParse($selection, [ref]$parsed)) {
            $index = $parsed - 1
            if ($index -ge 0 -and $index -lt $Candidates.Count) {
                return $Candidates[$index]
            }
        }

        Write-Warn "Invalid selection. Please enter a number between 1 and $($Candidates.Count)."
    }
}

function Update-EnvironmentVariables {
    param(
        [string]$JavaHome,
        [string]$Scope
    )

    $javaBin = Join-Path $JavaHome "bin"
    $normalizedJavaBin = Normalize-Path $javaBin

    $targets = switch ($Scope) {
        "System" { @("Machine") }
        "Both" { @("User", "Machine") }
        default { @("User") }
    }

    foreach ($target in $targets) {
        $targetLabel = if ($target -eq "Machine") { "system" } else { "current user" }

        Write-Info "Setting JAVA_HOME for the $targetLabel to '$JavaHome'."
        try {
            [Environment]::SetEnvironmentVariable("JAVA_HOME", $JavaHome, $target)
            if ($target -eq "User") {
                $env:JAVA_HOME = $JavaHome
            }
        } catch {
            Write-Warn "Failed to set JAVA_HOME for the $targetLabel. Try running PowerShell as Administrator. $_"
            continue
        }

        $existingPath = [Environment]::GetEnvironmentVariable("Path", $target)
        $pathEntries = @()
        if ($existingPath) {
            $pathEntries = $existingPath -split ';'
        }

        $pathNeedsUpdate = $true

        foreach ($entry in $pathEntries) {
            $normalizedEntry = Normalize-Path $entry
            if ($normalizedEntry -and $normalizedJavaBin -and $normalizedEntry -eq $normalizedJavaBin) {
                $pathNeedsUpdate = $false
                break
            }
        }

        if ($pathNeedsUpdate) {
            Write-Info "Adding '$javaBin' to the $targetLabel PATH."
            $newPathEntries = @($javaBin)
            if ($pathEntries.Count -gt 0) {
                $newPathEntries += $pathEntries
            }
            $newPath = ($newPathEntries | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) -join ';'

            try {
                [Environment]::SetEnvironmentVariable("Path", $newPath, $target)
            } catch {
                Write-Warn "Failed to update PATH for the $targetLabel. Try running PowerShell as Administrator. $_"
            }
        } else {
            Write-Info "$targetLabel PATH already contains '$javaBin'."
        }
    }

    # ensure current session picks up bin
    if (-not ($env:Path -split ';' | Where-Object { $normalizedEnv = Normalize-Path $_; $normalizedEnv -and $normalizedJavaBin -and $normalizedEnv -eq $normalizedJavaBin })) {
        $env:Path = "$javaBin;$($env:Path)"
    }
}

if ($JdkPath) {
    $validated = Test-JavaHomeCandidate $JdkPath
    if (-not $validated) {
        Write-Error "The provided path '$JdkPath' does not look like a valid JDK (missing bin\java.exe)."
        exit 1
    }
    $javaHome = $validated
} else {
    $candidates = Find-JavaHomes
    if (-not $candidates -or $candidates.Length -eq 0) {
        Write-Error "No JDK installation found automatically. Install a JDK and re-run this script, or provide the path via -JdkPath."
        exit 1
    }

    $javaHome = Prompt-ForJavaHome -Candidates $candidates
    if (-not $javaHome) {
        Write-Error "Unable to select a JDK. Aborting."
        exit 1
    }
}

Update-EnvironmentVariables -JavaHome $javaHome -Scope $Scope

Write-Host ""
Write-Info "All done! Close and reopen any terminals or IDEs to pick up the new JAVA_HOME."
Write-Info "Verify by running 'java -version' and 'javac -version' in a new PowerShell window."
