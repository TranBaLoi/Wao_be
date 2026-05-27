# Health Connect, Workout Tracking, Workout Journal, and Step Flow

Tai lieu nay giai thich phan backend hien co trong repo `Wao_be` lien quan den:
- workout tracking tu mobile
- luong mon `Di bo`
- luu workout vao database
- workout journal / workout history
- step log / daily summary / thong ke
- ranh gioi giua backend nay va FE / Health Connect

Muc tieu:
- Chi ra file nao trong repo dang lam gi
- Noi ro du lieu nao FE lay tu Health Connect / GPS va gui sang BE
- Giai thich backend validate, map, luu DB nhu the nao
- Noi ro phan nao backend hien chua ho tro

---

## 1) Tong quan kien truc

Backend trong repo nay KHONG ket noi truc tiep toi Health Connect.

Backend chi dong vai tro:
- nhan payload workout / step do FE gui len
- validate payload
- luu vao MySQL qua JPA
- tra response de FE render workout journal / workout history
- tong hop daily summary

Health Connect, GPS, pedometer, sensor, dong ho, timer tap luyen deu nam ben FE.

Noi cach khac:
- FE la noi doc du lieu tu Health Connect
- BE la noi luu va tong hop du lieu sau khi FE da tinh hoac thu thap xong

Backend co ho tro metadata cho nguon du lieu, vi du:
- `HEALTH_CONNECT`
- `GPS`
- `SENSOR`
- `MANUAL`
- `ESTIMATED`

Nhung backend KHONG goi SDK Health Connect.

---

## 2) Cac file backend lien quan trong repo nay

### 2.1 Workout log

`src/main/java/com/example/wao_be/controller/WorkoutLogController.java`
- Expose API workout log:
  - `POST /api/users/{userId}/workout-logs`
  - `GET /api/users/{userId}/workout-logs?date=...`
  - `GET /api/users/{userId}/workout-logs/history?from=...&to=...`
  - `GET /api/users/{userId}/workout-logs/summary?from=...&to=...`
  - `DELETE /api/users/{userId}/workout-logs/{logId}`
- Sau khi tao / xoa workout log, controller goi `DailySummaryService.buildAndSave(...)`.

`src/main/java/com/example/wao_be/service/WorkoutLogService.java`
- Chua business logic chinh cua workout log.
- Validate request.
- Cho phep 2 nhom use case:
  - app cu: log theo `exerciseId` / `programId`
  - mobile tracking moi: log theo `workoutType`
- Tu suy ra:
  - `activityType` tu `workoutType`
  - `durationMin` tu `startedAt` + `endedAt` neu can
  - `logDate` tu `startedAt`
  - default source cho calories / distance / steps / heart rate
- Tao response cho history va summary.

`src/main/java/com/example/wao_be/dto/WorkoutLogDto.java`
- Dinh nghia contract request/response cho workout log.
- Request co ca `activityType` va alias `workoutType`.
- Response tra ve day du cac field FE can cho journal/history.

`src/main/java/com/example/wao_be/entity/UserWorkoutLog.java`
- Entity JPA map toi bang `user_workout_logs`.
- Chua cac cot workout tracking:
  - `activity_type`
  - `started_at`
  - `ended_at`
  - `duration_min`
  - `distance_meters`
  - `calories_burned`
  - `step_count`
  - `avg_speed_kmh`
  - `max_speed_kmh`
  - `avg_heart_rate`
  - `max_heart_rate`
  - `log_date`
  - `created_at`
  - source fields

`src/main/java/com/example/wao_be/mapper/WorkoutLogMapper.java`
- Map entity `UserWorkoutLog` sang `WorkoutLogDto.Response`.
- Response tra ve ca `activityType` va `workoutType`.

`src/main/java/com/example/wao_be/repository/UserWorkoutLogRepository.java`
- Query workout theo ngay va theo khoang ngay.
- Co cac ham aggregate de tinh tong calories, distance, steps cho daily summary.

### 2.2 Step log

`src/main/java/com/example/wao_be/controller/StepLogController.java`
- API cho step log theo ngay / khoang ngay.

`src/main/java/com/example/wao_be/service/StepLogService.java`
- Luu tong so buoc theo ngay.
- Neu da co ban ghi trong ngay thi update, khong tao moi.

`src/main/java/com/example/wao_be/entity/StepLog.java`
- Entity bang `step_logs`.
- Hien chi luu tong buoc theo ngay, KHONG luu bucket theo gio.

`src/main/java/com/example/wao_be/repository/StepLogRepository.java`
- Query step theo ngay / range.
- Tinh tong steps cua mot ngay.

