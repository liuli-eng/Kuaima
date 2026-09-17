# 项目长期记忆（Kuaima 日结项目）

## 技术栈与代码约定
- **后端**：Spring Boot + JPA，`com.kuaima.app.*`（注意包名是 `app` 不是 `controller` 直挂）。实体继承 `BaseEntity`；金额以「分」(Long) 存储；控制器统一返回 `Result<T>`（code/message/data/page/total），身份取 JWT 不信任前端 userId。boss 端前缀 `/boss/projects/...`，admin 端 `/admin/...`。
- **uniapp（老板端）**：Options API（`export default {data,methods,onLoad}`），`navigationStyle:custom`；HTTP 走 `src/api/http.js` 的 `request()`，自动注入 `Authorization`/`X-User-Id` 并解包 `payload.data`。
- **admin-web**：Vue3 `<script setup>` + Element Plus 2.14.5 + vue-router 5 + Pinia。`@/api/*` 模块模式：`request.get(url,{params})`，解构 `res.data`/`res.total`。全局 CSS 变量（`var(--primary)` 等）来自 admin.css；`.status-badge` 颜色类 success/warning/danger/default。路由带 `permKey` 时由守卫校验，无权限则菜单不显示且直访重定向 dashboard。

## 当前模块进度
- SQL：project / employee / payroll 模块已落盘 `kuaima_backend/.../db`。
- 后端：project / employee / payroll 领域 entity/service/controller 已生成。
- uniapp：boss 项目管理模块 9 个页面已生成 + profile.vue 入口。
- admin-web：员工管理、发薪管理（列表/详情）已生成。

## admin-web 业务约束（已校验修正）
- **权限模型**：后端角色/员工 `permissions` 字段以**嵌套对象**存储 `{permOrg:{org_member:true}, permPayroll:{batch_payroll:true,...}}`；`/admin/employee-roles/permission-tree` 返回嵌套 `{key,name,children:[{key,name}]}`。前端须用此格式（叶子 key：org_member/org_project/batch_payroll/payroll_approve/transfer_record/balance_view），不要用扁平数组。
- **发薪单状态流**：无 draft 状态，新建即 `pending`；`tab=submitted`=非 withdrawn，`tab=reviewed`=approved/rejected。列表页审批（通过/驳回）须对 `pending` 行显示（reviewed tab 后端不返回 pending，故不能放在 reviewed tab）。
- **发薪单过滤**：`GET /admin/payrolls` 仅支持 `tab/status/projectId/keyword`（keyword 搜 title/creator/reviewBy），不支持 `company`；company/projectName 过滤只能前端本地做。

## 版本控制
- **E:\Kuaima 是 Git 仓库**（`kuaima_backend` 本身无独立 .git）。查配置/代码改动历史用 `git log -- <file>` + `git show <hash>:<file>`。git 输出中文乱码（GBK），但内容可读。
- **数据库地址变更史**（`application-dev.yml`）：`fd3bcba`→`4a88aad` 为 `127.0.0.1:3306/kuaima` root/**11112222**；`08b3c79`(09-14 20:46 Hanson) 改为局域网 `192.168.2.73:3306/kuaima` user/**zhUTA5rp228hjKSW**；`d209782`(09-14 23:43 zhouyangbo 解合并冲突) 改回 `127.0.0.1` root/**root** 并延续至今。
- `application.yml` 的 `spring.profiles.active` 在 `c457264`(09-15 14:54) 由 `dev` 改为 **`test`** —— 排查配置不生效时先看激活的是哪个 profile。

## 本地构建/运行环境（2026-09-14 实测）
- **JDK**：`D:\Java\jdk-17.0.13+11`（JAVA_HOME）。Java 17 已在 PATH。
- **Maven**：系统 PATH 无 `mvn`。可用 IntelliJ 内置 `D:\apache-maven-3.8.1`，或项目 `mvnw`（wrapper 3.3.4，会下载 3.9.16）；已手动解压一份到 `C:\Users\Administrator\Downloads\maven-extract\apache-maven-3.9.16`。`.m2/repository` 依赖已缓存，但首次编译需联网拉 `spring-boot-configuration-processor`（离线 `-o` 会失败）。
- **MySQL**：Windows 服务 `MySQL80` 已安装且运行中。dev/test 现均为 `127.0.0.1:3306/kuaima`、`root/root`（历史曾用 `root/11112222` 与局域网 `192.168.2.73` user/`zhUTA5rp228hjKSW`）。JPA `ddl-auto=update` 会自动建表。
- **启动命令**：`java -jar target/kuaima-0.0.1-SNAPSHOT.jar --server.port=8080`。**必须显式指定端口**——不指定时会绑定到 53575，而该端口被 WorkBuddy 进程占用导致 `Port 53575 was already in use`。
- **打包前先停应用**：运行中的 java 进程会占用 jar，导致 spring-boot repackage 报 `Unable to rename ... to .jar.original`。
- **沙箱工具限制**：Bash 缺 coreutils（grep/head/dirname/which）；`cmd /c` 在 Bash 中实际启动交互式 shell，不可靠；PowerShell 工具**不回显 stdout**，需把结果写入文件再 Read；PowerShell 内**禁止调用 cmd.exe**；`Start-Process` 重定向输出不可靠，可靠写法是 `$o = & mvn.cmd ... 2>&1` 再 `WriteAllText` 到文件。

## 外部约束
- `mockplus` 原型（rp.mockplus.cn）因密码门未能读取，产品说明均取自本地 13 个 HTML 原型。
- 用户偏好结构化输出（表格 + change list），对话中常推翻先前决定，需二次确认真实意图。
