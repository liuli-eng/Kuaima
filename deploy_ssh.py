#!/usr/bin/env python3
"""
快马日结项目部署脚本（通过SSH隧道连接远程MySQL）
功能：
1. 建立SSH隧道连接远程MySQL
2. 执行SQL脚本
3. 部署Java后端
4. 构建uniapp
5. 构建Web管理后台
"""

import os
import sys
import time
import subprocess
import paramiko
import pymysql
from pathlib import Path

# ==================== 配置 ====================
# 服务器配置
SERVER_HOST = "8.148.144.146"
SERVER_USER = "root"
SERVER_PASSWORD = "Abc223344"
SERVER_SSH_PORT = 22

# MySQL 配置（通过SSH隧道）
MYSQL_LOCAL_PORT = 3307  # 本地转发端口
MYSQL_REMOTE_HOST = "127.0.0.1"
MYSQL_REMOTE_PORT = 3306
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


class SSHTunnel:
    """SSH隧道管理器"""
    
    def __init__(self, server_host, server_port, username, password, 
                 remote_host, remote_port, local_port):
        self.server_host = server_host
        self.server_port = server_port
        self.username = username
        self.password = password
        self.remote_host = remote_host
        self.remote_port = remote_port
        self.local_port = local_port
        self.client = None
        self.transport = None
    
    def start(self):
        """建立SSH隧道"""
        print(f"正在建立SSH隧道: localhost:{self.local_port} -> {self.remote_host}:{self.remote_port}")
        
        self.client = paramiko.SSHClient()
        self.client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        self.client.connect(
            self.server_host,
            port=self.server_port,
            username=self.username,
            password=self.password,
            timeout=30
        )
        
        # 获取transport
        self.transport = self.client.get_transport()
        
        # 请求端口转发
        self.transport.request_port_forward("127.0.0.1", self.local_port)
        
        # 启动转发线程
        self.transport.set_keepalive(30)
        
        print(f"SSH隧道已建立: localhost:{self.local_port} -> {self.remote_host}:{self.remote_port}")
        return True
    
    def stop(self):
        """关闭SSH隧道"""
        if self.client:
            self.client.close()
            print("SSH隧道已关闭")


def execute_sql_scripts():
    """连接 MySQL 并执行 SQL 脚本"""
    print("=" * 60)
    print("开始执行 SQL 脚本")
    print("=" * 60)
    
    try:
        connection = pymysql.connect(
            host="127.0.0.1",
            port=MYSQL_LOCAL_PORT,
            user=MYSQL_USER,
            password=MYSQL_PASSWORD,
            database=MYSQL_DATABASE,
            charset='utf8mb4',
            autocommit=False
        )
        cursor = connection.cursor()
        
        for sql_file in SQL_SCRIPTS:
            sql_path = os.path.join(BACKEND_PATH, "src/main/resources", sql_file)
            if not os.path.exists(sql_path):
                print(f"[跳过] 文件不存在: {sql_file}")
                continue
            
            print(f"\n[执行] {sql_file}")
            with open(sql_path, 'r', encoding='utf-8') as f:
                sql_content = f.read()
            
            # 分割 SQL 语句
            statements = split_sql_statements(sql_content)
            
            for stmt in statements:
                stmt = stmt.strip()
                if not stmt:
                    continue
                try:
                    cursor.execute(stmt)
                except pymysql.Error as e:
                    # 忽略已存在的错误
                    if "Duplicate" in str(e) or "already exists" in str(e):
                        print(f"  [跳过] 已存在")
                    else:
                        print(f"  [警告] {e}")
            
            connection.commit()
            print(f"  [完成] {sql_file}")
        
        cursor.close()
        connection.close()
        print("\n[SUCCESS] 所有 SQL 脚本执行完成")
        return True
        
    except Exception as e:
        print(f"\n[FAILED] SQL 执行失败: {e}")
        return False


def split_sql_statements(sql_content):
    """分割 SQL 语句，处理存储过程"""
    statements = []
    current = []
    delimiter = ';'
    in_procedure = False
    
    for line in sql_content.split('\n'):
        line = line.strip()
        
        # 跳过注释
        if line.startswith('--') or line.startswith('/*'):
            continue
        
        # 处理 DELIMITER
        if line.upper().startswith('DELIMITER'):
            delimiter = line.split()[1]
            continue
        
        current.append(line)
        
        if line.endswith(delimiter):
            stmt = ' '.join(current)
            if delimiter != ';':
                # 存储过程结束
                in_procedure = False
                statements.append(stmt)
                current = []
                delimiter = ';'
            else:
                statements.append(stmt)
                current = []
    
    return statements


