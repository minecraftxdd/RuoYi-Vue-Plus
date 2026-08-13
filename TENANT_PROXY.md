# 多级代理租户（租户树）使用说明

> 基于 RuoYi-Vue-Plus 的多租户扩展：支持「平台超管建顶层公司 → 公司建下级公司」的多级代理结构，
> 上级公司可查看并管理自己及全部下级公司的数据，下级公司只能看到自己。

## 一、功能模型

```
平台超管（租户 000000）
└── A 公司（顶层租户）
    ├── 自己的数据
    └── B 公司（A 的下级租户）
        ├── 自己的数据
        └── C 公司（B 的下级租户，可继续往下）
```

- **平台超管**：创建顶层租户（A），可管理所有租户。
- **租户管理员（如 A）**：可创建下级租户（B）并管理其账号；A 登录后所有业务数据的可见范围为
  `tenant_id IN (A, B, ...)`，即可查看、修改、删除自己和全部下级公司的数据。
- **下级租户（如 B）**：只能看到 `tenant_id = B` 的数据。

数据隔离仍采用"共享库 + 行级 `tenant_id` 过滤"，只是过滤条件从单值扩展为范围（`IN` 列表）。

## 二、数据库迁移

执行升级脚本（MySQL）：

```bash
mysql -u<user> -p<pass> <db> < script/sql/update/update_tenant_parent_tree.sql
```

脚本内容：

1. `sys_tenant` 增加 `parent_id` 字段（0 = 顶层租户），存量数据置 0；
2. 新增「下级租户管理」菜单（id 1700）及按钮权限（1701~1704，权限码 `system:tenantChild:*`）；
3. 将新菜单加入默认租户套餐（`sys_tenant_package.package_id = 1`）；
4. 给所有现有租户管理员角色（`role_key = 'admin'`）补绑新菜单。

> 若你的套餐 id 不是 1、或菜单 id 1700~1704 已占用，请先修改脚本再执行。
> 只做多级代理数据结构（不开放租户管理员界面）时，可仅执行脚本的第 1、2 步。

## 三、后端已提供的接口

`SysTenantController`（权限码均为 OR：`system:tenant:*` 或 `system:tenantChild:*`）：

| 方法 | 路径 | 说明 | 权限 |
|---|---|---|---|
| GET | `/system/tenant/list` | 租户列表（超管=全部；租户管理员=自己+下级） | `system:tenant:list` / `system:tenantChild:list` |
| GET | `/system/tenant/scope` | 当前账号可见租户列表（前端下拉/树用） | 同上 |
| POST | `/system/tenant` | 新增租户（超管=顶层；租户管理员=自己的下级） | `system:tenant:add` / `system:tenantChild:add` |
| PUT | `/system/tenant` | 修改租户（非超管仅限自己+下级） | `system:tenant:edit` / `system:tenantChild:edit` |
| PUT | `/system/tenant/changeStatus` | 停用/启用（非超管仅限自己+下级） | 同上 |
| DELETE | `/system/tenant/{ids}` | 删除租户（非超管仅限自己+下级） | `system:tenant:remove` / `system:tenantChild:remove` |

`SysUserController`（原有接口不变，新增能力）：

- **列表**：范围过滤已生效，A 的用户列表会包含 A 及所有下级公司的用户；
- **新增**：`SysUserBo` 新增 `tenantId` 字段，填写下级公司租户 id 即为该公司创建账号（不填默认归属当前租户）；
- **编辑**：修改 `tenantId` 可将用户转移归属到下级公司（目标租户必须在可见范围内）。

## 四、前端改造建议（ruoyi-ui 仓库）

1. 新建页面 `src/views/system/childTenant/index.vue`（对应菜单 1700 的组件路径 `system/childTenant/index`），
   复用现有 `src/views/system/tenant/index.vue` 的表格与表单逻辑：
   - 列表接口 `GET /system/tenant/list`（自动按角色过滤范围）；
   - 可见公司下拉用 `GET /system/tenant/scope`；
   - 新增/修改时显示"上级公司"为当前账号自身（后端强制，前端只读展示）。
2. 用户管理页（`src/views/system/user/index.vue`）：
   - 列表新增"所属公司"列：`SysUserVo.tenantId` 结合 `/system/tenant/scope` 映射公司名；
   - 新增/编辑弹窗增加"归属公司"下拉（数据源 `/system/tenant/scope`），回填 `tenantId`。
3. 按钮权限码：使用 `system:tenantChild:*`（菜单 1700 自动下发）。

## 五、行为细节与限制

- **范围缓存**：登录时按租户树快照写入 Redis（key `global:tenantScope:{tenantId}`，TTL 1 天）；
  创建/修改/删除租户后服务端自动全量刷新，无需手动处理。
- **插入归属**：所有业务表新增数据默认归属当前登录租户；如需"代下级公司新增"，在实体/BO 中显式
  设置 `tenantId`（须在可见范围内，`SysUser` 已支持，业务表可参照 `SysUserServiceImpl` 的校验方式）。
- **删除租户**：删除某租户后，其下级租户会脱离上级可见范围（数据仍在，仍可正常登录）。
- **超管切换**：`GET /system/tenant/dynamic/{tenantId}` 仍为超管专属，用于跨租户运维。
- **停用下级租户**：停用后该租户无法登录，但上级仍可看到其历史数据（如需数据级隐藏请自行扩展）。