### 2.3 Daily summary

`src/main/java/com/example/wao_be/controller/DailySummaryController.java`
- API xem daily summary hom nay / 1 ngay / lich su.

`src/main/java/com/example/wao_be/service/DailySummaryService.java`
- Tong hop du lieu ngay tu:
  - food logs
  - workout logs
  - water logs
  - step logs
- Luu / update vao `daily_summaries`.

`src/main/java/com/example/wao_be/entity/DailySummary.java`
- Bang tong hop theo composite key `user_id + log_date`.

### 2.4 Error handling

`src/main/java/com/example/wao_be/exception/GlobalExceptionHandler.java`
- Chuyen exception thanh JSON error response.
- Hien da tra message ro hon cho:
  - `User not found: ...`
  - `WorkoutProgram not found: ...`
  - `Exercise not found: ...`
  - `startedAt must be before endedAt.`
  - `Invalid workoutType: ...`

### 2.5 Migration DB

`src/main/resources/migrations/V2__extend_user_workout_logs_for_tracking.sql`
- Mo rong bang workout de chua tracking fields.

`src/main/resources/migrations/V3__extend_user_workout_logs_for_history_and_summary.sql`
- Them `step_source`, `created_at`, index `user_id + log_date`.

`src/main/resources/migrations/V4__add_cycling_activity_type_to_user_workout_logs.sql`
- Mo rong enum DB de support `CYCLING`.

---

## 3) FE lien ket voi backend nay o dau

Vi FE khong nam trong repo nay, backend chi biet FE thong qua contract API.

### 3.1 FE phai lam gi

FE la noi:
- bat dau / pause / resume / end session
- dem thoi gian tap
- doc GPS
- doc Health Connect
- doc step sensor / pedometer
- tinh calories neu can
- hien thi man hinh live tracking
- quyet dinh khi nao goi API save

### 3.2 FE goi backend khi nao

Thong thuong luong la:
1. User mo man `Di bo`.
2. FE bat dau tracking GPS / Health Connect / timer.
3. FE cap nhat live state tren man hinh.
4. User nhan `End`.
5. FE dong goi payload.
6. FE goi `POST /api/users/{userId}/workout-logs`.
7. BE validate va luu.
8. BE refresh `daily summary`.
9. FE goi:
   - `GET /api/users/{userId}/workout-logs/summary?...`
   - `GET /api/users/{userId}/workout-logs/history?...`
   de render journal/history.

### 3.3 FE khong duoc lam gi nua

Flow cu:
- FE goi `POST /api/exercises`
- fallback `categoryId = 1`
- roi moi luu workout

Flow nay sai cho mobile tracking.

Ly do:
- `Exercise` la catalog bai tap / noi dung tap, khong phai session tracking.
- `Di bo` la mot workout session, khong can tao exercise moi.
- Vi vay mon `Di bo` phai luu truc tiep qua `workout-logs`.

---

## 4) Luong mon `Di bo` tu Health Connect/GPS den database

Day la luong day du cua case `Di bo`.

### 4.1 Tren FE

FE thu thap:
- `startedAt`
- `endedAt`
- `durationMin`
- `distanceMeters`
- `stepCount`
- `avgSpeedKmh`
- `caloriesBurned`
- `note`

Nguon:
- `distanceMeters`: thuong tu GPS
- `stepCount`: co the tu Health Connect hoac pedometer
- `caloriesBurned`: co the do FE tinh, hoac lay tu Health Connect
- `avgSpeedKmh`: FE tinh tu distance + duration, hoac tu GPS stream

Neu FE muon danh dau nguon du lieu thi gui them:
- `distanceSource`
- `stepSource`
- `caloriesSource`

Gia tri co the la:
- `GPS`
- `HEALTH_CONNECT`
- `SENSOR`
- `MANUAL`
- `ESTIMATED`

### 4.2 Payload FE gui len backend

Vi du payload chuan cho `Di bo`:

```json
{
  "workoutType": "OUTDOOR_WALKING",
  "startedAt": "2026-04-01T17:20:00",
  "endedAt": "2026-04-01T17:23:33",
  "distanceMeters": 50.0,
  "durationMin": 3,
  "caloriesBurned": 12.5,
  "stepCount": 80,
  "avgSpeedKmh": 1.0,
  "note": "saved from mobile tracking",
  "distanceSource": "GPS",
  "stepSource": "HEALTH_CONNECT",
  "caloriesSource": "HEALTH_CONNECT"
}
```

`exerciseId` va `programId` de `null` hoac khong gui.

### 4.3 Backend nhan request o dau

Request vao:
- `WorkoutLogController.log(...)`

