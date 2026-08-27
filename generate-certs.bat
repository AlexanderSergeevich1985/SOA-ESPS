@echo off
setlocal enabledelayedexpansion

:: Check if the user has provided at least one service name as an argument
if "%~1"=="" (
    echo ❌ Error: No services specified for certificate generation.
    echo 💡 Usage: %~0 [service1] [service2] [service3] ...
    echo 📝 Example: %%~0 auth-service payments-service profile-service scheduler-service
    exit /b 1
)

:: =============================================================================
:: DYNAMIC PROJECT ROOT DETECTION
:: =============================================================================
:: Get the directory where THIS script file actually resides
set "SCRIPT_DIR=%~dp0"
:: Remove trailing backslash
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

:: Resolve the absolute project root (assuming script is inside a subfolder like .\script)
:: If the script is in root, PROJECT_ROOT will be equal to SCRIPT_DIR
if exist "%SCRIPT_DIR%\..\pom.xml" (
    cd /d "%SCRIPT_DIR%\.."
    set "PROJECT_ROOT=!CD!"
) else (
    cd /d "%SCRIPT_DIR%"
    set "PROJECT_ROOT=!CD!"
)

echo 🏠 Project root detected at: "%PROJECT_ROOT%"

:: =============================================================================
:: SMART JAVA DETECTOR FOR CUSTOM USER PATHS
:: =============================================================================
set "KEYTOOL_BIN=keytool"

where keytool >nul 2>nul
if %errorlevel% equ 0 goto java_ready

if defined JAVA_HOME (
    if exist "%JAVA_HOME%\bin\keytool.exe" (
        set "KEYTOOL_BIN=%JAVA_HOME%\bin\keytool"
        goto java_ready
    )
)

for /r "%USERPROFILE%\Documents" %%f in (keytool.exe) do (
    if exist "%%f" (
        set "KEYTOOL_BIN=%%~dpfkeytool"
        goto java_ready
    )
)

for /d %%d in ("%USERPROFILE%\AppData\Local\JetBrains\IntelliJIdea*\jbr") do (
    if exist "%%d\bin\keytool.exe" (
        set "KEYTOOL_BIN=%%d\bin\keytool"
        goto java_ready
    )
)

for /d %%d in ("C:\Program Files\Java\jdk*") do (
    if exist "%%d\bin\keytool.exe" (
        set "KEYTOOL_BIN=%%d\bin\keytool"
        goto java_ready
    )
)

echo ❌ Error: "keytool.exe" utility not found anywhere.
exit /b 1

:java_ready
echo 🔍 Using keytool wrapper: "%KEYTOOL_BIN%"

:: Target passwords for stores (synchronized with your root .env file)
set "PASSWORD=my_keystore_password_p12"
set "TRUST_PASSWORD=my_internal_truststore_password_p12"

:: =============================================================================
:: CLEANUP & INITIALIZATION OF ABSOLUTE PATHS
:: =============================================================================
set "CERTS_DIR=%PROJECT_ROOT%\certs"

:: If a nested or messy certs dir exists, purge it completely for a fresh build
if exist "%CERTS_DIR%" (
    echo 🧹 Cleaning up old certificates from "%CERTS_DIR%"...
    rmdir /s /q "%CERTS_DIR%"
)
mkdir "%CERTS_DIR%"
cd /d "%CERTS_DIR%"

echo === [1/3] Generating Shared Internal Truststore ===
"%KEYTOOL_BIN%" -genkeypair -alias delete-me -keyalg RSA -keysize 2048 -validity 1 ^
  -keystore internal-truststore.p12 -storepass "%TRUST_PASSWORD%" -storetype PKCS12 ^
  -dname "CN=Temporary"
"%KEYTOOL_BIN%" -delete -alias delete-me -keystore internal-truststore.p12 -storepass "%TRUST_PASSWORD%"

echo === [2/3] Generating Keystores for Specified Services ===
:loop
if "%~1"=="" goto endloop
set "SERVICE=%~1"

echo --------------------------------------------------------
echo Processing certs for: !SERVICE!
echo --------------------------------------------------------

:: Generate a private keystore explicitly inside the clean target directory
"%KEYTOOL_BIN%" -genkeypair ^
  -alias "!SERVICE!" ^
  -keyalg RSA ^
  -keysize 2048 ^
  -validity 365 ^
  -keystore "%CERTS_DIR%\!SERVICE!.p12" ^
  -storepass "%PASSWORD%" ^
  -storetype PKCS12 ^
  -ext "SAN=dns:!SERVICE!,dns:localhost" ^
  -dname "CN=!SERVICE!, OU=SOA-ESPS, O=Development, L=Local, C=RU"

:: Export the public certificate
"%KEYTOOL_BIN%" -exportcert -alias "!SERVICE!" -keystore "%CERTS_DIR%\!SERVICE!.p12" -storepass "%PASSWORD%" -file "%CERTS_DIR%\!SERVICE!.crt"

:: Import the public certificate into the centralized master truststore
"%KEYTOOL_BIN%" -importcert -noprompt -alias "!SERVICE!-public" -file "%CERTS_DIR%\!SERVICE!.crt" -keystore "%CERTS_DIR%\internal-truststore.p12" -storepass "%TRUST_PASSWORD%"

if exist "%CERTS_DIR%\!SERVICE!.crt" del "%CERTS_DIR%\!SERVICE!.crt"

:: 3. Special absolute routing routine for scheduler-service (Classpath injection)
if "!SERVICE!"=="scheduler-service" (
    echo 🔹 Detected scheduler-service. Injecting certs to classpath via absolute path...

    :: Duplicate the master truststore matching the exact name used in scheduler's config
    copy "%CERTS_DIR%\internal-truststore.p12" "%CERTS_DIR%\scheduler-service-truststore.p12" >nul

    :: Use target absolute path directly relative to the project root
    set "SCHEDULER_RES_DIR=%PROJECT_ROOT%\scheduler-service\src\main\resources\certs"
    if not exist "!SCHEDULER_RES_DIR!" mkdir "!SCHEDULER_RES_DIR!"

    :: Copy directly into the target resource directory safely
    copy "%CERTS_DIR%\!SERVICE!.p12" "!SCHEDULER_RES_DIR!\" >nul
    copy "%CERTS_DIR%\scheduler-service-truststore.p12" "!SCHEDULER_RES_DIR!\" >nul
)

shift
goto loop
:endloop

echo ========================================================
echo 🚀 SUCCESS! Certificates securely generated in:
echo    👉 "%CERTS_DIR%"
echo ========================================================
pause