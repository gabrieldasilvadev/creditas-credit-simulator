.PHONY: setup teardown logs localstack-start localstack-stop

setup:
	@if [ "$$(docker ps -q -f name=localstack)" = "" ]; then \
		echo "⚠️  LocalStack não está rodando. Iniciando..."; \
		docker-compose up -d localstack; \
		sleep 5; \
	fi
	@if [ "$$(docker ps -q -f name=mongodb)" = "" ]; then \
		echo "⚠️  MongoDB não está rodando. Iniciando..."; \
		docker-compose up -d mongodb; \
		sleep 5; \
	fi
	bash ./infra/localstack/setup.sh

teardown:
	bash ./infra/localstack/teardown.sh

logs:
	docker logs -f localstack

localstack-start:
	docker-compose up -d localstack

localstack-stop:
	docker-compose stop localstack

localstack-restart:
	docker-compose restart localstack

mongodb-start:
	docker-compose up -d mongodb

mongodb-stop:
	docker-compose stop mongodb

mongodb-restart:
	docker-compose restart mongodb