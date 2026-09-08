@echo off
chcp 65001 >nul
cd /d %~dp0

echo ============================================
echo   快马日结后端 - 带阿里云短信密钥启动
echo ============================================
echo.

REM 密钥存放在 .env.local（已加入 .gitignore，不会提交）
if exist .env.local (
  for /f "usebackq tokens=1,* delims==" %%a in (.env.local) do (
    if "%%a"=="ALIYUN_SMS_ACCESS_KEY_ID" set ALIYUN_SMS_ACCESS_KEY_ID=%%b
    if "%%a"=="ALIYUN_SMS_ACCESS_KEY_SECRET" set ALIYUN_SMS_ACCESS_KEY_SECRET=%%b
  )
)

if "%ALIYUN_SMS_ACCESS_KEY_ID%"=="" (
  echo [ERROR] 未找到密钥，请在 kuaima_backend/.env.local 中配置：
  echo   ALIYUN_SMS_ACCESS_KEY_ID=你的AccessKeyId
  echo   ALIYUN_SMS_ACCESS_KEY_SECRET=你的AccessKeySecret
  pause
  exit /b 1
)

echo [INFO] 阿里云短信密钥已注入环境变量: %ALIYUN_SMS_ACCESS_KEY_ID%
echo [INFO] 正在启动 Spring Boot 服务...
echo.

mvn spring-boot:run -Dspring-boot.run.profiles=dev

pause
