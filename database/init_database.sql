-- 初始化 Markdown Notes 数据库
-- 请先确保MySQL服务正在运行，并且您有创建数据库的权限

-- 创建数据库
CREATE DATABASE IF NOT EXISTS markdown_notes DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE markdown_notes;

-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
    email VARCHAR(100) UNIQUE NOT NULL COMMENT '邮箱',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    nickname VARCHAR(50) COMMENT '昵称',
    avatar VARCHAR(500) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用, 1-正常',
    last_login_time DATETIME COMMENT '最后登录时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 创建文件夹表
CREATE TABLE IF NOT EXISTS folders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父文件夹ID',
    icon VARCHAR(50) COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES folders(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件夹表';

-- 创建笔记表
CREATE TABLE IF NOT EXISTS notes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    folder_id BIGINT DEFAULT NULL COMMENT '文件夹ID',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT 'Markdown内容',
    summary VARCHAR(500) COMMENT '摘要',
    tags VARCHAR(500) COMMENT '标签(JSON数组)',
    is_starred BOOLEAN DEFAULT FALSE COMMENT '是否收藏',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    is_public BOOLEAN DEFAULT FALSE COMMENT '是否公开',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    share_code VARCHAR(32) COMMENT '分享码',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_folder_id (folder_id),
    INDEX idx_title (title),
    INDEX idx_tags (tags),
    INDEX idx_share_code (share_code),
    INDEX idx_updated_at (updated_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- 插入测试用户
INSERT INTO users (username, email, password, nickname, status) 
VALUES ('admin', 'admin@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Administrator', 1)
ON DUPLICATE KEY UPDATE username = username;

-- 插入测试笔记
INSERT INTO notes (user_id, title, content, summary, is_starred, is_public) 
SELECT 
    u.id,
    '欢迎使用 Markdown Notes',
    '# 欢迎使用 Markdown Notes\n\n这是您的第一个笔记！\n\n## 功能特点\n\n- **实时预览**: 类似Typora的编辑体验\n- **AI助手**: 集成通义千问AI\n- **云端同步**: 随时随地访问您的笔记\n\n## 快速开始\n\n1. 点击左上角"新建笔记"创建笔记\n2. 使用Markdown语法编写内容\n3. 点击右侧AI助手获得智能帮助\n\n**开始您的创作之旅吧！** 🚀',
    '欢迎使用 Markdown Notes！这是您的第一个笔记，包含了基本功能介绍和使用指南。',
    false,
    false
FROM users u 
WHERE u.username = 'admin'
AND NOT EXISTS (
    SELECT 1 FROM notes n WHERE n.user_id = u.id AND n.title = '欢迎使用 Markdown Notes'
);

-- 验证数据
SELECT 'Database initialized successfully!' as message;
SELECT COUNT(*) as user_count FROM users;
SELECT COUNT(*) as note_count FROM notes;
