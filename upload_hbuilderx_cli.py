#!/usr/bin/env python3
"""
使用 HBuilderX CLI 发布小程序
"""

import os
import subprocess
import time

# 配置
HBUILDERX_CLI = r"D:\开发软件\HBuilderX\cli.exe"
PROJECT_PATH = r"E:\Kuaima\kuaima-uniapp"
APP_ID = "wx4c163632a2e19ea5"
VERSION = "1.0.0"
DESC = "快马日结小程序发布"


def run_cmd(cmd, timeout=120):
    """运行命令"""
    print(f"$ {' '.join(cmd) if isinstance(cmd, list) else cmd}")
    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        encoding='utf-8',
        timeout=timeout
    )
    if result.stdout:
        print(f"  stdout: {result.stdout[:800]}")
    if result.stderr:
        print(f"  stderr: {result.stderr[:500]}")
    print(f"  returncode: {result.returncode}")
    return result


def main():
    print("=" * 60)
    print("HBuilderX CLI 小程序发布")
    print("=" * 60)
    print(f"  CLI路径: {HBUILDERX_CLI}")
    print(f"  项目路径: {PROJECT_PATH}")
    print(f"  AppID: {APP_ID}")
    
    # 检查CLI是否存在
    if not os.path.exists(HBUILDERX_CLI):
        print(f"\n[ERROR] CLI不存在: {HBUILDERX_CLI}")
        return
    
    # 步骤1: 查看CLI帮助
    print("\n[1/4] 查看CLI帮助...")
    result = run_cmd([HBUILDERX_CLI, "--help"])
    
    # 步骤2: 尝试发行小程序
    print("\n[2/4] 尝试发行小程序...")
    
    # HBuilderX CLI 可能的命令格式
    # cli publish --type weixin --project <path> --appid <appid> --version <version> --desc <desc>
    # 或者
    # cli release --platform mp-weixin --project <path>
    
    # 尝试不同的命令格式
    commands_to_try = [
        # 格式1
        [HBUILDERX_CLI, "publish", "--type", "weixin", "--project", PROJECT_PATH, 
         "--appid", APP_ID, "--version", VERSION, "--desc", DESC],
        # 格式2
        [HBUILDERX_CLI, "release", "--platform", "mp-weixin", "--project", PROJECT_PATH],
        # 格式3
        [HBUILDERX_CLI, "build", "--platform", "mp-weixin", "--project", PROJECT_PATH],
    ]
    
    for i, cmd in enumerate(commands_to_try):
        print(f"\n  尝试命令格式 {i+1}...")
        result = run_cmd(cmd, timeout=30)
        
        if result.returncode == 0:
            print(f"  [SUCCESS] 命令执行成功!")
            break
    
    print("\n" + "=" * 60)
    print("发布流程结束")
    print("=" * 60)


if __name__ == "__main__":
    main()
