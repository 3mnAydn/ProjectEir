# Project EIR – Requirements Document

## 1. Functional Requirements

### Module 0: Identity & Authorization (Auth/IAM)

| Code | Description |
|-----|----------|
| FR-0.1 | **Role-Based Login**: Admin, Doctor, Desk Officer etc. log in with email + password and are redirected according to their role. |
| FR-0.2 | **Password Operations**: Reset via "Forgot Password" flow through a secure email link; password change from within the system. |
| FR-0.3 | **Secure Logout**: Manual session termination; automatic logout after 30 minutes of inactivity. |

### Module 1: Patient Registration & Management

| Code | Description |
|-----|----------|
| FR-1.1 | **Patient Registration**: Required fields: Turkish ID No (unique), First Name, Last Name, Date of Birth, Gender, Phone, Address. |
| FR-1.2 | **Patient Search**: Search by Turkish ID No or First Name + Last Name combination. |
| FR-1.3 | **Profile Update**: Update contact information. |

### Module 2: Appointment Management

| Code | Description |
|-----|----------|
| FR-2.1 | **Work Schedule**: Store doctors' weekly working days and time slots (e.g., 15-minute intervals). |
| FR-2.2 | **Appointment Creation**: Follow steps: Polyclinic → Doctor → Date → Time Slot. |
| FR-2.3 | **Appointment Status & Notification**: Statuses: Pending, Completed, Cancelled, No Show. Send automatic SMS/Email notification on cancellation or change. |

### Module 3: Doctor Examination & Medical Process

| Code | Description |
|-----|----------|
| FR-3.1 | **Active Appointments**: Doctor sees today's appointments in chronological order on their dashboard. |
| FR-3.2 | **Examination/Anamnesis**: Text input for complaint, history, and findings during examination. |
| FR-3.3 | **Diagnosis/ICD-10**: Search and add diagnosis using global ICD-10 codes. |
| FR-3.4 | **Prescription Writing**: Add medication with dosage and frequency; generate a prescription number. |
| FR-3.5 | **Lab/Test Request**: Request laboratory (blood, urine) or radiology (X-ray, MRI) tests during examination. |

### Module 4: AI-Assisted Triage

| Code | Description |
|-----|----------|
| FR-4.1 | **Symptom Input**: Free text entry (e.g., "severe headache"). |
| FR-4.2 | **Smart Matching**: Process text and route patient to appropriate polyclinics with a confidence score (%). |

### Module 5: Billing & Finance

| Code | Description |
|-----|----------|
| FR-5.1 | **Pricing**: Automatic pricing based on defined fee schedules for examinations, lab tests, and radiology. |
| FR-5.2 | **Invoice/Payment**: Cashier handles collection under accountant supervision; generate invoice or receipt. |

---

## 2. Non-Functional Requirements (NFR)

### 2.1 Architecture & Technology

| Code | Description |
|-----|----------|
| NFR-1.1 | **Microservice Architecture**: Multiple languages working together. |
| NFR-1.2 | **Frontend**: TypeScript + Angular. |
| NFR-1.3 | **Gateway**: Spring Boot WebFlux Gateway. |
| NFR-1.4 | **Backend Services**: Spring Boot, Laravel, Python. |
| NFR-1.5 | **Synchronous Communication**: gRPC + Protocol Buffers. |
| NFR-1.6 | **Service Discovery**: Eureka/Consul + Client-Side Load Balancing. |
| NFR-1.7 | **Documentation**: Kruchten 4+1, UML diagrams. |

### 2.2 Performance & Scalability

| Code | Description |
|-----|----------|
| NFR-2.1 | Support 1000 concurrent users. |
| NFR-2.2 | Standard API response ≤ 2 seconds. |
| NFR-2.3 | AI service response ≤ 3 seconds. |
| NFR-2.4 | Redis in-memory caching for frequently read data. |
| NFR-2.5 | **AI Model**: Local (Local LLM / Clinical BERT), no third-party cloud services (KVKK compliance). |

### 2.3 Security (OWASP Top 10)

| Code | Description |
|-----|----------|
| NFR-3.1 | **Defense in Depth**: Gateway → Auth Service → Internal Services (3 layers). |
| NFR-3.2 | **RBAC**: Strict role-based authorization at API Gateway level. |
| NFR-3.3 | **HTTPS/TLS 1.3** for all data in transit; AES-256 field-level encryption for PII in database. |
| NFR-3.4 | **ORM only**, raw SQL prohibited; strict input validation via Spring Validation + Laravel FormRequests. |
| NFR-3.5 | **Stateless JWT**: Short-lived Access Token + rotating Refresh Token. |
| NFR-3.6 | **Rate Limiting**: Redis-based IP and user rate limiting at Gateway level. |

### 2.4 Availability, Logging & Disaster Recovery

| Code | Description |
|-----|----------|
| NFR-4.1 | %99.9 uptime target. |
| NFR-4.2 | All CUD (Create, Update, Delete) operations logged with IP, timestamp, and user info. |
| NFR-4.3 | Daily full + hourly incremental backups; RPO ≤ 1 hour, RTO ≤ 4 hours. |

### 2.5 Database & Data Management

| Code | Description |
|-----|----------|
| NFR-5.1 | Hard delete prohibited; soft delete (`deleted_at`) required. |
| NFR-5.2 | Prevent N+1 queries; index frequently queried columns. |
| NFR-5.3 | Mandatory pagination for all multi-record API responses. |
| NFR-5.4 | Redis for temporary data only; persistent data stays in RDBMS. |
| NFR-5.5 | Data older than 5 years moved to cold storage. |

### 2.6 Development Processes (DevOps)

| Code | Description |
|-----|----------|
| NFR-6.1 | Development / Staging / Production environments; CI/CD pipeline (Blue-Green / Rolling Deployment). |
| NFR-6.2 | **Language**: Turkish (tr-TR); timestamps stored in UTC (DB), displayed in UTC+3 (frontend). |
