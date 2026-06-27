# 🏥 Project Eir: Microservices-Based Hospital ERP System

![Version](https://img.shields.io/badge/version-1.0.0-blue.svg)
![Architecture](https://img.shields.io/badge/architecture-Microservices-orange)
![License](https://img.shields.io/badge/license-MIT-green)

**Project Eir** is a modern, scalable, and highly available Hospital Enterprise Resource Planning (ERP) system. Built with a strict adherence to Software Engineering principles, this project demonstrates the implementation of complex domain logic using modular microservices, clean architecture, and advanced design patterns.

## 🚀 Overview

The primary goal of Project Eir is to orchestrate hospital operations—ranging from appointment scheduling to billing and patient management—through a robust, event-driven microservices architecture. 

Rather than jumping straight into code, this system was designed ground-up with comprehensive UML modeling (referencing Kruchten's 4+1 View Model), focusing on:
* **High Availability:** Targeting 99.9% uptime.
* **Security:** Implementing OWASP standards and "Defense in Depth" strategies.
* **Scalability:** Independent scaling of domain-specific services.

## 🏗️ Architecture & Tech Stack

Project Eir utilizes a polyglot microservices approach, choosing the best tool for each specific domain logic.

| Layer | Technology | Description |
| :--- | :--- | :--- |
| **Frontend** | Angular | SPA for hospital staff and patient portals. |
| **API Gateway** | Spring WebFlux | Reactive gateway handling routing, rate limiting, and JWT validation. |
| **Core Services** | Spring Boot | Handles complex business logic (e.g., Appointments, Users). |
| **Financial Services** | Laravel | Manages billing, invoicing, and payment strategies. |
| **Analytics/AI** | Python | Reserved for data processing and potential predictive modules. |
| **Async Comm.** | RabbitMQ | Event broker for decoupled inter-service communication. |
| **Sync Comm.** | gRPC | High-performance synchronous internal communication. |
| **Database** | PostgreSQL / MySQL | Relational databases with Soft-Delete and Audit Logging. |

## 🎨 Design Patterns Utilized

To keep the domain logic isolated, maintainable, and scalable, several core design patterns are heavily utilized across the services:

* **State Pattern:** Manages the complex lifecycle of an `Appointment` (e.g., Pending, Confirmed, InConsultation, Completed) and API Gateway request states.
* **Strategy Pattern:** Isolates payment processing methods (e.g., `CreditCardPayment`, `InsurancePayment`) within the Billing service.
* **Mediator Pattern:** Centralizes event orchestration and dispatching via `CentralMediator` and RabbitMQ to prevent tight coupling between services.

## 📂 Project Structure

```text
project-eir/
├── eir-gateway/             # Spring WebFlux API Gateway & Auth Filters
├── eir-auth-service/        # Authentication and RBAC Management
├── eir-appointment-service/ # Spring Boot - Appointment & State Logic
├── eir-billing-service/     # Laravel - Invoicing & Payment Strategies
├── eir-patient-service/     # Patient Records & History
├── eir-frontend/            # Angular User Interface
└── docs/                    # UML Diagrams, SRS, and Architecture Notes
