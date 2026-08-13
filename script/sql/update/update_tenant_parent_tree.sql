-- ============================================================
-- 多级代理租户结构升级脚本
-- 功能：sys_tenant 增加 parent_id 字段，支持"租户树"（平台超管建顶层租户，
--       租户管理员可在其下创建子租户，上级可见并管理自己及所有下级租户的数据）
-- 适用版本：RuoYi-Vue-Plus 5.6.2（MySQL）
-- ============================================================

-- 1. sys_tenant 增加父租户字段（parent_id 存父租户主键 id，0 表示顶层租户）
ALTER TABLE sys_tenant
    ADD COLUMN parent_id bigint(20) NOT NULL DEFAULT 0 COMMENT '父租户id(0为顶层租户)' AFTER id;

-- 2. 存量租户数据全部视为顶层租户
UPDATE sys_tenant SET parent_id = 0 WHERE parent_id = 0 OR parent_id IS NULL;

-- ============================================================
-- 3. 菜单与权限（可选，仅当需要让"租户管理员"在界面上管理下级租户时执行）
--    新增"下级租户管理"菜单（挂在"租户管理"目录 id=6 下）及按钮权限
-- ============================================================

-- 3.1 新增菜单"下级租户管理"（菜单 id 使用一个较大的固定值，避免与现有冲突）
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1700, '下级租户管理', 6, 6, 'childTenant', 'system/childTenant/index', '', 1, 0, 'C', '0', '0', 'system:tenantChild:list', 'tree', 103, 1, sysdate(), null, null, '租户管理员管理下级租户菜单');

-- 3.2 新增按钮权限
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1701, '下级租户新增', 1700, 1, '#', '', '', 1, 0, 'F', '0', '0', 'system:tenantChild:add', '#', 103, 1, sysdate(), null, null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1702, '下级租户修改', 1700, 2, '#', '', '', 1, 0, 'F', '0', '0', 'system:tenantChild:edit', '#', 103, 1, sysdate(), null, null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1703, '下级租户删除', 1700, 3, '#', '', '', 1, 0, 'F', '0', '0', 'system:tenantChild:remove', '#', 103, 1, sysdate(), null, null, '');
INSERT INTO sys_menu (menu_id, menu_name, parent_id, order_num, path, component, query_param, is_frame, is_cache, menu_type, visible, status, perms, icon, create_dept, create_by, create_time, update_by, update_time, remark)
VALUES (1704, '下级租户状态修改', 1700, 4, '#', '', '', 1, 0, 'F', '0', '0', 'system:tenantChild:edit', '#', 103, 1, sysdate(), null, null, '');

-- 3.3 将新菜单加入"默认租户套餐"（套餐 id 请按实际修改，默认套餐通常为 1）
--     新套餐的 menu_ids 需以逗号拼接，这里采用 UPDATE 拼接方式（MySQL 语法）
UPDATE sys_tenant_package
SET menu_ids = CONCAT(menu_ids, ',1700,1701,1702,1703,1704')
WHERE package_id = 1 AND menu_ids NOT LIKE '%1700%';

-- 3.4 给所有已存在的"租户管理员"角色（role_key = 'admin'）补绑新菜单
--     使已有租户的管理员也能看到"下级租户管理"入口
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
         JOIN sys_menu m ON m.menu_id IN (1700, 1701, 1702, 1703, 1704)
WHERE r.role_key = 'admin'
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

-- 说明：
--   a) 菜单 id 1700-1704 为预留值，若与现有数据冲突请改成其他未使用的 id；
--   b) 若没有启用多级代理，仅执行第 1、2 步即可（parent_id 字段不影响原有功能）；
--   c) 前端页面 system/childTenant/index 需在 ruoyi-ui 前端仓库中开发，接口见后端文档。
