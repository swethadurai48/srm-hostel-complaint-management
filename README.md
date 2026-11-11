# 🏛️ SRM Hostel Smart Complaint Management & Enterprise ERP Portal

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%2B-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=hibernate&logoColor=white)](https://hibernate.org/)
[![Frontend](https://img.shields.io/badge/Frontend-Enterprise%20Vanilla%20ES6%2B-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![GitHub](https://img.shields.io/badge/Repository-GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/swethadurai48/srm-hostel-complaint-management.git)

A unified, modern **Enterprise ERP Portal & Hostel Complaint Management System** designed for SRM Institute of Science and Technology. Built on top of Spring Boot 3.3.3 and PostgreSQL, the platform bridges student resident complaints with an enterprise-grade ERP workflow encompassing Human Resource Management (HRM), Department Administration, Multi-tier Approvals, Automated SLA Tracking, Real-time Business Intelligence, and Granular Role-Based Access Control (RBAC).

---

## 📑 Table of Contents

- [Overview](#-overview)
- [Enterprise System Architecture](#-enterprise-system-architecture)
- [ERP Platform Modules](#-erp-platform-modules)
  - [1. Executive ERP Dashboard](#1-executive-erp-dashboard)
  - [2. Complaint & Incident Lifecycle](#2-complaint--incident-lifecycle)
  - [3. Human Resource Management (HRM)](#3-human-resource-management-hrm)
  - [4. Department Management](#4-department-management)
  - [5. Workflow & Approval Engine](#5-workflow--approval-engine)
  - [6. Reports & Analytics (BI)](#6-reports--analytics-bi)
  - [7. Role-Based Access Control (RBAC)](#7-role-based-access-control-rbac)
  - [8. Real-Time Notification Center](#8-real-time-notification-center)
  - [9. System Settings & Enterprise Audit Logs](#9-system-settings--enterprise-audit-logs)
- [UI/UX Design System (SAP Fiori / Modern SaaS)](#-uiux-design-system)
- [Technology Stack](#-technology-stack)
- [Project Directory Structure](#-project-directory-structure)
- [Database Schema & Data Model](#-database-schema--data-model)
- [Default System Credentials](#-default-system-credentials)
- [Installation & Getting Started](#-installation--getting-started)
- [REST API Specifications](#-rest-api-specifications)
- [Testing & Quality Assurance](#-testing--quality-assurance)
- [Security & Credential Management](#-security--credential-management)
- [Troubleshooting & FAQ](#-troubleshooting--faq)

---

## 🌟 Overview

The platform operates as a two-sided enterprise system:
1. **Resident Self-Service Portal (`/student.html`)**: Simple, fast ticket submission for room maintenance, plumbing, electrical, Wi-Fi, and dining issues with instant live tracking and ratings.
2. **Administration Portal (`/admin.html`)**: Fast triage interface for wardens to filter, respond, and update ticket lifecycle states.
3. **Enterprise ERP Portal (`/erp.html`)**: Full-scale operational platform featuring cross-department routing, employee workload balancing, procurement approvals, SLA escalations, CSV reporting, and tamper-evident audit trails.

---

## 🏛️ Enterprise System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                                 PRESENTATION LAYER (Browser)                            │
│  Resident Portal (/student.html) │ Warden Portal (/admin.html) │ Enterprise ERP (/erp.html)│
│  ├── SAP Fiori / Linear Inspired Design System (erp.css, theme-dark / theme-light)      │
│  ├── Interactive Dynamic Views (Dashboard, Complaints, HRM, Departments, Workflows, BI) │
│  └── Global Command Palette (Cmd+K / Ctrl+K) & Live Notification Center                 │
└────────────────────────────────────────────┬────────────────────────────────────────────┘
                                             │ HTTP / JSON (Secure Cookie Sessions)
                                             ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                        SPRING BOOT 3.3.3 APPLICATION LAYER (Port 8080)                  │
│                                                                                         │
│  [REST Controllers]                                                                     │
│   ├── AuthController (/api/auth)                                                        │
│   ├── ComplaintController & AdminComplaintController (/api/complaints, /api/admin/...)  │
│   ├── ErpDashboardController (/api/erp/dashboard, /api/erp/reports/export-csv)          │
│   ├── DepartmentController (/api/erp/departments)                                       │
│   ├── EmployeeController (/api/erp/employees)                                           │
│   ├── WorkflowController (/api/erp/complaints/{id}/assign, /escalate, /approvals, etc.) │
│   ├── NotificationController (/api/erp/notifications)                                   │
│   ├── AuditController (/api/erp/audit-logs)                                             │
│   └── UserManagementController (/api/erp/users)                                         │
│                                                                                         │
│  [Enterprise Service Layer]                                                             │
│   ├── UserService (Salted SHA-256 Hashing, RBAC verification, Session Security)         │
│   ├── ComplaintService (Fast Triage, Department Auto-Routing, Status Lifecycle)         │
│   ├── DepartmentService (Department hierarchy, budgets, workload metrics)               │
│   ├── EmployeeService (Staff directory, skill taxonomy, task allocation)                │
│   ├── WorkflowService (Assignments, SLA countdowns, escalations, cost approvals)        │
│   ├── AnalyticsService (Category & Priority aggregations, SLA compliance, CSV export)   │
│   ├── NotificationService (In-app notifications, unread badge counter)                  │
│   └── AuditService (Timestamped action logs for regulatory compliance)                  │
│                                                                                         │
│  [Data Access Layer (Spring Data JPA / Hibernate 6.5)]                                  │
│   ├── 14 Repositories: User, Complaint, Department, Employee, Assignment, Comment,       │
│   │   Attachment, History, Approval, Notification, AuditLog, SlaConfiguration, etc.     │
└────────────────────────────────────────────┬────────────────────────────────────────────┘
                                             │ JDBC (HikariCP Connection Pool)
                                             ▼
┌─────────────────────────────────────────────────────────────────────────────────────────┐
│                        DATABASE LAYER (PostgreSQL 14+ / 16)                             │
│   Database: smart_complaint_db                                                          │
│   Entities: users, complaints, departments, employees, complaint_assignments,           │
│             complaint_comments, complaint_attachments, complaint_history, approvals,     │
│             notifications, audit_logs, sla_configurations, feedback, notices            │
└─────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 💼 ERP Platform Modules

### 1. Executive ERP Dashboard
- **Real-Time KPI Cards**: Total Tickets, Open Tickets, Work in Progress, Resolved, Overdue Tickets, Escalations, Active Staff, and Department count.
- **SLA Compliance Widget**: Live percentage calculation of tickets resolved within SLA target deadlines.
- **Interactive Visualizations**:
  - Category Distribution Chart (Water, Electricity, Network, Maintenance, Housekeeping).
  - Priority Breakdown (Low, Medium, High, Urgent).
  - Department Workload & Budget Breakdown with direct status bars.
- **Activity Stream**: Live chronological feed of recent ticket events, employee assignments, and supervisor decisions.

### 2. Complaint & Incident Lifecycle
- **End-to-End Tracking**: Full state machine (`PENDING` ➔ `IN_PROGRESS` ➔ `RESOLVED`).
- **Department Routing**: Automatic or manual routing to responsible facility department (Electrical, Plumbing, IT Network, etc.).
- **Technician Assignment**: Assign qualified staff members with specific internal instructions.
- **Internal Notes & Comments**: Public comments visible to residents and internal notes restricted to staff.
- **Audit History**: Complete chronological history of status transitions, previous values, and operator IDs.
- **Cost Estimation**: Track expenditure estimates for parts and labor.

### 3. Human Resource Management (HRM)
- **Employee Directory**: Manage staff records with system-generated IDs (e.g. `EMP-ELEC-01`, `EMP-PLUMB-01`).
- **Department Assignment**: Link technicians to facility departments.
- **Skill Taxonomy**: Track specialized capabilities (e.g. "High voltage wiring", "Booster pumps", "Fiber splicing").
- **Workload Tracking**: Real-time metrics on `assignedTasksCount` and `resolvedTasksCount`.
- **Status Control**: Toggle employee availability (`ACTIVE`, `ON_LEAVE`, `INACTIVE`).

### 4. Department Management
- **Department Catalog**: Pre-configured campus divisions:
  - `ELEC` — Electrical & Power Systems
  - `PLUMB` — Plumbing & Water Infrastructure
  - `NET` — IT & Campus Network
  - `HOUSE` — Housekeeping & Sanitation
  - `MAINT` — Civil & Room Maintenance
  - `MESS` — Dining & Mess Quality
  - `SEC` — Hostel Security & Safety
- **Management & Oversight**: Designate Department Managers, official contact emails, campus telephone extensions, and allocated operating budgets.
- **Performance Metrics**: Department-specific complaint resolution rates, pending queues, and staff counts.

### 5. Workflow & Approval Engine
- **Supervisor Approvals**: Request and approve maintenance expenditures or equipment replacement requisitions.
- **SLA Breach Escalations**: Automatically mark and escalate overdue tickets directly to department heads with priority elevation.
- **Resolution Verification**: Verification trail before closing critical campus infrastructure issues.

### 6. Reports & Analytics (BI)
- **SLA Performance Metrics**: Real-time compliance tracking across all departments.
- **Resolution Velocity**: Mean time to resolve per category.
- **One-Click CSV Export**: Download complete complaints ledger (`/api/erp/reports/export-csv`) with columns: Ticket ID, Title, Category, Department, Priority, Status, Resident, Location, Staff, Timestamps, and Cost Estimates.

### 7. Role-Based Access Control (RBAC)
Granular 5-tier role hierarchy:
1. `SUPER_ADMIN`: Full system access, audit log review, role configuration, department budget management.
2. `ADMIN`: Warden and general facility operations, ticket assignment, employee dispatch.
3. `DEPT_MANAGER`: Department-specific triage, approval authorizations, technician workload management.
4. `EMPLOYEE`: Field technician view, ticket status progression, task notes.
5. `COMPLAINT_REPORTER` / `STUDENT`: Ticket lodging, personal status tracking, satisfaction rating.

### 8. Real-Time Notification Center
- Dropdown notification flyout for new assignments, status updates, approval results, and escalations.
- Unread badge counter with one-click "Mark All as Read" API integration.

### 9. System Settings & Enterprise Audit Logs
- **Tamper-Evident Audit Logging**: Every ticket assignment, status modification, internal note, department edit, and employee registration is logged with user identity, timestamp, and action description.
- **SLA Target Management**: Configurable resolution timeframes by ticket category (e.g. Electrical: 24h, Plumbing: 24h, Wi-Fi: 12h, Housekeeping: 8h).

---

## 🎨 UI/UX Design System

The ERP interface is designed following **SAP Fiori**, **Linear**, and **Microsoft Dynamics 365** design principles:
- **Clean Sidebar Navigation**: Collapsible sidebar with active module indicators, badge counts, and user profile card.
- **Dark / Light Theme Toggle**: Seamless CSS variable switching with persistent theme memory.
- **Quick Command Palette (`Cmd+K` / `Ctrl+K`)**: Rapid keyboard-driven search and navigation across all modules.
- **Enterprise Data Tables**: Structured, sortable tables with status badges, action buttons, and responsive overflow handling.
- **Zero Heavy External Frameworks**: Fast, lightweight ES6+ native implementation without bloated third-party dependencies.

---

## 🛠️ Technology Stack

| Component | Technology | Description |
| :--- | :--- | :--- |
| **Backend Framework** | Spring Boot 3.3.3 | Enterprise RESTful service architecture |
| **Language** | Java 17 (OpenJDK) | Strongly typed business logic |
| **Database** | PostgreSQL 14+ / 16 | Relational data store with ACID guarantees |
| **ORM & Data Access** | Hibernate 6.5 / Spring Data JPA | Entity management, JPQL queries, connection pooling via HikariCP |
| **Frontend** | HTML5, CSS3, ES6+ JavaScript | Modern vanilla frontend, SAP Fiori / SaaS styling |
| **Security** | Salted SHA-256 & Session Cookies | Secure credential storage, HttpOnly session handling, RBAC |
| **Build & Tooling** | Apache Maven 3.9+ | Build lifecycle, dependency management, automated tests |

---

## 📁 Project Directory Structure

```
smart-complaint-management-system/
├── pom.xml
├── mvnw / mvnw.cmd
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/smartcomplaint/
│   │   │   ├── SmartComplaintApplication.java
│   │   │   ├── config/
│   │   │   │   └── DataInitializer.java           # Seeds default SLA rules, departments, staff
│   │   │   ├── controller/
│   │   │   │   ├── AuthController.java            # Login, registration, session /me
│   │   │   │   ├── ComplaintController.java       # Resident ticket submission & my tickets
│   │   │   │   ├── AdminComplaintController.java  # Fast triage & warden updates
│   │   │   │   ├── ErpDashboardController.java    # Real-time metrics & CSV report export
│   │   │   │   ├── DepartmentController.java      # Department CRUD & statistics
│   │   │   │   ├── EmployeeController.java        # HRM staff CRUD & allocations
│   │   │   │   ├── WorkflowController.java        # Assignments, escalations, approvals, history
│   │   │   │   ├── NotificationController.java    # Notification center & read receipts
│   │   │   │   ├── AuditController.java           # Enterprise audit log records
│   │   │   │   ├── UserManagementController.java  # RBAC user management
│   │   │   │   ├── NoticeController.java          # Hostel announcements
│   │   │   │   └── FeedbackController.java        # Resident star ratings
│   │   │   ├── dto/                               # Request/response data transfer objects
│   │   │   ├── model/                             # 14 JPA entities & enums
│   │   │   ├── repository/                        # 14 Spring Data JPA repositories
│   │   │   └── service/                           # Business logic & transaction boundaries
│   │   └── resources/
│   │       ├── application.properties             # Database connection, port & JPA settings
│   │       └── static/
│   │           ├── index.html                     # Resident landing & authentication
│   │           ├── student.html                   # Student resident ticket dashboard
│   │           ├── admin.html                     # Warden fast triage portal
│   │           ├── erp.html                       # Enterprise ERP Platform
│   │           ├── css/
│   │           │   ├── style.css                  # SRM resident & warden styling
│   │           │   └── erp.css                    # Modern enterprise ERP design system
│   │           ├── js/
│   │           │   ├── api.js                     # Unified API client & session handler
│   │           │   ├── auth.js                    # Landing authentication logic
│   │           │   ├── student.js                 # Resident interactions
│   │           │   ├── admin.js                   # Warden triage interactions
│   │           │   └── erp.js                     # Full ERP module controller
│   │           └── images/
│   │               └── srm-logo.png               # Official SRMIST insignia
│   └── test/
│       └── java/com/smartcomplaint/
│           └── SmartComplaintApplicationTests.java # Automated integration test suite
```

---

## 🗄️ Database Schema & Data Model

The PostgreSQL database (`smart_complaint_db`) utilizes a normalized relational schema:

- **`users`**: Resident students, maintenance technicians, department managers, and administrators (`id`, `name`, `email`, `password_hash`, `role`, `student_id`, `department`, `room_or_hostel`).
- **`departments`**: Facility divisions (`id`, `code`, `name`, `description`, `manager_id`, `contact_email`, `contact_phone`, `budget`).
- **`employees`**: Staff profiles (`id`, `employee_code`, `user_id`, `department_id`, `name`, `email`, `phone`, `designation`, `status`, `skills`, `assigned_tasks_count`, `resolved_tasks_count`).
- **`complaints`**: Core ticket registry (`id`, `title`, `description`, `category`, `status`, `priority`, `location`, `assigned_staff`, `department_id`, `student_id`, `sla_due_date`, `is_escalated`, `cost_estimate`, `created_at`, `resolved_at`).
- **`complaint_assignments`**: Worker dispatch ledger (`id`, `complaint_id`, `employee_id`, `assigned_by`, `assigned_at`, `completed_at`, `status`, `notes`).
- **`complaint_comments`**: Discussion thread (`id`, `complaint_id`, `author_id`, `message`, `is_internal`, `created_at`).
- **`complaint_history`**: Audit trail (`id`, `complaint_id`, `changed_by`, `action`, `old_value`, `new_value`, `notes`, `timestamp`).
- **`approvals`**: Financial and operational requisitions (`id`, `complaint_id`, `requester_id`, `approver_id`, `approval_type`, `amount`, `reason`, `status`, `comments`, `requested_at`, `decided_at`).
- **`notifications`**: Targeted user alerts (`id`, `user_id`, `title`, `message`, `type`, `target_id`, `is_read`, `created_at`).
- **`audit_logs`**: System security ledger (`id`, `user_id`, `action`, `resource`, `ip_address`, `details`, `timestamp`).
- **`sla_configurations`**: Resolution target matrix (`id`, `category`, `priority`, `resolution_target_hours`, `escalation_threshold_hours`).

---

## 🔑 Default System Credentials

| Role | Account / Email | Password | Intended Portal |
| :--- | :--- | :--- | :--- |
| **System Admin** | `r.haresh11.8.2007@gmail.com` | `SRM@484` | `/erp.html` & `/admin.html` |
| **Student Resident** | `student@campus.edu` | `SRM@484` | `/student.html` |

*Additional student accounts:*
- `haresh23@srmist.edu.in` (`SRM@484`)
- `rithika131313@gmail.com` (`SRM@484`)
- `st0187@srmist.edu.in` (`SRM@484`)

---

## 🚀 Installation & Getting Started

### 1. Prerequisites
- **Java Development Kit (JDK 17 or higher)**
- **PostgreSQL 14+ or 16**
- **Git**

### 2. Clone the Repository
```bash
git clone https://github.com/swethadurai48/srm-hostel-complaint-management.git
cd srm-hostel-complaint-management
```

### 3. PostgreSQL Database Setup
```bash
# Start PostgreSQL service (macOS Homebrew example)
brew services start postgresql@16

# Create application database
psql -U postgres -c "CREATE DATABASE smart_complaint_db;"
```

### 4. Database Configuration (`src/main/resources/application.properties`)
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/smart_complaint_db
spring.datasource.username=postgres
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
server.port=8080
```

### 5. Build and Launch
```bash
# Compile and run test suite
./mvnw clean test

# Start the Spring Boot application
./mvnw spring-boot:run
```

Once started, open your web browser:
- **Landing & Resident Portal**: [http://localhost:8080](http://localhost:8080)
- **Enterprise ERP Platform**: [http://localhost:8080/erp.html](http://localhost:8080/erp.html)
- **Warden Triage Portal**: [http://localhost:8080/admin.html](http://localhost:8080/admin.html)

---

## 📡 REST API Specifications

### Authentication & Sessions
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/auth/register` | Register new resident student |
| `POST` | `/api/auth/login` | Authenticate and create secure session |
| `POST` | `/api/auth/logout` | Terminate session |
| `GET` | `/api/auth/me` | Retrieve active authenticated user profile |

### Core Complaints
| Method | Endpoint | Description |
| :--- | :--- | :--- |
| `POST` | `/api/complaints` | Submit new resident complaint |
| `GET` | `/api/complaints/my` | List authenticated student's tickets |
| `GET` | `/api/complaints/my/stats` | Student KPI ticket metrics |
| `GET` | `/api/admin/complaints` | Filter complaints by `?category=` & `?status=` |
| `PUT` | `/api/admin/complaints/{id}/status` | Update status & assign staff |
| `PUT` | `/api/admin/complaints/{id}/response`| Add official warden response |

### Enterprise ERP Endpoints
| Method | Endpoint | Access | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/erp/dashboard` | Admin / Staff | Executive KPI metrics, charts, department workloads |
| `GET` | `/api/erp/reports/export-csv` | Admin / Staff | Download full complaints ledger as CSV |
| `GET` | `/api/erp/departments` | Authenticated | List all facility departments |
| `POST` | `/api/erp/departments` | Admin | Create department |
| `PUT` | `/api/erp/departments/{id}` | Admin | Update department info, manager, or budget |
| `DELETE`| `/api/erp/departments/{id}` | Admin | Delete department (with validation checks) |
| `GET` | `/api/erp/departments/{id}/stats` | Admin | Department-specific performance metrics |
| `GET` | `/api/erp/employees` | Authenticated | Directory of maintenance technicians |
| `POST` | `/api/erp/employees` | Admin | Register new employee |
| `PUT` | `/api/erp/employees/{id}` | Admin | Update employee profile or designation |
| `DELETE`| `/api/erp/employees/{id}` | Admin | Remove employee profile |
| `POST` | `/api/erp/complaints/{id}/assign`| Admin / Mgr | Assign technician to ticket |
| `POST` | `/api/erp/complaints/{id}/escalate`| Admin / Mgr | Escalate overdue ticket |
| `GET` | `/api/erp/complaints/{id}/history` | Authenticated | Retrieve full ticket audit history |
| `GET` | `/api/erp/complaints/{id}/comments`| Authenticated | Retrieve public/internal ticket comments |
| `POST` | `/api/erp/complaints/{id}/comments`| Authenticated | Post comment or internal staff note |
| `POST` | `/api/erp/complaints/{id}/approvals`| Staff | Submit financial/part approval request |
| `POST` | `/api/erp/approvals/{id}/decide` | Admin / Mgr | Approve or reject request |
| `GET` | `/api/erp/notifications` | Authenticated | Fetch user notifications |
| `PUT` | `/api/erp/notifications/mark-read` | Authenticated | Mark notifications as read |
| `GET` | `/api/erp/audit-logs` | Admin | Fetch system-wide security audit logs |
| `GET` | `/api/erp/users` | Admin | RBAC user directory |
| `PUT` | `/api/erp/users/{id}/role` | Admin | Update user role |

---

## 🧪 Testing & Quality Assurance

Automated integration tests verify system stability and backwards compatibility:
```bash
./mvnw test
```

### Verified Test Scenarios:
- **`contextLoads()`**: Verifies that Spring application context boots and scans all 14 JPA repositories cleanly.
- **`testNewAdminLoginSuccess()`**: Confirms administrative authentication with current credentials.
- **`testPreviousAdminCredentialsDisabled()`**: Enforces proper credential lifecycle management.
- **`testStudentAccountsUnchanged()`**: Ensures student resident accounts and existing complaint history remain intact.

---

## 🔒 Security & Credential Management

- **Password Cryptography**: Salted SHA-256 password hashing.
- **Role-Based Access Control**: Strict endpoint authorization verifying administrative permissions on management endpoints.
- **Session Security**: Session tokens maintained in HttpOnly, SameSite=Strict cookies to guard against XSS and session hijacking.
- **Non-Destructive Database Management**: Hibernate `ddl-auto=update` safely adds new tables and columns while preserving all existing database records.

---

## 🛠️ Troubleshooting & FAQ

1. **`Unable to locate a Java Runtime` on macOS**:
   The bundled `./mvnw` script auto-detects Homebrew's OpenJDK 17 (`/opt/homebrew/opt/openjdk@17`). Ensure Java 17 is installed:
   ```bash
   export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
   export PATH="$JAVA_HOME/bin:$PATH"
   ```

2. **`connection to server on socket "/tmp/.s.PGSQL.5432" failed`**:
   Start your local PostgreSQL instance:
   ```bash
   brew services start postgresql@16
   ```

3. **`Port 8080 already in use`**:
   Identify and terminate the existing listener:
   ```bash
   lsof -ti :8080 | xargs kill -9
   ```

---

## 📄 License & Attribution

Developed for SRM Institute of Science and Technology hostel administration, resident support, and campus facility management. All rights reserved.
