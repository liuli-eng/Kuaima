#!/usr/bin/env python3
"""
快马日结项目部署脚本（SSH远程执行MySQL）
功能：
1. SSH登录服务器
2. 在远程服务器上执行本地SQL文件
3. 部署Java后端
4. 构建uniapp
5. 构建Web管理后台
"""

import os
import sys
import time
import paramiko
import subprocess
from pathlib import Path

# ==================== 配置 ====================
# 服务器配置
SERVER_HOST = "8.148.144.146"
SERVER_USER = "root"
SERVER_PASSWORD = "Abc223344"
SERVER_SSH_PORT = 22

# 远程MySQL配置
MYSQL_HOST = "127.0.0.1"
MYSQL_PORT = 3306
MYSQL_USER = "root"
MYSQL_PASSWORD = "11112222"
MYSQL_DATABASE = "kuaima"

# 项目路径（本地）
LOCAL_PROJECT_ROOT = r"E:\Kuaima"
BACKEND_PATH = os.path.join(LOCAL_PROJECT_ROOT, "kuaima_backend")
UNIAPP_PATH = os.path.join(LOCAL_PROJECT_ROOT, "kuaima-uniapp")
ADMIN_WEB_PATH = os.path.join(LOCAL_PROJECT_ROOT, "admin-web")

# SQL 脚本路径
SQL_SCRIPTS = [
    "db/employee_module.sql",
    "db/project_module.sql",
    "db/payroll_module.sql",
    "db/worker_profile_fields.sql",
    "db/job_category_init.sql",
    "db/boss_order_recruit_settings.sql",
    "db/boss_order_filter_fields.sql",
]


def get_ssh_client():
    """建立SSH连接"""
    print("正在连接服务器...")
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(
        SERVER_HOST,
        port=SERVER_SSH_PORT,
        username=SERVER_USER,
        password=SERVER_PASSWORD,
        timeout=30
    )
    print("SSH连接成功")
    return client


def execute_sql_via_ssh(ssh_client):
    """通过SSH在远程服务器上执行SQL脚本"""
    print("\n" + "=" * 60)
    print("开始执行 SQL 脚本")
    print("=" * 60)
    
    sftp = ssh_client.open_sftp()
    
    for sql_file in SQL_SCRIPTS:
        local_path = os.path.join(BACKEND_PATH, "src/main/resources", sql_file)
        if not os.path.exists(local_path):
            print(f"[跳过] 文件不存在: {sql_file}")
            continue
        
        print(f"\n[执行] {sql_file}")
        
        # 上传SQL文件到服务器
        remote_path = f"/root/{os.path.basename(sql_file)}"
        sftp.put(local_path, remote_path)
        print(f"  [上传] {sql_file} -> {remote_path}")
        
        # 在服务器上执行SQL
        cmd = f"mysql -u{MYSQL_USER} -p{MYSQL_PASSWORD} {MYSQL_DATABASE} < {remote_path}"
        stdin, stdout, stderr = ssh_client.exec_command(cmd, timeout=120)
        
        output = stdout.read().decode()
        error = stderr.read().decode()
        
        if error and "Warning" not in error:
            print(f"  [错误] {error[:200]}")
        else:
            print(f"  [完成] {sql_file}")
        
        # 删除远程SQL文件
        ssh_client.exec_command(f"rm -f {remote_path}")
    
    sftp.close()
    print("\n[SUCCESS] 所有 SQL 脚本执行完成")


