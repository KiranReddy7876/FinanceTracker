# SMS Transaction Review Flow - Visual Architecture

## Complete User Journey Map

```
┌─────────────────────────────────────────────────────────────────────┐
│                        SMS TRANSACTION FLOW                          │
└─────────────────────────────────────────────────────────────────────┘

                            BANK SMS RECEIVED
                                    │
                                    ▼
                        ┌──────────────────────┐
                        │   SmsReceiver        │
                        │ (Background process) │
                        └──────────────────────┘
                                    │
                    ┌───────────────┴───────────────┐
                    │                               │
                    ▼                               ▼
        ┌──────────────────────┐      ┌──────────────────────┐
        │  Account Match       │      │  No Account Match    │
        │  (Last 4 digits)     │      │  (Not found)         │
        └──────────────────────┘      └──────────────────────┘
                    │                               │
                    ▼                               ▼
        ┌──────────────────────┐      ┌──────────────────────┐
        │ AUTO-CONFIRMED       │      │  PENDING (Manual)    │
        │ (No user action)     │      │  (Needs review)      │
        └──────────────────────┘      └──────────────────────┘
                    │                               │
                    │                   ┌───────────┴─────────┐
                    │                   │                     │
                    │                   ▼                     ▼
                    │          ┌────────────────┐  ┌──────────────────┐
                    │          │ User Opens App │  │ Gets Notification│
                    │          │ Sees Dashboard │  │ Taps to Review   │
                    │          └────────────────┘  └──────────────────┘
                    │                   │                     │
                    │                   └─────────┬───────────┘
                    │                             │
                    │                             ▼
                    │                  ┌─────────────────────┐
                    │                  │ SMS Import Screen   │
                    │                  │ Shows pending list  │
                    │                  └─────────────────────┘
                    │                             │
                    │                             ▼
                    │                  ┌─────────────────────┐
                    │                  │ User clicks item    │
                    │                  │ Review dialog opens │
                    │                  └─────────────────────┘
                    │                             │
                    │              ┌──────────────┴──────────────┐
                    │              │                             │
                    │              ▼                             ▼
                    │      ┌─────────────────┐        ┌──────────────────┐
                    │      │ User selects    │        │ User selects     │
                    │      │ Account         │        │ Category         │
                    │      │ (Required)      │        │ (Optional)       │
                    │      └─────────────────┘        └──────────────────┘
                    │              │                             │
                    │              └──────────────┬──────────────┘
                    │                             │
                    │              ┌──────────────┴──────────────┐
                    │              │                             │
                    │              ▼                             ▼
                    │      ┌──────────────────┐       ┌─────────────────┐
                    │      │ [Confirm] Click  │       │ [Ignore] Click  │
                    │      └──────────────────┘       └─────────────────┘
                    │              │                             │
                    ▼              ▼                             ▼
        ┌─────────────────────────────────┐        ┌──────────────────┐
        │  TRANSACTION CREATED            │        │  SMS DISCARDED   │
        │  ├─ Insert in transactions DB   │        │  ├─ Status: IGN  │
        │  ├─ Link to SMS import record   │        │  └─ Hidden       │
        │  └─ Timestamp recorded          │        └──────────────────┘
        └─────────────────────────────────┘
                    │
                    ▼
        ┌─────────────────────────────────┐
        │  DASHBOARD UPDATES              │
        │  ├─ Recent Transactions shown   │
        │  ├─ Amount totals calculated   │
        │  ├─ Account balance updated    │
        │  └─ Category stats updated     │
        └─────────────────────────────────┘
                    │
                    ▼
        ┌─────────────────────────────────┐
        │  ✓ COMPLETE                     │
        │  Transaction now in records     │
        └─────────────────────────────────┘
```

---

## Screen Navigation Flow

