# Workout UI/UX Prompts

Tai lieu nay tong hop cac prompt UI/UX de gui cho ben FE/UI code man `Luyen tap`.

Scope da chot:
- Chi co 4 mon co dinh: `Di bo`, `Chay bo ngoai troi`, `Chay bo trong phong`, `Dap xe`
- FE tu xu ly tracking trong qua trinh tap
- BE chi nhan du lieu cuoi buoi de luu log
- Neu tap duoi 3 phut thi popup va khong luu

## Prompt 1: Tong the feature

```text
Thiet ke va code UI/UX cho feature "Luyen tap" trong mobile app suc khoe.

Yeu cau co dinh:
- Chi hien thi 4 mon: Di bo, Chay bo ngoai troi, Chay bo trong phong, Dap xe
- Khong co tinh nang them/xoa/sua mon tap
- Visual gon, sach, de nhin khi dang van dong
- Chu so lon, contrast cao, uu tien thao tac 1 tay
- Giao dien tracking theo style toi gian, nen den, chi so trang, nut tron mau noi bat

Can tao 2 man chinh:
1. Man danh sach mon tap
2. Man tracking chi tiet cua tung mon

Rule nghiep vu:
- Bam vao tung mon se mo man tracking rieng
- Co nut Start va Pause
- Khi nhan giu nut Pause trong 3 giay thi ket thuc buoi tap
- Neu tong thoi gian tap duoi 3 phut thi hien popup va khong luu
- Neu tu 3 phut tro len thi cho phep luu

Thong so hien thi:
- Di bo: Tong so km, Toc do, Kcal, Nhip tim
- Chay bo ngoai troi: Tong so km, Toc do, Kcal, Nhip tim
- Dap xe: Tong so km, Toc do, Kcal, Nhip tim
- Chay bo trong phong: Tong so km, Toc do, Kcal, So buoc

Yeu cau UX:
- Co state ro rang: idle, running, paused
- Khi running thi man hinh phai toi gian, de user nhin nhanh
- Khi paused thi hien trang thai tam dung ro rang
- Long press 3 giay de ket thuc phai co animation vong tron hoac progress de user biet dang giu
- Neu sensor khong co du lieu nhip tim thi hien --
- Distance hien theo m neu < 1000m, hien km neu >= 1000m
- Speed hien theo km/h
- Calories hien theo kcal

Hay tao UI theo huong hien dai, don gian, de code React Native.
```

## Prompt 2: Man danh sach 4 mon

```text
Code giao dien mobile cho man "Hoat dong tap luyen".

Yeu cau:
- Man nay chi hien thi 4 mon co dinh:
  - Di bo
  - Chay bo ngoai troi
  - Chay bo trong phong
  - Dap xe
- Khong co danh sach dong, khong co search, khong co filter
- Moi mon la 1 card/buton lon, bo tron, de bam
- Moi card co:
  - icon mon tap
  - ten mon
  - mo ta ngan 1 dong neu can
- Layout 2 cot tren mobile
- Khoang cach rong, de nhin, de cham
- Mau sac nhat quan voi man tracking

UX:
- Bam card nao thi mo man tracking cua mon do
- Transition nhe, muot, nhanh
- Giao dien nen toi gian, khong qua nhieu thong tin phu

Output mong muon:
- 1 screen React Native hoac Flutter de code duoc ngay
- component card co the tai su dung
- route cho 4 mon
```

## Prompt 3: Man tracking chung

```text
Code man tracking tap luyen mobile theo style toi gian, fullscreen, nen den.

Bo cuc:
- Header tren cung:
  - ben trai: ten mon tap
  - ben phai: icon setting hoac more
- O giua man hinh:
  - chi so lon nhat: Tong so km
  - don vi nho hon ngay ben duoi hoac ben canh
- Ben duoi la 4 thong so phu dang grid 2 cot:
  - Toc do
  - Kcal
  - Nhip tim hoac So buoc tuy mon
  - 1 o con lai co the la thoi gian hoac de trong tuy layout
- Duoi cung co 2 nut tron lon:
  - nut trai: Start hoac Resume
  - nut phai: Pause

State UI:
- Idle:
  - hien thong so mac dinh = 0 hoac --
  - nut chinh la Start
- Running:
  - cap nhat thong so realtime
  - nut Pause hien ro rang
- Paused:
  - hien nhan "Tam dung"
  - cho phep Resume

Interaction:
- Nhan giu nut Pause trong 3 giay de ket thuc session
- Khi long press, hien progress vong tron quanh nut
- Neu tha tay som thi huy hanh dong ket thuc

Yeu cau visual:
- Font lon, de doc khi dang van dong
- Khoang trong nhieu
- Khong dung qua nhieu text
- Button contrast cao
- Chuyen dong nhe, khong lam user roi

Code theo huong de tach reusable component:
- Header
- MainMetric
- MetricGrid
- CircularActionButton
- LongPressFinishOverlay
```

## Prompt 4: Man tracking cho tung mon