def deploy_java_backend(ssh_client):
    """部署 Java 后端"""
    print("\n" + "=" * 60)
    print("开始部署 Java 后端")
    print("=" * 60)
    
    # 1. 打包 jar
    print("\n[1/3] 打包 jar...")
    os.chdir(BACKEND_PATH)
    result = subprocess.run(
        ["cmd", "/c", "mvnw.cmd", "clean", "package", "-DskipTests", "-q"],
        capture_output=True,
        text=True,
        cwd=BACKEND_PATH
    )
    
    if result.returncode != 0:
        print(f"[FAILED] 打包失败: {result.stderr[:500]}")
        return False
    
    # 查找 jar 文件
    jar_path = None
    target_dir = os.path.join(BACKEND_PATH, "target")
    for f in os.listdir(target_dir):
        if f.endswith('.jar') and 'sources' not in f and 'javadoc' not in f:
            jar_path = os.path.join(target_dir, f)
            break
    
    if not jar_path:
        print("[FAILED] 未找到 jar 文件")
        return False
    
    print(f"  [完成] jar 文件: {jar_path}")
    
    # 2. 上传 jar 到服务器
    print("\n[2/3] 上传 jar 到服务器...")
    sftp = ssh_client.open_sftp()
    remote_jar_path = "/root/kuaima_backend.jar"
    sftp.put(jar_path, remote_jar_path)
    sftp.close()
    print(f"  [完成] 上传 {os.path.basename(jar_path)} -> {remote_jar_path}")
    
    # 3. 启动服务
    print("\n[3/3] 启动服务...")
    commands = [
        "pkill -f kuaima_backend.jar 2>/dev/null || true",
        f"nohup java -jar {remote_jar_path} --spring.profiles.active=dev > /root/kuaima.log 2>&1 &",
        "sleep 3",
        "ps aux | grep kuaima_backend | grep -v grep"
    ]
    
    for cmd in commands:
        stdin, stdout, stderr = ssh_client.exec_command(cmd)
        output = stdout.read().decode().strip()
        if output:
            print(f"  {output}")
    
    print("\n[SUCCESS] Java 后端部署完成")
    return True


def build_uniapp():
    """构建 uniapp"""
    print("\n" + "=" * 60)
    print("开始构建 uniapp")
    print("=" * 60)
    
    result = subprocess.run(
        ["cmd", "/c", "npm", "run", "build:mp-weixin"],
        capture_output=True,
        text=True,
        cwd=UNIAPP_PATH
    )
    
    if result.returncode != 0:
        print(f"[FAILED] uniapp 构建失败: {result.stderr[:500]}")
        return False
    
    print("[SUCCESS] uniapp 构建完成")
    return True


def build_admin_web():
    """构建 Web 管理后台"""
    print("\n" + "=" * 60)
    print("开始构建 Web 管理后台")
    print("=" * 60)
    
    result = subprocess.run(
        ["cmd", "/c", "npm", "run", "build"],
        capture_output=True,
        text=True,
        cwd=ADMIN_WEB_PATH
    )
    
    if result.returncode != 0:
        print(f"[FAILED] Web 管理后台构建失败: {result.stderr[:500]}")
        return False
    
    print("[SUCCESS] Web 管理后台构建完成")
    return True


def main():
    print("快马日结项目部署")
    print(f"服务器: {SERVER_HOST}")
    print(f"项目路径: {LOCAL_PROJECT_ROOT}")
    
    ssh_client = None
    
    try:
        # 建立SSH连接
        ssh_client = get_ssh_client()
        
        # 步骤 1: 执行 SQL
        execute_sql_via_ssh(ssh_client)
        
        # 步骤 2: 部署 Java 后端
        if not deploy_java_backend(ssh_client):
            print("\n[WARNING] Java 后端部署失败，继续...")
        
        # 关闭SSH连接
        ssh_client.close()
        ssh_client = None
        
        # 步骤 3: 构建 uniapp
        if not build_uniapp():
            print("\n[WARNING] uniapp 构建失败，继续...")
        
        # 步骤 4: 构建 Web 管理后台
        if not build_admin_web():
            print("\n[WARNING] Web 管理后台构建失败，继续...")
        
        print("\n" + "=" * 60)
        print("部署完成!")
        print("=" * 60)
        
    except Exception as e:
        print(f"\n[ERROR] {e}")
        import traceback
        traceback.print_exc()
    finally:
        if ssh_client:
            ssh_client.close()


if __name__ == "__main__":
    main()
