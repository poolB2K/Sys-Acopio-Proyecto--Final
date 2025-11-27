@echo off
echo ================================================
echo  LIMPIEZA TOTAL DE REPORTES COMPILADOS
echo ================================================
echo.

REM Detener cualquier instancia de Java ejecutando la app
echo [1/5] Deteniendo aplicaciones Java...
taskkill /F /IM java.exe /T 2>nul
timeout /t 2 /nobreak >nul

REM Eliminar archivos .jasper del directorio jasper/
echo [2/5] Eliminando archivos .jasper antiguos...
if exist "jasper\*.jasper" (
    del /F /Q "jasper\*.jasper"
    echo    - Eliminados archivos en jasper/
) else (
    echo    - No hay archivos en jasper/
)

REM Limpiar completamente el directorio target
echo [3/5] Limpiando directorio target con Maven...
call mvn clean
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Maven clean fallo
    pause
    exit /b 1
)

REM Recompilar el proyecto
echo [4/5] Recompilando proyecto...
call mvn compile
if %ERRORLEVEL% NEQ 0 (
    echo ERROR: Compilacion fallo
    pause
    exit /b 1
)

echo.
echo ================================================
echo  LIMPIEZA COMPLETADA EXITOSAMENTE
echo ================================================
echo.
echo Ahora puedes ejecutar la aplicacion con:
echo    mvn javafx:run
echo.
echo O presiona cualquier tecla para iniciarla ahora...
pause >nul

REM Iniciar la aplicación
echo [5/5] Iniciando aplicacion...
call mvn javafx:run
