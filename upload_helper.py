#!/usr/bin/env python3
"""
小程序发布脚本 - 使用HBuilderX命令行
"""

import os
import subprocess
import time

# 配置
HBUILDERX_PATH = r"D:\开发软件\HBuilderX\HBuilderX.exe"
PROJECT_PATH = r"E:\Kuaima\kuaima-uniapp"
APP_ID = "wx4c163632a2e19ea5"


def check_hbuilderx_cli():
    """检查HBuilderX是否有CLI"""
    print("[检查HBuilderX CLI]")
    
    # HBuilderX 命令行工具路径
    cli_paths = [
        os.path.join(os.path.dirname(HBUILDERX_PATH), "cli.exe"),
        os.path.join(os.path.dirname(HBUILDERX_PATH), "hbuilderx-cli.exe"),
        r"D:\开发软件\HBuilderX\cli\cli.exe",
    ]
    
    for path in cli_paths:
        if os.path.exists(path):
            print(f"  找到CLI: {path}")
            return path
    
    print("  未找到HBuilderX CLI")
    return None


def main():
    print("=" * 60)
    print("小程序发布工具")
    print("=" * 60)
    
    # 检查CLI
    cli = check_hbuilderx_cli()
    
    if cli:
        print(f"\n使用CLI: {cli}")
        # TODO: 使用CLI发布
    else:
        print("\n[HBuilderX CLI不可用]")
        print("\n请按以下步骤手动发布：")
        print_manual_instructions()


def print_manual_instructions():
    print("""
╔══════════════════════════════════════════════════════════════╗
║                    小程序手动发布步骤                          ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  方式一：通过 HBuilderX                                      ║
║  ──────────────────────────────────────────────────────────  ║
║  1. 打开 HBuilderX (D:\\开发软件\\HBuilderX\\HBuilderX.exe)     ║
║  2. 菜单: 文件 → 导入 → 从本地目录导入                        ║
║  3. 选择: E:\\Kuaima\\kuaima-uniapp                           ║
║  4. 菜单: 发行 → 小程序-微信                                 ║
║  5. 确认信息:                                                ║
║     - 小程序名称: 快马日结                                    ║
║     - AppID: wx4c163632a2e19ea5                             ║
║  6. 点击"发行"按钮                                           ║
║  7. HBuilderX会自动打开微信开发者工具                          ║
║  8. 在微信开发者工具中点击"上传"                              ║
║                                                              ║
║  方式二：通过微信开发者工具                                   ║
║  ──────────────────────────────────────────────────────────  ║
║  1. 打开微信开发者工具                                        ║
║     (D:\\开发软件\\微信web开发者工具\\微信开发者工具.exe)       ║
║  2. 导入项目: E:\\Kuaima\\kuaima-uniapp\\dist\\build\\mp-weixin  ║
║  3. 使用微信账号扫码登录                                      ║
║  4. 点击右上角"上传"按钮                                      ║
║  5. 填写版本号: 1.0.0                                        ║
║  6. 填写项目备注: 快马日结小程序发布                           ║
║  7. 点击"上传"提交到微信服务器                                ║
║                                                              ║
╠══════════════════════════════════════════════════════════════╣
║  发布完成后，请登录微信公众平台提交审核:                        ║
║  https://mp.weixin.qq.com/                                   ║
╚══════════════════════════════════════════════════════════════╝
""")


if __name__ == "__main__":
    main()
