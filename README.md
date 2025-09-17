# Typora-like Markdown Notes System

一款功能类似 Typora 的在线 Markdown 笔记系统，提供优雅的编辑体验、完整的用户认证系统和 AI 助手功能。

## 🚀 功能特性

### 核心功能
- 📝 **Markdown 编辑器** - 类似 Typora 的所见即所得编辑体验
- 🎨 **优雅界面** - 简约且专业的设计，支持明暗主题切换
- 🤖 **AI 助手** - 集成阿里云通义千问，提供智能写作辅助
- 🔐 **用户系统** - 完整的注册、登录、JWT 认证流程
- 📁 **笔记管理** - 文件夹组织、标签分类、全文搜索
- 💾 **版本控制** - 自动保存历史版本，支持版本回滚
- 📤 **导入导出** - 支持 Markdown、PDF、HTML 格式
- 🔗 **笔记分享** - 生成分享链接，支持密码保护

### 编辑器特性
- 实时预览模式
- 源码/预览/分屏模式切换
- 语法高亮
- 数学公式支持 (LaTeX)
- 流程图、时序图支持
- 表格编辑
- 代码块高亮
- 图片上传管理

## 🛠 技术栈

### 后端
- **框架**: Spring Boot 3.x
- **语言**: Java 17
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **认证**: Spring Security + JWT
- **ORM**: MyBatis Plus
- **API文档**: Swagger/OpenAPI

### 前端
- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI组件**: Element Plus
- **样式**: TailwindCSS + SCSS
- **编辑器**: CodeMirror 6
- **Markdown渲染**: markdown-it
- **状态管理**: Pinia
- **路由**: Vue Router

## 📦 项目结构

```
markdown-notes/
├── backend/                    # Spring Boot 后端
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/markdown/notes/
│   │   │   │   ├── config/     # 配置类
│   │   │   │   ├── controller/ # 控制器
│   │   │   │   ├── service/    # 服务层
│   │   │   │   ├── mapper/     # MyBatis映射
│   │   │   │   ├── entity/     # 实体类
│   │   │   │   ├── dto/        # 数据传输对象
│   │   │   │   ├── common/     # 公共类
│   │   │   │   └── security/   # 安全相关
│   │   │   └── resources/
│   │   │       └── application.yml
│   └── pom.xml
├── frontend/                    # Vue 3 前端
│   ├── src/
│   │   ├── api/                # API 接口
│   │   ├── components/         # 组件
│   │   ├── views/              # 页面
│   │   ├── layouts/            # 布局
│   │   ├── router/             # 路由
│   │   ├── stores/             # 状态管理
│   │   ├── styles/             # 样式文件
│   │   └── main.ts
│   ├── package.json
│   └── vite.config.ts
├── database/                    # 数据库脚本
│   └── schema.sql              # 表结构初始化脚本
└── requirements.md             # 需求文档
```

## 🚀 快速开始

### 环境要求
- JDK 17+
- Node.js 18+
- MySQL 8.0+
- Redis (可选)
- Maven 3.6+

### 数据库初始化

1. 创建数据库：
```bash
mysql -u root -p
```

2. 执行初始化脚本：
```sql
source /path/to/database/schema.sql
```

### 后端启动

1. 进入后端目录：
```bash
cd backend
```

2. 修改配置文件 `application.yml`：
- 配置数据库连接信息
- 配置 Redis 连接（可选）
- 配置阿里云通义千问 API Key

3. 编译运行：
```bash
mvn clean install
mvn spring-boot:run
```

后端服务将在 `http://localhost:8080` 启动

### 前端启动

1. 进入前端目录：
```bash
cd frontend
```

2. 安装依赖：
```bash
npm install
```

3. 启动开发服务器：
```bash
npm run dev
```

前端服务将在 `http://localhost:5173` 启动

### 默认账号
- 用户名: admin
- 密码: admin123

## 📝 API 文档

启动后端服务后，访问 Swagger UI：
```
http://localhost:8080/api/swagger-ui.html
```

## 🔧 配置说明

### 阿里云通义千问配置

1. 获取 API Key：
   - 访问 [阿里云 DashScope 控制台](https://dashscope.console.aliyun.com/)
   - 创建 API Key

2. 配置 API Key：
   在 `application.yml` 中配置：
   ```yaml
   alibaba:
     dashscope:
       api-key: your-api-key-here
   ```

### 文件存储配置

支持本地存储和阿里云 OSS：

```yaml
storage:
  type: local  # local 或 oss
  local:
    path: /data/markdown-notes/uploads
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com
    access-key-id: your-access-key
    access-key-secret: your-secret-key
    bucket-name: your-bucket
```

## 🐳 Docker 部署

### 使用 Docker Compose

1. 构建镜像：
```bash
docker-compose build
```

2. 启动服务：
```bash
docker-compose up -d
```

服务端口：
- 前端: http://localhost:80
- 后端 API: http://localhost:8080
- MySQL: localhost:3306
- Redis: localhost:6379

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交 Issue 和 Pull Request！

## 📧 联系方式

如有问题或建议，请提交 Issue 或发送邮件至 admin@example.com

---

**注意事项：**
1. 生产环境部署时请修改默认密码
2. 配置 HTTPS 以确保数据传输安全
3. 定期备份数据库
4. 配置合适的日志级别和监控