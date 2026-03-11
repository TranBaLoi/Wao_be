# 🏃 Wao_be — Health Tracking Backend

Backend RESTful API cho ứng dụng theo dõi sức khỏe cá nhân, xây dựng bằng **Spring Boot 4** + **MySQL**.

---

## 📋 Mục lục

- [Công nghệ sử dụng](#-công-nghệ-sử-dụng)
- [Yêu cầu hệ thống](#-yêu-cầu-hệ-thống)
- [Cấu trúc dự án](#-cấu-trúc-dự-án)
- [Cài đặt & Chạy dự án](#-cài-đặt--chạy-dự-án)
- [Cấu hình Database](#-cấu-hình-database)
- [Mô hình dữ liệu](#-mô-hình-dữ-liệu)
- [API Endpoints](#-api-endpoints)

---

## 🛠 Công nghệ sử dụng

| Công nghệ | Phiên bản |
|---|---|
| Java | 21 |
| Spring Boot | 4.0.3 |
| Spring Data JPA | ✅ |
| Spring Validation | ✅ |
| MySQL Connector | ✅ |
| Lombok | ✅ |
| Maven | 3.x |

---

## 💻 Yêu cầu hệ thống

Trước khi bắt đầu, hãy đảm bảo máy tính đã cài đặt:

- **JDK 21** trở lên → [Tải tại đây](https://adoptium.net/)
- **Maven 3.8+** → [Tải tại đây](https://maven.apache.org/download.cgi) *(hoặc dùng `mvnw` wrapper có sẵn)*
- **MySQL 8.0+** → [Tải tại đây](https://dev.mysql.com/downloads/mysql/)
- **IDE** (khuyến nghị IntelliJ IDEA)

---

## 📁 Cấu trúc dự án

```
Wao_be/
├── src/
│   ├── main/
│   │   ├── java/com/example/wao_be/
│   │   │   ├── controller/       # REST Controllers (xử lý HTTP request)
│   │   │   ├── dto/              # Data Transfer Objects (request/response)
│   │   │   ├── entity/           # JPA Entities (ánh xạ bảng DB)
│   │   │   ├── exception/        # Xử lý ngoại lệ toàn cục
│   │   │   ├── repository/       # Spring Data JPA Repositories
│   │   │   ├── service/          # Business Logic
│   │   │   └── WaoBeApplication.java
│   │   └── resources/
│   │       └── application.properties   # Cấu hình ứng dụng
│   └── test/
├── pom.xml                              # Quản lý dependencies
└── README.md
```

---

## 🚀 Cài đặt & Chạy dự án

### Bước 1 — Clone dự án

```bash
git clone https://github.com/<your-username>/Wao_be.git
cd Wao_be
```

### Bước 2 — Tạo Database MySQL

Mở MySQL Workbench hoặc terminal MySQL, chạy lệnh:

```sql
CREATE DATABASE wao CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### Bước 3 — Cấu hình kết nối Database

Mở file `src/main/resources/application.properties` và chỉnh sửa thông tin phù hợp:

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/wao
spring.datasource.username=root
spring.datasource.password=123456
```

> ⚠️ **Lưu ý:** Mặc định đang dùng cổng `3307`. Nếu MySQL của bạn chạy ở cổng `3306` (mặc định), hãy thay `3307` thành `3306`.

### Bước 4 — Chạy ứng dụng

**Cách 1: Dùng Maven Wrapper (khuyến nghị)**

```bash
# Windows (PowerShell / CMD)
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

**Cách 2: Dùng Maven đã cài**

```bash
mvn spring-boot:run
```

**Cách 3: Qua IntelliJ IDEA**

1. Mở dự án trong IntelliJ IDEA
2. Tìm file `WaoBeApplication.java`
3. Nhấn nút ▶️ **Run** (hoặc `Shift + F10`)

### Bước 5 — Kiểm tra ứng dụng

Ứng dụng chạy thành công khi thấy log:

```
Started WaoBeApplication in x.xxx seconds
```

Truy cập: `http://localhost:8080`

---

## ⚙️ Cấu hình Database

File `application.properties` đầy đủ:

```properties
# Tên ứng dụng
spring.application.name=Wao_be

# Kết nối MySQL
spring.datasource.url=jdbc:mysql://localhost:3307/wao
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA / Hibernate
spring.jpa.hibernate.ddl-auto=update     # Tự động tạo/cập nhật bảng
spring.jpa.show-sql=true                 # Hiển thị SQL ra console
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Server
server.port=8080
```

> 💡 `ddl-auto=update` sẽ tự động tạo các bảng trong DB dựa trên Entity khi khởi động lần đầu.

---

## 🗄️ Mô hình dữ liệu

### 👤 Module User (Người dùng)

| Bảng | Mô tả |
|---|---|
| `Users` | Thông tin đăng nhập (email, password, full_name, status) |
| `UserHealthProfiles` | Hồ sơ sức khỏe (giới tính, ngày sinh, chiều cao, cân nặng, mức hoạt động, mục tiêu, target_calories) |

### 🍎 Module Food & Nutrition (Dinh dưỡng)

| Bảng | Mô tả |
|---|---|
| `Foods` | Thông tin dinh dưỡng (calo, protein, carbs, fat, is_verified) |
| `MealPlans` | Kế hoạch bữa ăn (System_Suggestion / User_Custom) |
| `MealPlanFoods` | Chi tiết thực phẩm trong từng kế hoạch |
| `UserFoodLogs` | Nhật ký ăn uống hàng ngày của user |

### 🏋️ Module Exercise (Tập luyện)

| Bảng | Mô tả |
|---|---|
| `ExerciseCategories` | Danh mục bài tập |
| `Exercises` | Dữ liệu bài tập (video_url, calories_per_min) |
| `WorkoutPrograms` | Chương trình tập luyện tổng thể |
| `ProgramExercises` | Chi tiết bài tập trong chương trình (sets, reps, rest_time) |
| `UserWorkoutLogs` | Nhật ký tập luyện thực tế của user |

### 📊 Module Tracking (Theo dõi)

| Bảng | Mô tả |
|---|---|
| `StepLogs` | Nhật ký bước chân hàng ngày |
| `UserWaterLogs` | Nhật ký uống nước |
| `DailySummaries` | Tổng kết ngày (calo vào/ra, nước, bước chân, đạt mục tiêu chưa) |

---

## 🔌 API Endpoints

| Module | Base URL |
|---|---|
| User | `POST /api/users/register`, `POST /api/users/login` |
| Health Profile | `/api/health-profiles` |
| Food | `/api/foods` |
| Food Log | `/api/food-logs` |
| Meal Plan | `/api/meal-plans` |
| Exercise | `/api/exercises` |
| Workout Program | `/api/workout-programs` |
| Workout Log | `/api/workout-logs` |
| Step Log | `/api/step-logs` |
| Water Log | `/api/water-logs` |
| Daily Summary | `/api/daily-summaries` |

> 📌 Tất cả API đều hỗ trợ các phương thức: `GET`, `POST`, `PUT`, `DELETE`

---

## 🐛 Xử lý lỗi thường gặp

### Lỗi kết nối Database
```
Communications link failure
```
→ Kiểm tra MySQL đang chạy và đúng cổng trong `application.properties`

### Lỗi Access Denied
```
Access denied for user 'root'@'localhost'
```
→ Kiểm tra lại `username` và `password` trong `application.properties`

### Lỗi Port đã được sử dụng
```
Port 8080 already in use
```
→ Đổi port trong `application.properties`: `server.port=8081`

---

## 👨‍💻 Tác giả

Dự án được phát triển phục vụ ứng dụng theo dõi sức khỏe di động **Wao**.

