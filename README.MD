# 🚀 Digital Wallet Microservices System  

**Java | Spring Boot | Microservices | Kafka | MySQL | Eureka | API Gateway | JWT**

A fully functional **Digital Wallet System** built using a **production-grade microservices architecture**, including:

- Secure authentication  
- Wallet management  
- Payment validation  
- Transaction processing with ledger  
- Kafka-based event-driven notifications  

This project demonstrates:
- Scalable backend engineering  
- Service isolation  
- Inter-service communication  
- Asynchronous messaging  
- Modern cloud-native patterns  

---

## 🏗️ Architecture Overview  

The system consists of **5 core microservices**, along with:

- **Eureka Server** — Service discovery  
- **API Gateway** — Unified entry point  
- **Kafka** — Asynchronous event messaging  
- **MySQL** — Database per service  

---

## 📐 High-Level Architecture Diagram (Text Version)  

                 +------------------+
                 |   API Gateway    |
                 +---------+--------+
                           |
                +----------+----------+
                |                     |
         +------+-------+     +-------+-------+
         |  Auth-Service |     |  Wallet-Service |
         +------+-------+     +-------+-------+
                |                     |
        +-------+-------+     +-------+-------+
        | Payment-Service |     | Transaction-Service |
        +-------+--------+     +-------+--------+
                |                      |
                +----------+-----------+
                           |
                       (Kafka Event)
                           |
                 +---------+---------+
                 | Notification Service |
                 +-------------------+

---

## 🔥 Key Features  

### ✅ Authentication & Authorization  
- User registration  
- User login  
- JWT token generation  
- Token validation endpoint  

### ✅ Wallet Management  
- Create wallet  
- View balance    
- Link mock bank account  

### ✅ Payment Validation  
- Balance checking  
- Wallet status validation  
- Daily & amount limit enforcement  
- Store validation logs  

### ✅ Transactions & Ledger  
- Send & receive funds  
- Balance deduction & addition  
- Transaction entry storage  
- Double-entry ledger system  

### ✅ Kafka Notifications  
- Asynchronous notification events  
- Sender: *"You sent money"*  
- Receiver: *"You received money"*  

---

## 🗄️ Databases (MySQL – Database per Service)  

| Database Name | Purpose |
|--------------|---------|
| `authdb` | User registration & authentication |
| `walletdb` | Wallet balance & bank account |
| `paymentdb` | Payment validation logs |
| `transactiondb` | Transaction history & ledger |
| `notificationdb` | Kafka-based notifications |


---

### 📂 Microservices Structure  
```text
digital-wallet-microservices/
│
├── eureka-server/
├── api-gateway/
├── auth-service/
├── wallet-service/
├── payment-service/
├── transaction-service/
└── notification-service/
```

### Each Service Contains:
- `controller/`  
- `service/`  
- `repository/`  
- `entity/`  
- `dto/`  
- `config/`  

---

## 🌐 API Endpoints (Summary)  

### 🔵 Auth-Service  
- `POST /auth/register`  
- `POST /auth/login`  
- `GET /auth/validate`  

### 🟢 Wallet-Service  
- `POST /wallet/create`  
- `GET /wallet/{userId}/balance`  
- `POST /wallet/{userId}/link-bank`  
- Internal:
  - `/wallet/{userId}/deduct`  
  - `/wallet/{userId}/add`  

### 🔴 Transaction-Service  
- `POST /transaction/send`  
- `GET /transaction/history/{userId}`  
- `GET /transaction/{txnId}`  

### 🟣 Payment-Service  
- `POST /payment/validate`  
- `POST /payment/process`  

### 🟠 Notification-Service  
- `GET /notification/user/{userId}`  
- `DELETE /notification/{id}`  

---

## 🧪 How to Test (Step-by-Step)  

1. Register **2 users**  
2. Login both users → copy JWT tokens  
3. Create wallets for both users  
4. Add balance manually in MySQL  
5. Send money using `/transaction/send`  
6. Verify updated balances  
7. Check transaction history  
8. Check notifications using `/notification/user/{id}`  

---

## 💡 Tech Stack  

- Java 17  
- Spring Boot 3  
- Spring MVC  
- Spring Security + JWT  
- Spring Cloud Gateway  
- Spring Cloud Eureka  
- Apache Kafka  
- MySQL  
- Hibernate / JPA  
- Maven  
- Docker (optional)  
- Postman  

---

## 📘 How to Run the Project  

### 1️⃣ Start MySQL  
Create the following databases:
- `authdb`
- `walletdb`
- `paymentdb`
- `transactiondb`
- `notificationdb`

### 2️⃣ Start Kafka & Zookeeper  
- Or use `docker-compose`

### 3️⃣ Run Services in This Order:
1. Eureka Server  
2. API Gateway  
3. Auth-Service  
4. Wallet-Service  
5. Payment-Service  
6. Transaction-Service  
7. Notification-Service  


