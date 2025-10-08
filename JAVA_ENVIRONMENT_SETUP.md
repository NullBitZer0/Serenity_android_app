# Java Environment Setup Guide

## Overview
This guide will help you set up the Java environment required to build and run your Android project. The Android project requires JAVA_HOME to be set for Gradle to work properly.

## Prerequisites
- Android Studio installed (already detected at `C:\Program Files\Android\Android Studio`)
- Android Studio includes a bundled JDK (Java Development Kit)

## Method 1: Permanent Setup (Recommended)

### Step 1: Open Environment Variables Dialog
1. Press `Win + R` to open the Run dialog
2. Type `sysdm.cpl` and press Enter
3. Click on the "Environment Variables" button

### Step 2: Set JAVA_HOME Variable
1. In the "System variables" section, click "New..."
2. Set the following values:
   - Variable name: `JAVA_HOME`
   - Variable value: `C:\Program Files\Android\Android Studio\jbr`
3. Click "OK"

### Step 3: Update PATH Variable
1. In the "System variables" section, find and select the "Path" variable
2. Click "Edit..."
3. Click "New" and add: `%JAVA_HOME%\bin`
4. Click "OK" on all dialogs

### Step 4: Verify Setup
1. Close all command prompt/PowerShell windows
2. Open a new PowerShell window
3. Run the following commands:
   ```powershell
   java -version
   javac -version
   echo $env:JAVA_HOME
   ```

Expected output should show:
- Java version information
- Java compiler version information
- JAVA_HOME path (`C:\Program Files\Android\Android Studio\jbr`)

## Method 2: Temporary Setup (Session Only)

If you don't have administrator privileges, you can use the provided batch file:

1. Run `set_java_env.bat` from the project directory
2. This will set the environment variables for your current session only

## Method 3: Using PowerShell (Requires Administrator)

If you have administrator privileges, you can run these commands in PowerShell as Administrator:

```powershell
# Set JAVA_HOME for current user
[Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Android\Android Studio\jbr", "User")

# Add Java to PATH for current user
$currentPath = [Environment]::GetEnvironmentVariable("PATH", "User")
[Environment]::SetEnvironmentVariable("PATH", "$currentPath;%JAVA_HOME%\bin", "User")
```

## Verification

After setting up the environment, verify it works by:

1. Opening a new PowerShell/command prompt window
2. Running the following commands:
   ```powershell
   java -version
   javac -version
   echo $env:JAVA_HOME
   ```

You should see output similar to:
```
openjdk version "17.0.6" 2023-01-17
OpenJDK Runtime Environment (build 17.0.6+0-17.0.6-1e48f85fca7c)
OpenJDK 64-Bit Server VM (build 17.0.6+0-17.0.6-1e48f85fca7c, mixed mode, sharing)

javac 17.0.6

C:\Program Files\Android\Android Studio\jbr
```

## Building Your Project

Once the Java environment is set up, you can build your project:

```powershell
cd "c:\Users\Adeesha\Desktop\mad_lab"
.\gradlew.bat clean
.\gradlew.bat build
```

## Troubleshooting

### Issue: "JAVA_HOME is not set" error
**Solution:** Make sure you've set the JAVA_HOME environment variable and opened a new command prompt/PowerShell window.

### Issue: "The term 'java' is not recognized"
**Solution:** Make sure you've added `%JAVA_HOME%\bin` to your PATH environment variable.

### Issue: Permission denied when setting environment variables
**Solution:** Run PowerShell as Administrator or use the manual method through System Properties.

### Issue: Android Studio works but command line doesn't
**Solution:** Android Studio uses its own bundled JDK, but command-line builds require JAVA_HOME to be set in the system environment.

## Additional Notes

1. The bundled JDK in Android Studio is sufficient for Android development
2. You don't need to install a separate JDK unless you have specific requirements
3. Make sure to restart your command prompt/PowerShell after setting environment variables
4. Some IDEs may require a restart after environment variable changes