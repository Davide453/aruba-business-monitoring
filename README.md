# Aruba Business Monitoring

Sistema di caricamento, elaborazione e monitoraggio dei dati cliente per i servizi cloud business di Aruba. L'applicazione consente agli operatori interni di caricare file CSV contenenti informazioni sui servizi, elaborarli automaticamente, generare report aggregati e inviare notifiche asincrone in base a condizioni specifiche.

## 📋 Indice
- [Requisiti](#requisiti)
- [Quick Start](#quick-start)
- [Architettura](#architettura)
- [Security](#security)
- [API](#api)
- [Database Schema](#database-schema)
- [Autore](#Autore)

## Requisiti

- Docker & Docker Compose
- Java 17+ (solo per sviluppo locale)
- Maven 3.8+ (solo per sviluppo locale)

## Quick Start

### Avvio con Docker 

```bash
# 1. Clone del repository
git clone https://github.com/Davide453/aruba-business-monitoring.git
cd aruba-business-monitoring

# 2. Avvio dell'intera infrastruttura (PostgreSQL + RabbitMQ + App)
docker-compose up --build

# L'applicazione sarà disponibile su http://localhost:8080
# RabbitMQ Management UI: http://localhost:15672 (guest/guest)

Avvio Locale 
# 1. Avvia solo infrastruttura
docker-compose up postgres rabbitmq

# 2. Avvia l'applicazione
mvn spring-boot:run
```
### 🧪 Test Manuali (Bruno Collection)

Nella repository è inclusa una **Bruno Collection** per testare manualmente le API REST esposte dall’applicazione.

## Architettura
### Stack Tecnologico
 
| Component        | Technology              | Version         |
|------------------|-------------------------|-----------------|
| Backend          | Spring Boot             | 4.0.2           |
| Language         | Java                    | 17              |
| Database         | PostgreSQL              | 16              |
| Message Broker   | RabbitMQ                | 3.x             |
| Security         | OAuth2 + JWT            | Resource Server |
| API Documentation| OpenAPI / Swagger       | 3.0             |
| Build Tool       | Maven                   | 3.8+            |
| Containerization | Docker + Docker Compose | -               |
 

### Componenti Principali
```
┌─────────────────────────────────────────────────────────────┐
│                     External Client                         │
│                (Operatore / Sistema Interno)                │
└───────────────────────┬─────────────────────────────────────┘
                        │ HTTP + JWT Bearer
                        │ (OAuth2 Resource Server)
                        ▼
┌─────────────────────────────────────────────────────────────┐
│                   Spring Boot Application                   │
│                                                             │
│  ┌───────────────────────────────────────────────────────┐  │
│  │                Controllers (REST API)                 │  │
│  │  • FileUploadController                               │  │
│  │  • ReportController (/api/report/summary)             │  │
│  └─────────────┬─────────────────────────┬───────────────┘  │
│                │                         │                  │
│                ▼                         ▼                  │
│  ┌─────────────────────────┐  ┌────────────────────────┐    │
│  │   CsvProcessingService  │  │  ServiceRecordService  │    │
│  │ • Parse CSV             │  │ • Generate Reports     │    │
│  │ • Validate Data         │  │ • Aggregate Queries    │    │
│  │ • Batch Save            │  └─────────────┬──────────┘    │
│  └──────────┬──────────────┘                │               │
│             │                               │               │
│             │ Validation / Parsing Error    │               │
│             ▼                               ▼               │
│  ┌─────────────────────────┐  ┌──────────────────────────┐  │
│  │ ProcessingErrorService  │  │ ServiceRecordRepository  │  │
│  │ • Persist errors        │  │ • JPA Queries            │  │
│  │ • Track invalid rows    │  └────────┬─────────────────┘  │
│  └──────────┬──────────────┘           │                    │
│             │                          │                    │
│             ▼                          ▼                    │
│  ┌──────────────────────────┐  ┌───────────────────────────┐│
│  │ ProcessingErrorRepository│  │     PostgreSQL DB         ││
│  │ • Save processing_error  │  │ • service_record          ││
│  └──────────┬───────────────┘  │  • processing_error       ││
│             │                  └───────────────────────────┘│
│             │                                               │
│             ▼                                               │
│  ┌─────────────────────────┐                                │
│  │ SpecialConditionService │                                │
│  │ • Detect Conditions     │                                │
│  │ • Trigger Notifications │                                │
│  └──────────┬──────────────┘                                │
│             │                                               │
│             ▼                                               │
│  ┌─────────────────────────┐                                │
│  │   NotificationService   │                                │
│  │ • Enqueue Messages      │                                │
│  │ • RabbitMQ Publisher    │                                │
│  └──────────┬──────────────┘                                │
│             │                                               │
└─────────────┼───────────────────────────────────────────────┘
              │ AMQP
              ▼
┌─────────────────────────────────────────────────────────────┐
│                          RabbitMQ                           │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ notification.outbox.queue (Work Queue)               │   │
│  │ • Retry policy                                       │   │
│  └───────────────┬──────────────────────────────────────┘   │
│                  │ retry exhausted                          │
│                  ▼                                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ notification.dlq (Dead Letter Queue)                 │   │
│  └───────────────┬──────────────────────────────────────┘   │
│                  ▼                                          │
│        DeadLetterHandler (logging / analysis)               │
│                                                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ NotificationWorker (@RabbitListener)                 │   │
│  │ • Consume notifications                              │   │
│  │ • Route to exchanges                                 │   │
│  └───────────────┬──────────────────────────────────────┘   │
│                  ▼                                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ events.exchange (Direct Exchange)                    │   │
│  │                                                      │   │
│  │ Routing Keys:                                        │   │
│  │  • alerts.customer_expired ────────────► External    │   │
│  │  • events.special_condition ───────────► External    │   │
│  │  • events.upselling ───┐                             │   │
│  └────────────────────────┼─────────────────────────────┘   │
│                           │                                 │
│                           ▼                                 │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ marketing.mail.queue                                 │   │
│  └───────────────┬──────────────────────────────────────┘   │
│                  ▼                                          │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ MarketingMailWorker (@RabbitListener)                │   │
│  │ • Send upselling email notifications                 │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘

```
### Security
```
· Autenticazione OAuth2 con JWT Bearer token
· Protezione di tutti gli endpoint API
· Configurazione mock per testing (accetta qualsiasi Bearer token)
· Swagger UI accessibile senza autenticazione per documentazione
```
## API
### Upload File CSV

**Endpoint**  
`POST /api/files/upload`

**Descrizione**  
Carica un file CSV per l’elaborazione asincrona.  
Il file viene validato, processato in batch e i record validi vengono persistiti.  
Le righe non conformi vengono tracciate come errori di elaborazione.

---

#### Headers
| Header | Valore | Obbligatorio |
|--------|--------|--------------|
| `Authorization` | `Bearer <your-jwt-token>` | ✅ Sì |
| `Content-Type` | `multipart/form-data` | ✅ Sì |
---

#### Body (multipart)

| Campo | Tipo | Descrizione |
|-----|-----|-------------|
| `csv` | File | File CSV da caricare |
---

#### Formato CSV richiesto
```csv
customer_id,service_type,activation_date,expiration_date,amount,status
CUST001,hosting,2020-01-15,2025-12-31,99.99,active
CUST002,pec,2021-06-01,2024-01-15,49.99,expired
```
#### Colonne Obbligatorie

| Colonna | Tipo | Formato | Valori Ammessi | Note |
|---------|------|---------|----------------|------|
| `customer_id` | String | Max 64 char | Alfanumerico | Identificativo univoco cliente |
| `service_type` | Enum | - | `hosting`, `pec`, `spid`, `fatturazione` | Tipo di servizio |
| `activation_date` | Date | ISO 8601 (YYYY-MM-DD) | Date valide | Data attivazione servizio |
| `expiration_date` | Date | ISO 8601 (YYYY-MM-DD) | Date valide | Deve essere successiva ad activation_date |
| `amount` | Decimal | Max 10,2 | Numero positivo | Importo in euro |
| `status` | Enum | - | `active`, `expired`, `pending_renewal` | Stato del servizio |

#### Responses
| HTTP Status          | Descrizione                                 |
| -------------------- | ------------------------------------------- |
| **202 Accepted**     | File accettato e in elaborazione            |
| **400 Bad Request**  | File non valido, vuoto o formato CSV errato |
| **401 Unauthorized** | Token mancante o non valido                 |

#### cURL
```
curl -X POST http://localhost:8080/api/files/upload \
-H "Authorization: Bearer demo-token" \
-F "csv=@services.csv"
```

### 📊 Report Riepilogativo

**Endpoint**  
`GET /api/report/summary`

**Descrizione**  
Restituisce un report aggregato con statistiche sui servizi dei clienti, calcolate sui dati attualmente presenti nel sistema.

---

#### Headers

| Header | Valore | Obbligatorio |
|--------|--------|--------------|
| `Authorization` | `Bearer <your-jwt-token>` | ✅ Sì |

---

#### Response

**200 OK**

```json
{
  "activeServicesByType": [
    {
      "serviceType": "hosting",
      "count": 150
    },
    {
      "serviceType": "pec",
      "count": 89
    }
  ],
  "averageSpendPerCustomer": [
    {
      "customerId": "CUST001",
      "average": 125.50
    },
    {
      "customerId": "CUST002",
      "average": 67.30
    }
  ],
  "customersWithMultipleExpiredServices": [
    "CUST003",
    "CUST007"
  ],
  "customersWithServicesExpiringSoon": [
    "CUST001",
    "CUST012"
  ]
}
```
#### Responses

| HTTP Status          | Descrizione                  |
| -------------------- | ---------------------------- |
| **200 OK**           | Report generato con successo |
| **401 Unauthorized** | Token mancante o non valido  |
#### cURL
```
curl http://localhost:8080/api/report/summary \
-H "Authorization: Bearer demo-token" \
| jq .
```
Accesso RabbitMQ Management UI
```
1. Apri nel browser: http://localhost:15672
2. Login: guest / guest
3. Vai su Queues per vedere le code:
    - notification.outbox.queue - work queue per notifiche
    - marketing.mail.queue - email al team marketing
    - notification.dlq – Dead Letter Queue per i messaggi che falliscono l’elaborazione
```
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

-- Indici per performance
CREATE INDEX idx_service_record_customer ON service_record(customer_id);
CREATE INDEX idx_service_record_expiration_date ON service_record(expiration_date);
CREATE INDEX idx_service_record_status_customer ON service_record(status, customer_id);
CREATE INDEX idx_service_record_status_service_type ON service_record(status, service_type);

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
## Riferimenti

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [RabbitMQ Tutorials](https://www.rabbitmq.com/tutorials/tutorial-one-java.html)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [OpenAPI Specification](https://swagger.io/specification/)

---

## Autore

Sviluppato come esercizio tecnico per la posizione di **API Platform Developer** presso **Aruba S.p.A.**

Per domande o chiarimenti:  
**davidepedrazzini1@gmail.com**
