# Finance Tracker — Android App

A personal finance expense tracking app built with Java, Room, MVVM, and Google Drive sync.

---

## Project Structure

```
app/src/main/java/com/financetracker/
├── data/
│   ├── db/           — Room database, entities, DAOs
│   └── repository/   — Repository layer (single source of truth)
├── ui/               — Fragments + ViewModels for each screen
├── service/          — SMS detection, Drive sync, WorkManager
└── util/             — Helper classes
```

---

## Setup Steps

### 1. Open in Android Studio

File → Open → select the `FinanceTracker` folder.
Android Studio will auto-detect it as a Gradle project.

### 2. Update `local.properties`

Set your Android SDK path:
```
sdk.dir=/Users/YOUR_USERNAME/Library/Android/sdk
```

### 3. Configure Google Drive API

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project → Enable **Google Drive API**
3. Create OAuth 2.0 credentials → Android app
4. Enter your app's package name: `com.financetracker`
5. Enter your SHA-1 debug fingerprint:
   ```
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   ```
6. Download `google-services.json` and place it in `app/`

### 4. Add JitPack repository (for MPAndroidChart)

In `build.gradle` (project level), the `repositories` block already includes:
```groovy
maven { url 'https://jitpack.io' }
```

### 5. SMS Permissions

On first launch, the app will request SMS read permission.
Grant it to enable automatic SMS transaction detection.

### 6. Sync Build + Run

Click **Sync Project with Gradle Files**, then **Run**.

---

## Screen Navigation

| Screen | Description |
|---|---|
| Dashboard | Monthly summary + recent transactions |
| Transactions | Full list with search |
| Add Transaction | Manual entry form |
| SMS Review | Approve/ignore detected SMS transactions |
| Accounts | Manage cash/bank/card accounts |
| Categories | Manage expense/income categories |
| Reports | Monthly charts (pie + bar) |
| Sync | Google Drive sync status + manual trigger |
| Settings | Currency, app lock, backup |

---

## Sync Design

- Every entity has `uuid`, `createdAt`, `updatedAt`, `deleted` fields
- Changes are logged in `sync_log` table
- Drive sync uploads JSON files per entity type to a `FinanceTrackerData` folder
- Conflict rule: **latest `updatedAt` wins**
- Background sync runs every hour via WorkManager when connected

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java |
| Architecture | MVVM + Repository |
| Database | Room (SQLite) |
| Charts | MPAndroidChart |
| Background | WorkManager |
| Drive Sync | Google Drive API v3 |
| Auth | Google Sign-In |
| UI | Material Components |
