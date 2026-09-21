---
name: "kuaima-deploy"
description: "Deploy Kuaima project: execute SQL scripts on remote MySQL via SSH, deploy Java backend to /root/webapp and start with start-with-sms.sh, deploy web admin to /root/html. Invoke when user asks to deploy, publish, or push changes to the server."
---

# 快马日结项目部署

将本地代码部署到远程服务器：通过 SSH 执行 MySQL 脚本、部署 Java 后端 jar 到 `/root/webapp/` 并启动 `start-with-sms.sh`、构建 Web 管理后台并部署到 `/root/html/`。

## 触发条件

- 用户要求"部署"、"发布"、"推送到服务器"、"更新线上"
- 数据库表结构变更后需要同步到线上
- 后端或前端代码更新后需要重新部署

## 前置条件

- Python 3.11+ 已安装
- 依赖包：`paramiko`、`pymysql`（首次执行 `py -m pip install paramiko pymysql`）
- 服务器 SSH（端口 22）可达
- 项目路径：`E:\Kuaima`，包含 `kuaima_backend`、`admin-web`、`db/`

## 配置文件

脚本位于 `E:\Kuaima\deploy.py`，首次使用前需修改顶部配置：

```python
SERVER_HOST = "<服务器IP>"
SERVER_USER = "<SSH用户名>"
SERVER_PASSWORD = "<SSH密码>"

MYSQL_USER = "<数据库用户>"
MYSQL_PASSWORD = "<数据库密码>"
MYSQL_DATABASE = "<数据库名>"
```

## 远程目录结构

| 路径 | 用途 |
|------|------|
| `/root/webapp/` | Java 后端 jar 存放目录 |
| `/root/webapp/start-with-sms.sh` | 后端启动脚本 |
| `/root/html/` | Web 管理前台静态文件 |

## 执行方式

```bash
cd E:\Kuaima
py deploy.py
```

## 部署流程

1. **执行 SQL 脚本**：通过 SSH 登录服务器，上传 SQL 文件到 `/tmp/`，再通过 `mysql` 命令行执行（自动跳过已存在的键值错误）
2. **部署 Java 后端**：`mvnw clean package -Dmaven.test.skip=true` → SFTP 上传 jar 到 `/root/webapp/` → 执行 `start-with-sms.sh` 启动服务
3. **部署 Web 管理后台**：`npm run build` → 清空 `/root/html/` 旧文件 → 上传 `dist/` 内容到 `/root/html/`

## SQL 脚本约定

- 使用 `CREATE TABLE IF NOT EXISTS` 和 `ALTER TABLE ADD COLUMN`，支持重复执行
- 新增表结构变更时，在 `db/` 下新建或修改对应 `.sql` 文件，并更新 `deploy.py` 中的 `SQL_SCRIPTS` 列表
- 脚本文件统一放在 `E:\Kuaima\db/` 目录
- MySQL 不支持 `ADD COLUMN IF NOT EXISTS`，重复执行时会打印错误但**不影响已有数据**，可忽略

## 常见问题

| 问题 | 解决 |
|------|------|
| `Error reading SSH protocol banner` | 服务器 SSH 不可达，检查安全组/防火墙是否放行端口 22 |
| `ADD COLUMN IF NOT EXISTS` 语法错误 | MySQL 不支持该语法，重复执行时会报错但**不影响数据**；如需避免报错，可手动确认列已存在 |
| SQL 执行报错 `Duplicate entry` | 示例数据已存在，可忽略 |
| 打包失败 | 检查 `mvnw.cmd` 是否存在、Java 环境是否配置 |
| 测试编译失败 | 使用 `-Dmaven.test.skip=true` 跳过测试编译 |
| `start-with-sms.sh` 不存在 | 确认服务器 `/root/webapp/` 目录下存在该启动脚本 |
