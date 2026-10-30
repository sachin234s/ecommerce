# Morrow Commerce

An India-first, multi-market ecommerce starter in a separate project from the
calculator. It contains a Vue operations dashboard and a Java 17 / Maven
microservices backend. Spring MVC services use embedded Apache Tomcat and
PostgreSQL databases separated by service. The Spring Cloud Gateway is the
single REST entry point for the dashboard.

**Payments are sandbox records only.** This project does not connect to a
payment provider or move money. Tax configuration is illustrative, not a tax
calculation or invoice engine.

## Run locally

Requirements: Java 17+, Maven 3.9+, Node.js 18+, npm 9+. Docker Compose is
optional for local H2 development.

From `morrow-commerce/backend`, start each service in its own terminal:

```bash
mvn -pl services/customer-service spring-boot:run
mvn -pl services/inventory-service spring-boot:run
mvn -pl services/order-service spring-boot:run
mvn -pl services/payment-service spring-boot:run
mvn -pl services/market-service spring-boot:run
mvn -pl services/dashboard-service spring-boot:run
mvn -pl services/api-gateway spring-boot:run
```

Services use separate file-backed H2 databases by default. Ports are 8081
(customers), 8082 (inventory), 8083 (orders), 8084 (payments), 8085 (markets),
8090 (dashboard), and 8080 (gateway). Start the frontend in another terminal
from the project root:

```bash
npm install
npm run dev
```

Vite proxies `/api` requests to the gateway on port 8080. Open the URL printed
by Vite. Build the dashboard with `npm run build`.

To run with PostgreSQL instead, start `docker compose up --build` at the
project root. This starts PostgreSQL, the gateway, dashboard read model, and
domain services; the frontend can still run locally with `npm run dev`. The
Compose database password is for local development only. Docker is not
required to run the services individually with H2.

Run all Java service tests from `backend/`:

```bash
mvn test
```

## Architecture

```text
Vue admin ──REST──> Spring Cloud Gateway :8080
                       ├── Customer service :8081 ──> customers DB
                       ├── Inventory service :8082 ──> inventory DB
                       ├── Order service :8083 ──> orders DB
                       ├── Payment service :8084 ──> payments DB
                       ├── Market service :8085
                       └── Dashboard service :8090 ──REST──> domain services
```

Each domain service is an independently runnable Maven module, has its own
Spring Boot application, REST API, and data ownership. PostgreSQL uses a
separate database per stateful service. The dashboard service is a read model
that queries services; service unavailability is returned as an API error,
not silently replaced with stale/mock data.

The microservices communicate over REST today. Apache Kafka, transactional
outbox, checkout saga orchestration, Redis, OpenSearch, Kubernetes deployment,
and CI/CD are production-scale next steps rather than included runtime
components. Add event streaming when asynchronous fulfillment, inventory
reservations, and reliable cross-service workflows are implemented.

### Service APIs

| Service | API | Use cases |
| --- | --- | --- |
| Customer | `GET/POST /api/v1/customers`, `GET/PATCH /api/v1/customers/{id}` | Customer profile lifecycle, market, city, payment preference, and marketing consent; duplicate email is rejected |
| Inventory | `GET/POST /api/v1/inventory`, `PATCH /api/v1/inventory/{id}/stock` | Product variants, warehouse stock, atomic stock adjustment, no-negative-stock rule |
| Order | `GET/POST /api/v1/orders`, `PATCH /api/v1/orders/{id}/status` | Market-priced orders and guarded lifecycle transitions |
| Payment | `GET /api/v1/payments`, `POST /api/v1/payments/intents` | Idempotent sandbox payment intents; never captures funds |
| Market | `GET /api/v1/markets`, `GET /api/v1/markets/{marketId}` | India/global currency, locale, time zone, tax label, and available method configuration |
| Dashboard | `GET /api/v1/dashboard/summary` | Aggregated order, customer, product, payment, sales, and low-stock counts |
| All services | `GET /actuator/health` | Liveness/health information |

Amounts are represented as integer minor units with an ISO 4217 currency code.
Order state changes are validated; for example, a pending-payment order cannot
skip directly to delivery. Inventory rejects stock adjustments that would
make quantity negative. Payment intents accept an `Idempotency-Key` header and
remain in `SANDBOX_PENDING`.

## India and market portability

Market configuration is isolated in the Market service rather than embedded
throughout order/payment workflows. The example India market uses INR, GST as
a tax-system label, Asia/Kolkata, UPI, cards, netbanking, and COD. The example
US market uses USD, sales tax as a label, and cards/wallets. Add a market by
providing its locale, time zone, currency, tax policy, address/phone
validation, payment-provider and shipping adapters, and legal launch controls.

The current Market service is example configuration only. It does not calculate
GST, determine tax liability, validate Indian pincodes, create invoices,
perform live currency conversion, or enforce COD eligibility. Dashboard USD
display uses an illustrative fixed conversion and must not be used for
settlement or accounting.

For production payments, introduce a `PaymentProvider` port and provider
adapters for a contracted Indian PSP (for example, Razorpay, Cashfree, or PayU)
and other regions. Keep card collection provider-hosted/tokenized and outside
this application; implement signed webhook validation, capture/refund state
machines, reconciliation, and PCI scope review before processing real
payments.

## Security and production readiness

The default profile is for local development: domain API routes are public.
**Do not expose a default-profile service to an untrusted network.** Gateway and
services have an optional JWT/OIDC resource-server profile. For local
development against an OIDC issuer, set `SPRING_PROFILES_ACTIVE=secured` and
`JWT_ISSUER_URI` on the gateway and each service. The dashboard forwards the
incoming bearer token to its domain-service reads.

The customer service manages customer profiles and consent, not staff identities
or password-based login; use the configured OIDC identity provider for
authentication and implement the required application roles.

JWT authentication alone is not a complete production authorization policy.
Before launch, configure role/tenant-level authorization, token audience and
service identity, TLS, managed secrets, database migrations, backup and
recovery, rate limiting, audit logging, observability, and network isolation.
Review market-specific privacy, data residency, consumer, tax, and payment
regulation with qualified compliance professionals.

## Repository layout

```text
morrow-commerce/
├── backend/
│   ├── pom.xml                     # Maven reactor
│   ├── Dockerfile                  # Dashboard image
│   ├── Dockerfile.service          # Independently built service image
│   └── services/
│       ├── api-gateway/
│       ├── dashboard-service/
│       ├── customer-service/
│       ├── inventory-service/
│       ├── order-service/
│       ├── payment-service/
│       └── market-service/
├── src/                            # Vue dashboard
├── compose.yaml
└── README.md
```
