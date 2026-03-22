# SMS Text & Merchant NickName - Visual Guide

## Feature Overview

### Before & After

#### BEFORE Implementation
```
SMS: "Your A/C •••1234 debited Rs.500 to AMAZON INDIA PVT LTD on 14-Mar-2024"

Recent Transactions List:
┌────────────────────────────────────────┐
│ SMS Import - AMAZON INDIA...  14 Mar   │ ← Limited info
│ Rs. -500  [EXPENSE]                    │
└────────────────────────────────────────┘
```

#### AFTER Implementation
```
SMS: "Your A/C •••1234 debited Rs.500 to AMAZON INDIA PVT LTD on 14-Mar-2024"

Recent Transactions List:
┌────────────────────────────────────────┐
│ Your A/C •••1234 debited...  14 Mar    │ ← Full SMS text shown!
│ Rs. -500  [EXPENSE]                    │
└────────────────────────────────────────┘

Tap to view full details:
┌────────────────────────────────────────┐
│ Transaction Details                    │
├────────────────────────────────────────┤
│ Date: 14-Mar-2024                      │
│ Amount: Rs. -500                       │
│ Type: EXPENSE                          │
│ Merchant: AMAZON INDIA PVT LTD         │
│ Note: Your A/C •••1234 debited         │
│       Rs.500 to AMAZON INDIA...        │
│ ✓ Full SMS message preserved           │
└────────────────────────────────────────┘
```

---

## Display Priority Chain

### Visual Flowchart

```
                    Transaction Item
                           │
                           ▼
                   ┌─ Has Note Field? ─┐
                   │                   │
                  YES                 NO
                   │                   │
                   ▼                   │
            ┌─────────────┐            │
            │ Display Note│            │
            │ (SMS Text)  │            │
            └─────────────┘            │
                                       ▼
                            ┌─ Has Merchant? ─┐
                            │                 │
                           YES               NO
                            │                 │
                            ▼                 │
                    ┌─ Has nickName? ─┐     │
                    │                 │     │
                   YES               NO     │
                    │                 │     │
                    ▼                 ▼     │
            ┌─────────────┐  ┌──────────┐  │
            │  Display    │  │ Display  │  │
            │  nickName   │  │   Name   │  │
            └─────────────┘  └──────────┘  │
                                           ▼
                                  ┌──────────────┐
                                  │ Display Type │
                                  │ (EXPENSE/    │
                                  │ INCOME/etc)  │
                                  └──────────────┘
```

---

## Data Model Relationships

```
┌─────────────────────────────────────────────────────────────┐
│                     TRANSACTION ENTITY                      │
├─────────────────────────────────────────────────────────────┤
│ uuid: "abc123"                                              │
│ type: "EXPENSE"                                             │
│ amount: 500.0                                               │
│ date: 1710432000000                                         │
│ note: "Your A/C •••1234 debited Rs.500 to AMAZON..." ◄──┐ │
│ merchantId: "xyz789" ────┐                                │ │
│ categoryId: "cat456"     │                                │ │
└─────────────────────────┼──┬───────────────────────────────┘
                          │  │
                          │  └──── Stored from SmsImport.smsText
                          │
                          ▼
           ┌──────────────────────────────┐
           │    MERCHANT ENTITY           │
           ├──────────────────────────────┤
           │ uuid: "xyz789"               │
           │ name: "AMAZON INDIA PVT LTD" │
           │ nickName: "Amazon" ◄─── NEW!│
           │ categoryId: "cat789"         │
           │ default: true                │
           └──────────────────────────────┘
```

---

## Display Examples

### Scenario 1: SMS-Imported Transaction
```
Original SMS Text:
"Your A/C •••1234 debited Rs.500 to AMAZON INDIA PVT LTD on 14-Mar-2024"

Process Flow:
SmsImport.smsText
    │
    ▼
Transaction.note = "Your A/C •••1234 debited Rs.500 to AMAZON..."
    │
    ▼
TransactionAdapter Display:
    "Your A/C •••1234 debited Rs.500 to AMAZON..."  [14 Mar]
```