Controller:
- nhan `userId`
- nhan body `WorkoutLogDto.Request`
- goi `workoutLogService.log(userId, req)`
- neu save thanh cong thi goi tiep `dailySummaryService.buildAndSave(userId, response.getLogDate())`

### 4.4 Backend validate gi

Validation chinh nam trong `WorkoutLogService.validateRequest(...)`.

Rule:
- Khong duoc gui dong thoi `exerciseId` va `programId`
- Phai co it nhat 1 trong:
  - `exerciseId`
  - `programId`
  - `activityType`
  - `workoutType`
- Neu request tracking co `distanceMeters` / `startedAt` / `stepCount` / `avgSpeedKmh` ...
  thi phai co `activityType` hoac `workoutType`
- `startedAt` va `endedAt` phai di cung nhau
- `startedAt` phai truoc `endedAt`
- Neu khong gui `durationMin` thi backend tu tinh tu `startedAt` va `endedAt`
- Neu khong gui `logDate` thi backend tu suy ra `startedAt.toLocalDate()`
- `avgSpeedKmh` khong lon hon `maxSpeedKmh`
- `avgHeartRate` khong lon hon `maxHeartRate`

### 4.5 Backend map `workoutType` nhu the nao

Trong code hien tai:
- Request co 2 field:
  - `activityType`
  - `workoutType`
- `workoutType` la alias FE de de hieu hon
- Backend hop nhat 2 field nay qua ham `resolveActivityType(...)`
- Cuoi cung du lieu duoc luu vao cot `activity_type`

Tuc la:
- FE gui `workoutType = OUTDOOR_WALKING`
- BE luu `activity_type = OUTDOOR_WALKING`
- Response tra lai ca:
  - `activityType`
  - `workoutType`

### 4.6 Backend tao entity va luu DB

`WorkoutLogService.log(...)` tao `UserWorkoutLog.builder()` va set:
- `user`
- `exercise = null` neu la mobile tracking
- `program = null` neu la mobile tracking
- `activityType`
- `durationMin`
- `caloriesBurned`
- `distanceMeters`
- `avgSpeedKmh`
- `maxSpeedKmh`
- `stepCount`
- `avgHeartRate`
- `maxHeartRate`
- `caloriesSource`
- `distanceSource`
- `stepSource`
- `heartRateSource`
- `logDate`
- `startedAt`
- `endedAt`
- `note`

Sau do goi:

```text
workoutLogRepository.save(log)
```

Hibernate se insert vao bang `user_workout_logs`.

### 4.7 Luu vao bang gi

Bang luu chinh:
- `user_workout_logs`

Cot quan trong cho mon `Di bo`:
- `user_id`
- `activity_type = OUTDOOR_WALKING`
- `started_at`
- `ended_at`
- `duration_min`
- `distance_meters`
- `calories_burned`
- `step_count`
- `avg_speed_kmh`
- `log_date`
- `note`
- `created_at`

### 4.8 Sau khi luu workout thi gi xay ra

Ngay sau khi controller save xong:
- `DailySummaryService.buildAndSave(userId, logDate)` duoc goi

Service nay se:
- tinh tong calories an vao trong ngay
- tinh tong calories workout trong ngay
- tinh tong nuoc
- tinh tong buoc tu `step_logs`
- cap nhat bang `daily_summaries`

Luu y:
- `stepCount` trong `user_workout_logs` la so buoc cua rieng buoi workout
- `totalSteps` trong `daily_summaries` lai dang lay tu bang `step_logs`, khong lay tu sum workout logs
- Vi vay neu FE muon dashboard tong so buoc ngay chinh xac, FE van can dong bo step log theo ngay vao `/step-logs`

---

## 5) Man hinh live tracking hien thong so nhu the nao

Backend KHONG phuc vu live tracking theo real-time stream.

Man hinh live `Di bo` hien tai la do FE tu quan ly.

### 5.1 Du lieu hien tren man

Tren FE, cac o thong so thuong hien:
- tong km
- thoi gian
- calories
- so buoc
- toc do
- nhip tim

Nguon:
- GPS
- Health Connect
- Sensor
- Timer local

Backend chi nhan ket qua cuoi buoi, khong stream tung giay.

### 5.2 FE lien ket voi BE o diem nao

Chi khi:
- user End
- FE validate buoi tap hop le
- FE goi save API

Va khi:
- FE can render history / journal / summary

---

## 6) Workout Journal / Workout History hoat dong nhu the nao

Backend nay da co 2 API de phuc vu journal/history.

### 6.1 API summary

