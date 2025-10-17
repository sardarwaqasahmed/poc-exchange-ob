
# POC Exchange Order Book

A Proof-of-Concept **Exchange Order Book** application implemented using **Java 21** and **Spring Boot 3.4**.  
This application handles **limit orders** (BUY/SELL) and executes trades based on **price-time priority**.

---

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 3.4
- **Build Tool:** Maven
- **Testing:** JUnit 5, Mockito
- **Logging:** SLF4J + Logback
- **API Documentation:** Swagger/OpenAPI 3 (`springdoc-openapi`)
- **Data Structures:** ConcurrentSkipListMap, Queue
- **IDE:** IntelliJ IDEA (with built-in code coverage)

---

## API Documentation

Interactive API documentation is available via **Swagger UI**:

```
http://localhost:8080/swagger-ui.html
```

Swagger UI allows you to:

- View all endpoints and request/response models
- Execute API requests directly from the browser
- Explore order book state and trades

---

## Endpoints

### 1. Place a Limit Order

```
POST /api/v1/orders
```

**Request Body Examples:**

```json
{
    "asset": "BTC",
    "price": 43251.00,
    "amount": 1.0,
    "direction": "SELL"
}
```


**Response Example:**

```json
{
  "order": {
    "id": 0,
    "timestamp": "2025-10-17T00:49:02.137585600Z",
    "asset": "BTC",
    "price": 43251.00,
    "amount": 1.0,
    "direction": "SELL",
    "pendingAmount": 1.0,
    "trades": []
  }
}
```

**Description:**

- Places a new **BUY** or **SELL** limit order.
- If a matching counterparty exists, trades are executed immediately.
- `pendingAmount` shows the remaining quantity to be filled.
- `trades` array lists executed trades for this order.

---

### 2. Get Current Order State

```
GET /api/v1/orders/{orderId}
```

**Path Parameter:**

- `orderId` – ID of the order to query

**Response Example (Order Found):**

```json
{
  "order": {
    "id": 0,
    "timestamp": "2025-10-17T00:49:02.137585600Z",
    "asset": "BTC",
    "price": 43251.00,
    "amount": 1.0,
    "direction": "SELL",
    "pendingAmount": 1.0,
    "trades": []
  }
}
```

**Response Example (Order Not Found):**

```json
{}
```

---

## How to Run

1. Clone the repository:

```bash
git clone https://github.com/yourusername/poc-exchange-ob.git
cd poc-exchange-ob
```

2. Build the project:

```bash
mvn clean install
```

3. Run the application:

```bash
mvn spring-boot:run
```

4. Open **Swagger UI** to test endpoints:

```
http://localhost:8080/swagger-ui.html
```

---

## Testing

- Unit tests are implemented using **JUnit 5** and **Mockito**.
- Run tests using:

```bash
mvn test
```

- IntelliJ **Run with Coverage** can be used to view code coverage.

---

## Order Matching Logic

- Orders are matched using **price-time priority**:
    - **Buy orders:** sorted descending by price
    - **Sell orders:** sorted ascending by price
- Partial fills are supported: remaining amounts are stored in the **order book**.
- Trades are recorded as soon as matching counterparty orders exist.


---
## Validation

The API performs **request parameter validation** for all endpoints. If the request contains invalid or missing parameters, the API responds with a structured **validation error** response.

**Example 1: Missing Asset**

```json
{
    "type": "validation-error",
    "title": "Your request parameters didn't validate.",
    "detail": "Request has invalid parameters",
    "invalid-params": [
        {
            "name": "asset",
            "reason": "Asset is mandatory"
        }
    ]
}
```

**Example 2: Invalid Price**

```json
{
    "type": "validation-error",
    "title": "Your request parameters didn't validate.",
    "detail": "Request has invalid parameters",
    "invalid-params": [
        {
            "name": "price",
            "reason": "Price must be greater than 0"
        }
    ]
}
```


**Example 3: Invalid Direction and Price**

```json
{
    "type": "validation-error",
    "title": "Your request parameters didn't validate.",
    "detail": "Request has invalid parameters",
    "invalid-params": [
        {
            "name": "direction",
            "reason": "Direction must be BUY or SELL"
        },
        {
            "name": "price",
            "reason": "Price must be greater than 0"
        }
    ]
}
```


**Example 4: Invalid Amount and Price**

```json
{
    "type": "validation-error",
    "title": "Your request parameters didn't validate.",
    "detail": "Request has invalid parameters",
    "invalid-params": [
        {
            "name": "amount",
            "reason": "Amount must be greater than 0"
        },
        {
            "name": "price",
            "reason": "Price must be greater than 0"
        }
    ]
}
```
