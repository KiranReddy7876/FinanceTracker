# SMS Transaction Capture - Visual Flow Diagrams

## 1. High-Level System Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                      FinanceTracker App                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                ┌─────────────┼─────────────┐
                │             │             │
        ┌───────▼────────┐    │      ┌──────▼──────────┐
        │   SmsReceiver  │    │      │  MainActivity   │
        │ (Broadcast)    │    │      │   (UI/Nav)      │
        └───────┬────────┘    │      └──────────────────┘
                │             │
        ┌───────▼──────────┐  │   ┌──────────────────┐
        │  SmsParser       │  │   │ SmsImportFragment│
        │  (Extract Data)  │  │   │  (Review Queue)  │
        └───────┬──────────┘  │   └──────────────────┘
                │             │
        ┌───────▼──────────────┐     ┌──────────────────┐
        │  Account Matching    │────▶│  AccountRepository
        │  (last 4 digits)     │     │  (Database Query)
        └───────┬──────────────┘     └──────────────────┘
                │
        ┌───────▼──────────────┐
        │  SmsImport Record    │
        │  (PENDING status)    │
        └───────┬──────────────┘
                │
        ┌───────▼──────────────┐
        │  Notification        │
        │  (Alert User)        │
        └──────────────────────┘
                │
                │ User Review
                │
        ┌───────▼──────────────┐
        │ Confirm/Ignore       │
        │                      │
        └───────┬──────────────┘
                │
        ┌───────▼──────────────────┐
        │ SmsImportConversionSvc   │
        │ (Convert to Transaction) │
        └───────┬──────────────────┘
                │
        ┌───────▼──────────────┐
        │  Transaction Record  │
        │  (In Database)       │
        └──────────────────────┘
                │
        ┌───────▼──────────────┐
        │  Dashboard Update    │
        │  (Show in UI)        │
        └──────────────────────┘
```

## 2. Detailed SMS Processing Flow

```
Incoming SMS from Bank
       │
       ▼
┌──────────────────────────────┐
│ SmsReceiver.onReceive()      │
│ - Extract SMS content        │
│ - Verify format             │
└──────────────┬───────────────┘
               │
               ▼
        ┌──────────────────────────────┐
        │ SmsParser.isTransactionSms() │
        │ Check keywords:              │
        │ - debit/credit               │
        │ - transfer/payment           │
        │ - balance/charged            │
        └──────────────┬───────────────┘
                       │
            ┌──────────┴──────────┐
            │ (Yes)    │ (No)     │
            │          │          │
            ▼          └─► DISCARD
      ┌─────────────────┐
      │ SmsParser.parse()│
      │ Extract:        │
      │ - amount        │
      │ - type (E/I)    │
      │ - date          │
      │ - merchant      │
      └────────┬────────┘
               │
    ┌──────────┴──────────┐
    │ (Success) (Failure) │
    │          │          │
    ▼          └─► DISCARD
 ┌──────────────────────────────────┐
 │ SmsAccountNumberExtractor        │
 │ - Check if contains account#     │
 │ - Extract last 4 digits          │
 │   Patterns:                      │
 │   • •••1234 (masked)             │
 │   • xxxx1234                     │
 │   • A/C 1234                     │
 │   • ACCOUNT 1234                 │
 └────────┬─────────────────────────┘
          │
   ┌──────▼──────┐
   │ Account# found?
   └──────┬───────┘
          │
   ┌──────┴──────────────┐
   │ (Yes)    │ (No)    │
   │          │         │
   ▼          ▼         │
┌─────────┐ ┌────────┐  │
│ Match   │ │ Use    │  │
│ Account │ │ NULL   │  │
└────┬────┘ └───┬────┘  │
     │          │       │
     └──────┬───┘       │
            ▼           │
      ┌──────────────┐  │
      │ Found Match? │  │
      │ YES ─────────┼──┤
      │ NO  │        │  │
      └────┘        │  │
            │        │  │
            ▼        │  │
   ┌─────────────┐   │  │
   │ accountId = │   │  │
   │ <UUID>      │   │  │
   └─────────────┘   │  │
            │        │  │
            │        ▼  │
            │  ┌─────────────┐
            │  │ accountId = │
            │  │ null        │
            │  └─────────────┘
            │        │
            └───┬────┘
                ▼
        ┌───────────────────┐
        │ Create SmsImport  │
        │ Record:           │
        │ - uuid            │
        │ - smsText         │
        │ - amount          │
        │ - detectedType    │
        │ - date            │
        │ - accountId       │
        │ - status: PENDING │
        └────────┬──────────┘
                 │
                 ▼
        ┌────────────────┐
        │ Save to DB     │
        │ (ExecutorSvc)  │
        └────────┬───────┘
                 │
                 ▼
        ┌────────────────┐
        │ Show Notif:    │
        │ "Transaction   │
        │  Detected"     │
        └────────┬───────┘
                 │
                 ▼
        ┌────────────────┐
        │ Wait for User  │
        └────────────────┘
