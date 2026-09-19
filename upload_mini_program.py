#!/usr/bin/env python3
"""
使用微信开发者工具CLI上传小程序到微信服务器
"""

import os
import subprocess
import sys

# 微信开发者工具CLI路径
WECHAT_CLI = r"D:\开发软件\微信web开发者工具\cli.bat"

# 小程序项目路径（uniapp构建输出目录）
MINI_PROGRAM_PATH = r"E:\Kuaima\kuaima-uniapp\dist\build\mp-weixin"

# 小程序AppID
APP_ID = "wx4c163632a2e19ea5"

# 版本号
VERSION = "1.0.0"

# 描述信息
DESC = "快马日结小程序发布"


def run_cli_command(args):
    """运行微信开发者工具CLI命令"""
    cmd = [WECHAT_CLI] + args
    print(f"执行命令: {' '.join(cmd)}")
    
    result = subprocess.run(
        cmd,
        capture_output=True,
        text=True,
        encoding='utf-8'
    )
    
    print(f"返回码: {result.returncode}")
    if result.stdout:
        print(f"输出:\n{result.stdout}")
    if result.stderr:
        print(f"错误:\n{result.stderr}")
    
    return result.returncode == 0


def check_cli_available():
    """检查CLI是否可用"""
    if not os.path.exists(WECHAT_CLI):
        print(f"[ERROR] 微信开发者工具CLI不存在: {WECHAT_CLI}")
        return False
    print(f"[OK] 微信开发者工具CLI路径: {WECHAT_CLI}")
    return True


def check_mini_program_exists():
    """检查小程序构建产物是否存在"""
    if not os.path.exists(MINI_PROGRAM_PATH):
        print(f"[ERROR] 小程序构建产物不存在: {MINI_PROGRAM_PATH}")
        return False
    
    # 检查关键文件
    required_files = ["app.js", "app.json", "project.config.json"]
    for f in required_files:
        file_path = os.path.join(MINI_PROGRAM_PATH, f)
        if not os.path.exists(file_path):
            print(f"[ERROR] 缺少必要文件: {f}")
            return False
    
    print(f"[OK] 小程序构建产物路径: {MINI_PROGRAM_PATH}")
    return True


def upload_mini_program():
    """上传小程序到微信服务器"""
    print("\n" + "=" * 60)
    print("开始上传小程序到微信服务器")
    print("=" * 60)
    
    # 检查前置条件
    if not check_cli_available():
        return False
    
    if not check_mini_program_exists():
        return False
    
    # 上传命令
    # cli upload <project_path> --appid <appid> --version <version> --desc <desc>
    args = [
        "upload",
        MINI_PROGRAM_PATH,
        "--appid", APP_ID,
        "--version", VERSION,
        "--desc", DESC,
        "--project", r"E:\Kuaima\kuaima-uniapp"
    ]
    
    print(f"\n上传参数:")
    print(f"  项目路径: {MINI_PROGRAM_PATH}")
    print(f"  AppID: {APP_ID}")
    print(f"  版本号: {VERSION}")
    print(f"  描述: {DESC}")
    
    success = run_cli_command(args)
    
    if success:
        print("\n[SUCCESS] 小程序上传成功！")
    else:
        print("\n[WARNING] CLI上传失败，请尝试手动操作")
        print_manual_instructions()
    
    return success


def print_manual_instructions():
    """打印手动操作指引"""
    print("\n" + "=" * 60)
    print("手动发布指引（通过HBuilderX）")
    print("=" * 60)
    print(f"""
1. 打开 HBuilderX
2. 导入项目: E:\\Kuaima\\kuaima-uniapp
3. 点击顶部菜单: 发行 -> 小程序-微信
4. 在弹出的对话框中:
   - 小程序名称: 快马日结
   - AppID: {APP_ID}
5. 点击"发行"按钮
6. HBuilderX会自动打开微信开发者工具
7. 在微信开发者工具中点击"上传"
8. 填写版本号: {VERSION}
9. 填写项目备注: {DESC}
10. 点击"上传"按钮提交到微信服务器
""")


def main():
    print("快马日结小程序发布工具")
    print(f"小程序路径: {MINI_PROGRAM_PATH}")
    print(f"AppID: {APP_ID}")
    
    # 尝试使用CLI上传
    upload_mini_program()


if __name__ == "__main__":
    main()
