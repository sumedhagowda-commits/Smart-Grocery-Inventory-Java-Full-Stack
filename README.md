# Smart Grocery List & Inventory Manager — Java Full Stack

A full-stack inventory application built with **Java 17, Spring Boot, Spring MVC, Thymeleaf, Spring Data JPA, H2 Database, and Bootstrap**. It is a Java implementation of the Smart Grocery List & Inventory Manager concept.

## Features
- Register and log in with password hashing (BCrypt)
- Session-based sign-in and sign-out
- Add, edit, delete, search, and filter grocery items
- Increase/decrease stock quantity with safeguards against negative stock
- Minimum-stock alerts and expiry reminders
- Shopping list derived from low-stock items
- Dashboard summary cards
- Export inventory to CSV
- User-owned records: each user only sees their own items
- Responsive Bootstrap interface

## Requirements
- Java 17 or newer
- Maven 3.8+ (or use your IDE's Maven integration)
- Internet access on first build to download Maven dependencies

## Run on Windows
1. Extract the ZIP.
2. Open PowerShell in the extracted `Smart-Grocery-Inventory-Java-Full-Stack` folder.
3. Verify Java: `java -version`
4. Verify Maven: `mvn -version`
5. Run: `mvn spring-boot:run`
6. Open http://localhost:8080

## Run on macOS/Linux
```bash
cd Smart-Grocery-Inventory-Java-Full-Stack
java -version
mvn -version
mvn spring-boot:run
```

## First use
1. Open http://localhost:8080/register
2. Create an account using your own email and a password of at least six characters.
3. Sign in.
4. Select **Add grocery item** and create items such as rice, milk, bread, eggs, or vegetables.
5. Set a minimum stock level to activate low-stock alerts.
6. Add expiry dates to see reminders for items expiring within seven days.
7. Use Inventory to search, filter, edit, delete, and adjust stock.
8. Click **Export inventory CSV** on the dashboard to download inventory data.

## Database
The app uses an embedded H2 file database. Data is stored under `./data/` and remains available between restarts. The optional H2 console is at http://localhost:8080/h2-console. For the local console, JDBC URL is `jdbc:h2:file:./data/grocerydb`, username `sa`, and an empty password. Do not expose the H2 console publicly.

## Main routes
| Method | Route | Purpose |
|---|---|---|
| GET | `/register` | Registration page |
| POST | `/register` | Create account |
| GET | `/login` | Login page |
| POST | `/login` | Start session |
| POST | `/logout` | End session |
| GET | `/dashboard` | Dashboard and alerts |
| GET | `/inventory` | Inventory with search/category filters |
| GET | `/items/new` | Add item form |
| POST | `/items/save` | Create or update item |
| GET | `/items/{id}/edit` | Edit form |
| POST | `/items/{id}/quantity` | Increase/decrease stock |
| POST | `/items/{id}/delete` | Delete item |
| GET | `/shopping-list.csv` | Export inventory CSV |

## Project structure
```text
src/main/java/com/grocery/
├── SmartGroceryApplication.java
├── config/AppConfig.java
├── controller/
│   ├── AuthController.java
│   └── GroceryController.java
├── model/
│   ├── AppUser.java
│   └── GroceryItem.java
└── repository/
    ├── UserRepository.java
    └── GroceryItemRepository.java

src/main/resources/
├── application.properties
├── static/styles.css
└── templates/
    ├── fragments.html
    ├── login.html
    ├── register.html
    ├── dashboard.html
    ├── inventory.html
    └── item-form.html
```

## Technology overview
- **Spring Boot / Spring MVC:** application startup and web routes
- **Thymeleaf:** server-rendered HTML templates
- **Spring Data JPA:** database persistence and repository access
- **H2:** local file-backed relational database for easy setup
- **BCrypt:** password hashing
- **Bootstrap + custom CSS:** responsive UI

## Screenshots
Create `docs/screenshots/` and add your own screenshots after running the app:
- `register.png`
- `login.png`
- `dashboard.png`
- `inventory.png`
- `add-item.png`
- `edit-item.png`
- `low-stock-alert.png`
- `expiry-alert.png`
- `csv-export.png`

## Important notes
- This is a student portfolio project, not a production-ready commercial deployment.
- For production, add CSRF protection, stronger validation, rate limiting, secure cookie settings, and a managed database.
- The current authentication uses an HTTP session and BCrypt password hashes. Do not reuse a real personal password for a demo.
- The application uses a local H2 database by default; PostgreSQL or MySQL can be configured later.

## Suggested GitHub topics
`java`, `spring-boot`, `thymeleaf`, `spring-data-jpa`, `h2-database`, `bootstrap`, `inventory-management`, `grocery-app`, `full-stack-development`, `student-project`