```

## 3. User Review & Confirmation Flow

```
User sees notification
       │
       ▼
┌─────────────────────────┐
│ Tap Notification        │
│ (or open SMS Imports)   │
└────────────┬────────────┘
             │
             ▼
    ┌────────────────────┐
    │ SmsImportFragment  │
    │ Shows pending list │
    └────────┬───────────┘
             │
             ▼
    ┌────────────────────┐
    │ User taps import   │
    │ to review          │
    └────────┬───────────┘
             │
             ▼
   ┌─────────────────────────────┐
   │ Review Dialog Shows:        │
   │ - Amount                    │
   │ - Type (EXPENSE/INCOME)     │
   │ - Full SMS text             │
   │ - Account spinner           │
   │ - Category spinner          │
   │ - Action buttons            │
   └────────┬────────────────────┘
            │
    ┌───────┼───────┬────────┐
    │       │       │        │
    │ Account │     │        │
    │ auto-   │     │        │
    │ filled? │     │        │
    │ YES ───┼──┐  │        │
    │ NO  ──┐│  │  │        │
    │   │  ││  │  │        │
    ▼   ▼  ▼▼  │  │        │
  User selects:
    │   Account (required)
    │   Category (optional)
    │
    └───┬──────────────────┐
        │                  │
    ┌───▼───┐  ┌───────┐  ┌─▼────┐
    │Confirm│  │ Ignore│  │Cancel│
    └───┬───┘  └───┬───┘  └──────┘
        │          │
        ▼          ▼
   ┌────────────────────────┐
   │ Confirm Action:        │
   │ 1. Update accountId    │
   │ 2. Update categoryId   │
   │ 3. Change status to    │
   │    CONFIRMED           │
   └────────┬───────────────┘
            │
            ▼
   ┌────────────────────────┐
   │ SmsImportRepository    │
   │ .confirm(uuid)         │
   └────────┬───────────────┘
            │
            ▼
   ┌────────────────────────┐
   │ Update DB:             │
   │ status = CONFIRMED     │
   │ accountId = selected   │
   │ categoryId = selected  │
   └────────┬───────────────┘
            │
            ▼
   ┌────────────────────────┐
   │ SmsImportConversion    │
   │ Service.convert()      │
   └────────┬───────────────┘
            │
            ▼
   ┌────────────────────────┐
   │ Create Transaction:    │
   │ - Generate new UUID    │
   │ - Copy amount          │
   │ - Copy type            │
   │ - Copy date            │
   │ - Copy accountId       │
   │ - Copy categoryId      │
   │ - Set referenceId to   │
   │   SmsImport UUID       │
   │ - Set note             │
   └────────┬───────────────┘
            │
            ▼
   ┌────────────────────────┐
   │ Insert Transaction     │
   │ into DB                │
   └────────┬───────────────┘
            │
            ▼
   ┌────────────────────────┐
   │ Transaction Created    │
   │ ✓ Appears in dashboard │
   │ ✓ Shows in account     │
   │ ✓ Updates balance      │
   │ ✓ Available in reports │
   └────────────────────────┘
```

## 4. Database State Transitions

```
Initial State:
┌─────────────────────┐
│ No SMS Imports      │
│ sms_import table    │
│ is empty            │
└─────────────────────┘

                │ SMS Arrives
                ▼
        ┌────────────────┐
        │ SmsImport      │
        │ Created:       │
        │                │
        │ uuid: xxx      │
        │ amount: 500    │
        │ type: EXPENSE  │
        │ account: null  │
        │ category: null │
        │ status:        │
        │ PENDING  ◄─────┤ No match
        └────────┬───────┘
                 │
        ┌────────▼────────┐
        │ (Auto-matched)  │
        │ OR              │
        │ (User selects)  │
        │ Update:         │
        │ accountId: yyy  │
        │ categoryId: zzz │
        └────────┬────────┘
                 │
        ┌────────▼────────┐
        │ SmsImport       │
        │ Updated:        │
        │                 │
        │ uuid: xxx       │
        │ amount: 500     │
        │ type: EXPENSE   │
        │ account: yyy ◄──┤ User sel.
        │ category: zzz   │
        │ status:         │
        │ PENDING         │
        └────────┬────────┘
                 │ User Confirms
                 ▼
        ┌────────────────┐
        │ SmsImport      │
        │ Confirmed:     │
        │                │
        │ uuid: xxx      │
        │ amount: 500    │
        │ type: EXPENSE  │
        │ account: yyy   │
        │ category: zzz  │
        │ status:        │
        │ CONFIRMED  ◄───┤ Status change
        └────────┬───────┘
                 │
        ┌────────▼────────────┐
        │ Transaction Created:│
        │                     │
        │ uuid: aaa (new)     │
        │ amount: 500         │
        │ type: EXPENSE       │
        │ accountId: yyy      │
        │ categoryId: zzz     │
        │ referenceId: xxx ◄──┤ Link to SMS
        │ note: Auto-import   │
        │ status: active      │
        └─────────────────────┘