```text
Tao 4 variant UI cua man tracking, cung 1 layout goc nhung khac label thong so theo tung mon:

1. Di bo
- Main metric: Tong so km
- Metrics:
  - Toc do
  - Kcal
  - Nhip tim
  - Thoi gian

2. Chay bo ngoai troi
- Main metric: Tong so km
- Metrics:
  - Toc do
  - Kcal
  - Nhip tim
  - Thoi gian

3. Dap xe
- Main metric: Tong so km
- Metrics:
  - Toc do
  - Kcal
  - Nhip tim
  - Thoi gian

4. Chay bo trong phong
- Main metric: Tong so km
- Metrics:
  - Toc do
  - Kcal
  - So buoc
  - Thoi gian

Yeu cau:
- Dung chung 1 component screen va render theo config cua mon tap
- Label va don vi thay doi theo mode
- Neu nhip tim khong co du lieu thi hien --
- Neu so buoc chua co thi hien 0 hoac --
- Uu tien code clean, de mo rong sau nay
```

## Prompt 5: Popup duoi 3 phut

```text
Code popup xac nhan khi user ket thuc buoi tap nhung tong thoi gian duoi 3 phut.

Noi dung popup:
- Title: Buoi tap qua ngan
- Message: Buoi tap duoi 3 phut se khong duoc luu. Ban muon thoat hay tiep tuc tap?
- Actions:
  - Thoat khong luu
  - Tiep tuc tap

Yeu cau UX:
- Popup gon, ro, khong qua nhieu text
- Nut "Tiep tuc tap" la primary action
- Nut "Thoat khong luu" la secondary/destructive action
- Khi chon "Thoat khong luu" thi dong session va quay lai man truoc
- Khi chon "Tiep tuc tap" thi quay lai state paused hoac running truoc do

Visual:
- Dong bo voi theme toi gian cua man tracking
```

## Prompt 6: Long press finish

```text
Code interaction long press 3 giay tren nut Pause de ket thuc buoi tap.

Yeu cau:
- User phai nhan giu lien tuc 3 giay moi kich hoat ket thuc
- Trong luc giu, hien progress ring hoac progress fill quanh nut
- Co rung nhe hoac feedback nhe khi long press bat dau
- Neu tha tay truoc 3 giay thi reset progress ve 0
- Sau 3 giay:
  - Neu thoi gian tap < 3 phut thi hien popup "Buoi tap qua ngan"
  - Neu thoi gian tap >= 3 phut thi hien confirm ket thuc hoac save truc tiep tuy logic

Can code interaction nay muot, ro, khong gay bam nham.
```

## Prompt 7: Prompt chi tiet de giao cho FE code thang

```text
Hay code feature mobile "Workout Tracking" cho app suc khoe voi cac yeu cau sau:

Scope:
- 4 activity modes fixed:
  - WALK
  - OUTDOOR_RUN
  - TREADMILL_RUN
  - CYCLING

Screens:
1. WorkoutActivityListScreen
2. WorkoutTrackingScreen
3. ShortWorkoutPopup

WorkoutActivityListScreen:
- Hien thi 4 card co dinh
- Bam card de navigate sang WorkoutTrackingScreen voi mode tuong ung

WorkoutTrackingScreen:
- Layout fullscreen, dark theme
- Header hien title theo mode
- Main metric la distance
- 4 metrics phu:
  - WALK: speed, kcal, heartRate, duration
  - OUTDOOR_RUN: speed, kcal, heartRate, duration
  - CYCLING: speed, kcal, heartRate, duration
  - TREADMILL_RUN: speed, kcal, steps, duration
- Bottom actions:
  - idle -> Start
  - running -> Pause
  - paused -> Resume
- Long press nut Pause/Resume trong 3 giay de finish

Business rules:
- duration < 180s => show ShortWorkoutPopup va khong save
- duration >= 180s => cho phep save
- heartRate unavailable => show --

Technical expectations:
- tach component ro rang
- state machine don gian: idle/running/paused/saving
- code de mock data duoc
- de sau nay noi voi API backend
- clean architecture, de maintain

Uu tien:
- de doc
- de dung
- de code nhanh
- khong over-engineer
```

## Prompt 8: Prompt cho designer

```text
Hay thiet ke UI/UX cho mobile workout tracking screen trong app suc khoe.

Concept:
- toi gian
- tap trung vao tracking realtime
- dang van dong van doc nhanh duoc
- hien dai, sach, co cam giac "fitness premium"

Phai co:
- 1 man danh sach 4 mon co dinh
- 1 man tracking chung cho 4 mon
- 1 popup buoi tap qua ngan

4 mon:
- Di bo
- Chay bo ngoai troi
- Chay bo trong phong
- Dap xe

Metrics:
- 3 mon ngoai troi: Tong so km, Toc do, Kcal, Nhip tim
- 1 mon trong phong: Tong so km, Toc do, Kcal, So buoc

Interaction:
- Start
- Pause
- Resume
- Long press 3 giay de ket thuc
- Neu buoi tap duoi 3 phut thi popup thong bao va khong luu

Style guide:
- dark background
- white typography
- green/orange action buttons
- metric number rat lon
- labels nho gon
- bo cuc thong thoang
- uu tien thao tac ngon tay cai

Can output:
- mockup mobile
- component states
- interaction notes
- spacing, color, typography suggestion
```
