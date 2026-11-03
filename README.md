# 📬 Gmail Management Service

---

## 📖 Introduction

**Gmail Management Service (GMS)** is a Java-based backend application developed using **Spring Boot** to automate daily Gmail inbox management. It retrieves emails received during the previous **24 hours**, analyzes their content using **AI**, categorizes them based on their importance, and generates a summary for each email.

The processed emails are combined into a single **daily email digest**, containing details such as the sender, subject, category, and AI-generated summary. The digest also provides action buttons such as **Delete**, **Mark as Important**, **Mark as Spam**, allowing users to manage their emails directly.

The entire workflow is automated using **AWS EventBridge** and **AWS Lambda**, which trigger the email processing workflow every 24 hours and deliver the generated digest to the user at a scheduled time.

This eliminates the need for users to manually review every incoming email, helping them quickly identify important messages and manage their inbox more efficiently.

---

## 📸 Daily Digest Preview

<p align="center">
  <img src="GMS%20CODE/.idea/gms.png" alt="Daily Email Digest" width="700"/>
</p>

---

## 🔍 Overview

Gmail Management Service is an Email management system that automatically processes incoming emails and digest.

- 🔐 Users authenticate their Gmail accounts using **Google OAuth 2.0**.
- 🔑 The application securely obtains  **refresh tokens**.
- 📩 It retrieves emails received within the previous 24 hours using the **Gmail API**.
- 🤖 AI analyzes email content and categorizes messages into **Important**, **Normal**, **Promotional** & **Spam**.
- 📝 The application generates concise summaries of individual emails for each category
- 📧 A daily email digest is generated containing sender details, subject, email summary, and action buttons.
- ⚡ Users can manage emails using actions buttons such as **Delete**, **Mark as Important**, **Mark as

The application is developed using **Spring Boot** and **Java**, with **MySQL** for storing user informils.

---

## 🎯 Use Case

Gmail Management Service can be used by individuals who receive a large number of emails and want to mawithout manually reading every message.

It is especially useful for:

- ✅ Automatically identifying important emails.
- ⏱️ Reducing time spent reading unnecessary emails.
- 📝 Generating daily email summaries.
- 🗂️ Organizing promotional and spam emails.
- ⚙️ Managing emails through automated actions.
- 📌 Keeping track of important messages and pending responses.

---

## 📋 Prerequisites

- ☕ **Java** 21 or later
- 📦 **Maven** 3.8+
- 🗄️ **MySQL** 8.0 or later
- ☁️ **Google Cloud** account
- 🔐 **Google OAuth 2.0** credentials
- 📩 **Gmail API** enabled in Google Cloud Console
- 🤖 **API key** of any AI
- 🟧 **AWS Account** for Relational Database

---

## ☁️ Google Cloud Setup

### 1️⃣ Enable Gmail API

1. Sign in to your **Google Cloud Console**.
2. Create a new project or select an existing project.
3. Navigate to **APIs & Services**.
4. Click **Enable APIs and Services**.
5. Search for **Gmail API**.
6. Open **Gmail API** and click **Enable**.

### 2️⃣ Configure OAuth Consent Screen

1. Navigate to **Google Auth Platform**.
2. Configure the application information.
3. Enter the application name.
4. Add the developer contact email.
5. Configure add test users
6. Save the configuration.

### 3️⃣ Create OAuth 2.0 Credentials

1. Navigate to **APIs & Services → Credentials**.
2. Click **Create Credentials**.
3. Select **OAuth client ID**.
4. Choose **Web Application** as the application type.
5. Enter the application name.
6. Add  **Redirect URI**:
7. Click **Create**.
8. Copy and save the generated **Client ID** and **Client Secret**.

---

## 🗄️ Amazon RDS MySQL Database Setup ☁️

GMS uses **Amazon RDS** with **MySQL** to store user information, Gmail account details, and OAuth refresh tokens.

### Create RDS MySQL Instance

1. Sign in to **AWS Management Console**.
2. Search for **RDS** and open **Amazon RDS**.
3. Click **Create database**.
4. Select **MySQL** as the database engine.
5. Choose the required database template.
6. Configure the following details:

   | Setting | Value |
   |---|---|
   | DB Identifier | Your Identifier |
   | Master Username | Your Master UserName |
   | Master Password | Your secure password |
   | Database Name | Database Name |

7. Configure connectivity and security group settings.
8. Allow MySQL traffic on port `3306` from trusted sources.
9. Click **Create database**.the database for subsequent Gmail API access.

---

## 🔧 Environment Variables (For Local Development)

1. Open your project in **IntelliJ IDEA**.
2. Navigate to **Run → Edit Configurations**.
3. Select your **Spring Boot** application.
4. Locate the **Environment Variables** field.
5. Click the **Edit** button.
6. Configure the following environment variables.

| Key | Value |
|---|---|
| `GOOGLE_CLIENT_ID` | Your Google OAuth Client ID |
| `GOOGLE_CLIENT_SECRET` | Your Google OAuth Client Secret |
| `OPENAI_API_KEY` | Your OpenAI API Key |
| `DB_USERNAME` | Your MySQL username |
| `DB_PASSWORD` | Your MySQL password |

---

## 🚀 Getting Started

### 1. Clone the repository
```bash
git clone https://github.com/Shiva20075/Gmail-Managment-Service.git
cd Gmail-Managment-Service
```

### 2. Set environment variables
```bash
export GOOGLE_CLIENT_ID=your_client_id
export GOOGLE_CLIENT_SECRET=your_client_secret
export OPENAI_API_KEY=your_openai_api_key
export DB_USERNAME=your_mysql_username
export DB_PASSWORD=your_mysql_password
```

### 3. Build and run
```bash
mvn clean install
mvn spring-boot:run
```

The app runs at **http://localhost:8081**

### 4. Authenticate
Open the auth endpoint in your browser, sign in with Google and grant Gmail permissions. The refresh ton't need you to log in again.

---

## 📂 Email Actions

| Action | Description |
|---|---|
| 🗑️ Delete | Permanently deletes the email |
| ⭐ Mark as Important | Flags the email as important |
| 👁️ Mark as Read | Marks the email as read |

---

## 🔒 Security

- OAuth 2.0 for Gmail access, limited to the scopes the user grants
- Secrets are kept in environment variables, never in source code
- Refresh tokens are stored securely and kept out of logs

---