`GET /api/users/{userId}/workout-logs/summary?from=YYYY-MM-DD&to=YYYY-MM-DD`

Muc dich:
- tra danh sach nhom workout da tap trong khoang thoi gian
- de FE render man `Workout Journal`

Summary group theo 3 nhom:
- `EXERCISE`
- `PROGRAM`
- `ACTIVITY`

Voi mon `Di bo` tracking tu mobile:
- no thuoc nhom `ACTIVITY`
- `displayName` se la `OUTDOOR_WALKING`

Service thuc hien:
- `WorkoutLogService.getSummary(...)`

No lam gi:
- lay tat ca workout log theo range
- group theo:
  - exercise
  - program
  - activityType
- tinh:
  - `totalSessions`
  - `totalDurationMin`
  - `totalCaloriesBurned`
  - `totalDistanceMeters`
  - `totalStepCount`
  - `lastSessionAt`

### 6.2 API history

`GET /api/users/{userId}/workout-logs/history?from=YYYY-MM-DD&to=YYYY-MM-DD`

Muc dich:
- tra danh sach tung buoi tap de FE render man `Workout History`

Service thuc hien:
- `WorkoutLogService.getByUserAndDateRange(...)`

Sort:
- `startedAt` giam dan
- fallback `endedAt`
- fallback `createdAt`
- fallback `logDate`

### 6.3 Response mapper

`WorkoutLogMapper.toResponse(...)` map entity thanh response FE can:
- `id`
- `userId`
- `exerciseId`
- `exerciseName`
- `programId`
- `programName`
- `activityType`
- `workoutType`
- `durationMin`
- `caloriesBurned`
- `distanceMeters`
- `avgSpeedKmh`
- `maxSpeedKmh`
- `stepCount`
- `avgHeartRate`
- `maxHeartRate`
- source fields
- `logDate`
- `startedAt`
- `endedAt`
- `note`
- `createdAt`

---

## 7) Daily summary va thong ke tap luyen

### 7.1 Daily summary dung de lam gi

`daily_summaries` la bang tong hop nhanh theo ngay.

No phuc vu cac man:
- dashboard theo ngay
- tong kcal in / out
- tong nuoc
- tong steps
- goal achieved

### 7.2 Daily summary tinh tu dau

`DailySummaryService.buildAndSave(...)` tong hop:
- `totalCalIn` tu `user_food_logs`
- `totalCalOut` tu `user_workout_logs`
- `totalWater` tu `user_water_logs`
- `totalSteps` tu `step_logs`

Dieu quan trong:
- workout calories trong ngay duoc lay tu `user_workout_logs`
- tong so buoc trong ngay KHONG lay tu `user_workout_logs.step_count`
- tong so buoc trong ngay dang lay tu `step_logs`

Neu FE chi luu workout walking ma KHONG dong bo `step_logs`, thi:
- history walking van co `stepCount` tren moi buoi
- nhung `daily summary.totalSteps` co the bang 0 hoac khong dung

### 7.3 Statistics trong repo nay

`StatisticsController` / `StatisticsService` hien chu yeu phuc vu:
- nutrition statistics
- weight statistics

Repo nay CHUA co statistics API rieng cho:
- tong quang duong tap luyen theo tuan / thang
- tong calories workout theo chart
- step timeline theo gio

Workout journal/history hien dang di bang `workout-logs/summary` va `workout-logs/history`, khong qua `StatisticsController`.

---

## 8) Step flow va "so do buoc chan"

### 8.1 Hien backend co gi

Backend co:
- `POST /api/users/{userId}/step-logs`
- `GET /api/users/{userId}/step-logs/date?date=...`
- `GET /api/users/{userId}/step-logs?from=...&to=...`

Bang `step_logs` chi luu:
- `user_id`
- `step_count`
- `log_date`

Tuc la chi co tong buoc theo ngay.

### 8.2 Backend CHUA co gi

Backend hien chua co:
- bang luu step theo gio
- endpoint `timeline?bucket=hour`
- du lieu de ve bieu do buoc chan theo 24h tu server

Neu FE dang ve "so do buoc chan" theo gio tu Health Connect, thi phan do hien dang la logic FE/local.

### 8.3 Khi nao FE can goi step API

Co 2 truong hop:

1. FE muon dong bo tong buoc ngay len server
- Goi `POST /api/users/{userId}/step-logs`
- Vi du cuoi ngay, mo man dashboard, hoac sau khi doc tong buoc hom nay tu Health Connect

2. FE chi can step cua rieng buoi di bo
- Khong can `step-logs`
- Chi can gui `stepCount` trong `workout-logs`

### 8.4 Neu muon server ho tro so do buoc chan

