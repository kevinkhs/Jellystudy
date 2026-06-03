@echo off
echo ========================================
echo   JellyStudy 微服务系统 - 启动脚本
echo ========================================
echo.

echo [1/5] 检查 Nacos 是否已启动...
netstat -an | findstr ":8848" > nul
if %errorlevel% neq 0 (
    echo   ⚠️  警告: Nacos 未运行！请先启动 Nacos 服务
    echo   启动命令: startup.cmd -m standalone
    pause
    exit /b 1
)
echo   ✅ Nacos 已运行

echo.
echo [2/5] 编译项目...
call C:\apache-maven-3.9.12\bin\mvn.cmd clean package -DskipTests
if %errorlevel% neq 0 (
    echo   ❌ 编译失败！
    pause
    exit /b 1
)
echo   ✅ 项目编译成功

echo.
echo ========================================
echo   开始启动微服务...
echo ========================================

echo.
echo [3/5] 启动知识点服务 (端口 8081, Dubbo 20881)...
start "Knowledge-Point-Provider" cmd /k "cd jellystudy-knowledge-point-provider && C:\apache-maven-3.9.12\bin\mvn.cmd spring-boot:run"
timeout /t 10 /nobreak > nul

echo.
echo [4/5] 启动问答服务 (端口 8082, Dubbo 20882)...
start "Question-Provider" cmd /k "cd jellystudy-question-provider && C:\apache-maven-3.9.12\bin\mvn.cmd spring-boot:run"
timeout /t 10 /nobreak > nul

echo.
echo [5/5] 启动评估服务 (端口 8083, Dubbo 20883)...
start "Evaluation-Service" cmd /k "cd jellystudy-evaluation-service && C:\apache-maven-3.9.12\bin\mvn.cmd spring-boot:run"
timeout /t 15 /nobreak > nul

echo.
echo [6/6] 启动 Web 应用 (端口 8080)...
start "JellyStudy-Web" cmd /k "cd jellystudy-web && C:\apache-maven-3.9.12\bin\mvn.cmd spring-boot:run"

echo.
echo ========================================
echo   ✅ 所有服务已启动！
echo ========================================
echo.
echo   访问地址:
echo     Web 应用: http://localhost:8080
echo     知识点服务: http://localhost:8081
echo     问答服务: http://localhost:8082
echo     评估服务: http://localhost:8083
echo     Nacos 控制台: http://localhost:8848/nacos
echo.
echo   按任意键退出...
pause > nul
