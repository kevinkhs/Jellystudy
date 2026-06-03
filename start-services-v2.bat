@echo off
chcp 65001 >nul
title JellyStudy 完整启动工具

echo ╔══════════════════════════════════════════╗
echo ║   JellyStudy 项目完整启动工具 v2.0      ║
echo ╚══════════════════════════════════════════╝
echo.

:: ==================== 第1步：清理环境 ====================
echo [1/5] 清理旧进程...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul
echo     ✅ 已清理

:: ==================== 第2步：检查Java环境 ====================
echo.
echo [2/5] 检查Java环境...
java -version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo     ❌ 错误: 未找到Java！请安装JDK 11+
    pause
    exit /b 1
)
echo     ✅ Java已安装

:: ==================== 第3步：启动Nacos（关键步骤）====================
echo.
echo [3/5] 启动 Nacos 服务（最重要）...
echo.

:: 检查Nacos目录是否存在
if not exist "C:\nacos\nacos\bin\startup.cmd" (
    echo     ❌ 错误: 找不到Nacos!
    echo     请确认路径: C:\nacos\nacos\bin\startup.cmd
    pause
    exit /b 1
)

:: 启动Nacos（前台模式，方便查看错误）
start "Nacos Server" cmd /k "cd /d C:\nacos\nacos\bin && startup.cmd -m standalone"
echo     ⏳ 正在启动Nacos，请耐心等待...

:: 等待Nacos完全就绪（最多90秒）
set NACOS_READY=0
set WAIT_COUNT=0

:wait_nacos
timeout /t 5 /nobreak >nul
set /a WAIT_COUNT+=5

:: 检查端口8848
netstat -an | findstr ":8848" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    set NACOS_READY=1
    echo.
    echo     ✅✅✅ Nacos 启动成功! (用时 %WAIT_COUNT% 秒)
    goto nacos_ok
)

:: 超时检查
if %WAIT_COUNT% GEQ 90 (
    echo.
    echo     ❌❌❌ Nacos 启动超时 (等待了90秒)!
    echo.
    echo     可能的原因:
    echo     1. 端口8848被占用
    echo     2. 内存不足
    echo     3. Java版本不兼容
    echo.
    echo     请查看"Nacos Server"窗口中的错误信息!
    goto nacos_fail
)

:: 显示进度
set /a REMAINING=90-%WAIT_COUNT
echo     ⏳ 已等待 %WAIT_COUNT% 秒 (剩余 %REMAINING% 秒)...

goto wait_nacos

:nacos_ok
echo.
echo     验证地址: http://localhost:8848/nacos
echo     默认账号: nacos / nacos
goto start_eval

:nacos_fail
echo.
set /p CHOICE="是否继续尝试启动其他服务? (y/n): "
if /i "%CHOICE%"=="y" goto start_eval
pause
exit /b 1

:: ==================== 第4步：启动评估服务 ====================
:start_eval
echo.
echo [4/5] 启动评估服务...
start "Evaluation Service" cmd /k "cd /d c:\onlywork\jellystudy\jellystudy-evaluation-service && mvn spring-boot:run"
echo     ⏳ 等待评估服务启动...

set EVAL_WAIT=0
:wait_eval
timeout /t 5 /nobreak >nul
set /a EVAL_WAIT+=5
netstat -an | findstr ":20883" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo     ✅ 评估服务已启动! (%EVAL_WAIT% 秒)
    goto start_web
)
if %EVAL_WAIT% LSS 45 goto wait_eval
echo     ⚠️ 评估服务可能还在启动中，继续...

:: ==================== 第5步：提示启动Web服务 ====================
:start_web
echo.
echo ╔══════════════════════════════════════════╗
echo ║   ✅ 基础服务已准备就绪!              ║
echo ╚══════════════════════════════════════════╝
echo.
echo [5/5] 请手动启动 Web 服务:
echo.
echo ┌─────────────────────────────────────────┐
echo │  打开新的 CMD 窗口，依次执行:          │
echo │                                         │
echo │  cd c:\onlywork\jellystudy\jellystudy-web\target │
echo │  java -jar jellystudy-web-1.0.0-SNAPSHOT.jar    │
echo └─────────────────────────────────────────┘
echo.
echo 📍 访问地址:
echo    主页: http://localhost:8080
echo    测试: http://localhost:8080/question/6a02e18ef48c6d35047c0e67
echo.
echo 💡 测试功能:
echo    • 点击 "🤖 AI 回答" 按钮
echo    • 点击 "删除" 按钮测试删除功能
echo.
pause
