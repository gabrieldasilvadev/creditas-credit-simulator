# 💰 Credit Simulator
>#### ⚠️ Alerta de Over Engineering
> Este projeto é uma demonstração de habilidades e não deve ser utilizado em produção.

Simulador de crédito com API REST para calcular parcelas fixas de empréstimos considerando diferentes políticas de juros, conversão de moedas, processamento em lote e notificações. Desenvolvido com Kotlin + Spring Boot, arquitetura hexagonal, coroutines e observabilidade via Micrometer.

---

## 📦 Tecnologias

- Kotlin + Spring Boot 3.4
- Gradle Kotlin DSL
- Arquitetura Hexagonal + DDD
- MongoDB
- Coroutines + Flow (para batch)
- Feign Client + Cache (Caffeine)
- KediatR (Command Handler)
- Micrometer (Prometheus)
- Docker + Docker Compose
- WireMock (mock de cotações)

---

## 🚀 Como executar

### Requisitos

- Docker + Docker Compose
- Java 21
- Linux/Mac/WSL recomendado

### Subindo tudo com Docker

```bash
docker-compose up --build
```

Acesse:
- API: [http://localhost:7000](http://localhost:7000)
- Prometheus metrics: [http://localhost:7000/actuator/prometheus](http://localhost:7000/actuator/prometheus)
- MongoDB: `mongodb://localhost:27017`

---

## 🔁 Endpoints

### 📌 Simular empréstimo (único)

```http
POST /simulations
```

```json
{
  "loan_amount": {
    "amount": "1000.00",
    "currency": "USD"
  },
  "customer_info": {
    "birth_date": "2000-01-01",
    "email": "a@a.com"
  },
  "months": 12,
  "policy_type": "age",
  "source_currency": "USD",
  "target_currency": "BRL"
}
```

### 📌 Simular empréstimos em lote

```http
POST /simulations/batch
```

Aceita até milhares de simulações em uma única chamada (reativo, com buffer configurável).

---

## 📐 Arquitetura

O projeto segue os princípios da arquitetura hexagonal (Ports and Adapters), com separação clara de domínios:

- `core`: lógica de negócio pura
- `application`: orquestração com comandos e handlers (KediatR)
- `adapters`: entrada (REST, mensagens), saída (Mongo, APIs externas)
- `container`: ponto de entrada da aplicação

---

## 🧠 Políticas de Juros

- Até 25 anos: 5% a.a.
- 26 a 40 anos: 3% a.a.
- 41 a 60 anos: 2% a.a.
- Acima de 60 anos: 4% a.a.

Com suporte a múltiplas `InterestRatePolicy` dinâmicas.

---

## 🌍 Conversão de Moedas

- Utiliza Feign Client para acessar a [AwesomeAPI](https://docs.awesomeapi.com.br/api-de-moedas).
- Cache com TTL (10min) usando Caffeine.
- Mock para ambiente local via WireMock.

---

## 📊 Observabilidade

### Annotation `@Monitorable`

Monitora todas as execuções públicas de classes anotadas, registrando:

- Tempo de execução (`method.execution`)
- Quantidade de chamadas (`method.calls`)

Expose: `GET /actuator/prometheus`

---

## 🧪 Testes

- Unitários: JUnit5 + MockK
- Integração: SpringBootTest + Testcontainers (mock Mongo)
- Reativos: Testes com Flow
- Cobertura: Jacoco

### Gerar cobertura

```bash
./gradlew jacocoTestReport
```

> 💡 Cobertura: ~85% classes de domínio, 100% dos casos principais

---

## 📬 Notificações por Email

Simulação envia e-mail com resultado via SQS (mockado/local). Estrutura pronta para mensageria assíncrona.

---

## ⚙️ Variáveis importantes

```env
exchange.url=http://mock-exchange:8080
spring.data.mongodb.uri=mongodb://mongo:27017/creditas?authSource=admin
```

---

## 🧪 Futuras melhorias

- Políticas de taxa indexada (ex: CDI, inflação)
- Suporte a WebSocket para notificação
- Painel com métricas via Grafana
- Cache distribuído com Redis

---

## 🧾 Licença

MIT
