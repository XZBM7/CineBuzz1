Full-Stack Web Application (Flask + MongoDB)

A production-style cinema management and booking system designed with clean architecture, role-based access control, and modular backend design.

📌 Table of Contents

Project Introduction

Motivation & Problem Definition

System Capabilities

User Roles & Permissions

High-Level System Architecture

Detailed Backend Design

Booking Logic & Seat Management

Promotion Engine

QR Code Ticketing

Food Ordering Subsystem

Admin Subsystem (Deep Dive)

Database Design Philosophy

Security Model

Validation & Error Handling

Performance & Scalability Considerations

Project Structure Explained

Installation & Environment Setup

Testing Strategy

Known Limitations

Future Improvements

Team & Collaboration

Conclusion

1️⃣ Project Introduction

The Cinema Booking System is a full-stack web application that digitizes the complete cinema workflow — from movie discovery to seat booking, QR ticket generation, food ordering, and administrative control.

The system is designed not as a simple CRUD app, but as a modular, extensible platform that simulates real-world cinema systems.

2️⃣ Motivation & Problem Definition

Traditional or basic cinema systems often suffer from:

Hard-coded workflows

Weak access control

Tight coupling between UI and logic

Poor scalability

This project aims to:

Separate concerns clearly

Enforce security at every layer

Model real cinema business rules

Provide both customer-facing and admin-facing subsystems

3️⃣ System Capabilities
🎟 Customer Capabilities

Account registration with password policy enforcement

Cinema branch selection (context-based browsing)

Movie discovery by cinema and status

Screening selection

Seat-based booking logic

Automatic promotion handling

QR code ticket generation

Food ordering and checkout

Profile & booking history management

🧑‍💼 Administrative Capabilities

Full system dashboard

Movies, screenings, and cinemas CRUD

Promotions engine management

Food items management

Users & bookings monitoring

Admin & System Admin separation

4️⃣ User Roles & Permissions
Role	Permissions
Guest	Browse movies, register
User	Book tickets, order food
Admin	Manage cinema data
System Admin	Manage admin accounts

Role-based access is enforced at the route level using decorators.

5️⃣ High-Level System Architecture

The system follows a Layered Architecture:

Client (Browser)
   ↓
Presentation Layer (HTML / Jinja / JS)
   ↓
Application Layer (Flask Controllers + Business Logic)
   ↓
Data Layer (MongoDB)


Each layer is isolated to:

Improve maintainability

Enable testing

Reduce coupling

6️⃣ Detailed Backend Design

The Flask backend is structured around logical modules, not just routes:

auth → authentication & authorization

cinema → cinema context handling

movies → movie discovery

screenings → scheduling

booking → seat logic, pricing, promotions

food → food ordering

profile → user account management

admin → admin-only operations

Each module encapsulates its own responsibilities.

7️⃣ Booking Logic & Seat Management

Seat booking follows a strict validation pipeline:

Load existing booked seats

Validate selected seats

Prevent double booking

Calculate base price

Apply promotion rules

Persist booking atomically

Generate QR ticket

This ensures data consistency even under concurrent requests.

8️⃣ Promotion Engine

The promotion system supports rule-based offers such as:

Buy 1 Get 1 Free (B1G1F)

Buy 2 Get 1 Free (B2G1F)

Promotions are:

Movie-specific

Time-independent

Applied dynamically during booking

9️⃣ QR Code Ticketing

After successful booking:

Ticket data is serialized

QR code is generated server-side

QR image is embedded in confirmation page

This simulates real-world digital ticket validation systems.

🔟 Food Ordering Subsystem

The food module is a standalone subsystem:

Separate collections

Independent logic

Order lifecycle (pending → completed)

It demonstrates domain separation within the same application.

1️⃣1️⃣ Admin Subsystem (Deep Dive)

The admin system is treated as a logical sub-architecture:

Separate UI

Separate controllers

Strong permission checks

Admins can:

Manage content

Monitor system usage

Control business rules

System Admins additionally manage admin accounts safely (soft delete).

1️⃣2️⃣ Database Design Philosophy

MongoDB is used due to:

Flexible schema

Embedded relationships

Scalability

Key design choices:

Referencing for major entities

Avoiding over-embedding

Clear ownership of data

1️⃣3️⃣ Security Model

Security is enforced through:

Password hashing

JWT tokens

Session validation

Role-based decorators

File upload validation

No sensitive data is stored in plain text.

1️⃣4️⃣ Validation & Error Handling

The system validates:

User inputs

Password strength

Email uniqueness

Seat availability

Admin permissions

Errors are handled gracefully and reported to the user.

1️⃣5️⃣ Performance & Scalability Considerations

Efficient MongoDB queries

Aggregation pipelines for admin analytics

Stateless JWT authentication

Modular design for horizontal scaling

1️⃣6️⃣ Project Structure Explained
app.py                → Main application
templates/            → UI templates
static/               → Assets & uploads
admin/                → Admin UI & logic


The structure favors clarity over over-engineering.

1️⃣7️⃣ Installation & Environment Setup
pip install -r requirements.txt
python app.py


Database:

mongodb://localhost:27017/cinema_db

1️⃣8️⃣ Testing Strategy

Manual end-to-end testing

Admin workflow testing

Edge case validation

Logical testing of booking rules

1️⃣9️⃣ Known Limitations

No real payment gateway

د
No WebSocket seat locking

Single deployment environment

2️⃣0️⃣ Future Improvements

Online payments

Seat locking

Mobile app

Recommendation system

Admin analytics dashboard

