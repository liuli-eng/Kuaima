# kuaima-h5

#### 软件架构
客户端采用 uni-app + Vue 3，统一编译 H5 和微信小程序；业务流程参考 `kuaima-uniapp-frontend` Skill 及其 `references/` 文档。`prototype/` 目录仅作为原型和视觉对照，管理后台后续单独规划为 Web 端。


#### 本地运行

本项目是基于 Vue 3 + uni-app + Vite 的多端工程，可同时编译 H5 和微信小程序。当前 Vite 版本要求 Node.js `20.19+` 或 `22.12+`。

首次运行或依赖发生变化时，在项目根目录执行：

```bash
npm install
```

启动 H5 本地环境：

```bash
npm run dev:h5:local
```

浏览器打开 <http://localhost:5173/>。如果 5173 端口已被占用，Vite 会自动切换到其他端口，并在终端打印实际地址。按 `Ctrl+C` 可停止开发服务器。

启动微信小程序本地环境：

```bash
npm run dev:mp-weixin:local
```

命令执行后，将 `dist/dev/mp-weixin` 导入微信开发者工具进行预览和调试。

真机预览时，复制本地覆盖文件：

```bash
cp .env.development.local.example .env.development.local
```

把 `.env.development.local` 中的 `192.168.2.88` 替换为运行后端电脑的局域网 IP，然后重新执行 `npm run dev:mp-weixin:local`。

环境配置统一放在：

- `.env.development`：本地环境
- `.env.test`：测试环境
- `.env.production`：生产环境
- `.env.development.local`：个人本地覆盖，不提交 Git

切换环境不再修改源码，直接使用对应命令：

```bash
# H5
npm run dev:h5:local
npm run dev:h5:test
npm run build:h5:prod

# 微信小程序
npm run dev:mp-weixin:local
npm run dev:mp-weixin:test
npm run build:mp-weixin:prod
```

各环境主要变量：

- `VITE_PROXY_TARGET`：H5 开发代理目标
- `VITE_MP_API_BASE_URL`：微信小程序接口根地址
- `VITE_USE_MOCK`：是否启用前端 Mock

常用命令：

```bash
# 使用锁定版本安装依赖（CI 或重新安装时推荐）
npm ci

# 生成 H5 生产文件
npm run build:h5

# 生成微信小程序生产文件，输出到 dist/build/mp-weixin/
npm run build:mp-weixin

# 本地预览 H5 生产构建结果
npm run preview
```

预览默认地址为 <http://localhost:4173/>，实际端口以终端输出为准。

原始高保真原型仍保留在 `prototype/` 目录，作为迁移视觉基准。当前登录页、零工首页和老板首页已使用 uni-app 跨端页面；其他业务页面将继续逐步迁移。

#### 前端开发规范

项目页面开发统一遵循本机 Skill：`kuaima-uniapp-frontend`。该 Skill 包含：

- uni-app H5/微信小程序跨端约束
- 老板端、admin、零工端业务泳道和状态转换
- 页面迁移顺序及原型映射
- 认证、审核、报名、录用、到岗、完工、结算、提现等前置条件
- 视觉验收和跨端构建要求

业务流程参考文件位于：`.codex/skills/kuaima-uniapp-frontend/references/`。

#### 使用说明

当前已接入登录、零工端首页和老板端首页等 uni-app 页面；其余业务页面仍保留在 `prototype/` 中，后续将按照业务流程逐页迁移为跨端 Vue 页面，不在正式客户端中使用 iframe。
