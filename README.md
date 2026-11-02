# 🏛️ SRM Hostel Smart Complaint Management System

[![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%2B-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Hibernate](https://img.shields.io/badge/Hibernate-ORM-59666C?style=for-the-badge&logo=hibernate&logoColor=white)](https://hibernate.org/)
[![JavaScript](https://img.shields.io/badge/Frontend-Vanilla%20ES6%2B-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)](https://developer.mozilla.org/en-US/docs/Web/JavaScript)
[![GitHub](https://img.shields.io/badge/Repository-GitHub-181717?style=for-the-badge&logo=github&logoColor=white)](https://github.com/swethadurai48/srm-hostel-complaint-management.git)

A dedicated, full-stack **Hostel Smart Complaint Management System** designed specifically for SRM Institute of Science and Technology residents and hostel wardens/maintenance staff. The system focuses exclusively on hostel maintenance operations, student issue reporting, rapid ticket triage, live progress tracking, and maintenance resolution management.

---

## 📑 Table of Contents

- [Overview](#-overview)
- [System Architecture](#-system-architecture)
- [Key Features](#-key-features)
  - [Resident & Student Portal](#-resident--student-portal)
  - [Fast Triage & Live Tracking](#-fast-triage--live-tracking)
  - [Warden & Maintenance Staff Portal](#-warden--maintenance-staff-portal)
- [Technology Stack](#-technology-stack)
- [Project Directory Structure](#-project-directory-structure)
- [Database Schema & Data Model](#-database-schema--data-model)
- [Default System Credentials](#-default-system-credentials)
- [Installation & Getting Started](#-installation--getting-started)
  - [1. Prerequisites](#1-prerequisites)
  - [2. Clone the Repository](#2-clone-the-repository)
  - [3. PostgreSQL Database Setup](#3-postgresql-database-setup)
  - [4. Environment & Database Configuration](#4-environment--database-configuration)
  - [5. Build & Run Application](#5-build--run-application)
- [Screenshots & UI Walkthrough](#-screenshots--ui-walkthrough)
- [REST API Documentation](#-rest-api-documentation)
- [Testing & Quality Assurance](#-testing--quality-assurance)
- [Security & Credential Management](#-security--credential-management)
- [Troubleshooting & FAQ](#-troubleshooting--faq)

---

## 🌟 Overview

The **SRM Hostel Smart Complaint Management System** digitizes residential hostel operations across campus. It replaces manual complaint registers and slow email loops with an automated ticket lifecycle:

1. **Student Logs Ticket**: Quick category selection (Water, Electricity, Wi-Fi, Housekeeping, Maintenance, Mess & Food, Security).
2. **Fast Triage by Warden**: Dashboard displays pending issues with one-click filtering by category or urgency.
3. **Staff Assignment**: Warden assigns qualified technicians (e.g., Electrician, Plumber) and updates ticket state.
4. **Live Tracking**: Resident tracks ticket progress (`PENDING` ➔ `IN_PROGRESS` ➔ `RESOLVED`) in real time with official administration remarks.
5. **Feedback Loop**: Residents rate resolved tickets (1–5 stars) to guarantee quality control.

---

## 🏛️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             CLIENT LAYER (Browser)                          │
│   Landing & Auth (index.html) │ Student Portal (student.html) │ Admin Portal (admin.html) │
│       ├── Custom SRM Design System (style.css, SRM Logo)                    │
│       └── Vanilla ES6+ API Client & Event Handlers (api.js, auth.js, etc.)  │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ HTTP / JSON (HttpOnly Session Cookies)
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    SPRING BOOT APPLICATION LAYER (Port 8080)                │
│                                                                             │
│  [REST Controllers]                                                         │
│   ├── AuthController (/api/auth)                                            │
│   ├── ComplaintController (/api/complaints)                                 │
│   ├── AdminComplaintController (/api/admin/complaints)                      │
│   ├── NoticeController (/api/notices)                                       │
│   └── FeedbackController (/api/feedback)                                    │
│                                                                             │
│  [Service Layer & Security]                                                 │
│   ├── UserService (Salted SHA-256 Hashing, Session Management)               │
│   └── ComplaintService (Fast Triage, Status Lifecycle, Staff Assignment)   │
│                                                                             │
│  [Data Access Layer (Spring Data JPA / Hibernate 6)]                        │
│   ├── UserRepository, ComplaintRepository, NoticeRepository, FeedbackRepo   │
└──────────────────────────────────────┬──────────────────────────────────────┘
                                       │ JDBC (HikariCP)
                                       ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                    DATABASE LAYER (PostgreSQL 14+ / 16)                     │
│    Database: smart_complaint_db                                             │
│    Tables: users ──< complaints, users ──< feedbacks, notices               │
└─────────────────────────────────────────────────────────────────────────────┘
```

---

## 🎯 Key Features

### 👨‍🎓 Resident & Student Portal
- **Account Registration & Self-Service**: Sign up with Registration Number, Academic Department, Hostel, Block, and Room Number.
- **Categorized Ticket Lodging**:
  - 💧 **Water & Plumbing**: Pipe bursts, tap leaks, lack of hot water.
  - ⚡ **Electrical**: Socket breakdown, switchboard short circuits, tube light replacement.
  - 📶 **Internet / Wi-Fi**: Speed degradation, router access point reset.
  - 🧹 **Housekeeping**: Floor hygiene, corridor and washroom cleaning.
  - 🔧 **Room Maintenance**: Door latches, locks, window glass, furniture repairs.
  - 🍴 **Mess & Food Quality**: Food freshness, meal hygiene, dining feedback.
  - 🛡️ **Security**: Corridor noise, guest entry protocols.
  - 💬 **Other**: General hostel enquiries.
- **Official Notices**: View live announcements, mess menus, and maintenance schedules directly from the hostel warden.
- **Facilities Directory**: Instant directory of campus health clinics, ambulances, electrical cell, and warden desks.
- **Feedback & Rating**: Rate completed work from 1 to 5 stars.

### ⚡ Fast Triage & Live Tracking
- **Instant Category & Status Filtering**: Wardens instantly filter complaints by domain or status without full page reloads.
- **Real-Time KPI Counters**:
  - 📋 Total Complaints Logged
  - ⏳ Pending Action Needed
  - 🕒 In-Progress / Technician Assigned
  - ✅ Resolved Issues
- **Live Status Badges**: Clear color-coded badges (`PENDING`, `IN_PROGRESS`, `RESOLVED`) on student and admin views.
- **Audit History**: Timestamped records of creation, updates, and resolution dates.

### 🛡️ Warden & Maintenance Staff Portal
- **Role-Gated Access**: Dedicated route protected by `ADMIN` session authorization.
- **Maintenance Staff Assignment**: Record assigned technicians (e.g., *Plumber Murugan*, *Electrician Kumar*).
- **Official Response System**: Publish remarks visible immediately to the student (e.g., *Parts ordered; replacement at 3:00 PM*).
- **One-Click Resolution**: Mark tickets resolved and log the completion timestamp.

---

## 💻 Technology Stack

| Layer | Technology | Description |
|---|---|---|
| **Language** | Java 17 LTS (OpenJDK) | Strongly typed enterprise runtime |
| **Framework** | Spring Boot 3.3.3 | Embedded Apache Tomcat 10.1 web container |
| **Persistence** | Spring Data JPA / Hibernate 6.5 | Object-Relational Mapping with connection pooling |
| **Database** | PostgreSQL 14+ / 16 | ACID-compliant relational SQL storage |
| **Connection Pool** | HikariCP 5.1 | Enterprise JDBC pool |
| **Security** | Salted SHA-256 + HttpOnly Sessions | Secure credential digestion and cookie-based sessions |
| **Frontend** | HTML5, CSS3, ES6+ JavaScript | Zero external JavaScript framework dependencies |
| **Build Tool** | Apache Maven Wrapper (`mvnw`) | Consistent, portable build execution |

---

## 🏗️ Project Directory Structure

```text
srm-hostel-complaint-management/
├── .gitignore                                 # Git ignore for target/, .env, IDE and secrets
├── mvnw / mvnw.cmd                            # Self-contained Maven wrapper scripts
├── pom.xml                                    # Spring Boot dependencies & Maven configuration
├── README.md                                  # Comprehensive system documentation
└── src/
    ├── main/
    │   ├── java/com/smartcomplaint/
    │   │   ├── SmartComplaintApplication.java # Spring Boot main entry point
    │   │   ├── config/                        # DataInitializer & WebConfig
    │   │   ├── controller/                    # Auth, Complaint, Admin, Notice, Feedback
    │   │   ├── dto/                           # API Request & Response transfer objects
    │   │   ├── model/                         # JPA Entities (User, Complaint, Notice, Feedback)
    │   │   ├── repository/                    # Spring Data JPA repositories
    │   │   └── service/                       # Business logic (ComplaintService, UserService)
    │   └── resources/
    │       ├── application.properties         # Main application configuration
    │       ├── application-dev.properties     # Development profile configuration
    │       ├── schema-postgresql.sql          # PostgreSQL DDL reference
    │       └── static/                        # Frontend UI assets
    │           ├── index.html                 # SRM Landing & Student/Admin Login
    │           ├── student.html               # Resident Portal & Tracking Dashboard
    │           ├── admin.html                 # Warden & Maintenance Portal
    │           ├── css/style.css              # Custom responsive stylesheet
    │           ├── images/                    # SRM Logo & campus imagery
    │           └── js/                        # Frontend controllers (api.js, auth.js, student.js, admin.js)
    └── test/                                  # Spring Boot integration tests
```

---

## 🗄️ Database Schema & Data Model

The PostgreSQL database `smart_complaint_db` consists of 4 relational tables:

```text
users (id, name, email, password, role, student_id, department, room_or_hostel, created_at)
  │
  ├──< complaints (id, student_id, title, description, category, status, priority,
  │                location, assigned_staff, admin_response, created_at, updated_at, resolved_at)
  │
  └──< feedbacks  (id, student_id, complaint_id, rating, message, created_at)

notices (id, title, content, category, is_important, published_at)
```

---

## 🔑 Default System Credentials

| Role | Username / Email | Password | Permissions |
|---|---|---|---|
| 🛡️ **Hostel Warden / Admin** | `r.haresh11.8.2007@gmail.com` | `SRM@484` | Full Triage, Status Updates, Staff Assignment |
| 👨‍🎓 **Demo Student Resident** | `student@campus.edu` | `student123` | Ticket Lodging, Live Tracking, Feedback |

> Residents can also self-register at any time from the home page.

---

## 🚀 Installation & Getting Started

### 1. Prerequisites
- **Java 17 LTS**: `java -version`
- **PostgreSQL 14+ / 16**: `psql --version`
- **Git**: `git --version`

### 2. Clone the Repository
```bash
git clone https://github.com/swethadurai48/srm-hostel-complaint-management.git
cd srm-hostel-complaint-management
```

### 3. PostgreSQL Database Setup
Create the target database in your local PostgreSQL instance:
```bash
psql -U postgres -c "CREATE DATABASE smart_complaint_db;"
```

### 4. Environment & Database Configuration
Configure credentials via environment variables or verify `src/main/resources/application.properties`:
```properties
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/smart_complaint_db}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
```

### 5. Build & Run Application
```bash
# Using the bundled Maven Wrapper (macOS / Linux)
./mvnw clean spring-boot:run

# Using Windows Command Prompt
mvnw.cmd clean spring-boot:run
```

Once started, open your web browser:
- 🌐 **Portal Landing & Login**: [http://localhost:8080](http://localhost:8080)
- 👨‍🎓 **Resident Portal**: [http://localhost:8080/student.html](http://localhost:8080/student.html)
- 🛡️ **Warden & Admin Portal**: [http://localhost:8080/admin.html](http://localhost:8080/admin.html)

---

## 📸 Screenshots & UI Walkthrough

### 1. Landing Page & Authentication
Unified SRM-branded entry point with official logo, hostel banner, and tabbed Student/Admin login and student self-registration.

```text
+-----------------------------------------------------------------------------------+
| [SRM LOGO]  SRM Hostel Complaint Management System               [Resident Portal]|
+-----------------------------------------------------------------------------------+
|  A Better Hostel Experience for a Brighter Tomorrow                               |
|                                                                                   |
|  [ Hostel Photo & Description ]        [  Login Tab  |  Register Tab  ]           |
|  - Instant ticket lodging              |  Email: [                     ]          |
|  - Real-time resolution alerts         |  Password: [                  ]          |
|  - Direct warden communication         |  [ Sign In to Portal ]                   |
+-----------------------------------------------------------------------------------+
```

### 2. Resident Dashboard & Live Tracking
Real-time KPI metrics (`Total`, `Resolved`, `In Progress`, `Pending`), fast complaint category tiles, active notice board, and dynamic complaint tracking.

```text
+-----------------------------------------------------------------------------------+
| [SRM LOGO]  SRM Hostel                         (🔔) [SR] Alex Rivera (Student)   |
+-----------------------------------------------------------------------------------+
| [📊 Dashboard ] | [Total: 3]  [Resolved: 1]  [In Progress: 1]  [Pending: 1]       |
| [➕ File Issue] |-----------------------------------------------------------------|
| [📄 History   ] | Fast Category Selection:                                        |
| [🏢 Amenities ] |  [💧 Water] [⚡ Electrical] [📶 Wi-Fi] [🧹 Housekeeping]       |
| [📢 Notices   ] |-----------------------------------------------------------------|
| [💬 Feedback  ] | Recent Complaints Table (Click row for live tracking modal)     |
| [👤 Profile   ] | 1. Wi-Fi dropping | Block B Room 304 | IN_PROGRESS | 28 Oct     |
+-----------------------------------------------------------------------------------+
```

### 3. Warden Fast Triage & Staff Assignment Modal
Full triage board with live category filtering, urgency flags, assigned maintenance technician fields, and official administrative responses.

```text
+-----------------------------------------------------------------------------------+
| [SRM LOGO]  Hostel Administration Portal                        [W] Warden Logout |
+-----------------------------------------------------------------------------------+
| Filters: [Category: All ▼] [Status: All ▼] [Action: Needs Action ▼] [Reset]       |
|-----------------------------------------------------------------------------------|
| #  | Category   | Complaint & Details    | Resident & Room  | Status   | Action   |
| 1  | Electrical | Short circuit socket   | John (B-201)     | PENDING  | ⏳ Action |
| 2  | Plumbing   | Tap leak bathroom      | Alex (B-304)     | IN_PROG  | 🕒 Wait  |
|-----------------------------------------------------------------------------------|
| >> Action Modal:                                                                  |
| Update Status: [ IN_PROGRESS ▼ ]                                                  |
| Assigned Staff: [ Electrician Kumar - Ext 4410 ]                                  |
| Official Response: [ Dispatched technician for switchboard inspection at 2 PM ]   |
| [ Save & Notify Resident ]                                                        |
+-----------------------------------------------------------------------------------+
```

---

## 📡 REST API Documentation

All API responses follow a unified JSON envelope:
```json
{
  "success": true,
  "message": "Status description",
  "data": { ... }
}
```

### Key Endpoints:
- `POST /api/auth/register` — Student resident registration
- `POST /api/auth/login` — Session authentication (Student / Admin)
- `GET /api/auth/me` — Retrieve active session user
- `POST /api/complaints` — Submit new complaint ticket
- `GET /api/complaints/my` — Retrieve authenticated student's tickets
- `GET /api/complaints/my/stats` — Retrieve student KPI stats
- `GET /api/admin/complaints` — Triage search with `?category=` and `?status=` filters
- `PUT /api/admin/complaints/{id}/status` — Update lifecycle status & assign staff
- `PUT /api/admin/complaints/{id}/response` — Add official administration response
- `GET /api/notices` — Fetch published hostel circulars
- `POST /api/feedback` — Submit resident service rating (1–5 stars)

---

## 🧪 Testing & Quality Assurance

Run the automated test suite with Maven:
```bash
./mvnw test
```

### Test Coverage Highlights:
- **`contextLoads()`**: Verifies that Spring application context boots cleanly.
- **`testNewAdminLoginSuccess()`**: Validates active admin authentication.
- **`testPreviousAdminCredentialsDisabled()`**: Enforces decommission of legacy credentials.
- **`testStudentAccountsUnchanged()`**: Verifies student authentication and data isolation.

---

## 🔒 Security & Credential Management

- **No Hardcoded Passwords**: All database passwords use environment variable fallbacks (`SPRING_DATASOURCE_PASSWORD`).
- **Salted SHA-256 Digest**: Passwords stored as cryptographically hashed message digests.
- **Protected Sessions**: HttpOnly, SameSite=Strict cookies to prevent XSS and CSRF attacks.
- **Strict `.gitignore`**: Excludes `.env`, IDE configurations, secrets, and build targets.

---

## 🛠️ Troubleshooting & FAQ

1. **`Unable to locate a Java Runtime` on macOS**:
   The bundled `./mvnw` script automatically detects Homebrew's OpenJDK 17 (`/opt/homebrew/opt/openjdk@17`). You can also export it in `~/.zshrc`:
   ```bash
   export PATH="/opt/homebrew/opt/openjdk@17/bin:$PATH"
   export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
   ```

2. **`connection to server on socket "/tmp/.s.PGSQL.5432" failed`**:
   Start your local PostgreSQL service:
   ```bash
   brew services start postgresql@16
   ```

3. **`Port 8080 already in use`**:
   Find and terminate the process holding port 8080:
   ```bash
   lsof -ti :8080 | xargs kill -9
   ```

---

## 📄 License & Attribution

Developed for SRM Institute of Science and Technology hostel administration and resident support. All rights reserved.