Can bo sung them:
- bang moi luu bucket steps theo gio
  hoac
- endpoint sync timeline day du tu FE

Vi du:
- `step_log_timeline`
  - `user_id`
  - `bucket_start`
  - `bucket_end`
  - `step_count`
  - `source`

Va endpoint:
- `GET /api/users/{userId}/step-logs/timeline?date=YYYY-MM-DD&bucket=hour`

Hien tai repo nay chua co phan do.

---

## 9) Cac route quan trong ma FE can noi

### 9.1 Save workout tracking

Route dung:

```text
POST /api/users/{userId}/workout-logs
```

Khong dung:
- `POST /api/exercises`
- `POST /api/users/{userId}/workout-logs/` voi trailing slash neu FE/network layer dang map sai

### 9.2 Xem summary journal

```text
GET /api/users/{userId}/workout-logs/summary?from=YYYY-MM-DD&to=YYYY-MM-DD
```

### 9.3 Xem history workout

```text
GET /api/users/{userId}/workout-logs/history?from=YYYY-MM-DD&to=YYYY-MM-DD
```

### 9.4 Dong bo tong so buoc theo ngay

```text
POST /api/users/{userId}/step-logs
```

### 9.5 Xem daily summary

```text
GET /api/users/{userId}/daily-summaries?date=YYYY-MM-DD
GET /api/users/{userId}/daily-summaries/history?from=YYYY-MM-DD&to=YYYY-MM-DD
```

---

## 10) Response loi backend hien dang tra nhu the nao

Tat ca loi duoc dua qua `GlobalExceptionHandler`.

Case 404:
- `User not found: ...`
- `Exercise not found: ...`
- `WorkoutProgram not found: ...`

Case 400:
- `startedAt must be before endedAt.`
- `durationMin is required unless both startedAt and endedAt are provided.`
- `activityType or workoutType is required when tracking metrics are provided.`
- `Invalid workoutType: ... Supported values: ...`

Dieu nay rat quan trong cho FE:
- FE nen log `response body`
- khong chi log `HTTP 400/404`

---

## 11) Mapping backend hien tai voi man FE

### 11.1 Man `Di bo`

FE:
- live tracking
- GPS
- Health Connect
- hien timer
- hien thong so tren man

BE:
- nhan payload khi End
- luu session vao `user_workout_logs`
- refresh `daily_summaries`

### 11.2 Man `Workout Journal`

FE:
- goi `summary`
- render danh sach mon / loai tap da tap

BE:
- group workout logs theo `ACTIVITY` / `EXERCISE` / `PROGRAM`

### 11.3 Man `Workout History`

FE:
- goi `history`
- render tung session

BE:
- tra log theo range, sort session moi nhat len truoc

### 11.4 Man `Step chart`

FE:
- neu dang hien chart theo gio thi kha nang cao dang doc truc tiep Health Connect

BE:
- chi co tong so buoc theo ngay
- chua co hourly timeline

---

## 12) Nhung dieu can nho khi tiep tuc phat trien

### 12.1 Hien tai backend da lam du cho workout tracking MVP

Da ho tro:
- luu tracking truc tiep bang `workoutType`
- khong bat FE create exercise truoc
- luu walking / running / cycling vao `user_workout_logs`
- tra history / summary cho journal
- tra loi ro rang hon

### 12.2 Diem can luu y

- `workoutType` thuc chat dang duoc luu vao cot `activity_type`
- `daily summary.totalSteps` dang dua vao `step_logs`, khong dua vao tong `workout_logs.step_count`
- backend khong co real-time session store
- backend khong co hourly step timeline

### 12.3 Neu muon ho tro tot hon cho Health Connect trong tuong lai

Co the bo sung:
- sync session id tu FE
- luu raw source reference
- step timeline theo gio
- heart rate timeline theo buoi tap
- workout statistics API rieng theo tuan / thang

---

## 13) Ket luan ngan

Trong repo backend nay:
- Health Connect KHONG duoc tich hop truc tiep
- FE la noi doc du lieu suc khoe / GPS / sensor
- Backend la noi luu, validate, tong hop va phuc vu journal/history

Luong `Di bo` dung la:
- FE tracking local
- End session
- `POST /api/users/{userId}/workout-logs`
- BE luu vao `user_workout_logs`
- BE refresh `daily_summaries`
- FE doc `summary` va `history` de hien thi journal / workout history

Phan "so do buoc chan theo gio":
- hien chua nam trong backend nay
- neu FE dang hien thi duoc thi phan do dang o FE / Health Connect
- neu muon server hoa thi can them schema va endpoint moi
