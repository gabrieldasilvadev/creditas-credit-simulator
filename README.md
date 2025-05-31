# 💰 Simulador de Crédito

> #### ⚠️ Alerta de Over Engineering
> Este projeto é uma demonstração de habilidades e não deve ser utilizado em produção.

## 📝 Descrição

Aplicação backend construída em Kotlin e Spring Boot para simulação de empréstimos com parcelas fixas, considerando
faixa etária do cliente para cálculo de juros.

Principais características:

- Cálculo de taxa de juros por faixa etária (até 25, 26–40, 41–60, acima de 60 anos)
- Fórmula de parcelas fixas (PMT)
- Arquitetura Hexagonal (Ports & Adapters) e DDD
- Processamento batch assíncrono via Kotlin Coroutines + Flow
- Cache estratégico usando Feign Client + Caffeine
- Observabilidade com Micrometer (exposição de métricas para Prometheus/Grafana)
- Documentação de API com OpenAPI/Swagger UI
- Endpoint de bulk simulation para alta volumetria (com abstração de mensageria)
- Testes de carga com K6, parametrizáveis via linha de comando (`VUS`, `ITERATIONS`, `SIMULATIONS`)

---

## 📋 Sumário

- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração-e-execução)
- [Testes](#-testes)
- [Endpoints](#-endpoints)
- [Arquitetura](#-arquitetura)
- [Futuras Melhorias](#-futuras-melhorias)

---

## 🌟 Pré-requisitos

- Java 21+ (JDK)
- Kotlin 1.9+
- Gradle Wrapper (incluído)
- Docker (para MongoDB e LocalStack)
- Kubectl + Minikube (opcional para Kubernetes)

---

## ⚙️ Instalação

1. Clone o repositório:
```bash
git clone https://github.com/gabrieldasilvadev/creditas-credit-simulator.git
cd creditas-credit-simulator
```
2. Compile o projeto:
```bash
./gradlew clean build
```

---

## 🔧 Configuração e Execução

#### Rodando localmente (Kotlin + MongoDB)

1. Preparar o ambiente para LocalStack (AWS emulado) e MongoDB:
```bash
docker-compose up -d
```
2. Configure a infraestrutura no LocalStack:
```bash
make setup
```
3. Inicie o projeto
```bash
gradlew :container:bootRun
```

4. Acesse o Swagger UI em:

   http://localhost:7000/swagger-ui/index.html

5. Ativar feature toggle para envio do email
```bash
feature.toggle.send-email=true
 ```

### 🛳️ Kubernetes com Minikube (opcional)

1. Inicie o Minikube:
```bash
minikube start
```

2. Construir o LocalStack:
```bash
kubectl apply -f k8s/localstack-deployment.yaml
kubectl rollout status deployment/localstack --timeout=120s
kubectl get pods -l app=localstack -w
```

3. Execute o job de configuração do LocalStack:
```bash
kubectl apply -f k8s/localstack-setup-job.yaml
kubectl logs job/localstack-setup-job -f
```

4. Construa a imagem do MongoDB:
 ```bash
kubectl apply -f k8s/mongodb-deployment.yaml
kubectl rollout status deployment/mongodb --timeout=120s
```

5. Construa a imagem do simulador de crédito
```bash
docker build -t credit-simulator:latest -f Dockerfile .
minikube image load credit-simulator:latest
```

6. Aplique o deployment do simulador de crédito
```bash
kubectl apply -f k8s/credit-simulator-deployment.yaml
kubectl rollout status deployment/credit-simulator --timeout=120s
kubectl get pods -l app=credit-simulator -w
```

7. Exponha os serviços:
```bash
kubectl port-forward svc/credit-simulator 7000:7000
kubectl port-forward svc/mongodb 27017:27017
```

8. Verifique os pods:
```bash
kubectl get pods
```

---

## 🔬 Testes

- **Unitários** (JUnit + Mockito + Testcontainers):
```bash
./gradlew test
```
- **Integração** (Testcontainers):
```bash
./gradlew :integrationTest:test
```
- **Performance com K6**:

Simulação única:
```bash
k6 run ./load-test-simulations.js --env VUS=100 --env ITERATIONS=1000000
```

  Simulação em lote:
```bash
k6 run ./load-test-simulations-bulk.js --env SIMULATIONS=10000 --env VUS=1 --env ITERATIONS=1
```

---

## 📌 Endpoints

| Método | Rota                     | Descrição                           |
|--------|--------------------------|-------------------------------------|
| POST   | `/simulations`           | Simulação de empréstimo único       |
| POST   | `/simulations/bulk`      | Simulação em lote (alta volumetria) |
| GET    | `/simulations/bulk/{id}` | Consultar de simulação em lote      |

### Exemplo: Simulação Única

```bash
curl -X POST http://localhost:7000/simulations \
  -H "Content-Type: application/json" \
  -d '{
    "loan_amount": {
      "amount": "1000.00",
      "currency": "USD"
    },
    "customer_info": {
      "birth_date": "2003-04-15",
      "email": "user@example.com"
    },
    "months": 12,
    "policy_type": "age",
    "source_currency": "BRL",
    "target_currency": "USD"
  }'
```

### Exemplo: Simulação em Lote

```bash
curl -X POST http://localhost:7000/simulations/bulk \
  -H "Content-Type: application/json" \
  -d '{
    "simulations": [
      {
        "loan_amount": {
          "amount": "10000.00",
          "currency": "BRL"
        },
        "customer_info": {
          "birth_date": "1990-01-15",
          "email": "cliente1@example.com"
        },
        "months": 12,
        "policy_type": "fixed",
        "source_currency": "BRL",
        "target_currency": "USD"
      },
      {
        "loan_amount": {
          "amount": "5000.00",
          "currency": "BRL"
        },
        "customer_info": {
          "birth_date": "2002-05-20",
          "email": "cliente2@example.com"
        },
        "months": 24,
        "policy_type": "age",
        "source_currency": "BRL",
        "target_currency": "BRL"
      }
    ]
  }'
```

Resposta inicial:

```json
{
  "bulk_id": "<uuid>",
  "status": "RECEIVED"
}
```

Consulte o progresso:

```bash
curl http://localhost:7000/simulations/bulk/{bulk_id}
```

---

## 🏗️ Arquitetura

- **Domain**: Entidades, Value Objects, políticas de taxa (Strategy Pattern)
- **Application**: Command Handlers usando KediatR (mediator)
- **Adapters**:
  - REST Controllers (Spring MVC)
  - Persistence: MongoDB (Spring Data)
  - External Api: Feign Client
  - Cache: Caffeine
  - Mensageria (SQS/SNS) – abstração para high-volume (Bulk)
  - Notificação (Email) – Envio de notificações via, por exemplo, Email
- **Config**: Beans, Resilience4j (Circuit Breaker, Retry, Timeout), Micrometer

---

## 🛠️ Futuras Melhorias

- Suporte a políticas de taxa indexadas (CDI, inflação)
- Processamento assíncrono com Kafka
- Dashboard Grafana com métricas detalhadas (ThreadPoolTaskExecutor, latência)
- Cache distribuído (Redis)
- Testes de carga segmentados com batches assíncronos no bulk
- Load balancing com Nginx
- Event store para auditoria

