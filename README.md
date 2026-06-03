# 🚀 Enterprise Department-Employee Management System

A high-performance, stateless REST API engineered with Spring Boot 3, Java 17, and Hibernate/JPA. This application implements decoupled Data Transfer Objects (DTOs), transactional collection synchronization, role-based HTTP Basic Security, and comprehensive request validation tracking.

---

## 🏗️ Architectural Layout & Design Patterns

The project follows enterprise clean architecture standards to ensure maximum separation of concerns, scalability, and system security:

* **Layered Decoupling**: Direct database entities are strictly isolated from the controller tier. Network communication transits purely through dedicated Request/Response DTO layers mapped via `ModelMapper`.
* **Stateful Edge Validation**: Input values are trapped and sanitized at the application entry threshold using Jakarta Bean Validation constraints (`@Valid`, `@NotBlank`, `@Email`, `@Positive`).
* **Transactional Lifecycle Mapping**: Database actions are wrapped within transactional operational contexts (`@Transactional`). Relational updates leverage Hibernate's parent state tracking to maintain relational graph stability.
* **Unified Global Exception Middleware**: Unhandled business exceptions, parsing faults, or validation failures are automatically caught, translated, and structured into formal HTTP JSON payload contracts.

---

## 📊 Relational Database Schema Model

The persistence engine maps two primary data boundaries using an automated, bidirectional mapping scheme:

* **Cascade Constraint Rule**: Configured via `CascadeType.ALL` paired with `orphanRemoval = true`. Purging a parent department entity automatically cleanses and drops associated child employee references within a single transaction pipeline.
* **Heap Loop Protection**: Collections mappings are isolated with `@ToString.Exclude` blocks, preventing circular serialization patterns and isolating your environment from `StackOverflowError` exceptions.

---

## ✨ System Features & Core Guardrails

1. **Cross-Department Lockout Defense**: Custom repository logic validation (`existsByEmailAndDepartmentIdNot`) checks email data changes on updates. It allows employees to retain their emails during edits, but blocks cross-department data theft with a `409 Conflict` response.
2. **Hardened Pagination Guardrails**: The API intercepts query attempts and validates parameters against strict bounds ($\le0$, $<0$, or $>100$), shielding database memory pools from high-volume paging request overloads.
3. **Stateless Security Boundary**: Implements standard HTTP Basic access controls without saving processing history or cookies to application memory, optimizing the system for modern load-balanced container structures.

---

## 🛠️ System Prerequisites & Database Preparation

Before booting your Spring Boot runtime container, you must manually deploy an empty target database instance inside your local MySQL server environment.

1. Open your terminal database client, connection manager, or MySQL Workbench.
2. Execute the setup statement below:

```sql
CREATE DATABASE one_to_many_demo;
```

---

## 💻 Environment Configuration Profile (`application.properties`)

Verify that the local environment configuration profile matches your local infrastructure specifications inside `src/main/resources/application.properties`:

```properties
spring.application.name=06-one-to-many-mapping
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/one_to_many_demo?useSSL=false&serverTimezone=UTC
spring.datasource.username=root
spring.datasource.password=Puppy2107@

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

springdoc.swagger-ui.path=/swagger-ui.html
```

---

## 🔐 Credentials & Access Control Specification

The endpoint security layer enforces **HTTP Basic Authentication**. Before testing your requests, navigate to the **Authorization** tab in Postman, select **Basic Auth**, and apply one of the following testing accounts:


| Testing Account Username | Account Plaintext Password | Spring Security Role | System Clearance Level |
| :--- | :--- | :--- | :--- |
| `admin` | `admin123` | `ROLE_ADMIN` | Comprehensive access (Read, Write, Update, Delete) |
| `user` | `user123` | `ROLE_USER` | Read-only access (Restricted strictly to `GET` endpoints) |

---

## 🚀 Execution & Interactive Testing

To build your binaries and run the embedded application server, run the following commands within your terminal root folder:

```bash
mvn clean compile
mvn spring-boot:run
```

Once successfully initialized, full endpoint documentation and interface validation sandboxes are accessible at:
* **Interactive Open-API Document Interface:** `http://localhost:8080/swagger-ui.html`

---

## 📝 API Integration Test Playbook (Postman Sequence)

Follow this testing order sequentially to initialize matching entity records before triggering exception traps.

### 🟢 Phase 1: Authorized System Workflows (Positive Tests)
*Prerequisite: Configure Postman Authorization to Basic Auth using credentials -> admin / admin123*

