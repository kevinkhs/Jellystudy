@echo off
chcp 65001 >nul
title JellyStudy 完整系统启动器

echo ╔═══════════════════════════════════════════════╗
echo ║     JellyStudy 微服务系统 - 完整启动脚本      ║
echo ╚═══════════════════════════════════════════════╝
echo.

:: ==================== 第1步：清理环境 ====================
echo [1/8] 清理旧进程...
taskkill /F /IM java.exe >nul 2>&1
timeout /t 3 /nobreak >nul
echo     ✅ 已清理

:: ==================== 第2步：检查Redis ====================
echo.
echo [2/8] 检查Redis...
netstat -an | findstr ":6379" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo     ✅ Redis已运行 (端口6379)
) else (
    echo     ⚠️  Redis未运行! 请先启动Memurai/Redis
    pause
    exit /b 1
)

:: ==================== 第3步：启动Nacos ====================
echo.
echo [3/8] 启动 Nacos 注册中心 (端口8848)...
start "Nacos Server" cmd /k "cd /d C:\nacos\nacos\bin && startup.cmd -m standalone"
echo     ⏳ 等待Nacos启动...

set NACOS_WAIT=0
:wait_nacos
timeout /t 5 /nobreak >nul
set /a NACOS_WAIT+=5
netstat -an | findstr ":8848" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo     ✅ Nacos已启动! (%NACOS_WAIT%秒)
    goto start_kp
)
if %NACOS_WAIT% LSS 90 goto wait_nacos
echo     ❌ Nacos启动超时!
pause
exit /b 1

:: ==================== 第4步：启动知识点服务 ====================
:start_kp
echo.
echo [4/8] 启动知识点服务 (Dubbo:20881)...
start "Knowledge-Point-Provider" cmd /k "cd /d c:\onlywork\jellystudy\jellystudy-knowledge-point-provider && mvn spring-boot:run"
echo     ⏳ 等待知识点服务启动...

set KP_WAIT=0
:wait_kp
timeout /t 5 /nobreak >nul
set /a KP_WAIT+=5
netstat -an | findstr ":20881" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo     ✅ 知识点服务已启动! (%KP_WAIT%秒)
    goto start_question
)
if %KP_WAIT% LSS 60 goto wait_kp
echo     ⚠️ 知识点服务可能还在启动中...

:: ==================== 第5步：启动问题服务 ====================
:start_question
echo.
echo [5/8] 启动问题服务 (Dubbo:20882, Redis缓存)...
start "Question Provider" cmd /k "cd /d c:\onlywork\jellystudy\jellystudy-question-provider && mvn spring-boot:run"
echo     ⏳ 等待问题服务启动...

set Q_WAIT=0
:wait_q
timeout /t 5 /nobreak >nul
set /a Q_WAIT+=5
netstat -an | findstr ":20882" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo     ✅ 问题服务已启动! (%Q_WAIT%秒)
    goto start_eval
)
if %Q_WAIT% LSS 60 goto wait_q
echo     ⚠️ 问题服务可能还在启动中...

:: ==================== 第6步：启动评估服务 ====================
:start_eval
echo.
echo [6/8] 启动评估服务 (HTTP:8083, Dubbo:20883)...
start "Evaluation Service" cmd /k "cd /d c:\onlywork\jellystudy\jellystudy-evaluation-service && mvn spring-boot:run"
echo     ⏳ 等待评估服务启动...

set E_WAIT=0
:wait_e
timeout /t 5 /nobreak >nul
set /a E_WAIT+=5
netstat -an | findstr ":20883" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo     ✅ 评估服务已启动! (%E_WAIT%秒)
    goto start_zipkin
)
if %E_WAIT% LSS 60 goto wait_e
echo     ⚠️ 评估服务可能还在启动中...

:: ==================== 第7步：启动Zipkin ====================
:start_zipkin
echo.
echo [7/8] 启动 Zipkin 调用链追踪 (端口9411)...
start "Zipkin Server" cmd /k "cd /d c:\onlywork\jellystudy && java -jar zipkin.jar"
echo     ⏳ 等待Zipkin启动...

set Z_WAIT=0
:wait_z
timeout /t 5 /nobreak >nul
set /a Z_WAIT+=5
netstat -an | findstr ":9411" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo     ✅ Zipkin已启动! (%Z_WAIT%秒)
    goto start_web
)
if %Z_WAIT% LSS 30 goto wait_z
echo     ⚠️ Zipkin可能还在启动中...

:: ==================== 第8步：启动Web前端 ====================
:start_web
echo.
echo [8/8] 启动 Web 前端应用 (端口8080)...
start "JellyStudy Web" cmd /k "cd /d c:\onlywork\jellystudy\jellystudy-web && mvn spring-boot:run"
echo     ⏳ 等待Web服务启动...

set W_WAIT=0
:wait_w
timeout /t 5 /nobreak >nul
set /a W_WAIT+=5
netstat -an | findstr ":8080" | findstr "LISTENING" >nul
if %ERRORLEVEL%==0 (
    echo     ✅ Web服务已启动! (%W_WAIT%秒)
    goto success
)
if %W_WAIT% LSS 45 goto wait_w
echo     ⚠️ Web服务可能还在启动中...

:: ==================== 启动完成 ====================
:success
echo.
echo ╔═══════════════════════════════════════════════╗
echo ║          🎉 所有服务已成功启动！              ║
echo ╚═══════════════════════════════════════════════╝
echo.
echo 📍 服务访问地址:
echo ┌─────────────────────────────────────────────┐
echo │  🌐 Web应用:        http://localhost:8080   │
echo │  📚 知识点服务:     http://localhost:8081   │
echo │  ❓ 问题服务:       http://localhost:8082   │
echo │  🤖 评估服务:       http://localhost:8083   │
echo │  🔧 Nacos控制台:    http://localhost:8848   │
echo │  🔍 Zipkin调用链:   http://localhost:9411   │
echo │  💾 Redis缓存:      localhost:6379         │
echo └─────────────────────────────────────────────┘
echo.
echo 💡 测试功能:
echo    • 访问主页查看问题列表
echo    • 点击 "🤖 AI 回答" 测试AI功能
echo    • 查看 Zipkin 监控调用链
echo.
echo 📝 本地终端窗口说明:
echo    • 每个服务都在独立窗口中运行
echo    • 关闭窗口会停止对应服务
echo    • 按 Ctrl+C 可优雅停止服务
echo.
pause