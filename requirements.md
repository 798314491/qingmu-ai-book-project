# Typora-like Online Markdown Notes System Requirements

## 项目概述
一款功能类似Typora的在线Markdown笔记网站，提供简约优雅的编辑界面，完整的用户认证系统，以及集成千问通义大语言模型的AI助手功能。

## 技术栈
- **前端**: Vue 3 + TypeScript + Vite
- **UI框架**: Element Plus + TailwindCSS
- **Markdown编辑器**: CodeMirror 6 / Monaco Editor
- **后端**: Spring Boot 3.x + Java 17
- **数据库**: MySQL 8.0
- **认证**: JWT Token
- **AI集成**: 阿里云通义千问 API

## 功能模块

### 1. 用户认证模块
- **注册功能**
  - 邮箱注册
  - 用户名、密码验证
  - 邮箱验证码（可选）
- **登录功能**
  - 用户名/邮箱登录
  - 记住登录状态
  - JWT Token认证
- **用户管理**
  - 个人信息修改
  - 密码重置
  - 头像上传

### 2. Markdown编辑器模块
- **编辑功能**
  - 实时预览（类似Typora的所见即所得模式）
  - 源码模式/预览模式/实时模式切换
  - 语法高亮
  - 自动保存
  - 撤销/重做
- **Markdown功能支持**
  - 标题、列表、引用
  - 代码块（支持多种语言高亮）
  - 表格编辑
  - 数学公式（LaTeX）
  - 流程图、时序图、甘特图
  - 图片上传和管理
  - 链接管理
- **主题**
  - 多种编辑器主题
  - 明暗模式切换

### 3. 笔记管理模块
- **文件管理**
  - 文件夹创建/删除/重命名
  - 笔记创建/删除/重命名
  - 笔记搜索（标题、内容、标签）
  - 笔记分类（标签系统）
- **版本控制**
  - 自动保存历史版本
  - 版本对比
  - 版本回滚
- **导入导出**
  - 导出为PDF、HTML、Markdown
  - 导入Markdown文件
  - 批量导入导出

### 4. AI Chat模块
- **聊天功能**
  - 与通义千问对话
  - 上下文记忆
  - 对话历史保存
- **智能功能**
  - 文本润色
  - 内容总结
  - 翻译
  - 代码解释
  - 笔记内容问答
- **集成功能**
  - 选中文本快速询问
  - AI建议插入到笔记
  - Prompt模板管理

### 5. 协作功能（可选）
- 笔记分享（只读链接）
- 协同编辑（实时同步）
- 评论系统

## 界面设计要求

### 整体风格
- 简约、优雅、专业
- 类似Typora的极简设计理念
- 减少视觉干扰，专注内容创作

### 布局设计
1. **主界面布局**
   - 左侧：文件目录树（可折叠）
   - 中间：编辑器区域
   - 右侧：AI Chat面板（可折叠）
   - 顶部：工具栏（简洁）

2. **编辑器界面**
   - 无边框设计
   - 优雅的排版
   - 平滑的动画过渡
   - 专注模式（全屏编辑）

3. **配色方案**
   - 亮色主题：白色背景，深灰色文字
   - 暗色主题：深色背景，浅色文字
   - 强调色：蓝色系（#4A90E2）

## 数据库设计

### 用户表 (users)
- 用户ID、用户名、邮箱、密码、头像、创建时间、更新时间

### 笔记表 (notes)
- 笔记ID、用户ID、标题、内容、文件夹ID、标签、创建时间、更新时间

### 文件夹表 (folders)
- 文件夹ID、用户ID、名称、父文件夹ID、创建时间

### AI对话表 (ai_conversations)
- 对话ID、用户ID、笔记ID（可选）、消息内容、角色、创建时间

### 版本历史表 (note_versions)
- 版本ID、笔记ID、内容、版本号、创建时间

## API设计

### 认证相关
- POST /api/auth/register - 用户注册
- POST /api/auth/login - 用户登录
- POST /api/auth/logout - 用户登出
- POST /api/auth/refresh - 刷新Token

### 笔记相关
- GET /api/notes - 获取笔记列表
- GET /api/notes/{id} - 获取单个笔记
- POST /api/notes - 创建笔记
- PUT /api/notes/{id} - 更新笔记
- DELETE /api/notes/{id} - 删除笔记
- GET /api/notes/search - 搜索笔记

### AI相关
- POST /api/ai/chat - 发送消息
- GET /api/ai/conversations - 获取对话历史
- POST /api/ai/enhance - 文本增强
- POST /api/ai/summarize - 内容总结

## 性能要求
- 页面加载时间 < 2秒
- 编辑器响应时间 < 100ms
- 自动保存间隔：30秒
- 支持大文件编辑（>10MB）

## 安全要求
- HTTPS传输
- SQL注入防护
- XSS防护
- CSRF防护
- 密码加密存储（BCrypt）
- API访问限流

## 部署要求
- Docker容器化部署
- 支持水平扩展
- 数据备份策略
- 日志管理
- 监控告警