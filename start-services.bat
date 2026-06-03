@echo off
chcp 65001 >nul
title JellyStudy 一键启动脚本

echo ============================================
echo   JellyStudy 项目一键启动工具
echo ============================================
echo.

echo [步骤 1/3] 清理旧进程...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul
echo ✅ 已清理

echo.
echo [步骤 2/3] 启动 Nacos...
start "Nacos" cmd /k "cd /d C:\nacos\nacos\bin && startup.cmd -m standalone"
echo ⏳ 等待 Nacos 启动 (40秒)...

set WAIT_TIME=0
:check_nacos
timeout /t 5 /nobreak >nul
set /a WAIT_TIME+=5
netstat -an | findstr ":8848" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo ✅ Nacos 已启动! (用时 %WAIT_TIME% 秒)
    goto start_eval
)
if %WAIT_TIME% LSS 40 (
    echo   已等待 %WAIT_TIME% 秒...
    goto check_nacos
) else (
    echo ❌ Nacos 启动超时！请手动检查
    pause
    exit /b 1
)

:start_eval
echo.
echo [步骤 3/3] 启动评估服务...
start "Evaluation Service" cmd /k "cd /d c:\onlywork\jellystudy\jellystudy-evaluation-service && mvn spring-boot:run"
echo ⏳ 等待评估服务启动 (45秒)...

set WAIT_TIME=0
:check_eval
timeout /t 5 /nobreak >nul
set /a WAIT_TIME+=5
netstat -an | findstr ":20883" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo ✅ 评估服务已启动! (用时 %WAIT_TIME% 秒)
    goto start_web
)
if %WAIT_TIME% LSS 45 (
    echo   已等待 %WAIT_TIME% 秒...
    goto check_eval
) else (
    echo ❌ 评估服务启动超时！
    pause
    exit /b 1
)

:start_web
echo.
echo ============================================
echo   ✅ 基础服务已就绪!
echo ============================================
echo.
echo 📍 请手动启动 Web 服务:
echo.
echo   cd c:\onlywork\jellystudy\jellystudy-web\target
echo   java -jar jellystudy-web-1.0.0-SNAPSHOT.jar
echo.
echo 🌐 访问地址: http://localhost:8080
echo 🧪 测试页面: http://localhost:8080/question/6a02e18ef48c6d35047c0e67
echo.
pause