```

## 5. Account Matching Decision Tree

```
                SMS Received
                    │
                    ▼
        Is transaction SMS?
        /                 \
      YES                 NO
       │                  │
       │             ► DISCARD
       │
       ▼
Can extract amount?
/              \
YES           NO
│              │
│         ► DISCARD
│
▼
Contains account#?
/                \
YES              NO
│                │
▼                ├─ Create SmsImport
Is valid format? │  accountId = null
/             \  │
YES          NO  │
│            │   │
├────┬───────┘   │
│    │           │
▼    │           │
Extract    │   └─► Continue
last 4     │
│          │
├──────────┘
│
▼
Search for account
accountNumberLast4 = ?
/            \
FOUND       NOT FOUND
│           │
├──┐        ├─► Create SmsImport
│  │        │   accountId = null
│  │        │   (User selects later)
│  │        │
│  ▼        │
│ Match     │
│ Found!    │
│           │
│ Create    │
│ SmsImport │
│ accountId │
│ = <UUID>  │
│ (auto)    │
│
└─► Continue
    with
    notification
```

## 6. Category Selection Flow

```
SmsImport created with type = "EXPENSE"
                    │
                    ▼
        Load Categories filtered by type
        (getCategoriesByType("EXPENSE"))
                    │
                    ▼
        ┌─────────────────────┐
        │ Spinner shows:      │
        │ - No Category       │
        │ - Groceries         │
        │ - Shopping          │
        │ - Bills             │
        │ - Fuel              │
        │ - Entertainment     │
        └──────────┬──────────┘
                   │
        ┌──────────┴──────────┐
        │ (User Action)       │
        │                     │
        ▼                     ▼
    ┌──────────┐        ┌─────────────┐
    │ Select   │        │ No Category │
    │ Category │        │ (Leave blank)
    │          │        │             │
    └────┬─────┘        └────┬────────┘
         │                   │
         ▼                   ▼
    categoryId = uuid   categoryId = null
    (e.g., "xxxx")      (e.g., null)
         │                   │
         └─────────┬─────────┘
                   │
                   ▼
        ┌─────────────────────┐
        │ Create Transaction  │
        │ with selected       │
        │ categoryId          │
        └─────────────────────┘
                   │
                   ▼
        ┌─────────────────────┐
        │ If categoryId null: │
        │ User can edit later │
        │ to add category     │
        └─────────────────────┘
```

## 7. Error Handling Flow

```
                 SMS Processing
                        │
        ┌───────────────┼────────────────┐
        │               │                │
        ▼               ▼                ▼
    Parse error    Account error    DB error
        │               │                │
        ├─ Log error ────┼────────────────┤
        │                │                │
        ▼                ▼                ▼
    DISCARD      accountId=null      RETRY/LOG
        │                │                │
        │        ┌────────┘                │
        │        ▼                         │
        │   Create SmsImport        Retry with
        │   (requires user           exponential
        │    action)                 backoff
        │        │                   │
        │        └────┬──────────────┘
        │             │
        └─────────┬───┘
                  │
                  ▼
    ┌──────────────────────┐
    │ Notification sent    │
    │ (if successful)      │
    │                      │
    │ Wait for user action │
    └──────────────────────┘
```

## 8. Notification Flow

```
SmsImport created successfully
        │
        ▼
SmsImportNotificationService
.notifyPendingImport()
        │
        ▼
Create Notification Channel
(Android 13+ requirement)
        │
        ▼
Build Notification:
- Title: "SMS Transaction Detected"
- Text: "1 pending review"
- Action: Launch MainActivity
- Extra: navigate_to = "sms_import"
        │
        ▼
Show Notification
        │
        ▼
User receives notification
        │
        ├─ Tap notification ─┐
        │                    │
        │                    ▼
        │            ┌──────────────┐
        │            │ Open SmsImport
        │            │ Fragment     │
        │            └──────────────┘
        │
        ├─ Ignore notification ─┐
        │                       │
        │                       ▼
        │              ┌──────────────┐
        │              │ Notification │
        │              │ Auto-clears  │
        │              │ when SMS is  │
        │              │ confirmed    │
        │              └──────────────┘
        │
        └─ Manual clear notification
                       │
                       ▼
              ┌──────────────┐
              │ Clear:       │
              │ NotificationID
              │ = 1001       │
              └──────────────┘
```

---

These diagrams provide a comprehensive visual understanding of the SMS transaction capture system's architecture, flows, and decision logic.

