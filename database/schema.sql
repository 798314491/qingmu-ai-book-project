-- Typora-like Markdown Notes System Database Schema
-- MySQL 8.0

-- Create database
CREATE DATABASE IF NOT EXISTS markdown_notes DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE markdown_notes;

-- User table
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

-- Folder table
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

-- Note table
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
    word_count INT DEFAULT 0 COMMENT '字数统计',
    view_count INT DEFAULT 0 COMMENT '查看次数',
    sort_order INT DEFAULT 0 COMMENT '排序',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (folder_id) REFERENCES folders(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_folder_id (folder_id),
    INDEX idx_created_at (created_at),
    INDEX idx_is_deleted (is_deleted),
    FULLTEXT idx_fulltext (title, content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- Note version history table
CREATE TABLE IF NOT EXISTS note_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    version_number INT NOT NULL COMMENT '版本号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    INDEX idx_note_id (note_id),
    INDEX idx_version (note_id, version_number),
    UNIQUE KEY uk_note_version (note_id, version_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记版本历史表';

-- AI conversation table
CREATE TABLE IF NOT EXISTS ai_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    session_id VARCHAR(100) NOT NULL COMMENT '会话ID',
    note_id BIGINT DEFAULT NULL COMMENT '关联的笔记ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_session_id (session_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话会话表';

-- AI message table
CREATE TABLE IF NOT EXISTS ai_messages (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    conversation_id BIGINT NOT NULL COMMENT '会话ID',
    role ENUM('user', 'assistant', 'system') NOT NULL COMMENT '角色',
    content TEXT NOT NULL COMMENT '消息内容',
    tokens_used INT DEFAULT 0 COMMENT '使用的token数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (conversation_id) REFERENCES ai_conversations(id) ON DELETE CASCADE,
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI消息表';

-- User settings table
CREATE TABLE IF NOT EXISTS user_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    theme VARCHAR(20) DEFAULT 'light' COMMENT '主题: light/dark',
    editor_theme VARCHAR(50) DEFAULT 'default' COMMENT '编辑器主题',
    font_size INT DEFAULT 14 COMMENT '字体大小',
    font_family VARCHAR(100) DEFAULT 'monospace' COMMENT '字体',
    auto_save BOOLEAN DEFAULT TRUE COMMENT '自动保存',
    auto_save_interval INT DEFAULT 30 COMMENT '自动保存间隔(秒)',
    show_line_numbers BOOLEAN DEFAULT TRUE COMMENT '显示行号',
    enable_spell_check BOOLEAN DEFAULT FALSE COMMENT '拼写检查',
    default_view_mode VARCHAR(20) DEFAULT 'edit' COMMENT '默认视图: edit/preview/split',
    ai_model VARCHAR(50) DEFAULT 'qwen-turbo' COMMENT 'AI模型选择',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';

-- Share links table
CREATE TABLE IF NOT EXISTS share_links (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    share_code VARCHAR(32) UNIQUE NOT NULL COMMENT '分享码',
    password VARCHAR(50) DEFAULT NULL COMMENT '访问密码',
    view_count INT DEFAULT 0 COMMENT '访问次数',
    expires_at DATETIME DEFAULT NULL COMMENT '过期时间',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    INDEX idx_share_code (share_code),
    INDEX idx_note_id (note_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='分享链接表';

-- Tags table
CREATE TABLE IF NOT EXISTS tags (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(50) NOT NULL COMMENT '标签名',
    color VARCHAR(7) DEFAULT '#4A90E2' COMMENT '颜色',
    use_count INT DEFAULT 0 COMMENT '使用次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_tag (user_id, name),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

-- Note tags relationship table
CREATE TABLE IF NOT EXISTS note_tags (
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    tag_id BIGINT NOT NULL COMMENT '标签ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (note_id, tag_id),
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记标签关联表';

-- Attachments table
CREATE TABLE IF NOT EXISTS attachments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    file_name VARCHAR(255) NOT NULL COMMENT '文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    file_type VARCHAR(50) COMMENT '文件类型',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    INDEX idx_note_id (note_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='附件表';

-- User login logs table
CREATE TABLE IF NOT EXISTS login_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent VARCHAR(500) COMMENT '用户代理',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- Insert default admin user (password: admin123)
INSERT INTO users (username, email, password, nickname, status) 
VALUES ('admin', 'admin@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Administrator', 1);

-- Insert default settings for admin
INSERT INTO user_settings (user_id) 
SELECT id FROM users WHERE username = 'admin';

-- Insert sample folders
INSERT INTO folders (user_id, name, parent_id) 
SELECT id, 'Work Notes', NULL FROM users WHERE username = 'admin';

INSERT INTO folders (user_id, name, parent_id) 
SELECT id, 'Personal', NULL FROM users WHERE username = 'admin';

-- Insert sample note
INSERT INTO notes (user_id, folder_id, title, content, summary, tags) 
SELECT 
    u.id,
    f.id,
    'Welcome to Markdown Notes',
    '# Welcome to Markdown Notes\n\n## Features\n\n- **Real-time Preview**: See your formatted text as you type\n- **AI Assistant**: Get help from AI while writing\n- **Version Control**: Never lose your work with automatic versioning\n- **Beautiful Themes**: Choose from multiple themes\n\n## Getting Started\n\n1. Create a new note\n2. Start writing in Markdown\n3. Use AI chat for assistance\n4. Organize with folders and tags\n\n## Markdown Syntax\n\n### Headers\n```markdown\n# H1\n## H2\n### H3\n```\n\n### Lists\n- Item 1\n- Item 2\n  - Nested item\n\n### Code\n```javascript\nconst greeting = "Hello, World!";\nconsole.log(greeting);\n```\n\n### Tables\n| Feature | Description |\n|---------|-------------|\n| Edit | Write in Markdown |\n| Preview | See formatted output |\n| Export | Save as PDF/HTML |\n\nEnjoy writing!',
    'A welcome note introducing the features of the Markdown Notes system',
    '["welcome", "tutorial", "markdown"]'
FROM users u
JOIN folders f ON f.user_id = u.id AND f.name = 'Work Notes'
WHERE u.username = 'admin';

-- Create indexes for performance
CREATE INDEX idx_notes_user_folder ON notes(user_id, folder_id);
CREATE INDEX idx_notes_starred ON notes(user_id, is_starred);
CREATE INDEX idx_ai_messages_conversation ON ai_messages(conversation_id, created_at);