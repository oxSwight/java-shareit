# ShareIt 🔄
## Educational project from the **Java Developer course – Yandex.Practicum**

ShareIt is a **RESTful API** for managing items.  
It allows users to create, read, update and delete items, as well as **search items by keyword**.

---

## ✨ Features
- 📦 Create, update and delete items  
- 📅 List all items for a given user  
- 🔍 Search items by text in name/description  
- ⭐ Simple user identification via request header  
- 🗃️ SQL database integration (PostgreSQL, JPA/Hibernate)  
- 🐳 Docker support  

---

## 🛠 Tech Stack
- **Java 17**  
- **Spring Boot**  
- **PostgreSQL, JPA/Hibernate**  
- **REST API**  
- **JUnit, Mockito**  
- **Docker**  

---

## 🚀 Getting Started

```bash
# clone repo
git clone https://github.com/oxSwight/java-shareit.git
cd java-shareit

# run tests
./mvnw test

# run app
./mvnw spring-boot:run
```
---
##📑 API Endpoints
GET /items

Returns a list of all items owned by the user.

GET /items/{id}

Returns the item with the specified identifier.

GET /items/search?text={keyword}

Returns a list of items containing the specified text in name or description.

POST /items

Creates a new item.

PATCH /items/{id}

Updates an existing item.

---

##📌 Request Parameters

X-Sharer-User-Id – request header containing the user ID

id – item identifier

text – keyword for search

item – JSON object representing an item

Updates an existing item.


##📤 Response Codes

200 OK – request successful

201 Created – item created successfully

400 Bad Request – invalid request data

404 Not Found – item not found

Updates an existing item.

