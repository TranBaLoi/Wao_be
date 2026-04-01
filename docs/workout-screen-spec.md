# Workout Screen Spec

Tai lieu nay mo ta scope hien tai cua man `Luyen tap` theo yeu cau moi nhat:
- Fix cung 4 mon
- Giao dien chi hien thi mot so thong so chinh
- FE tu quan ly session trong luc tap
- BE chi luu khi user ket thuc buoi tap hop le

## 1) Danh sach mon co dinh

Man `Hoat dong tap luyen` chi hien thi 4 mon sau:
- `Chay bo ngoai troi`
- `Di bo`
- `Chay bo trong phong`
- `Dap xe`

Khong cho user tu them, sua, xoa mon tap trong scope nay.

## 2) Luong tong quan

1. User mo man `Hoat dong tap luyen`.
2. App hien thi 4 card mon tap co dinh.
3. User bam vao 1 mon.
4. App mo man tracking cua mon do.
5. User bam `Start` de bat dau.
6. FE bat dau dem thoi gian va thu thap thong so.
7. User bam `Pause` thi session tam dung.
8. Neu user nhan giu nut `Pause` trong 3 giay, app hien confirm ket thuc buoi tap.
9. Neu user xac nhan ket thuc:
   - Neu tong thoi gian tap < 3 phut: hien popup thong bao va khong luu.
   - Neu tong thoi gian tap >= 3 phut: tinh kcal, dong goi payload, goi API log buoi tap.
10. Sau khi luu thanh cong, app quay ve man truoc hoac refresh lich su tap.

## 3) Quy tac UI chung

Man chi tiet cua 4 mon deu dung chung bo khung:
- Header: ten mon tap
- Chi so lon o giua: tong quang duong
- 4 thong so nho ben duoi
- 2 nut duoi cung:
  - `Start`
  - `Pause`

Quy tac nut:
- Luc chua tap: nut trai la `Start`, nut phai co the an hoac an di
- Luc dang tap: hien `Pause`
- Luc da pause: co the cho nut trai thanh `Resume`
- Nhan giu nut `Pause` 3 giay de ket thuc buoi tap

Goi y thuc te de de lam FE:
- Nut chinh de dieu khien session: `Start` -> `Pause` -> `Resume`
- Hanh vi ket thuc: long press tren nut `Pause/Resume` trong 3 giay

## 4) Thong so hien thi theo tung mon

### 4.1 Di bo

Thong so hien thi:
- `Tong so km`
- `Toc do`
- `Kcal`
- `Nhip tim`

### 4.2 Chay bo ngoai troi

Thong so hien thi:
- `Tong so km`
- `Toc do`
- `Kcal`
- `Nhip tim`

### 4.3 Dap xe

Thong so hien thi:
- `Tong so km`
- `Toc do`
- `Kcal`
- `Nhip tim`

### 4.4 Chay bo trong phong

Thong so hien thi:
- `Tong so km`
- `Toc do`
- `Kcal`
- `So buoc`

## 5) Rule nghiep vu bat buoc

- Neu thoi gian tap `< 3 phut`:
  - Hien popup thong bao buoi tap qua ngan
  - Khong goi API luu log
  - Khong cap nhat daily summary

- Neu thoi gian tap `>= 3 phut`:
  - Cho phep luu log
  - Goi API tao `workout log`
  - Sau khi luu thanh cong thi refresh `daily summary`

## 6) Cach tinh du lieu tren FE

### 6.1 Tong so km

- Ngoai troi (`Di bo`, `Chay bo ngoai troi`, `Dap xe`):
  - Tinh tu GPS
  - Cong don quang duong giua cac diem hop le

- Trong phong (`Chay bo trong phong`):
  - Tinh tu toc do va thoi gian

Cong thuc:

```text
distanceKm = speedKmh * durationHour
distanceM = speedKmh * 1000 / 3600 * durationSec
```

### 6.2 Toc do

Cong thuc:

```text
speedKmh = distanceKm / durationHour
```

Goi y:
- Ngoai troi: hien `toc do hien tai` neu co GPS stream on dinh
- Neu GPS khong on dinh thi dung `toc do trung binh`
- Trong phong: dung toc do user nhap hoac toc do may

### 6.3 Nhip tim

- Neu co wearable/HealthKit/Google Fit thi hien gia tri hien tai hoac trung binh
- Neu khong co du lieu thi hien `--`

### 6.4 So buoc

Chi dung cho `Chay bo trong phong` trong scope nay.

Nguon du lieu:
- Pedometer neu co
- Neu khong co sensor thi co the an thong so nay hoac hien `--`

## 7) Cong thuc tinh kcal

## 7.1 Cong thuc chung

Cong thuc chuyen tu VO2/MET sang kcal:

```text
kcal = VO2 x weightKg / 200 x durationMin
```

Hoac:

```text
kcal = MET x 3.5 x weightKg / 200 x durationMin
```

App can lay `weightKg` tu health profile moi nhat cua user.

## 7.2 Di bo

Dung phuong trinh di bo mat phang:

```text
S = toc do m/phut
VO2 = 0.1 x S + 3.5
kcal = VO2 x weightKg / 200 x durationMin
```

Quy doi:

```text
S = speedKmh x 1000 / 60
```

## 7.3 Chay bo ngoai troi

Dung phuong trinh chay bo mat phang:

```text
S = toc do m/phut
VO2 = 0.2 x S + 3.5
kcal = VO2 x weightKg / 200 x durationMin
```

Neu ve sau co them do doc:

```text
VO2 = 0.2 x S + 0.9 x S x grade + 3.5
```

## 7.4 Chay bo trong phong

Dung phuong trinh treadmill:

