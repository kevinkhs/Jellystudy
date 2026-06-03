@echo off
chcp 65001 >nul
title JellyStudy Docker 一键部署工具

echo ╔══════════════════════════════════════════════════╗
echo ║   JellyStudy Docker + Nacos 快速部署工具         ║
echo ╚══════════════════════════════════════════════════╝
echo.

:: ==================== 环境检查 ====================
echo [1/5] 检查Docker环境...
docker --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo     ❌ 错误: 未找到Docker！请先安装Docker Desktop
    pause
    exit /b 1
)
docker-compose --version >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo     ❌ 错误: 未找到Docker Compose！
    pause
    exit /b 1
)
echo     ✅ Docker环境正常

:: ==================== 编译项目 ====================
echo.
echo [2/5] 编译项目...
if not exist "jellystudy-question-provider\target\*.jar" (
    echo     ⏳ 首次运行，正在编译...
    call mvn clean package -DskipTests -q
    if %ERRORLEVEL% NEQ 0 (
        echo     ❌ 编译失败！请检查Maven配置
        pause
        exit /b 1
    )
) else (
    echo     ✅ JAR包已存在（跳过编译）
)

:: ==================== 构建镜像 ====================
echo.
echo [3/5] 构建Docker镜像...
call docker-compose build --no-cache question-provider-1
if %ERRORLEVEL% NEQ 0 (
    echo     ❌ 镜像构建失败！
    pause
    exit /b 1
)
echo     ✅ 镜像构建成功

:: ==================== 启动服务 ====================
echo.
echo [4/5] 启动所有服务（Redis + MongoDB + Nacos + 问题服务x2）...
call docker-compose up -d

echo.
echo     ⏳ 等待服务启动（约60秒）...

set WAIT_TIME=0
:wait_services
timeout /t 5 /nobreak >nul
set /a WAIT_TIME+=5

:: 检查Nacos是否就绪
curl -s http://localhost:8848/nacos/ >nul 2>&1
if %ERRORLEVEL%==0 (
    echo     ✅ Nacos已就绪 (%WAIT_TIME%秒)
    
    :: 再等30秒让问题服务注册完成
    timeout /t 30 /nobreak >nul
    goto success
)

if %WAIT_TIME% LSS 120 goto wait_services

echo     ❌ 服务启动超时！
pause
exit /b 1

:: ==================== 完成 ====================
:success
echo.
echo ╔══════════════════════════════════════════════════╗
echo ║          🎉 Docker部署完成！                   ║
echo ╚══════════════════════════════════════════════════╝
echo.
echo 📍 服务访问地址:
echo ┌─────────────────────────────────────────────┐
│  🐳 Docker容器状态:                               │
│    • Redis:        localhost:6379               │
│    • MongoDB:      localhost:27017              │
│    • Nacos:        localhost:8848               │
│    • 问题实例1:     localhost:8082 (Dubbo:20882) │
│    • 问题实例2:     localhost:8083 (Dubbo:20883) │
├─────────────────────────────────────────────┤
│  🔧 管理地址:                                   │
│    • Nacos控制台:   http://localhost:8848/nacos │
│    • 实例1配置API:  http://localhost:8082/...   │
│    • 实例2配置API:  http://localhost:8083/...   │
├─────────────────────────────────────────────┤
│  📋 常用命令:                                   │
│    • 查看日志: docker-compose logs -f          │
│    • 停止服务: docker-compose down             │
│    • 重启服务: docker-compose restart          │
└─────────────────────────────────────────────┘
echo.
echo 💡 下一步操作:
echo    1. 访问Nacos控制台查看双实例注册情况
echo    2. 按照 NACOS_CONFIG_GUIDE.md 添加配置参数
echo    3. 测试动态刷新功能
echo.
pause