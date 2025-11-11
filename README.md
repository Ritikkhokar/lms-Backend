# lms-Backend

# LMS Project

A Learning Management System (LMS) built with **Spring Boot**, **Gradle**, and **Java 25**.  
Supports user management, course/assignment creation, secure file uploads to AWS S3, grading, analytics, and more.

---

## 🛠️ Tech Stack

- **Java:** 25 (OpenJDK)
- **Spring Boot:** v3+
- **Gradle:** 9.1.0
- **Hibernate/JPA:** ORM, auto-updating schema
- **MySQL:** (or any JPA-compatible RDBMS)
- **AWS S3:** file storage for assignments/submissions
- **Lombok:** model boilerplate
- **Security:** Spring Security + JWT for authentication
- **Dotenv Java:** environment-based secrets management

---

## 🏗️ Features

- **User roles:** student, teacher, admin
- **Course management:** create, enroll, assign
- **Assignment lifecycle:** create/update/delete assignments (with doc/pdf upload)
- **Submission:** students upload solutions, files stored securely in S3
- **Grading:** instant grade feedback, performance anayltics
- **Email notifications:** send assignment updates/grade reports
- **AI Recommendations:** send ai recommendations on assignment grades
- **REST API**: Accessible via Postman/cURL/frontend

---

## 🚀 Getting Started

### **Requirements**

- Java 25+
- Gradle 9+
- MySQL (recommended) or Postgres
- AWS account with S3 bucket
- MacOS/Linux/Windows
- Secrets are managed by .env file

