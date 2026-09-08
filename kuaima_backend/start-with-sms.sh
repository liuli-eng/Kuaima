#!/bin/bash
# 快马日结后端 - 带阿里云短信密钥 nohup 后台启动
# 密钥存放在 .env.local（已加入 .gitignore，不会提交）
cd "$(dirname "$0")"

if [ -f .env.local ]; then
  set -a
  source .env.local
  set +a
fi

if [ -z "$ALIYUN_SMS_ACCESS_KEY_ID" ]; then
  echo "[ERROR] 未找到密钥，请在 kuaima_backend/.env.local 中配置："
  echo "  ALIYUN_SMS_ACCESS_KEY_ID=你的AccessKeyId"
  echo "  ALIYUN_SMS_ACCESS_KEY_SECRET=你的AccessKeySecret"
  exit 1
fi

# 自动查找 target 下的 jar（排除 original 备份包）
JAR=$(ls target/*.jar 2>/dev/null | grep -v "original" | head -n 1)
LOG_FILE="logs/app.log"
PID_FILE="app.pid"

mkdir -p logs

if [ -z "$JAR" ]; then
  echo "[ERROR] 未找到 jar 包，请先执行: mvn package -DskipTests"
  exit 1
fi

echo "============================================"
echo "  快马日结后端 - 带阿里云短信密钥启动"
echo "============================================"

# 若已有进程在运行，先停止
if [ -f "$PID_FILE" ]; then
  OLD_PID=$(cat "$PID_FILE")
  if kill -0 "$OLD_PID" 2>/dev/null; then
    echo "[INFO] 停止旧进程 PID=$OLD_PID"
    kill "$OLD_PID"
    sleep 3
  fi
  rm -f "$PID_FILE"
fi

echo "[INFO] 阿里云短信密钥已注入环境变量: $ALIYUN_SMS_ACCESS_KEY_ID"
echo "[INFO] 正在使用 nohup 后台启动: $JAR"

nohup java -jar "$JAR" --spring.profiles.active=dev > "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"

sleep 2
NEW_PID=$(cat "$PID_FILE")
if kill -0 "$NEW_PID" 2>/dev/null; then
  echo "[OK] 启动成功，PID=$NEW_PID"
  echo "[INFO] 日志文件: $LOG_FILE   查看日志: tail -f $LOG_FILE"
  echo "[INFO] 停止服务: kill \$(cat $PID_FILE)"
else
  echo "[ERROR] 启动失败，请查看日志: $LOG_FILE"
fi
