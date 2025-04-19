.PHONY: \
  setup teardown logs \
  localstack-start localstack-stop localstack-restart \
  mongodb-start mongodb-stop mongodb-restart \
  grafana-start grafana-stop grafana-restart grafana-logs influx-logs \
  observability-reset \
  observability-setup-dev \
  k6-run

GREEN=\033[0;32m
YELLOW=\033[1;33m
RED=\033[0;31m
NC=\033[0m

GRAFANA_API=http://localhost:3000
GRAFANA_USER=admin
GRAFANA_PASSWORD=admin
PROMETHEUS_DATASOURCE_NAME=prometheus
GRAFANA_DASHBOARD_UIDS=metrics-custom resilience4j


define elapsed
	@end=$$(date +%s); \
	elapsed=$$((end - start)); \
	echo ""; \
	echo "$(1) concluído em $${elapsed}s ✅"
endef

# ──────────────── SETUP ────────────────
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

# ──────────────── LOCALSTACK ────────────────
localstack-start:
	@echo "$(YELLOW)🚀 Iniciando LocalStack...$(NC)"
	docker-compose up -d localstack

localstack-stop:
	@echo "$(RED)⛔ Parando LocalStack...$(NC)"
	docker-compose stop localstack

localstack-restart:
	@echo "$(YELLOW)🔁 Reiniciando LocalStack...$(NC)"
	docker-compose restart localstack

# ──────────────── MONGODB ────────────────
mongodb-start:
	@echo "$(YELLOW)🚀 Iniciando MongoDB...$(NC)"
	docker-compose up -d mongodb

mongodb-stop:
	@echo "$(RED)⛔ Parando MongoDB...$(NC)"
	docker-compose stop mongodb

mongodb-restart:
	@echo "$(YELLOW)🔁 Reiniciando MongoDB...$(NC)"
	docker-compose restart mongodb

# ──────────────── OBSERVABILIDADE ────────────────
grafana-start:
	@echo "$(YELLOW)📊 Iniciando Grafana, Prometheus e InfluxDB...$(NC)"
	@start=$$(date +%s)
	docker-compose up -d influxdb grafana prometheus
	$(call elapsed,Grafana/Prometheus/InfluxDB)

grafana-stop:
	@echo "$(RED)⛔ Parando Grafana, Prometheus e InfluxDB...$(NC)"
	docker-compose stop influxdb grafana prometheus

grafana-restart:
	@echo "$(YELLOW)🔁 Reiniciando Grafana, Prometheus e InfluxDB...$(NC)"
	docker-compose restart influxdb grafana prometheus

grafana-logs:
	@echo "$(YELLOW)📜 Logs do Grafana:$(NC)"
	docker logs -f grafana

observability-reset:
	@echo "$(RED)🧨 Resetando observabilidade: Grafana + Prometheus + InfluxDB...$(NC)"
	@docker-compose stop grafana prometheus influxdb
	@docker-compose rm -f grafana prometheus influxdb
	@docker volume rm credit-simulator_grafana-data || true
	@docker volume rm credit-simulator_influxdb-data || true
	@rm -f /tmp/dashboard-4701.json || true
	@echo "$(YELLOW)🚀 Subindo stack limpa...$(NC)"
	@docker-compose up -d grafana prometheus influxdb
	@echo "$(GREEN)✅ Observabilidade resetada com sucesso!$(NC)"


observability-setup-dev:
	@echo "$(YELLOW)🚀 Iniciando observabilidade para dev...$(NC)"
	@$(MAKE) grafana-start
	@echo "$(YELLOW)⏳ Aguardando Grafana subir...$(NC)"
	@until curl -s -u $(GRAFANA_USER):$(GRAFANA_PASSWORD) $(GRAFANA_API)/api/health | jq -e '.database == "ok"' > /dev/null; do \
		sleep 2; \
	done
	@echo "$(GREEN)✅ Grafana está pronto.$(NC)"
	@$(MAKE) grafana-import-all
	@echo "$(GREEN)✅ Observabilidade pronta para uso local.$(NC)"

# ──────────────── K6 ────────────────
k6-run:
	@echo "$(YELLOW)🚀 Executando teste de carga com k6...$(NC)"
	@start=$$(date +%s)
	k6 run --out influxdb=http://localhost:8086/k6 ./load-test-simulations.js
	$(call elapsed,K6 Load Test)

influx-logs:
	@echo "$(YELLOW)📜 Logs do InfluxDB:$(NC)"
	docker logs -f influxdb

