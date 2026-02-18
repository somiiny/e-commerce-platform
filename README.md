# 🛍 e-commerce platform

* Production-style e-commerce backend built with Spring Boot 3.
* Implements JWT-based authentication, role separation, and complex purchase/payment business logic.
  
<br>

## 📌 Overview

* This project simulates a production-level e-commerce backend system with clear separation between user and admin APIs.

* The system is designed around a realistic commerce domain model: User/Admin → Category → Product → Cart → Purchase → Payment

* It includes:
- Role-based access control (USER / ADMIN)
- JWT authentication with stateless security configuration
- Purchase and Payment cancellation business logic
- Repository-level dynamic queries using QueryDSL
- H2-based JPA testing with pagination validation

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

### ORM
- JPA (Hibernate)
- QueryDSL

### Documentation
- Swagger (Springdoc OpenAPI)

<br>

## 🧩 Domain Model

* The system is structured around realistic commerce domain relationships:

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
- **History (Purchase / Payment tracking)**

* The design emphasizes clear parent-child relationships and lifecycle management between purchase, payment, and status transitions.

<br>

## 🏗 Architecture

- Layered architecture (Controller → Service → Repository)
- Domain-driven entity relationships
- Separation of User and Admin endpoints
- Stateless JWT authentication
- QueryDSL for dynamic search and pagination

<br>

### 🔐 Authentication & Authorization

- JWT-based authentication using ID & password
- Token issued upon successful login
- Access token stored in Redis
- Role separation (USER / ADMIN)
- Stateless security configuration

<br>

### 🛒 Purchase Processing

* Two strategies are supported:
1. Purchase from cart
2. Direct purchase (Buy Now)

* Business logic includes:
- Purchase creation with validation
- Purchase cancellation logic
- Status transition management

<br>

### 💳 Payment Processing Flow

- External payment API integration
- Temporary payment amount stored in Redis
- Amount verification before approval
- Payment approval flow
- Payment cancellation logic

* Flow:
- Order Created → Payment Amount Cached (Redis) → Pre-approval Validation → Payment Approval

<br>

## ⚠ Exception Handling

- Custom ServiceException with enum-based error codes
- Centralized error response handling
- Meaningful business-specific error messages

<br>

## 🧪 Testing

### Service Layer
- Mockito-based unit testing
- Business logic isolation

### Repository Layer
- @DataJpaTest with H2
- Pagination validation
- Dynamic search condition testing

<br>

## 🧠 Technical Challenges



<br>

## 📖 API Documentation

<br>

## 📈 Future Improvements
- Plan to deploy the system on AWS with production-ready infrastructure setup (EC2, RDS, Redis, CI/CD).
