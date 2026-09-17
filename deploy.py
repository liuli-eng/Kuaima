#!/usr/bin/env python3
"""
快马日结项目部署脚本
功能：
1. 连接远程 MySQL 数据库并执行 SQL 脚本
2. 部署 Java 后端
3. 构建 uniapp
4. 构建 Web 管理后台
"""

import os
import sys
import paramiko
import pymysql
from pathlib import Path

# ==================== 配置 ====================
# 服务器配置
SERVER_HOST = "8.148.144.146"
SERVER_USER = "root"
SERVER_PASSWORD = "Abc223344"

# MySQL 配置
MYSQL_HOST = "8.148.144.146"
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


def execute_sql_scripts():
    """连接 MySQL 并执行 SQL 脚本"""
    print("=" * 60)
    print("开始执行 SQL 脚本")
    print("=" * 60)
    
    try:
        connection = pymysql.connect(
            host=MYSQL_HOST,
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
            
            # 分割 SQL 语句（处理存储过程）
            statements = split_sql_statements(sql_content)
            
            for stmt in statements:
                stmt = stmt.strip()
                if not stmt:
                    continue
                try:
                    cursor.execute(stmt)
                except pymysql.Error as e:
                    # 忽略 IF NOT EXISTS 相关的错误
                    if "Duplicate" in str(e) or "already exists" in str(e):
                        print(f"  [跳过] 已存在")
                    else:
                        print(f"  [错误] {e}")
                        raise
            
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
    """分割 SQL 语句，处理存储过程和触发器"""
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
    result = os.system("mvnw.cmd clean package -DskipTests -q")
    if result != 0:
        print("[FAILED] 打包失败")
        return False
    
    # 2. 上传 jar 到服务器
    print("\n[2/3] 上传 jar 到服务器...")
    jar_path = os.path.join(BACKEND_PATH, "target", "kuaima_backend.jar")
    if not os.path.exists(jar_path):
        # 查找 jar 文件
        jar_files = list(Path(BACKEND_PATH, "target").glob("*.jar"))
        if jar_files:
            jar_path = str(jar_files[0])
        else:
            print("[FAILED] 未找到 jar 文件")
            return False
    
    ssh = paramiko.SSHClient()
    ssh.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh.connect(SERVER_HOST, username=SERVER_USER, password=SERVER_PASSWORD)
    
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
    result = os.system("npm run build:mp-weixin")
    if result != 0:
        print("[FAILED] uniapp 构建失败")
        return False
    
    print("\n[SUCCESS] uniapp 构建完成")
    return True


def build_admin_web():
    """构建 Web 管理后台"""
    print("\n" + "=" * 60)
    print("开始构建 Web 管理后台")
    print("=" * 60)
    
    os.chdir(ADMIN_WEB_PATH)
    result = os.system("npm run build")
    if result != 0:
        print("[FAILED] Web 管理后台构建失败")
        return False
    
    print("\n[SUCCESS] Web 管理后台构建完成")
    return True


def main():
    print("快马日结项目部署")
    print(f"服务器: {SERVER_HOST}")
    print(f"项目路径: {LOCAL_PROJECT_ROOT}")
    
    # 步骤 1: 执行 SQL
    if not execute_sql_scripts():
        print("\n[ABORTED] SQL 执行失败，停止部署")
        sys.exit(1)
    
    # 步骤 2: 部署 Java 后端
    if not deploy_java_backend():
        print("\n[ABORTED] Java 后端部署失败")
        sys.exit(1)
    
    # 步骤 3: 构建 uniapp
    if not build_uniapp():
        print("\n[WARNING] uniapp 构建失败，继续...")
    
    # 步骤 4: 构建 Web 管理后台
    if not build_admin_web():
        print("\n[WARNING] Web 管理后台构建失败，继续...")
    
    print("\n" + "=" * 60)
    print("部署完成!")
    print("=" * 60)


if __name__ == "__main__":
    main()
