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

## 问题分析

在您的配置中：
```nginx
location /api {
    limit_req zone=api burst=20 nodelay;  # 使用了api区域
    proxy_pass http://127.0.0.1:8080;
}
```

但是没有在`http`块中定义`api`区域。

## 解决方案

### 方案一：添加限流区域定义（推荐）

```bash
# 备份当前配置
sudo cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.backup

# 修改配置文件
sudo cat > /etc/nginx/nginx.conf << 'EOF'
#user  nobody;
worker_processes  1;

#error_log  logs/error.log;
#error_log  logs/error.log  notice;
#error_log  logs/error.log  info;

#pid        logs/nginx.pid;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    # 定义限流区域（这是关键修复）
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;

    #log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
    #                  '$status $body_bytes_sent "$http_referer" '
    #                  '"$http_user_agent" "$http_x_forwarded_for"';

    #access_log  logs/access.log  main;

    sendfile        on;
    #tcp_nopush     on;

    #keepalive_timeout  0;
    keepalive_timeout  65;

    #gzip  on;

    server {
        listen       80;
        server_name  localhost;

        #charset koi8-r;

        #access_log  logs/host.access.log  main;

        location / {
            root   html;
            index  index.html index.htm;
        }

        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
            access_log off;
        }

        # API代理（带限流）
        location /api {
            limit_req zone=api burst=20 nodelay;
            proxy_pass http://127.0.0.1:8080;
            
            # 添加代理头信息
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        #error_page  404              /404.html;

        # redirect server error pages to the static page /50x.html
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
EOF
```

### 方案二：移除限流配置（如果不需要限流）

```bash
<code_block_to_apply_changes_from>
```

### 方案三：完整的生产配置（推荐）

```bash
sudo cat > /etc/nginx/nginx.conf << 'EOF'
worker_processes  auto;
error_log  /var/log/nginx/error.log warn;
pid        /var/run/nginx.pid;

events {
    worker_connections  1024;
    use epoll;
    multi_accept on;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    # 日志格式
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log  /var/log/nginx/access.log  main;

    # 性能优化
    sendfile        on;
    tcp_nopush      on;
    tcp_nodelay     on;
    keepalive_timeout  65;
    types_hash_max_size 2048;

    # Gzip压缩
    gzip  on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_comp_level 6;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    # 限流区域定义
    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
    limit_req_zone $binary_remote_addr zone=login:10m rate=3r/m;

    server {
        listen       80;
        server_name  localhost;
        
        root   /usr/share/nginx/html;
        index  index.html index.htm;

        # 访问日志
        access_log  /var/log/nginx/access.log  main;
        error_log   /var/log/nginx/error.log warn;

        # Vue Router支持
        location / {
            try_files $uri $uri/ /index.html;
        }

        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
            access_log off;
        }

        # API代理（带限流）
        location /api {
            limit_req zone=api burst=20 nodelay;
            proxy_pass http://127.0.0.1:8080;
            
            # 代理头设置
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_set_header X-Forwarded-Host $host;
            proxy_set_header X-Forwarded-Port $server_port;
            
            # 超时设置
            proxy_connect_timeout 60s;
            proxy_send_timeout 60s;
            proxy_read_timeout 60s;
            
            # HTTP版本
            proxy_http_version 1.1;
            proxy_set_header Connection "";
        }

        # 健康检查
        location /health {
            access_log off;
            return 200 "OK";
            add_header Content-Type text/plain;
        }

        # WebSocket支持（如果需要）
        location /ws {
            proxy_pass http://127.0.0.1:8080;
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # 禁止访问隐藏文件
        location ~ /\. {
            deny all;
            access_log off;
            log_not_found off;
        }

        # 错误页面
        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   /usr/share/nginx/html;
        }
    }
}
EOF
```

## 一键修复脚本

```bash
#!/bin/bash
echo "🔧 修复nginx限流区域配置..."

# 备份配置
sudo cp /etc/nginx/nginx.conf /etc/nginx/nginx.conf.backup.$(date +%Y%m%d_%H%M%S)

# 在http块开头添加限流区域定义
sudo sed -i '/^http {$/a\    # 限流区域定义\n    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;' /etc/nginx/nginx.conf

# 测试配置
if sudo nginx -t; then
    echo "✅ nginx配置修复成功！"
    sudo nginx -s reload
    echo "🌐 服务已重新加载"
else
    echo "❌ 配置仍有问题"
    # 恢复备份
    sudo cp /etc/nginx/nginx.conf.backup.$(date +%Y%m%d_%H%M%S) /etc/nginx/nginx.conf
    echo "已恢复备份配置"
fi
```

## 限流参数说明

```nginx
# 限流区域语法说明
limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;
#              ^客户端IP         ^区域名:内存大小 ^限制速率
```

- `$binary_remote_addr`: 使用客户端IP作为键
- `zone=api:10m`: 创建名为"api"的区域，分配10MB内存
- `rate=10r/s`: 每秒允许10个请求

```nginx
# 使用限流的语法
limit_req zone=api burst=20 nodelay;
#         ^区域名  ^突发数量 ^不延迟处理
```

- `burst=20`: 允许突发20个请求
- `nodelay`: 不延迟处理突发请求

## 验证修复

```bash
# 测试配置
sudo nginx -t

# 重新加载
sudo nginx -s reload

# 测试API访问
curl http://localhost/api/

# 查看nginx状态
sudo systemctl status nginx

# 查看错误日志
sudo tail -f /var/log/nginx/error.log
```

**推荐使用方案一**，只需要添加一行限流区域定义就能解决问题。如果不需要限流功能，可以选择方案二。

运行一键修复脚本最简单：
```bash
sudo sed -i '/^http {$/a\    limit_req_zone $binary_remote_addr zone=api:10m rate=10r/s;' /etc/nginx/nginx.conf && sudo nginx -t && sudo nginx -s reload
```

# 如果您不需要限流功能，可以直接移除limit_req配置
sudo cat > /etc/nginx/nginx.conf << 'EOF'
#user  nobody;
worker_processes  1;

events {
    worker_connections  1024;
}

http {
    include       mime.types;
    default_type  application/octet-stream;

    sendfile        on;
    keepalive_timeout  65;

    server {
        listen       80;
        server_name  localhost;

        location / {
            root   html;
            index  index.html index.htm;
        }

        # 静态资源缓存
        location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
            expires 1y;
            add_header Cache-Control "public, immutable";
            access_log off;
        }

        # API代理（无限流）
        location /api {
            proxy_pass http://127.0.0.1:8080;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        error_page   500 502 503 504  /50x.html;
        location = /50x.html {
            root   html;
        }
    }
}
EOF
