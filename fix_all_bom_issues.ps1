# PowerShell script to fix BOM issues in all XML and Kotlin files
Write-Host "Fixing BOM issues in project files..."

# Function to remove BOM from a file
function Remove-BOM {
    param([string]$FilePath)
    
    # Read the file content
    $content = Get-Content -Path $FilePath -Raw
    
    # Check if file has BOM
    $bytes = [System.IO.File]::ReadAllBytes($FilePath)
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        Write-Host "Removing BOM from $FilePath"
        # Remove BOM and write back
        [System.IO.File]::WriteAllText($FilePath, $content, [System.Text.Encoding]::UTF8)
    }
}

# Fix all XML files in res directory
Get-ChildItem -Path "app\src\main\res" -Recurse -Include "*.xml" | ForEach-Object {
    Remove-BOM $_.FullName
}

# Fix all Kotlin files
Get-ChildItem -Path "app\src\main\java" -Recurse -Include "*.kt" | ForEach-Object {
    Remove-BOM $_.FullName
}

# Fix Gradle files
Remove-BOM "build.gradle.kts"
Remove-BOM "app\build.gradle.kts"
Remove-BOM "settings.gradle.kts"

Write-Host "BOM fix completed!"