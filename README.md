# CSE360 - Homework 4  
**Project Title:** Staff Role (HW4)  
**Author:** Sarah Wyner 
**Course:** CSE360 – Software Engineering  

---

## 📘 Overview
This project is a modular JavaFX-based course support system that allows users (students, instructors, and staff) to interact through FAQs, clarifications, announcements, and reviews. It demonstrates core software engineering principles such as modularization, MVC architecture, and documentation using Javadoc.

---

## 🧩 Features

### 🏗 Architecture
The project follows an **MVC-like structure**, divided into:
- **`application/`** – Entry point and application startup logic.  
- **`databasePart1/`** – Handles database operations and helper utilities.  
- **`logic/`** – Core logic layer managing user input, validation, and backend coordination.  
- **`model/`** – Data models (e.g., `FAQ`, `Question`, `Review`, `User`, etc.).  
- **`pages/`** – JavaFX UI pages for different user views and interactions.  

### 🧠 Functional Highlights
- View and manage **Frequently Asked Questions (FAQ)**  
- Post and edit **Questions and Answers**  
- Manage **Announcements and Clarifications**  
- Support for multiple user roles (Student, Staff, Instructor, Reviewer)  
- Modular JavaFX application using `module-info.java`  

---

## ⚙️ Technologies Used
| Component | Technology |
|------------|-------------|
| GUI Framework | JavaFX 24 |
| Language | Java 17+ |
| Unit Testing | JUnit 5 |
| Build / Run | Java Modules |
| Documentation | Javadoc |
| IDE (optional) | Eclipse / IntelliJ IDEA |

