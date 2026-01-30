# Aruba Business Monitoring

Sistema di caricamento, elaborazione e monitoraggio dati cliente per servizi cloud.

## 📋 Indice
- [Requisiti](#requisiti)
- [Quick Start](#quick-start)
- [Architettura](#architettura)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Configurazione](#configurazione)

## 🔧 Requisiti

- Docker & Docker Compose
- Java 17+ (solo per sviluppo locale)
- Maven 3.8+ (solo per sviluppo locale)

## 🚀 Quick Start

### Avvio con Docker (RACCOMANDATO)

```bash
# 1. Clone del repository
git clone <repository-url>
cd aruba-business-monitoring

# 2. Avvio dell'intera infrastruttura
docker-compose up --build

# L'applicazione sarà disponibile su http://localhost:8080
# RabbitMQ Management UI: http://localhost:15672 (guest/guest)

Avvio Locale (per sviluppo)
# 1. Avvia solo infrastruttura
docker-compose up postgres rabbitmq

# 2. Avvia l'applicazione
mvn spring-boot:run
```

### Architettura
```
Stack Tecnologico
·	Backend: Spring Boot 4.0.2, Java 17
·	Database: PostgreSQL 16
·	Message Broker: RabbitMQ 3
·	Containerization: Docker & Docker Compose
```
### Componenti Principali
```
┌─────────────────┐
│  File Upload    │
│   Controller    │
└────────┬────────┘
         │
         ▼
┌─────────────────┐      ┌──────────────┐
│  CSV Processing │─────►│  PostgreSQL  │
│     Service     │      └──────────────┘
└────────┬────────┘
         │
         ▼
┌─────────────────┐      ┌──────────────┐
│ Special Cond.   │─────►│  RabbitMQ    │
│    Service      │      │   Queues     │
└─────────────────┘      └──────┬───────┘
                                │
         ┌──────────────────────┼─────────────────┐
         ▼                      ▼                 ▼
┌─────────────────┐  ┌──────────────┐  ┌─────────────────┐
│  Notification   │  │  Marketing   │  │   External      │
│     Worker      │  │    Worker    │  │   Consumers     │
└─────────────────┘  └──────────────┘  └─────────────────┘
```
### Architettura Message Broker
```
Producer                     RabbitMQ                    Consumer
┌──────────────┐            ┌─────────────────┐         ┌──────────────┐
│   CSV        │            │ notification    │         │ Notification │
│ Processing   ├───────────►│ .outbox.queue   ├────────►│   Worker     │
│  Service     │            └─────────────────┘         └──────┬───────┘
└──────────────┘                                               │
                                                               ▼
                            ┌─────────────────┐         ┌──────────────┐
                            │ events.exchange │────────►│   External   │
                            │  (direct)       │         │   Systems    │
                            └────────┬────────┘         └──────────────┘
                                     │
                                     ├─► alerts.customer_expired
                                     ├─► events.special_condition
                                     └─► events.upselling ──► marketing.mail.queue
```
### Security
API secured as OAuth2 Resource Server
JWT Bearer authentication
Token validation is mocked for simplicity
Any Bearer token is accepted for demo/testing purposes

Pattern Implementati
·	Layered Architecture: Controller → Service → Repository
·	DTO Pattern: Separazione tra entità e response
·	Publisher-Worker: Processing asincrono con RabbitMQ
·	Repository Pattern: JPA per data access

### Database Schema
```
-- Tabella principale servizi
service_record (
    id BIGSERIAL PRIMARY KEY,
    customer_id VARCHAR(64) NOT NULL,
    service_type VARCHAR(32) NOT NULL,
    activation_date DATE,
    expiration_date DATE,
    amount NUMERIC(10,2),
    status VARCHAR(20) NOT NULL
)

-- Tabella errori elaborazione
processing_error (
    id BIGSERIAL PRIMARY KEY,
    row_number INT NOT NULL,
    error_type VARCHAR(20) NOT NULL,
    message TEXT NOT NULL,
    raw_row TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL
)
```