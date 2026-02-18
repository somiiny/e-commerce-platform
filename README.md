# 🛍 E-Commerce Platform

Production-style e-commerce backend built with **Spring Boot 3**.  
Implements JWT-based authentication, role separation, and complex purchase/payment business logic.

<br>

## 📌 Overview

This project simulates a production-level e-commerce backend system with:

- Clear separation between **User** and **Admin** APIs
- Realistic commerce domain modeling
- Secure stateless authentication
- Dynamic search and pagination
- Robust business validation logic

### 🧱 Core Domain Flow

User/Admin → Category → Product → Cart → Purchase → Payment

The system focuses on lifecycle management, status transitions, and business integrity between purchase and payment processes.

<br>

## 🛠 Tech Stack

### Backend
- Java 17
- Spring Boot 3

### Data & Persistence
- MySQL (Production)
- H2 (Test)
- Redis
- Flyway

### Security
- Spring Security
- JWT (Access / Refresh Token)

### ORM & Query
- JPA (Hibernate)
- QueryDSL

### Documentation
- Swagger (Springdoc OpenAPI)

<br>

## 🧩 Domain Model

The application is structured around realistic commerce relationships:

- **User / Admin**
- **Category**
- **Product**
  - ProductImage
  - ProductOption
- **Cart**
  - CartProduct
- **Purchase**
  - PurchaseProduct
- **Payment**
- **History** (Purchase / Payment tracking)

The design emphasizes:
- Clear parent-child entity relationships
- Soft deletion handling
- Status transition management
- Business rule consistency

<br>

## 🏗 Architecture

- Layered architecture (Controller → Service → Repository)
- Domain-driven entity modeling
- Stateless JWT authentication
- QueryDSL-based dynamic query handling
- Role-based endpoint separation

<br>

## 🔐 Authentication & Authorization

- JWT-based authentication using email & password
- Access & Refresh tokens issued on login
- Refresh tokens stored in Redis with TTL
- Access tokens blacklisted upon logout
- Role separation: `USER` / `ADMIN`
- Stateless security configuration

<br>

## 🛒 Purchase Processing

Two purchase strategies are supported:

1. Purchase from cart
2. Direct purchase (Buy Now)

Business logic includes:

- Purchase validation
- Status transition management
- Purchase cancellation flow
- Data integrity enforcement

<br>

## 💳 Payment Processing Flow

Integrated with an external payment API.

### Payment Flow

Order Created  
→ Payment Amount Cached (Redis)  
→ Pre-approval Amount Validation  
→ Payment Approval  
→ Payment Cancellation (if needed)

Key features:

- Temporary amount storage in Redis
- Amount verification before approval
- Secure cancellation handling

<br>

## ⚠ Exception Handling

- Custom `ServiceException` with enum-based error codes
- Centralized exception response handling
- Business-specific error messaging

<br>

## 🧪 Testing Strategy

### Service Layer
- Mockito-based unit testing
- Business logic isolation

### Repository Layer
- `@DataJpaTest` with H2
- Pagination validation
- Dynamic search condition testing
- QueryDSL query verification

<br>

## 🧠 Technical Challenges

### 1️⃣ JWT Token Management with Redis

#### ❓ Problem

JWT is stateless by design, meaning tokens are not stored on the server.

This creates challenges:

- Handling logout in a stateless system  
- Preventing reuse of revoked tokens  
- Managing refresh token lifecycle securely  

#### 💡 Solution

Redis was introduced as an in-memory token store:

- Refresh tokens stored with TTL
- Access tokens blacklisted upon logout
- Token validation includes:
  - Signature verification
  - Expiration check
  - Blacklist lookup

#### 🚀 Result

- Maintained stateless architecture
- Enabled secure logout
- Prevented token reuse
- Reduced database load via in-memory storage

---

### 2️⃣ H2 Reserved Keyword Conflict in Test Environment

#### ❓ Problem

H2 treats `user` as a reserved keyword, causing SQL syntax errors during repository tests.

#### 🧠 Challenge

Resolve the issue without modifying the production (MySQL) schema.

#### 🛠 Solution

- Configured H2 with `MODE=MYSQL`
- Added `NON_KEYWORDS=USER`
- Separated test configuration using `test` profile
- Created H2-specific datasource & JPA settings

#### 📈 Result

- Production and test environments fully isolated
- Stable `@DataJpaTest` execution
- Improved test reliability

<br>

## 📖 API Documentation

🔗 https://somiiny.github.io/e-commerce-swagger-docs/#/

<br>

## 📈 Future Improvements
- Plan to deploy the system on AWS with production-ready infrastructure setup (EC2, RDS, Redis, CI/CD).
