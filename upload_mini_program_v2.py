#!/usr/bin/env python3
"""
小程序发布脚本 - 使用微信开发者工具CLI
处理登录状态检查和上传流程
"""

import os
import subprocess
import sys
import json
import time

# 配置
WECHAT_DEVTOOLS_DIR = r"D:\开发软件\微信web开发者工具"
WECHAT_CLI = os.path.join(WECHAT_DEVTOOLS_DIR, "cli.bat")
MINI_PROGRAM_PATH = r"E:\Kuaima\kuaima-uniapp\dist\build\mp-weixin"
PROJECT_PATH = r"E:\Kuaima\kuaima-uniapp"
APP_ID = "wx4c163632a2e19ea5"
VERSION = "1.0.0"
DESC = "快马日结小程序发布"


def run_cmd(cmd, timeout=60):
    """运行命令并返回结果"""
    print(f"$ {' '.join(cmd) if isinstance(cmd, list) else cmd}")
    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        encoding='utf-8',
        timeout=timeout,
        cwd=WECHAT_DEVTOOLS_DIR
    )
    print(f"  stdout: {result.stdout[:500] if result.stdout else '(empty)'}")
    print(f"  stderr: {result.stderr[:500] if result.stderr else '(empty)'}")
    print(f"  returncode: {result.returncode}")
    return result


def check_login_status():
    """检查微信开发者工具登录状态"""
    print("\n[检查登录状态]")
    
    # 尝试获取登录状态
    result = run_cmd([WECHAT_CLI, "login", "--status"])
    
    if "已登录" in result.stdout or "logged in" in result.stdout.lower():
        return True
    
    # 尝试其他方式检查
    result = run_cmd([WECHAT_CLI, "whoami"])
    if result.returncode == 0 and "code" not in result.stdout:
        return True
    
    return False


def login_wechat():
    """登录微信开发者工具"""
    print("\n[登录微信开发者工具]")
    print("  正在打开登录窗口...")
    
    # 启动登录流程
    result = run_cmd([WECHAT_CLI, "login"])
    
    if result.returncode == 0:
        print("  请在弹出的窗口中扫码登录")
        print("  等待登录完成...")
        
        # 等待用户扫码登录
        for i in range(60):  # 最多等待60秒
            time.sleep(5)
            if check_login_status():
                print("  登录成功!")
                return True
            print(f"  等待登录... ({(i+1)*5}秒)")
    
    return False


def upload_mini_program():
    """上传小程序"""
    print("\n[上传小程序]")
    
    cmd = [
        WECHAT_CLI,
        "upload",
        MINI_PROGRAM_PATH,
        "--appid", APP_ID,
        "--version", VERSION,
        "--desc", DESC,
        "--project", PROJECT_PATH
    ]
    
    result = run_cmd(cmd, timeout=120)
    
    if result.returncode == 0:
        print("  [SUCCESS] 上传成功!")
        return True
    elif "需要重新登录" in result.stdout or "需要重新登录" in result.stderr:
        print("  [需要登录] 登录状态已失效")
        return False
    else:
        print(f"  [FAILED] 上传失败")
        return False


def main():
    print("=" * 60)
    print("小程序发布工具")
    print("=" * 60)
    print(f"  项目: {PROJECT_PATH}")
    print(f"  构建产物: {MINI_PROGRAM_PATH}")
    print(f"  AppID: {APP_ID}")
    print(f"  CLI路径: {WECHAT_CLI}")
    
    # 检查构建产物
    if not os.path.exists(MINI_PROGRAM_PATH):
        print("\n[ERROR] 构建产物不存在，请先运行: npm run build:mp-weixin")
        return
    
    # 检查CLI是否存在
    if not os.path.exists(WECHAT_CLI):
        print(f"\n[ERROR] 微信开发者工具CLI不存在: {WECHAT_CLI}")
        return
    
    # 检查登录状态
    if not check_login_status():
        print("\n未登录，尝试登录...")
        if not login_wechat():
            print("\n[FAILED] 无法登录，请手动登录微信开发者工具后重试")
            return
    
    # 上传小程序
    if not upload_mini_program():
        # 尝试重新登录后上传
        print("\n尝试重新登录并上传...")
        if login_wechat():
            upload_mini_program()
    
    print("\n" + "=" * 60)
    print("发布流程结束")
    print("=" * 60)


if __name__ == "__main__":
    main()
