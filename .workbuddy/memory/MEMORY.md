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

## 外部约束
- `mockplus` 原型（rp.mockplus.cn）因密码门未能读取，产品说明均取自本地 13 个 HTML 原型。
- 用户偏好结构化输出（表格 + change list），对话中常推翻先前决定，需二次确认真实意图。
