# 🍽️ Restaurant Management System

A full-stack restaurant management app built with **React + Spring Boot**. It supports multiple restaurants, role-based dashboards, order management, and Stripe payment.

---

## Features

* JWT authentication (Admin / Restaurant Owner / Customer)
* Multi-vendor system (restaurant registration & approval)
* Menu management (add/update/delete food items)
* Browse restaurants & food items with search/filter
* Orders split per restaurant
* Single Stripe payment for multiple orders
* Dashboards for admin, restaurant owners, and customers
* Auto UI updates after actions (no manual refresh)

---

## Tech Stack

* Frontend: React
* Backend: Spring Boot, Spring Security, JPA
* Database: PostgreSQL
* Payment: Stripe

---

## Run Locally

### Backend

Update DB config in `application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/your_db
    username: your_username
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
```

Run:

```bash
./mvnw spring-boot:run
```

### Frontend

```bash
npm install
npm run dev
```

---

## Notes

* Works on any machine with proper DB config
* Seed data is added on startup
* Stripe test mode supported

---


