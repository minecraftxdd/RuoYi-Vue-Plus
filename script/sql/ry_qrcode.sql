-- ----------------------------
-- 二维码通行模块 (通用能力)
-- 设计文档: 知识库/人脸识别系统/01-通用能力/二维码通行设计.md
-- 说明: 表结构遵循设计文档, 采用秒级时间戳; 不含租户字段, 需在 tenant.excludes 中排除
-- ----------------------------

-- ----------------------------
-- 二维码表
-- ----------------------------
drop table if exists face_qr_code;
create table face_qr_code (
    id            bigint          not null                   comment '二维码ID',
    qr_type       char(2)         not null default 'A#'       comment '二维码类型: A#业主/用户 B#访客',
    person_id     bigint          default null               comment '绑定人员ID (A#必填)',
    order_id      bigint          default null               comment '访客单ID (可选)',
    project_id    bigint          not null                   comment '项目ID (需与设备识别设置的二维码项目ID一致)',
    sn            varchar(32)     default null               comment '指定设备序列号, 字符串"null"表示通配所有设备',
    user_id       varchar(64)     default null               comment '用户id (人脸+二维码模式 A#必填)',
    valid_start   bigint          not null                   comment '有效开始时间 (秒级时间戳)',
    valid_end     bigint          not null                   comment '有效结束时间 (秒级时间戳)',
    total_times   int             not null default 1         comment '有效次数',
    used_times    int             not null default 0         comment '已使用次数',
    content       varchar(500)    default null               comment '生成的二维码内容',
    status        char(1)         not null default '0'       comment '状态: 0有效 1作废 2用完 3过期',
    remark        varchar(500)    default null               comment '备注',
    create_dept   bigint          default null               comment '创建部门',
    create_by     bigint          default null               comment '创建者',
    create_time   datetime        default null               comment '创建时间',
    update_by     bigint          default null               comment '更新者',
    update_time   datetime        default null               comment '更新时间',
    primary key (id),
    key idx_qr_project (project_id),
    key idx_qr_valid_end (valid_end)
) engine = innodb comment = '二维码通行表';

-- ----------------------------
-- 二维码通行/核销记录表 (在线比对+离线识别记录)
-- ----------------------------
drop table if exists face_qr_record;
create table face_qr_record (
    id                  bigint         not null              comment '记录ID',
    qr_id               bigint         not null              comment '二维码ID',
    qr_type             char(2)        default null          comment '二维码类型: A#业主/用户 B#访客',
    person_id           bigint         default null          comment '绑定人员ID',
    project_id          bigint         default null          comment '项目ID',
    sn                  varchar(32)    default null          comment '设备号',
    pass_status         tinyint        not null default 0    comment '通行状态: 0放行 1拦截',
    result_code         int            default null          comment '在线比对结果码: 0成功 1无效 2过期 3超次数 4未绑定设备 5项目不一致 6其他错误 7需1:1人脸比对',
    fail_reason         varchar(50)    default null          comment '拦截原因',
    body_temperature    decimal(4,1)   default null          comment '体温',
    qr_code             varchar(500)   default null          comment '二维码内容',
    photo               varchar(500)   default null          comment '抓拍照片 (离线识别)',
    panoramic_picture   varchar(500)   default null          comment '全景抓拍 (离线识别)',
    health_code_color   varchar(20)    default null          comment '健康颜色: 1000绿 2000黄 3000红 -1未知',
    health_code_picture varchar(500)   default null          comment '健康码抓拍图 (离线识别)',
    recog_time          datetime       default null          comment '识别时间',
    source              tinyint        not null default 1    comment '记录来源: 1在线比对 2离线识别',
    create_dept         bigint         default null          comment '创建部门',
    create_by           bigint         default null          comment '创建者',
    create_time         datetime       default null          comment '创建时间',
    update_by           bigint         default null          comment '更新者',
    update_time         datetime       default null          comment '更新时间',
    primary key (id),
    key idx_qr_record_qr (qr_id),
    key idx_qr_record_sn_time (sn, recog_time)
) engine = innodb comment = '二维码通行记录表';

-- ----------------------------
-- 菜单初始化数据 (menu_id 3000 段, 避免与系统菜单冲突)
-- ----------------------------
-- 一级目录
insert into sys_menu values ('3000', '二维码通行', '0', '6', 'qrcode', null, '', 1, 0, 'M', '0', '0', '', 'qrcode', 103, 1, sysdate(), null, null, '二维码通行目录');
-- 二级菜单
insert into sys_menu values ('3001', '二维码管理', '3000', '1', 'index', 'qrcode/index', '', 1, 0, 'C', '0', '0', 'qrcode:list', 'qrcode', 103, 1, sysdate(), null, null, '二维码管理菜单');
insert into sys_menu values ('3002', '通行记录', '3000', '2', 'record', 'qrcode/record', '', 1, 0, 'C', '0', '0', 'qrcode:record:list', 'log', 103, 1, sysdate(), null, null, '通行记录菜单');
-- 按钮
insert into sys_menu values ('3003', '二维码查询', '3001', '1', '', '', '', 1, 0, 'F', '0', '0', 'qrcode:query', '#', 103, 1, sysdate(), null, null, '二维码查询按钮');
insert into sys_menu values ('3004', '二维码新增', '3001', '2', '', '', '', 1, 0, 'F', '0', '0', 'qrcode:add', '#', 103, 1, sysdate(), null, null, '二维码新增按钮');
insert into sys_menu values ('3005', '二维码修改', '3001', '3', '', '', '', 1, 0, 'F', '0', '0', 'qrcode:edit', '#', 103, 1, sysdate(), null, null, '二维码修改按钮');
insert into sys_menu values ('3006', '二维码删除', '3001', '4', '', '', '', 1, 0, 'F', '0', '0', 'qrcode:remove', '#', 103, 1, sysdate(), null, null, '二维码删除按钮');
insert into sys_menu values ('3007', '二维码作废', '3001', '5', '', '', '', 1, 0, 'F', '0', '0', 'qrcode:revoke', '#', 103, 1, sysdate(), null, null, '二维码作废按钮');
insert into sys_menu values ('3008', '通行记录查询', '3002', '1', '', '', '', 1, 0, 'F', '0', '0', 'qrcode:record:query', '#', 103, 1, sysdate(), null, null, '通行记录查询按钮');

-- ----------------------------
-- 角色-菜单授权 (将二维码菜单分配给所有非超级管理员角色)
-- 注意: 修改授权后, 用户需要重新登录才能刷新权限缓存
-- ----------------------------
insert into sys_role_menu (role_id, menu_id)
select r.role_id, m.menu_id
from sys_role r
         cross join sys_menu m
where m.menu_id between 3000 and 3008
  and r.role_id <> 1
  and not exists (select 1 from sys_role_menu rm where rm.role_id = r.role_id and rm.menu_id = m.menu_id);

-- ----------------------------
-- 超级管理员权限绑定 (role_id = 1)
-- 说明: 确保超级管理员拥有二维码通行的所有权限
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 3000 AND 3008
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = sys_menu.menu_id
  );

-- ----------------------------
-- 默认租户套餐权限绑定 (package_id = 1)
-- 说明: 将二维码菜单加入默认租户套餐
-- ----------------------------
UPDATE sys_tenant_package
SET menu_ids = CONCAT(menu_ids, ',3000,3001,3002,3003,3004,3005,3006,3007,3008')
WHERE package_id = 1
  AND menu_ids NOT LIKE '%3000%';