```
┌────────────────────────────────────────────────────────────┐
│                    APP HOME SCREEN                         │
│                    (Dashboard)                             │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Monthly Income: ₹50,000                              │  │
│  │ Monthly Expense: ₹20,000                             │  │
│  │                                                      │  │
│  │ Recent Transactions        [Badge: 1] ← SMS pending │  │
│  │ ├─ ₹500 EXPENSE (Auto SMS) ← Recent transaction    │  │
│  │ └─ ₹1000 INCOME                                      │  │
│  │                                                      │  │
│  │ FAB: [+] Add Transaction                             │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
                           │
                   ┌───────┴───────┐
                   │               │
        Click Badge│               │Menu → SMS Imports
                   │               │
                   ▼               ▼
    ┌──────────────────────────────────────┐
    │     SMS IMPORT REVIEW SCREEN          │
    │     (Primary review interface)       │
    │  ┌────────────────────────────────┐  │
    │  │ Pending SMS Transactions       │  │
    │  │                                │  │
    │  │ ₹500.00          EXPENSE       │  │
    │  │ SMS: Dear customer, ₹500...    │  │ ← Item 1
    │  │ [Click to review]              │  │
    │  │                                │  │
    │  │ ₹1000.00         INCOME        │  │
    │  │ SMS: Your salary credited...   │  │ ← Item 2
    │  │ [Click to review]              │  │
    │  │                                │  │
    │  │ No more pending transactions   │  │ ← If empty
    │  └────────────────────────────────┘  │
    │                                      │
    │ Scroll up for more items ↑           │
    └──────────────────────────────────────┘
                   │
        Click any pending item
                   │
                   ▼
    ┌──────────────────────────────────────┐
    │     REVIEW DIALOG                     │
    │  ┌────────────────────────────────┐  │
    │  │ Amount:  ₹500.00               │  │
    │  │ Type:    EXPENSE               │  │
    │  │                                │  │
    │  │ SMS Text:                      │  │
    │  │ [Dear customer, ₹500 debited  │  │
    │  │  from your A/C •••1234 on     │  │
    │  │  Mar 19 at 10:00 AM. Balance  │  │
    │  │  ₹10,000] ↕ Scrollable        │  │
    │  │                                │  │
    │  │ Select Account:                │  │
    │  │ [HDFC Bank (Savings) ▼]        │  │
    │  │                                │  │
    │  │ Select Category:               │  │
    │  │ [Groceries ▼]                  │  │
    │  │                                │  │
    │  │ [Confirm] [Ignore] [Cancel]    │  │
    │  └────────────────────────────────┘  │
    │                                      │
    │ DialogResult = User Action           │
    └──────────────────────────────────────┘
        │              │              │
        │              │              │
   Confirm        Ignore          Cancel
        │              │              │
        ▼              ▼              ▼
    Success        Deleted        Stay
    (Toast:        (Toast:        (No
    "Transaction   "SMS            Action)
     recorded")    ignored")       │
        │              │           │
        ▼              ▼           ▼
    Return to SMS Import Screen
        │
        ▼
    List Updated
    (Item removed if confirmed/ignored)
        │
        ▼
    Back to Dashboard
    (Recent Transactions now shows new items)
```

---

## Data Models & Flow

### SmsImport Record

```
SmsImport {
  uuid: "sms-001"
  smsText: "Dear customer, ₹500 debited from A/C •••1234"
  amount: 500.0
  detectedType: "EXPENSE"
  date: 1710894000000
  accountId: null → "acc-001" (user selects)
  categoryId: null → "cat-grocery" (user selects, optional)
  status: "PENDING" → "CONFIRMED" → "IGNORED"
  createdAt: 1710894000000
  updatedAt: 1710894000000
  deleted: false
}
```

### Transaction Record (Created on Confirmation)

```
Transaction {
  uuid: "txn-001"
  accountId: "acc-001" (user selected)
  type: "EXPENSE"
  amount: 500.0
  date: 1710894000000
  categoryId: "cat-grocery" (user selected, nullable)
  merchantId: null
  note: "Auto-imported from SMS"
  referenceId: "sms-001" (audit trail)
  transferToAccountId: null
  createdAt: 1710894000000
  updatedAt: 1710894000000
  deleted: false
}
```