```text
S = toc do m/phut
G = do doc / 100
VO2 = 0.2 x S + 0.9 x S x G + 3.5
kcal = VO2 x weightKg / 200 x durationMin
```

Neu khong co do doc, tam coi:

```text
G = 0
```

## 7.5 Dap xe

Scope MVP dung MET theo toc do trung binh:

| Avg speed (km/h) | MET |
|---|---|
| `< 16` | `4.0` |
| `16 - 19.2` | `6.8` |
| `19.3 - 22.4` | `8.0` |
| `22.5 - 25.6` | `10.0` |
| `25.7 - 30.6` | `12.0` |

Cong thuc:

```text
kcal = MET x 3.5 x weightKg / 200 x durationMin
```

## 8) State FE de xuat

```ts
type WorkoutType = 'WALK' | 'OUTDOOR_RUN' | 'TREADMILL_RUN' | 'CYCLING'

type WorkoutStatus = 'idle' | 'running' | 'paused' | 'saving' | 'finished'

type WorkoutSessionState = {
  type: WorkoutType
  status: WorkoutStatus
  durationSec: number
  distanceM: number
  speedKmh?: number
  heartRate?: number
  caloriesKcal: number
  steps?: number
  weightKg: number
}
```

Field rieng:
- `WALK`: GPS + heartRate neu co
- `OUTDOOR_RUN`: GPS + heartRate neu co
- `CYCLING`: GPS + heartRate neu co
- `TREADMILL_RUN`: speedKmh + steps, khong bat buoc GPS

## 9) Mapping UI theo tung mon

### 9.1 Di bo

- Chi so lon: `Tong so km`
- O 1: `Toc do`
- O 2: `Kcal`
- O 3: `Nhip tim`
- O 4: co the de trong, hien `--`, hoac dung cho `Thoi gian` neu can them

### 9.2 Chay bo ngoai troi

- Chi so lon: `Tong so km`
- O 1: `Toc do`
- O 2: `Kcal`
- O 3: `Nhip tim`
- O 4: co the de trong, hien `--`, hoac dung cho `Thoi gian` neu can them

### 9.3 Dap xe

- Chi so lon: `Tong so km`
- O 1: `Toc do`
- O 2: `Kcal`
- O 3: `Nhip tim`
- O 4: co the de trong, hien `--`, hoac dung cho `Thoi gian` neu can them

### 9.4 Chay bo trong phong

- Chi so lon: `Tong so km`
- O 1: `Toc do`
- O 2: `Kcal`
- O 3: `So buoc`
- O 4: co the de trong, hien `--`, hoac dung cho `Thoi gian` neu can them

Luu y:
- Ve UX, nen van hien `Thoi gian` duoi dang timer lon nho o khu thong so hoac gan nut dieu khien.
- Neu muon bam sat yeu cau user, van co the uu tien 4 thong so tren va dat timer o vi tri phu.

## 10) Payload luu backend

Co the tiep tuc dung API hien co:

`POST /api/users/{userId}/workout-logs`

Payload:

```json
{
  "exerciseId": 1,
  "durationMin": 12,
  "caloriesBurned": 96.5,
  "logDate": "2026-03-31",
  "note": "{\"type\":\"WALK\",\"distanceM\":950,\"speedKmh\":4.8,\"heartRate\":110}"
}
```

Goi y `note` theo tung mon:

- `WALK`
```json
{"type":"WALK","distanceM":1800,"speedKmh":5.1,"heartRate":108}
```

- `OUTDOOR_RUN`
```json
{"type":"OUTDOOR_RUN","distanceM":3200,"speedKmh":8.7,"heartRate":145}
```

- `CYCLING`
```json
{"type":"CYCLING","distanceM":6200,"speedKmh":18.4,"heartRate":132}
```

- `TREADMILL_RUN`
```json
{"type":"TREADMILL_RUN","distanceM":2500,"speedKmh":9.0,"steps":3100}
```

## 11) Xu ly popup duoi 3 phut

Logic FE:

```text
Khi user ket thuc buoi tap:
- Neu durationSec < 180:
  - hien popup: "Buoi tap duoi 3 phut se khong duoc luu. Ban co muon thoat?"
  - action: `Thoat khong luu` / `Tiep tuc tap`
- Neu durationSec >= 180:
  - cho phep luu
```

## 12) Checklist FE

- Tao man danh sach 4 mon co dinh
- Tao 4 route/man tracking dung chung 1 component voi config theo mon
- Xu ly GPS cho `Di bo`, `Chay bo ngoai troi`, `Dap xe`
- Xu ly timer active khi `running`
- Xu ly `Pause`
- Xu ly long press 3 giay de ket thuc
- Xu ly popup khi duration < 3 phut
- Tinh kcal tren FE truoc khi save
- Goi API luu workout log

## 13) Checklist BE

- Seed 4 mon co dinh trong bang `exercises`
- Dam bao API `POST /api/users/{userId}/workout-logs` luu duoc `exerciseId`
- Cho phep luu them du lieu chi tiet trong `note`
- Sau khi luu log thanh cong, refresh `daily summary`

## 14) Nguon tham khao cong thuc

- Cong thuc doi MET/VO2 sang kcal va thong tin Compendium:
  - https://pacompendium.com/corrected-mets/
- Bang MET chay bo:
  - https://pacompendium.com/running/
- Bang MET di bo:
  - https://pacompendium.com/walking/
- Bang MET dap xe:
  - https://pacompendium.com/bicycling/
- Tham chieu phuong trinh treadmill/walking/running theo ACSM duoc tong hop trong bai bao PMC:
  - https://pmc.ncbi.nlm.nih.gov/articles/PMC12419060/
