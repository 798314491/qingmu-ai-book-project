#!/bin/bash

echo "🔄 重新启动服务以应用CORS配置更改..."

# 停止服务
echo "⏹️ 停止现有服务..."
docker-compose down

# 重新构建并启动服务
echo "🏗️ 重新构建并启动服务..."
docker-compose up --build -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "📋 服务状态:"
docker-compose ps

echo ""
echo "✅ 服务重启完成！"
echo "🌐 前端: http://localhost"
echo "🚀 后端API: http://localhost/api"
echo "📖 API文档: http://localhost/api/swagger-ui.html"
echo "🧪 CORS测试: 在浏览器中打开 test-cors.html 文件"
echo ""
echo "📝 测试CORS是否修复:"
echo "1. 在浏览器中打开 test-cors.html"
echo "2. 点击 '测试 OPTIONS 预检请求' 按钮"
echo "3. 点击 '测试 GET 请求' 按钮"
echo "4. 查看测试结果，应该显示成功"
echo ""
echo "如果测试失败，请检查:"
echo "- Docker容器是否正常运行: docker-compose ps"
echo "- nginx配置是否正确加载: docker-compose logs frontend"
echo "- 后端配置是否正确加载: docker-compose logs backend"
