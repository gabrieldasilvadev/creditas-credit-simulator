.PHONY: setup teardown logs localstack-start localstack-stop localstack-restart mongodb-start mongodb-stop mongodb-restart

GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
NC=\033[0m

define elapsed
	@end=$$(date +%s); \
	elapsed=$$((end - start)); \
	echo ""; \
	echo "$(1) concluído em $${elapsed}s ✅"
endef

setup:
	@echo "$(YELLOW)🔧 Iniciando setup do ambiente...$(NC)"
	@start=$$(date +%s)
	@if [ "$$(docker ps -q -f name=localstack)" = "" ]; then \
		echo "$(RED)⚠️  LocalStack não está rodando. Iniciando...$(NC)"; \
		docker-compose up -d localstack; \
		sleep 5; \
	fi
	@if [ "$$(docker ps -q -f name=mongodb)" = "" ]; then \
		echo "$(RED)⚠️  MongoDB não está rodando. Iniciando...$(NC)"; \
		docker-compose up -d mongodb; \
		sleep 5; \
	fi
	@bash ./infra/localstack/teardown.sh
	@bash ./infra/localstack/setup.sh
	$(call elapsed,Setup)

teardown:
	@echo "$(YELLOW)🧹 Iniciando teardown...$(NC)"
	@start=$$(date +%s)
	@bash ./infra/localstack/teardown.sh
	$(call elapsed,Teardown)

logs:
	@echo "$(YELLOW)📜 Logs do LocalStack:$(NC)"
	docker logs -f localstack

localstack-start:
	@echo "$(YELLOW)🚀 Iniciando LocalStack...$(NC)"
	docker-compose up -d localstack

localstack-stop:
	@echo "$(RED)⛔ Parando LocalStack...$(NC)"
	docker-compose stop localstack

localstack-restart:
	@echo "$(YELLOW)🔁 Reiniciando LocalStack...$(NC)"
	docker-compose restart localstack

mongodb-start:
	@echo "$(YELLOW)🚀 Iniciando MongoDB...$(NC)"
	docker-compose up -d mongodb

mongodb-stop:
	@echo "$(RED)⛔ Parando MongoDB...$(NC)"
	docker-compose stop mongodb

mongodb-restart:
	@echo "$(YELLOW)🔁 Reiniciando MongoDB...$(NC)"
	docker-compose restart mongodb