### Scenario 2: Manual Transaction with Merchant NickName
```
Transaction Details:
- note: null/empty
- merchantId: "xyz789" → Merchant "AMAZON INDIA"
- Merchant.nickName: "Amazon"

Process Flow:
Transaction.note = null
    │ (empty, check next)
    ▼
Merchant lookup → Found
Merchant.nickName = "Amazon"
    │
    ▼
TransactionAdapter Display:
    "Amazon"  [14 Mar]
```

### Scenario 3: Manual Transaction without Note or NickName
```
Transaction Details:
- note: null/empty
- merchantId: "xyz789" → Merchant without nickName
- Merchant.name: "VENDOR LTD"

Process Flow:
Transaction.note = null
    │ (empty, check next)
    ▼
Merchant lookup → Found
Merchant.nickName = null
    │ (empty, check name)
    ▼
Merchant.name = "VENDOR LTD"
    │
    ▼
TransactionAdapter Display:
    "VENDOR LTD"  [14 Mar]
```

### Scenario 4: Transfer without Note or Merchant
```
Transaction Details:
- note: null/empty
- merchantId: null
- type: "TRANSFER"

Process Flow:
Transaction.note = null
    │ (empty, check next)
    ▼
Merchant check → No merchant
    │ (skip, check next)
    ▼
Type fallback → "TRANSFER"
    │
    ▼
TransactionAdapter Display:
    "TRANSFER"  [14 Mar]
```

---

## Code Flow Diagram

### SMS Import Path
```
Step 1: SMS Received
        ↓
        "Your A/C •••1234 debited Rs.500 to AMAZON..."
        ↓
Step 2: SmsParser extracts info
        ├─ amount: 500
        ├─ type: EXPENSE
        ├─ merchant: "AMAZON INDIA"
        └─ rawText: "Your A/C •••1234..."
        ↓
Step 3: SmsImport created
        └─ smsText: "Your A/C •••1234..."
           merchantName: "AMAZON INDIA"
           accountId: "acc123"
           categoryId: "cat456"
        ↓
Step 4: User confirms in Review Screen
        ↓
Step 5: SmsReviewViewModel.confirmAndCreate()
        └─ Creates Transaction:
           note = smsImport.smsText ◄─── KEY CHANGE
           merchantId = <lookup or create>
           accountId = user selected
           categoryId = user selected
        ↓
Step 6: Transaction inserted in database
        ↓
Step 7: SmsImport deleted
        ↓
Step 8: Recent Transactions updated
        └─ Displays "Your A/C •••1234..."
```

### Display Rendering Path
```
Step 1: RecyclerView.onBindViewHolder(Transaction t)
        ↓
Step 2: TransactionAdapter.onBindViewHolder()
        ├─ Check: t.note != null && !isEmpty()
        │  ├─ YES → displayText = t.note
        │  │         [Shows SMS text]
        │  └─ NO → Continue to next check
        │
        ├─ Check: t.merchantId != null && db != null
        │  ├─ YES → merchant = db.merchantDao().getById()
        │  │         Check: merchant.nickName != null
        │  │         ├─ YES → displayText = nickName
        │  │         │         [Shows "Amazon"]
        │  │         └─ NO → Check: merchant.name != null
        │  │                 ├─ YES → displayText = name
        │  │                 │         [Shows "AMAZON INDIA"]
        │  │                 └─ NO → Continue
        │  │
        │  └─ NO → Continue to next check
        │
        └─ Final fallback: displayText = t.type
           [Shows "EXPENSE", "INCOME", "TRANSFER"]
        ↓
Step 3: holder.tvNote.setText(displayText)
        ↓
Step 4: Transaction appears in list
```

---

## Database State Comparison

### Before Implementation
```
TRANSACTIONS Table:
┌──────┬──────┬──────┬──────────────────────────┐
│ uuid │ type │ amt  │ note                     │
├──────┼──────┼──────┼──────────────────────────┤
│ abc1 │ EXP  │ 500  │ "SMS Import - AMAZON"   │ ← Limited
└──────┴──────┴──────┴──────────────────────────┘
```

