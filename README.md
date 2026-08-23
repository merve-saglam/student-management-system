# Student Management System

A Java Swing desktop application for managing students, courses, and emergency contact information through a Microsoft SQL Server database.

The project demonstrates object-oriented programming, graphical user interface development, database connectivity, CRUD operations, input validation, and secure configuration practices.

## Features

### Student Management

* Add new student records
* Display registered students in a table
* Update existing student information
* Delete student records
* Validate user input before database operations

### Course Management

* Add courses for students
* View and manage course records
* Update existing course information
* Delete course records

### Emergency Contact Management

* Add emergency contact information
* Search contacts by student ID
* Update existing emergency contacts
* Delete emergency contact records

## Technologies

* Java
* Java Swing
* JDBC
* Microsoft SQL Server
* SQL and T-SQL
* Stored procedures
* Object-oriented programming
* Git and GitHub

## Project Structure

| File                              | Purpose                                                          |
| --------------------------------- | ---------------------------------------------------------------- |
| `DatabaseConnection.java`         | Establishes a secure JDBC connection using environment variables |
| `AddStudentForm.java`             | Adds new student records                                         |
| `StudentListFrame.java`           | Displays registered students                                     |
| `StudentAddDeleteForm.java`       | Manages student creation and deletion                            |
| `UpdateStudentForm.java`          | Updates student information                                      |
| `AddCourseForm.java`              | Adds course records                                              |
| `CourseManagementFrame.java`      | Displays and manages courses                                     |
| `UpdateCourseForm.java`           | Updates course information                                       |
| `AddEmergencyForm.java`           | Adds emergency contact records                                   |
| `EmergencyContactFrame.java`      | Displays and manages emergency contacts                          |
| `UpdateEmergencyContactForm.java` | Updates emergency contact information                            |

## Database Configuration

The application reads its database configuration from environment variables. Database passwords and other sensitive credentials are not stored in the source code.

Supported environment variables:

| Variable      | Description        | Default                   |
| ------------- | ------------------ | ------------------------- |
| `DB_SERVER`   | SQL Server address | `127.0.0.1`               |
| `DB_PORT`     | SQL Server port    | `1433`                    |
| `DB_NAME`     | Database name      | `StudentManagementSystem` |
| `DB_USER`     | Database username  | `sa`                      |
| `DB_PASSWORD` | Database password  | Required                  |

Example configuration on macOS or Linux:

```bash
export DB_SERVER="127.0.0.1"
export DB_PORT="1433"
export DB_NAME="StudentManagementSystem"
export DB_USER="your_database_user"
export DB_PASSWORD="your_database_password"
```

On Windows PowerShell:

```powershell
$env:DB_SERVER="127.0.0.1"
$env:DB_PORT="1433"
$env:DB_NAME="StudentManagementSystem"
$env:DB_USER="your_database_user"
$env:DB_PASSWORD="your_database_password"
```

## Requirements

* Java Development Kit
* Microsoft SQL Server
* Microsoft JDBC Driver for SQL Server
* A configured `StudentManagementSystem` database
* Required tables and stored procedures
* Database environment variables

## Security

* Database credentials are loaded from environment variables.
* The database password is never committed to the repository.
* SQL operations use prepared or callable statements where applicable.
* Public repositories should never contain real database passwords or private connection details.

## Learning Outcomes

This project provided practical experience with:

* Building desktop interfaces with Java Swing
* Connecting Java applications to Microsoft SQL Server
* Implementing CRUD operations
* Working with JDBC, prepared statements, and stored procedures
* Organizing an application into separate management forms
* Validating user input and handling database errors
* Protecting credentials in a public repository

## Author

**Merve Sağlam**
Software Engineering Student
Ankara, Türkiye

[GitHub](https://github.com/merve-saglam) · [LinkedIn](https://www.linkedin.com/in/mervesaglam-)

