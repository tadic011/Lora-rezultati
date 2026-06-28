@echo off
setlocal
set GRADLE_VERSION=8.9
set GRADLE_HOME=%USERPROFILE%\.gradle\custom-gradle-%GRADLE_VERSION%
if exist "%GRADLE_HOME%\bin\gradle.bat" goto run
powershell -ExecutionPolicy Bypass -Command "New-Item -ItemType Directory -Force -Path $env:USERPROFILE\.gradle | Out-Null; Invoke-WebRequest -Uri https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip -OutFile $env:USERPROFILE\.gradle\gradle-%GRADLE_VERSION%-bin.zip; Expand-Archive -Force $env:USERPROFILE\.gradle\gradle-%GRADLE_VERSION%-bin.zip $env:USERPROFILE\.gradle; Rename-Item -Force $env:USERPROFILE\.gradle\gradle-%GRADLE_VERSION% custom-gradle-%GRADLE_VERSION%"
:run
"%GRADLE_HOME%\bin\gradle.bat" %*