### After Implementation
```
TRANSACTIONS Table:
┌──────┬──────┬──────┬────────────────────────────────────┐
│ uuid │ type │ amt  │ note                               │
├──────┼──────┼──────┼────────────────────────────────────┤
│ abc1 │ EXP  │ 500  │ "Your A/C •••1234 debited Rs.500"  │ ← Full SMS!
└──────┴──────┴──────┴────────────────────────────────────┘

MERCHANTS Table (linked via merchantId):
┌──────────┬──────────────────┬──────────┐
│ uuid     │ name             │ nickName │
├──────────┼──────────────────┼──────────┤
│ xyz789   │ AMAZON INDIA LTD │ "Amazon" │ ← NEW!
└──────────┴──────────────────┴──────────┘
```

---

## UI Rendering Examples

### Example 1: SMS with Merchant NickName
```
┌──────────────────────────────────────────┐
│ Recent Transactions                      │
├──────────────────────────────────────────┤
│ Your A/C •••1234 debited Rs.5...  14 Mar│ ◄─ SMS text
│ Rs. -500  [E]                            │
│                                          │
│ Coffee with client - meeting     13 Mar │ ◄─ Custom note
│ Rs. -150  [E]                            │
│                                          │
│ Amazon                            12 Mar │ ◄─ NickName (no note)
│ Rs. -299  [E]                            │
│                                          │
│ HDFC Bank                         11 Mar │ ◄─ Merchant name fallback
│ Rs. -100  [E]                            │
│                                          │
│ TRANSFER                          10 Mar │ ◄─ Type fallback
│ Rs. -5000  [T]                           │
└──────────────────────────────────────────┘
```

### Example 2: Transaction Detail View
```
┌──────────────────────────────────────────┐
│ Transaction Details                      │
├──────────────────────────────────────────┤
│ Date:       14-Mar-2024                  │
│ Time:       2:30 PM                      │
│ Type:       EXPENSE                      │
│ Amount:     Rs. -500                     │
├──────────────────────────────────────────┤
│ Account:    Main Account (•••1234)       │
│ Merchant:   AMAZON INDIA PVT LTD         │
│ NickName:   Amazon                       │
│ Category:   Shopping                     │
├──────────────────────────────────────────┤
│ Note:                                    │
│ Your A/C •••1234 debited Rs.500 to      │
│ AMAZON INDIA PVT LTD on 14-Mar-2024     │
│ (Full SMS text preserved)                │
├──────────────────────────────────────────┤
│ [Edit]  [Delete]  [Share]               │
└──────────────────────────────────────────┘
```

---

## User Interaction Flow

### SMS Import User Flow
```
User receives SMS
    ↓ (Auto-detected by app)
Notification: "3 SMS to review"
    ↓ (User taps notification)
SMS Review Screen
├─ SMS: "Your A/C •••1234 debited Rs.500..."
├─ Amount: 500
├─ Type: EXPENSE
├─ Merchant: [AMAZON INDIA]
    ↓ (User selects)
├─ Account: [Main Account]
├─ Category: [Shopping]
    ↓ (User confirms)
[CONFIRM] button
    ↓
Transaction created with:
  note = Full SMS text ✓
  merchantId = AMAZON ID
    ↓
Recent Transactions List
  Shows: "Your A/C •••1234 debited..."
  ✓ Full context visible!
```

### Merchant NickName User Flow
```
Merchants Screen
    ↓ (User taps to edit)
Merchant: "AMAZON INDIA PVT LTD"
├─ Name: [AMAZON INDIA PVT LTD]
├─ NickName: [_________]
    ↓ (User enters)
├─ NickName: [Amazon]
    ↓ (User saves)
    ▼
Recent Transactions List
  Transaction without note + this merchant:
  Shows: "Amazon"  [NickName] ✓
    ↓
Future SMS from same merchant:
  Will show: Full SMS text (takes priority)
  But fallback to "Amazon" if user clears note
```

---

## Comparison Table

| Feature | Before | After |
|---------|--------|-------|
| **SMS Text Storage** | "SMS Import - Merchant" | Full SMS text |
| **Display in List** | Limited format | Complete SMS |
| **Merchant NickName** | N/A | Shows as fallback |
| **Custom Notes** | Overwrites SMS info | Preserved |
| **Audit Trail** | Merchant name only | Full SMS message |
| **Context Visibility** | Low | High |
| **Quick ID (No Note)** | Merchant name | NickName if set |
| **Database Changes** | None | None (uses existing fields) |

---