def deploy_java_backend():
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
        text=True
    )
    
    if result.returncode != 0:
        print(f"[FAILED] 打包失败: {result.stderr}")
        return False
    
    # 查找 jar 文件
    jar_path = None
    target_dir = os.path.join(BACKEND_PATH, "target")
    for f in os.listdir(target_dir):
        if f.endswith('.jar') and 'sources' not in f:
            jar_path = os.path.join(target_dir, f)
            break
    
    if not jar_path:
        print("[FAILED] 未找到 jar 文件")
        return False
    
    print(f"  [完成] jar 文件: {jar_path}")
    
    # 2. 上传 jar 到服务器
    print("\n[2/3] 上传 jar 到服务器...")
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER_HOST, port=SERVER_SSH_PORT, username=SERVER_USER, password=SERVER_PASSWORD)
    
    sftp = ssh.open_sftp()
    remote_jar_path = "/root/kuaima_backend.jar"
    sftp.put(jar_path, remote_jar_path)
    sftp.close()
    print(f"  [完成] 上传 {jar_path} -> {remote_jar_path}")
    
    # 3. 启动服务
    print("\n[3/3] 启动服务...")
    commands = [
        "pkill -f kuaima_backend.jar 2>/dev/null || true",
        f"nohup java -jar {remote_jar_path} --spring.profiles.active=dev > /root/kuaima.log 2>&1 &",
        "sleep 3",
        "ps aux | grep kuaima_backend"
    ]
    
    for cmd in commands:
        stdin, stdout, stderr = ssh.exec_command(cmd)
        output = stdout.read().decode()
        if output:
            print(f"  {output.strip()}")
    
    ssh.close()
    print("\n[SUCCESS] Java 后端部署完成")
    return True


def build_uniapp():
    """构建 uniapp"""
    print("\n" + "=" * 60)
    print("开始构建 uniapp")
    print("=" * 60)
    
    os.chdir(UNIAPP_PATH)
    result = subprocess.run(
        ["cmd", "/c", "npm", "run", "build:mp-weixin"],
        capture_output=True,
        text=True
    )
    
    if result.returncode != 0:
        print(f"[FAILED] uniapp 构建失败: {result.stderr}")
        return False
    
    print("\n[SUCCESS] uniapp 构建完成")
    return True


def build_admin_web():
    """构建 Web 管理后台"""
    print("\n" + "=" * 60)
    print("开始构建 Web 管理后台")
    print("=" * 60)
    
    os.chdir(ADMIN_WEB_PATH)
    result = subprocess.run(
        ["cmd", "/c", "npm", "run", "build"],
        capture_output=True,
        text=True
    )
    
    if result.returncode != 0:
        print(f"[FAILED] Web 管理后台构建失败: {result.stderr}")
        return False
    
    print("\n[SUCCESS] Web 管理后台构建完成")
    return True


def main():
    print("快马日结项目部署")
    print(f"服务器: {SERVER_HOST}")
    print(f"项目路径: {LOCAL_PROJECT_ROOT}")
    
    # 建立 SSH 隧道
    tunnel = SSHTunnel(
        SERVER_HOST, SERVER_SSH_PORT, SERVER_USER, SERVER_PASSWORD,
        MYSQL_REMOTE_HOST, MYSQL_REMOTE_PORT, MYSQL_LOCAL_PORT
    )
    
    try:
        tunnel.start()
        time.sleep(2)  # 等待隧道建立
        
        # 步骤 1: 执行 SQL
        if not execute_sql_scripts():
            print("\n[ABORTED] SQL 执行失败，停止部署")
            return
        
        # 步骤 2: 部署 Java 后端
        if not deploy_java_backend():
            print("\n[ABORTED] Java 后端部署失败")
            return
        
        # 步骤 3: 构建 uniapp
        if not build_uniapp():
            print("\n[WARNING] uniapp 构建失败，继续...")
        
        # 步骤 4: 构建 Web 管理后台
        if not build_admin_web():
            print("\n[WARNING] Web 管理后台构建失败，继续...")
        
        print("\n" + "=" * 60)
        print("部署完成!")
        print("=" * 60)
        
    finally:
        tunnel.stop()


if __name__ == "__main__":
    main()
