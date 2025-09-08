# Database Setup Guide

This guide will help you set up the MySQL database for the ex4 project.

## Prerequisites

Before setting up the database, ensure you have:

- **MAMP** installed and running
- **MySQL** running on port `8889` (MAMP default)
- **phpMyAdmin** accessible at `http://localhost:8888/phpMyAdmin/`

## Database Setup Instructions

### Step 1: Download Database Dump

1. **Locate the Database File**
   - Navigate to the `database/` folder in this project
   - Find the file named `ex4.sql`
   - This file contains all the necessary sample data

### Step 2: Import Database Using phpMyAdmin

1. **Open phpMyAdmin**
   - Start MAMP
   - Open your web browser
   - Navigate to `http://localhost:8888/phpMyAdmin/`
   - Login with username: `root` and password: `root` (MAMP defaults)

2. **Import the Database**
   - In phpMyAdmin, click on the **"Import"** tab in the top menu
   - Click **"Choose File"** button
   - Select the `ex4.sql` file from the `database/` folder
   - Leave all other settings as default
   - Scroll down and click **"Import"** button

3. **Verify Database Creation**
   - After successful import, you should see a success message
   - Click on **"Databases"** in the left sidebar
   - Verify that `ex4` database appears in the list
   - Click on `ex4` database to expand it
   - You should see tables like: `user`, `role`, `event`, `responsibility`, `item`, `request`, etc.

## Default Test Data

After importing the database, you will have access to:

### Default Admin Account
```
Email: admin@admin.com
Password: 00000000
Role: Admin
```

### Chief Role Accounts
```
Email: chief1@gmail.com  - instead of 1 can be 2 or 3
Password: qwer43@!
```

### Users/Managers Role Accounts
```
Email: user1@gmail.com  - instead of 1 can be any number from 1-31
Password: qwer43@!
```

## Support

If you encounter issues:
1. [Verify MAMP is running correctly]
2. [Check application logs for errors]

---

**Last Updated**: 08/09/2025
**Database Version**: 1.0
**Compatible with**: Spring Boot 3.5.3, MySQL 8.0+