#### 1. Dispatch Create Department 1 (Human Resources)
* **HTTP Method**: `POST`
* **Network Endpoint**: `http://localhost:8080/api/departments`

```json
{
  "department_name": "Human Resources",
  "location": "Building A",
  "employees": [
    {
      "employee_name": "Alice Johnson",
      "email": "alice@company.com",
      "salary": 60000.0
    },
    {
      "employee_name": "Bob Smith",
      "email": "bob@company.com",
      "salary": 65000.0
    }
  ]
}
```

#### 2. Dispatch Create Department 2 (Technology)
* **HTTP Method**: `POST`
* **Network Endpoint**: `http://localhost:8080/api/departments`

```json
{
  "department_name": "Technology",
  "location": "Building B",
  "employees": [
    {
      "employee_name": "Clark Kent",
      "email": "clark@company.com",
      "salary": 90000.0
    }
  ]
}
```

#### 3. Fetch Comprehensive Department Collection
* **HTTP Method**: `GET`
* **Network Endpoint**: `http://localhost:8080/api/departments`
* *Verification Note: This read-only execution is fully accessible by both user and admin roles.*

#### 4. Fetch Paginated Department Collection
* **HTTP Method**: `GET`
* **Network Endpoint**: `http://localhost:8080/api/departments/page?pageNumber=0&pageSize=5`

#### 5. Fetch Isolated Department Profile by Index Identifier
* **HTTP Method**: `GET`
* **Network Endpoint**: `http://localhost:8080/api/departments/1`

#### 6. Mutate Existing Department (Verifying Cross-Update Isolation Patches)
* **HTTP Method**: `PUT`
* **Network Endpoint**: `http://localhost:8080/api/departments/1`

```json
{
  "department_name": "Global HR Operations",
  "location": "Building A - Suite 100",
  "employees": [
    {
      "employee_name": "Alice Johnson",
      "email": "alice@company.com",
      "salary": 68000.0
    }
  ]
}
```

---

### 🔴 Phase 2: Error Boundaries & Validation Traps (Negative Tests)
*Prerequisite: Configure Postman Authorization to Basic Auth using credentials -> admin / admin123*

#### 7. Duplicate Unique Attribute Exception Trap
* **HTTP Method**: `POST`
* **Network Endpoint**: `http://localhost:8080/api/departments`
* **Expected HTTP Response**: `409 Conflict`

```json
{
  "department_name": "Global HR Operations",
  "location": "Building C",
  "employees": [
    {
      "employee_name": "New Hire",
      "email": "newhire@company.com",
      "salary": 50000.0
    }
  ]
}
```

#### 8. Cross-Department Email Ownership Validation Conflict
* **HTTP Method**: `PUT`
* **Network Endpoint**: `http://localhost:8080/api/departments/1`
* **Expected HTTP Response**: `409 Conflict`

```json
{
  "department_name": "Global HR Operations",
  "location": "Building A",
  "employees": [
    {
      "employee_name": "Alice Johnson",
      "email": "clark@company.com",
      "salary": 70000.0
    }
  ]
}
```

#### 9. Payload Field Requirement Constraint Triggers
* **HTTP Method**: `POST`
* **Network Endpoint**: `http://localhost:8080/api/departments`
* **Expected HTTP Response**: `400 Bad Request`

```json
{
  "department_name": "",
  "location": "Building Z",
  "employees": [
    {
      "employee_name": "Bad Payload",
      "email": "not-an-email",
      "salary": -500.0
    }
  ]
}
```

#### 10. Malformed Payload Syntax Exception Trap
* **HTTP Method**: `POST`
* **Network Endpoint**: `http://localhost:8080/api/departments`
* **Expected HTTP Response**: `400 Bad Request`

```json
{
  "department_name": "Finance",
  "location": "Building F
}
```

#### 11. Pagination Parameter Range Breach
* **HTTP Method**: `GET`
* **Network Endpoint**: `http://localhost:8080/api/departments/page?pageNumber=-1&pageSize=0`
* **Expected HTTP Response**: `400 Bad Request`

#### 12. Non-Existent Resource Target
* **HTTP Method**: `GET`
* **Network Endpoint**: `http://localhost:8080/api/departments/999`
* **Expected HTTP Response**: `404 Not Found`

#### 13. Security Clearance Authorization Block Check
* **Prerequisite**: Set Postman Auth credentials to -> user / user123
* **HTTP Method**: `DELETE`
* **Network Endpoint**: `http://localhost:8080/api/departments/1`
* **Expected HTTP Response**: `403 Forbidden`
