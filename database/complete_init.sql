-- =====================================================
-- Markdown Notes 系统完整初始化脚本
-- 包含数据库创建、表结构、索引、初始数据
-- 使用方法: mysql -u root -p < complete_init.sql
-- =====================================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS markdown_notes DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE markdown_notes;

-- =====================================================
-- 表结构创建
-- =====================================================

-- 用户表
DROP TABLE IF EXISTS users;
CREATE TABLE users (
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
    INDEX idx_email (email),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 文件夹表
DROP TABLE IF EXISTS folders;
CREATE TABLE folders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    name VARCHAR(100) NOT NULL COMMENT '文件夹名称',
    parent_id BIGINT DEFAULT NULL COMMENT '父文件夹ID',
    icon VARCHAR(50) DEFAULT 'folder' COMMENT '图标',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (parent_id) REFERENCES folders(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_parent_id (parent_id),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件夹表';

-- 笔记表 (与Note实体类完全匹配)
DROP TABLE IF EXISTS notes;
CREATE TABLE notes (
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
    INDEX idx_title (title),
    INDEX idx_tags (tags),
    INDEX idx_updated_at (updated_at),
    INDEX idx_is_deleted (is_deleted),
    INDEX idx_is_public (is_public),
    INDEX idx_is_starred (is_starred),
    INDEX idx_word_count (word_count),
    INDEX idx_sort_order (sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记表';

-- 笔记版本历史表
DROP TABLE IF EXISTS note_versions;
CREATE TABLE note_versions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    note_id BIGINT NOT NULL COMMENT '笔记ID',
    version_number INT NOT NULL COMMENT '版本号',
    title VARCHAR(200) NOT NULL COMMENT '标题',
    content LONGTEXT COMMENT '内容',
    change_description VARCHAR(500) COMMENT '变更描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE CASCADE,
    INDEX idx_note_id (note_id),
    INDEX idx_version_number (version_number),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='笔记版本历史表';

-- 用户设置表
DROP TABLE IF EXISTS user_settings;
CREATE TABLE user_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    theme VARCHAR(20) DEFAULT 'light' COMMENT '主题: light, dark, auto',
    font_size INT DEFAULT 14 COMMENT '字体大小',
    font_family VARCHAR(50) DEFAULT 'system' COMMENT '字体',
    auto_save BOOLEAN DEFAULT TRUE COMMENT '自动保存',
    show_line_numbers BOOLEAN DEFAULT FALSE COMMENT '显示行号',
    word_wrap BOOLEAN DEFAULT TRUE COMMENT '自动换行',
    editor_mode VARCHAR(20) DEFAULT 'wysiwyg' COMMENT '编辑模式: wysiwyg, source, preview',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户设置表';

-- AI对话历史表
DROP TABLE IF EXISTS ai_conversations;
CREATE TABLE ai_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    conversation_id VARCHAR(64) NOT NULL COMMENT '对话ID',
    user_message TEXT NOT NULL COMMENT '用户消息',
    ai_response TEXT COMMENT 'AI回复',
    message_type VARCHAR(20) DEFAULT 'chat' COMMENT '消息类型: chat, enhance, summarize, translate',
    context_text TEXT COMMENT '上下文文本',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='AI对话历史表';

-- 文件上传表
DROP TABLE IF EXISTS file_uploads;
CREATE TABLE file_uploads (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    note_id BIGINT COMMENT '关联笔记ID',
    original_name VARCHAR(255) NOT NULL COMMENT '原始文件名',
    stored_name VARCHAR(255) NOT NULL COMMENT '存储文件名',
    file_path VARCHAR(500) NOT NULL COMMENT '文件路径',
    file_size BIGINT NOT NULL COMMENT '文件大小(字节)',
    mime_type VARCHAR(100) COMMENT '文件类型',
    file_hash VARCHAR(64) COMMENT '文件哈希值',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (note_id) REFERENCES notes(id) ON DELETE SET NULL,
    INDEX idx_user_id (user_id),
    INDEX idx_note_id (note_id),
    INDEX idx_file_hash (file_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件上传表';

-- 登录日志表
DROP TABLE IF EXISTS login_logs;
CREATE TABLE login_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    ip_address VARCHAR(45) COMMENT 'IP地址',
    user_agent TEXT COMMENT '用户代理',
    login_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
    login_status TINYINT DEFAULT 1 COMMENT '登录状态: 0-失败, 1-成功',
    failure_reason VARCHAR(200) COMMENT '失败原因',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_login_time (login_time),
    INDEX idx_ip_address (ip_address)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- =====================================================
-- 初始数据插入
-- =====================================================

-- 插入管理员用户 (密码: admin123)
INSERT INTO users (username, email, password, nickname, status) VALUES 
('admin', 'admin@example.com', '$2a$10$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeG6Lruj3vjPGga31lW', 'Administrator', 1);

-- 插入测试用户 (密码: test123)
INSERT INTO users (username, email, password, nickname, status) VALUES 
('test', 'test@example.com', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iYqiSekc6VvSqrTlWOhyuUl1CaP6', 'Test User', 1);

-- 为管理员用户插入默认设置
INSERT INTO user_settings (user_id, theme, font_size, auto_save) 
SELECT id, 'light', 14, TRUE FROM users WHERE username = 'admin';

-- 为测试用户插入默认设置
INSERT INTO user_settings (user_id, theme, font_size, auto_save) 
SELECT id, 'light', 14, TRUE FROM users WHERE username = 'test';

-- 为管理员创建示例文件夹
INSERT INTO folders (user_id, name, parent_id, sort_order) 
SELECT id, '工作笔记', NULL, 1 FROM users WHERE username = 'admin';

INSERT INTO folders (user_id, name, parent_id, sort_order) 
SELECT id, '学习笔记', NULL, 2 FROM users WHERE username = 'admin';

INSERT INTO folders (user_id, name, parent_id, sort_order) 
SELECT id, '个人日记', NULL, 3 FROM users WHERE username = 'admin';

-- 为管理员插入欢迎笔记
INSERT INTO notes (user_id, folder_id, title, content, summary, word_count, is_starred, is_public, sort_order) 
SELECT 
    u.id,
    NULL,
    '🎉 欢迎使用 Markdown Notes',
    '# 🎉 欢迎使用 Markdown Notes

欢迎来到您的专属Markdown笔记系统！这里是您创作和思考的数字空间。

## ✨ 主要功能

### 📝 强大的编辑器
- **实时预览**: 类似Typora的所见即所得体验
- **语法高亮**: 支持多种编程语言
- **自动保存**: 再也不用担心丢失内容
- **快捷键**: 提高您的编辑效率

### 🤖 AI智能助手
- **文字润色**: 让您的表达更加优雅
- **内容总结**: 快速提取文章要点
- **智能翻译**: 多语言无障碍交流
- **代码解释**: 理解复杂的代码逻辑

### 📁 组织管理
- **文件夹分类**: 井然有序的笔记管理
- **标签系统**: 灵活的内容标记
- **搜索功能**: 快速找到所需内容
- **收藏功能**: 重要内容一键收藏

### 🔐 安全可靠
- **用户认证**: JWT安全认证机制
- **数据加密**: 保护您的隐私安全
- **云端同步**: 多设备无缝访问

## 🚀 快速开始

1. **创建第一个笔记**: 点击左上角"新建笔记"按钮
2. **使用AI助手**: 点击右上角AI图标，获得智能写作帮助
3. **组织笔记**: 创建文件夹，为笔记分类
4. **搜索内容**: 使用顶部搜索框快速查找

## 💡 小贴士

- 使用 `Ctrl+S` 快速保存笔记
- 使用 `Ctrl+P` 切换预览模式
- 选中文本后可以快速询问AI助手
- 支持拖拽上传图片和文件

**开始您的创作之旅吧！** 🌟

---

> 如果您有任何问题或建议，欢迎通过设置页面联系我们。',
    '欢迎使用 Markdown Notes！这是一个功能强大的在线笔记系统，支持实时预览、AI助手、文件夹管理等功能。',
    850,
    true,
    false,
    1
FROM users u WHERE u.username = 'admin';

-- 为管理员插入示例工作笔记
INSERT INTO notes (user_id, folder_id, title, content, summary, word_count, is_starred, is_public, sort_order) 
SELECT 
    u.id,
    f.id,
    '📋 项目规划模板',
    '# 📋 项目规划模板

## 项目概述
- **项目名称**: 
- **项目目标**: 
- **开始时间**: 
- **预计完成**: 

## 📊 任务分解

### 阶段一：需求分析
- [ ] 收集用户需求
- [ ] 分析技术可行性
- [ ] 制定项目计划

### 阶段二：设计开发
- [ ] UI/UX设计
- [ ] 系统架构设计
- [ ] 核心功能开发

### 阶段三：测试部署
- [ ] 功能测试
- [ ] 性能优化
- [ ] 生产环境部署

## 🎯 关键里程碑

| 时间 | 里程碑 | 负责人 | 状态 |
|------|--------|--------|------|
| Week 1 | 需求确认 | | ⏳ |
| Week 2 | 设计完成 | | ⏳ |
| Week 4 | 开发完成 | | ⏳ |
| Week 5 | 测试完成 | | ⏳ |

## 📝 会议记录

### 2024-XX-XX 启动会议
- 参与人员：
- 主要决议：
- 下一步行动：

## 🔗 相关资源
- [项目文档](链接)
- [设计稿](链接)
- [开发环境](链接)',
    '项目规划模板，包含任务分解、里程碑跟踪、会议记录等完整的项目管理框架。',
    680,
    false,
    false,
    2
FROM users u 
JOIN folders f ON f.user_id = u.id 
WHERE u.username = 'admin' AND f.name = '工作笔记';

-- 为管理员插入学习笔记示例
INSERT INTO notes (user_id, folder_id, title, content, summary, word_count, is_starred, is_public, sort_order) 
SELECT 
    u.id,
    f.id,
    '🧠 学习方法论',
    '# 🧠 高效学习方法论

## 🎯 费曼学习法

> "如果你不能简单地解释它，说明你理解得不够深刻。" —— 爱因斯坦

### 四个步骤：
1. **选择概念**: 确定要学习的主题
2. **教授他人**: 用简单的语言解释给别人听
3. **发现缺陷**: 找出理解不深的地方
4. **重新学习**: 回到原材料，深化理解

## 🔄 间隔重复法

### 复习间隔：
- 第1次：学习后1天
- 第2次：学习后3天  
- 第3次：学习后7天
- 第4次：学习后15天
- 第5次：学习后30天

## 🧩 主动学习策略

### 阅读技巧
- **预览**: 先看目录和摘要
- **提问**: 带着问题去阅读
- **总结**: 用自己的话概括要点
- **联系**: 与已知知识建立联系

### 笔记方法
- **康奈尔笔记法**: 分区记录，便于复习
- **思维导图**: 可视化知识结构
- **代码注释**: 编程学习的最佳实践

## 💡 实践建议

1. **设定明确目标**: SMART原则
2. **创造学习环境**: 减少干扰因素
3. **保持一致性**: 每天固定时间学习
4. **及时反馈**: 通过测试检验学习效果
5. **教授他人**: 分享是最好的学习

## 📚 推荐资源

- 《如何阅读一本书》
- 《学习之道》
- 《刻意练习》

---

*记住：学习是一个持续的过程，保持好奇心和耐心！* 🌱',
    '高效学习方法论，包含费曼学习法、间隔重复法、主动学习策略等科学的学习方法。',
    920,
    true,
    false,
    3
FROM users u 
JOIN folders f ON f.user_id = u.id 
WHERE u.username = 'admin' AND f.name = '学习笔记';

-- 为管理员插入技术笔记示例
INSERT INTO notes (user_id, folder_id, title, content, summary, word_count, is_starred, is_public, sort_order) 
SELECT 
    u.id,
    f.id,
    '💻 Spring Boot 开发笔记',
    '# 💻 Spring Boot 开发笔记

## 🏗️ 项目结构

```
src/main/java/
├── controller/     # 控制器层
├── service/        # 业务逻辑层  
├── mapper/         # 数据访问层
├── entity/         # 实体类
├── dto/           # 数据传输对象
├── config/        # 配置类
└── security/      # 安全相关
```

## 🔧 核心注解

### 控制器注解
```java
@RestController    // REST控制器
@RequestMapping    // 请求映射
@GetMapping       // GET请求
@PostMapping      // POST请求
@PathVariable     // 路径变量
@RequestParam     // 请求参数
@RequestBody      // 请求体
```

### 依赖注入
```java
@Autowired        // 自动装配
@Service          // 服务层
@Repository       // 数据访问层
@Component        // 通用组件
@Configuration    // 配置类
```

## 🛡️ Security 配置

```java
@EnableWebSecurity
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated())
            .build();
    }
}
```

## 📊 数据库操作

### MyBatis-Plus 示例
```java
@Service
public class UserService extends ServiceImpl<UserMapper, User> {
    
    public User findByUsername(String username) {
        return this.getOne(new QueryWrapper<User>()
            .eq("username", username));
    }
}
```

## 🔐 JWT 认证

### 生成Token
```java
public String generateToken(UserDetails userDetails) {
    return Jwts.builder()
        .setSubject(userDetails.getUsername())
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + expiration))
        .signWith(key)
        .compact();
}
```

## 📝 最佳实践

1. **异常处理**: 使用@ControllerAdvice全局异常处理
2. **参数验证**: 使用@Valid注解验证请求参数
3. **日志记录**: 使用SLF4J记录关键操作
4. **事务管理**: 使用@Transactional确保数据一致性
5. **API文档**: 使用Swagger生成接口文档

## 🚀 性能优化

- **连接池**: 配置HikariCP连接池
- **缓存**: 使用Redis缓存热点数据
- **分页**: 避免一次性加载大量数据
- **索引**: 为查询字段添加数据库索引

---

*持续学习，不断进步！* 💪',
    'Spring Boot开发笔记，包含项目结构、核心注解、Security配置、数据库操作等开发要点。',
    1450,
    false,
    true,
    4
FROM users u 
JOIN folders f ON f.user_id = u.id 
WHERE u.username = 'admin' AND f.name = '工作笔记';

-- 为测试用户插入示例笔记
INSERT INTO notes (user_id, title, content, summary, word_count, is_starred, is_public, sort_order) 
SELECT 
    id,
    '我的第一个笔记',
    '# 我的第一个笔记\n\n这是我在 Markdown Notes 中创建的第一个笔记！\n\n## 功能测试\n\n- [x] 创建笔记\n- [x] 编辑内容\n- [ ] 使用AI助手\n- [ ] 分享笔记\n\n**感觉很不错！** 😊',
    '我的第一个笔记，测试Markdown Notes的基本功能。',
    85,
    false,
    false,
    1
FROM users WHERE username = 'test';

-- =====================================================
-- 数据验证和统计
-- =====================================================

-- 显示初始化结果
SELECT '✅ 数据库初始化完成！' as status;
SELECT '📊 数据统计：' as info;
SELECT COUNT(*) as '用户数量' FROM users;
SELECT COUNT(*) as '文件夹数量' FROM folders;
SELECT COUNT(*) as '笔记数量' FROM notes;
SELECT COUNT(*) as '用户设置数量' FROM user_settings;

-- 显示用户信息
SELECT '👤 用户账号信息：' as info;
SELECT username as '用户名', email as '邮箱', nickname as '昵称', status as '状态' FROM users;

-- 显示笔记信息
SELECT '📝 笔记信息：' as info;
SELECT 
    n.title as '标题',
    u.username as '作者',
    f.name as '文件夹',
    n.is_starred as '收藏',
    n.is_public as '公开',
    n.created_at as '创建时间'
FROM notes n
LEFT JOIN users u ON n.user_id = u.id
LEFT JOIN folders f ON n.folder_id = f.id
ORDER BY n.created_at DESC;

-- =====================================================
-- 完成提示
-- =====================================================
SELECT '🎉 初始化完成！您可以使用以下账号登录：' as message;
SELECT '👤 管理员账号: admin / admin123' as admin_account;
SELECT '🧪 测试账号: test / test123' as test_account;