### Database Relationship

```
┌──────────────────┐
│   sms_import     │
├──────────────────┤
│ uuid (PK)        │
│ smsText          │
│ amount           │
│ detectedType     │
│ date             │
│ accountId (FK)   │──────┐
│ categoryId (FK)  │      │
│ status           │      │
│ createdAt        │      │
│ updatedAt        │      │
│ deleted          │      │
└──────────────────┘      │
                          │
                    ┌─────▼──────────┐
                    │ transactions   │
                    ├────────────────┤
                    │ uuid (PK)      │
                    │ accountId (FK) │◄─┘
                    │ type           │
                    │ amount         │
                    │ date           │
                    │ categoryId (FK)│
                    │ merchantId     │
                    │ note           │
                    │ referenceId    │──┐
                    │ (points to     │  │
                    │  sms-001)      │  │
                    │ createdAt      │  │
                    │ updatedAt      │  │
                    │ deleted        │  │
                    └────────────────┘  │
                                        │
                    ┌───────────────────┘
                    │
                    └─► audit trail/reference
```

---

## State Transitions

### SMS Import Status Lifecycle

```
                    ┌─────────────────────┐
                    │   SMS Received      │
                    │   Create SmsImport  │
                    └──────────┬──────────┘
                               │
                ┌──────────────▼──────────────┐
                │                             │
                ▼                             ▼
    ┌──────────────────────────┐  ┌────────────────────────────┐
    │  Account Matched?        │  │   No Account Match         │
    │  ✓ YES                   │  │   ✗ NO                     │
    └──────────────┬───────────┘  └────────┬───────────────────┘
                   │                       │
                   ▼                       ▼
    ┌──────────────────────────┐  ┌────────────────────────────┐
    │  Status: PENDING         │  │  Status: PENDING           │
    │  accountId: Populated    │  │  accountId: null           │
    │                          │  │                            │
    │  Auto-confirm triggered  │  │  Notification shown        │
    │  (No user action)        │  │  User must review          │
    │                          │  │                            │
    │  Status: CONFIRMED       │  │  Wait for user...          │
    │  Transaction created ✓   │  │                            │
    │                          │  │  User opens SMS Import     │
    │  Recent Transactions     │  │  Clicks to review          │
    │  updated immediately     │  │  Selects account & cat     │
    └──────────────────────────┘  │  Clicks Confirm/Ignore     │
                                  │                            │
                                  ├─► [Confirm]               │
                                  │   Status: CONFIRMED        │
                                  │   Transaction created ✓    │
                                  │   Recent Trans updated ✓    │
                                  │                            │
                                  └─► [Ignore]                │
                                      Status: IGNORED         │
                                      (Hidden from view)      │
                                      (No transaction)        │
    └────────────────────────────────────────────────────────┘
                               │
                               ▼
                    ┌──────────────────────┐
                    │   FINAL STATE        │
                    │ Transaction in DB ✓  │
                    │ Visible in Dashboard │
                    │ Dashboard updated    │
                    └──────────────────────┘
```

---

## Account & Category Selection Flow

### Account Selection
```
┌─ User opens dialog
│
├─ System fetches all accounts
│  (AccountRepository.getAllActive())
│
├─ Populates Account Spinner:
│  ├─ HDFC Bank (Savings)
│  ├─ ICICI Bank (Current)
│  ├─ Axis Credit Card
│  └─ Kotak Investment
│
├─ If account auto-matched:
│  └─ Pre-select that account
│
├─ User can change selection
│  (Tap dropdown → Select different account)
│
└─ User must confirm with valid selection
   (Validation: accountId != null)
```

