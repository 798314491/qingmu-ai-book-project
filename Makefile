# Makefile for Markdown Notes System

.PHONY: help install build start stop restart clean logs

# Colors for output
RED=\033[0;31m
GREEN=\033[0;32m
YELLOW=\033[1;33m
NC=\033[0m # No Color

help: ## Show this help message
	@echo "$(GREEN)Markdown Notes System - Makefile Commands$(NC)"
	@echo ""
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "$(YELLOW)%-15s$(NC) %s\n", $$1, $$2}'

install: ## Install all dependencies
	@echo "$(GREEN)Installing backend dependencies...$(NC)"
	cd backend && mvn clean install
	@echo "$(GREEN)Installing frontend dependencies...$(NC)"
	cd frontend && npm install

build: ## Build all components
	@echo "$(GREEN)Building backend...$(NC)"
	cd backend && mvn clean package -DskipTests
	@echo "$(GREEN)Building frontend...$(NC)"
	cd frontend && npm run build
	@echo "$(GREEN)Building Docker images...$(NC)"
	docker-compose build

build-backend: ## Build backend only
	cd backend && mvn clean package -DskipTests

build-frontend: ## Build frontend only
	cd frontend && npm run build

dev: ## Start development environment
	@echo "$(GREEN)Starting development environment...$(NC)"
	docker-compose up mysql redis -d
	@echo "$(GREEN)Waiting for services to be ready...$(NC)"
	sleep 10
	@echo "$(GREEN)Starting backend...$(NC)"
	cd backend && mvn spring-boot:run &
	@echo "$(GREEN)Starting frontend...$(NC)"
	cd frontend && npm run dev

start: ## Start all services with Docker Compose
	@echo "$(GREEN)Starting all services...$(NC)"
	docker-compose up -d
	@echo "$(GREEN)Services started successfully!$(NC)"
	@echo "Frontend: http://localhost"
	@echo "Backend API: http://localhost:8080"
	@echo "Swagger UI: http://localhost:8080/api/swagger-ui.html"

stop: ## Stop all services
	@echo "$(YELLOW)Stopping all services...$(NC)"
	docker-compose down

restart: ## Restart all services
	@echo "$(YELLOW)Restarting all services...$(NC)"
	docker-compose restart

clean: ## Clean build artifacts and dependencies
	@echo "$(RED)Cleaning build artifacts...$(NC)"
	cd backend && mvn clean
	cd frontend && rm -rf node_modules dist
	docker-compose down -v
	rm -rf uploads/* logs/*

logs: ## Show logs from all services
	docker-compose logs -f

logs-backend: ## Show backend logs
	docker-compose logs -f backend

logs-frontend: ## Show frontend logs
	docker-compose logs -f frontend

logs-mysql: ## Show MySQL logs
	docker-compose logs -f mysql

db-init: ## Initialize database
	@echo "$(GREEN)Initializing database...$(NC)"
	docker-compose up -d mysql
	sleep 10
	docker exec -i markdown-notes-mysql mysql -uroot -proot123456 < database/schema.sql
	@echo "$(GREEN)Database initialized successfully!$(NC)"

db-backup: ## Backup database
	@echo "$(GREEN)Backing up database...$(NC)"
	mkdir -p backups
	docker exec markdown-notes-mysql mysqldump -uroot -proot123456 markdown_notes > backups/backup_$$(date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)Database backed up successfully!$(NC)"

db-restore: ## Restore database from backup (usage: make db-restore FILE=backup.sql)
	@echo "$(YELLOW)Restoring database from $(FILE)...$(NC)"
	docker exec -i markdown-notes-mysql mysql -uroot -proot123456 markdown_notes < $(FILE)
	@echo "$(GREEN)Database restored successfully!$(NC)"

test-backend: ## Run backend tests
	cd backend && mvn test

test-frontend: ## Run frontend tests
	cd frontend && npm run test

lint: ## Run linters
	@echo "$(GREEN)Running frontend linter...$(NC)"
	cd frontend && npm run lint

format: ## Format code
	@echo "$(GREEN)Formatting code...$(NC)"
	cd frontend && npm run format

update: ## Update dependencies
	@echo "$(GREEN)Updating backend dependencies...$(NC)"
	cd backend && mvn versions:use-latest-versions
	@echo "$(GREEN)Updating frontend dependencies...$(NC)"
	cd frontend && npm update

status: ## Check service status
	@echo "$(GREEN)Service Status:$(NC)"
	@docker-compose ps

health: ## Check service health
	@echo "$(GREEN)Checking service health...$(NC)"
	@curl -f http://localhost:8080/api/actuator/health || echo "$(RED)Backend is not healthy$(NC)"
	@curl -f http://localhost/health || echo "$(RED)Frontend is not healthy$(NC)"

env-setup: ## Setup environment variables
	@echo "$(GREEN)Setting up environment variables...$(NC)"
	@if [ ! -f .env ]; then cp .env.example .env; echo "$(GREEN).env file created. Please update it with your configuration.$(NC)"; else echo "$(YELLOW).env file already exists$(NC)"; fi