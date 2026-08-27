-- ----------------------------
-- 人脸照片管理模块
-- 设计文档: 知识库/人脸识别系统/01-通用能力/照片处理系统设计.md
-- ----------------------------

-- ----------------------------
-- 人脸照片表
-- ----------------------------
DROP TABLE IF EXISTS face_photo;
CREATE TABLE face_photo (
    id              BIGINT        NOT NULL           COMMENT '主键ID',
    file_path       VARCHAR(500)  NOT NULL           COMMENT '文件存储相对路径',
    original_name   VARCHAR(255)  DEFAULT ''         COMMENT '原始文件名',
    file_suffix     VARCHAR(10)   DEFAULT ''         COMMENT '文件后缀名',
    file_size       BIGINT        DEFAULT 0          COMMENT '文件大小(字节)',
    width           INT           DEFAULT NULL       COMMENT '图片宽度',
    height          INT           DEFAULT NULL       COMMENT '图片高度',
    face_encoding   MEDIUMTEXT    DEFAULT NULL       COMMENT '人脸特征编码(Base64,UrlEncode后)',
    encoding_status CHAR(1)       DEFAULT '0'        COMMENT '编码状态: 0待编码 1编码中 2成功 3失败',
    encoding_error  VARCHAR(1000) DEFAULT NULL       COMMENT '编码失败原因',
    face_count      INT           DEFAULT 0          COMMENT '检测到的人脸数量',
    remark          VARCHAR(500)  DEFAULT NULL       COMMENT '备注',
    create_dept     BIGINT        DEFAULT NULL       COMMENT '创建部门',
    create_by       BIGINT        DEFAULT NULL       COMMENT '创建人',
    create_time     DATETIME      DEFAULT NULL       COMMENT '创建时间',
    update_by       BIGINT        DEFAULT NULL       COMMENT '更新人',
    update_time     DATETIME      DEFAULT NULL       COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_file_path (file_path),
    KEY idx_encoding_status (encoding_status)
) ENGINE=InnoDB COMMENT='人脸照片管理表';

-- ----------------------------
-- 人脸照片编码日志表
-- ----------------------------
DROP TABLE IF EXISTS face_photo_encode_log;
CREATE TABLE face_photo_encode_log (
    id              BIGINT         NOT NULL          COMMENT '主键ID',
    photo_id        BIGINT         NOT NULL          COMMENT '照片ID',
    encode_type     VARCHAR(20)    NOT NULL          COMMENT '编码类型: image-base64',
    encoding_data   MEDIUMTEXT     DEFAULT NULL      COMMENT '编码后数据(Base64)',
    face_index      INT            DEFAULT NULL      COMMENT '人脸索引(多脸时)',
    status          CHAR(1)        DEFAULT '0'       COMMENT '状态: 0成功 1失败',
    error_msg       VARCHAR(1000)  DEFAULT NULL      COMMENT '错误信息',
    operator_id     BIGINT         DEFAULT NULL      COMMENT '操作人ID',
    create_dept     BIGINT         DEFAULT NULL      COMMENT '创建部门',
    create_by       BIGINT         DEFAULT NULL      COMMENT '创建人',
    create_time     DATETIME       DEFAULT NULL      COMMENT '创建时间',
    update_by       BIGINT         DEFAULT NULL      COMMENT '更新人',
    update_time     DATETIME       DEFAULT NULL      COMMENT '更新时间',
    PRIMARY KEY (id),
    KEY idx_photo_id (photo_id)
) ENGINE=InnoDB COMMENT='照片人脸编码日志表';

-- ----------------------------
-- 菜单初始化数据 (menu_id 3100 起)
-- ----------------------------
-- 先清理旧数据，保证可重复执行
DELETE FROM sys_role_menu WHERE menu_id BETWEEN 3100 AND 3108;
DELETE FROM sys_menu WHERE menu_id BETWEEN 3100 AND 3108;

-- 一级目录
INSERT INTO sys_menu VALUES ('3100', '照片管理', '0', '7', 'photo', null, '', 1, 0, 'M', '0', '0', '', 'image', 103, 1, sysdate(), null, null, '照片编解码管理目录');
-- 二级菜单
INSERT INTO sys_menu VALUES ('3101', '照片列表', '3100', '1', 'index', 'photo/index', '', 1, 0, 'C', '0', '0', 'photo:list', 'photo', 103, 1, sysdate(), null, null, '照片列表菜单');
INSERT INTO sys_menu VALUES ('3102', '编码日志', '3100', '2', 'log', 'photo/log', '', 1, 0, 'C', '0', '0', 'photo:log:list', 'log', 103, 1, sysdate(), null, null, '编码日志菜单');
-- 按钮
INSERT INTO sys_menu VALUES ('3103', '照片查询', '3101', '1', '', '', '', 1, 0, 'F', '0', '0', 'photo:query', '#', 103, 1, sysdate(), null, null, '照片查询按钮');
INSERT INTO sys_menu VALUES ('3104', '照片上传', '3101', '2', '', '', '', 1, 0, 'F', '0', '0', 'photo:upload', '#', 103, 1, sysdate(), null, null, '照片上传按钮');
INSERT INTO sys_menu VALUES ('3105', '照片编码', '3101', '3', '', '', '', 1, 0, 'F', '0', '0', 'photo:encode', '#', 103, 1, sysdate(), null, null, '照片编码按钮');
INSERT INTO sys_menu VALUES ('3106', '照片删除', '3101', '4', '', '', '', 1, 0, 'F', '0', '0', 'photo:remove', '#', 103, 1, sysdate(), null, null, '照片删除按钮');
INSERT INTO sys_menu VALUES ('3107', '日志查询', '3102', '1', '', '', '', 1, 0, 'F', '0', '0', 'photo:log:query', '#', 103, 1, sysdate(), null, null, '日志查询按钮');
INSERT INTO sys_menu VALUES ('3108', '日志删除', '3102', '2', '', '', '', 1, 0, 'F', '0', '0', 'photo:log:remove', '#', 103, 1, sysdate(), null, null, '日志删除按钮');

-- ----------------------------
-- 角色-菜单授权
-- ----------------------------
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT r.role_id, m.menu_id
FROM sys_role r
CROSS JOIN sys_menu m
WHERE m.menu_id BETWEEN 3100 AND 3108
  AND r.role_id <> 1
  AND NOT EXISTS (SELECT 1 FROM sys_role_menu rm WHERE rm.role_id = r.role_id AND rm.menu_id = m.menu_id);

-- 超级管理员权限绑定
INSERT INTO sys_role_menu (role_id, menu_id)
SELECT 1, menu_id
FROM sys_menu
WHERE menu_id BETWEEN 3100 AND 3108
  AND NOT EXISTS (
    SELECT 1 FROM sys_role_menu rm
    WHERE rm.role_id = 1 AND rm.menu_id = sys_menu.menu_id
  );

-- 默认租户套餐权限绑定（对所有套餐幂等追加，与上方全角色授权语义一致）
UPDATE sys_tenant_package
SET menu_ids = CONCAT(menu_ids, ',3100,3101,3102,3103,3104,3105,3106,3107,3108')
WHERE menu_ids IS NOT NULL
  AND menu_ids NOT LIKE '%3100%';
