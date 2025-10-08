@echo off
echo Setting up Java environment for Android development...
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
set PATH=%JAVA_HOME%\bin;%PATH%
echo JAVA_HOME set to: %JAVA_HOME%
echo.
echo Verifying Java installation:
java -version
echo.
echo Verifying Java compiler:
javac -version
echo.
echo Environment setup complete!
echo You can now build your Android project with:
echo   cd "c:\Users\Adeesha\Desktop\mad_lab"
echo   .\gradlew.bat build
pause