### Category Selection
```
┌─ System detects transaction type
│  └─ From SmsImport.detectedType (EXPENSE or INCOME)
│
├─ Fetches filtered categories
│  └─ CategoryRepository.getByType(type)
│
├─ For EXPENSE type, shows:
│  ├─ — No Category —
│  ├─ Groceries
│  ├─ Utilities
│  ├─ Transport
│  ├─ Entertainment
│  └─ Healthcare
│
├─ For INCOME type, shows:
│  ├─ — No Category —
│  ├─ Salary
│  ├─ Bonus
│  ├─ Interest
│  └─ Dividend
│
├─ User can select or skip
│  └─ "— No Category —" is always available
│
└─ Selection is optional
   (Transaction works with or without category)
```

---

## User Actions & System Response

```
╔════════════════════════════════════════════════════════════════╗
║              ACTION                │          RESPONSE         ║
╠════════════════════════════════════════════════════════════════╣
║ Open notification                  │ → SMS Import screen opens ║
║ Tap badge on dashboard             │ → SMS Import screen opens ║
║ Menu → SMS Imports                 │ → SMS Import screen opens ║
╠════════════════════════════════════════════════════════════════╣
║ Click pending transaction in list  │ → Review dialog shows    ║
║ Click [Confirm]                    │ → Validates & creates   ║
║ Click [Ignore]                     │ → Discards & hides      ║
║ Click [Cancel]                     │ → Closes, stays pending ║
╠════════════════════════════════════════════════════════════════╣
║ Select account from dropdown       │ → Spinner updates       ║
║ Select category from dropdown      │ → Spinner updates       ║
║ Scroll SMS text                    │ → Text scrolls (long msg)║
╠════════════════════════════════════════════════════════════════╣
║ AFTER CONFIRM:                     │                         ║
║ - View Recent Transactions         │ → New transaction shown ║
║ - View Account Details             │ → Amount in history     ║
║ - View Dashboard totals            │ → Recalculated          ║
║ - View SMS Import again            │ → Item removed          ║
╚════════════════════════════════════════════════════════════════╝
```

---

## Error Handling

```
┌─────────────────────────────────────────────┐
│          VALIDATION & ERRORS                │
└─────────────────────────────────────────────┘

Validation Check 1:
├─ Account selected?
│  ├─ ✓ YES → Continue
│  └─ ✗ NO → Toast: "Please select an account"
│           Return to dialog (no action)

Validation Check 2:
├─ Amount valid?
│  ├─ ✓ YES (from SMS) → Continue
│  └─ ✗ NO → SMS already filtered by SmsParser
│           Shouldn't reach here

Validation Check 3:
├─ Transaction type valid?
│  ├─ ✓ YES (EXPENSE/INCOME) → Continue
│  └─ ✗ NO → SMS already validated
│           Shouldn't reach here

Validation Check 4:
├─ Database accessible?
│  ├─ ✓ YES → Insert transaction
│  └─ ✗ NO → Toast: "Error saving transaction"
│           SMS remains PENDING
│           User can retry

Success:
└─ All checks pass → Transaction created ✓
                   → Toast: "Transaction recorded"
                   → Dialog closes
                   → SMS Import screen updates
```

---

## Summary Table

| Screen | Purpose | Accessed From | Shows |
|--------|---------|---------------|-------|
| **Dashboard** | Overview | App home | Monthly totals, Recent Transactions |
| **SMS Import** | Review pending | Notification, Badge, Menu | List of pending SMS imports |
| **Review Dialog** | Approve transaction | Click item in SMS Import list | Details, account dropdown, category dropdown |
| **Recent Trans** | View transactions | Dashboard, Menu | All transactions in account |
| **Account Details** | Account history | Accounts menu | Transactions for that account |

---

**Key Insight:**
```
┌─────────────────────────────────────────────────────────────┐
│  Automatic SMS (Matched Account)                            │
│  └─→ Skip SMS Import screen → Direct to dashboard          │
│                                                             │
│  Manual SMS (No Match)                                      │
│  └─→ SMS Import screen → Review dialog → Confirm           │
│      → Dashboard updated → Done                            │
└─────────────────────────────────────────────────────────────┘
```

