# SMS Transaction Workflow - Category-Required Model

## New Workflow (Updated)

### Problem Fixed
User reported SMS transactions not showing and badge disappearing. The issue was that auto-confirmation was converting SMS to transactions without requiring category selection.

### Solution Implemented
Changed the workflow to require explicit category selection before creating a transaction:

```
SMS Received
    ↓
✓ Parse & Extract Details
    ↓
✓ Match Account (if possible)
    ↓
Store as PENDING (regardless of account match)
    ↓
Show Notification & Badge
    ↓
User Opens SMS Import Screen
    ↓
User Reviews Transaction
    ↓
User MUST Select:
├─ Account (required)
└─ Category (required - NOT optional anymore)
    ↓
User Clicks "Confirm"
    ↓
✓ Transaction Created in DB
✓ Category Applied
✓ Appears in Recent Transactions
✓ Dashboard Updated
```

---

## Key Changes

### 1. **ALL SMS Stay PENDING Until Category Selected**
- Removed auto-confirmation logic
- All SMS stored with status = "PENDING"
- Notification shows for ALL pending SMS
- Badge shows count of all pending (not just unmatched)

### 2. **Category Selection is NOW Required**
- Dialog won't allow confirmation without category
- Shows toast: "Please select a category"
- Only creates transaction when category is selected
- Prevents incomplete transactions

### 3. **Delete Functionality Added**
- **Delete Pending SMS:** Click "Delete" button in review dialog
- **Delete Transaction:** Via Recent Transactions → Edit → Delete
- Both use soft delete (records not removed, just marked)
- Maintains audit trail

---

## User Workflow

### Step 1: SMS Arrives
```
SMS: "₹500 debited from A/C •••1234"
    ↓
System processes in background
    ↓
Account matched: YES (account "1234" exists)
    ↓
But SMS stays PENDING (no auto-confirm)
```

### Step 2: Notification Shown
```
Notification: "SMS Transaction Detected - 1 pending"
Badge on Dashboard: Shows "1"
User can tap notification to review
```

### Step 3: User Opens SMS Import Screen
```
Menu → SMS Imports
    ↓
See list of pending SMS
├─ ₹500 EXPENSE
└─ ₹1000 INCOME
```

### Step 4: Click Transaction to Review
```
Click ₹500 EXPENSE
    ↓
Review Dialog Opens
├─ Amount: ₹500.00 ✓
├─ Type: EXPENSE ✓
├─ SMS Text: [Full message] ✓
├─ Account: [HDFC Bank] (pre-selected if matched)
└─ Category: [Empty - MUST SELECT] ⚠
```

### Step 5: Select Category (Required)
```
Click Category Dropdown
    ↓
Select "Groceries" (or any EXPENSE category)
    ↓
Now category is selected
```

### Step 6: Confirm
```
Click [Confirm] button
    ↓
System validates:
├─ Account selected? ✓ YES
└─ Category selected? ✓ YES
    ↓
Transaction created & stored
    ↓
SMS Import marked as CONFIRMED
    ↓
Badge count decreases
```

### Step 7: See in Dashboard
```
Dashboard updates:
├─ Recent Transactions shows ₹500 Groceries ✓
├─ Monthly total updated ✓
├─ Account balance updated ✓
└─ Category totals updated ✓
```

---

## Dialog Buttons

```
┌──────────────────────────────────────┐
│      Review Dialog Buttons           │
├──────────────────────────────────────┤
│                                      │
│ [Confirm]  - Create transaction      │
│ (Creates transaction with selected   │
│  account & category)                 │
│                                      │
│ [Ignore]   - Skip this SMS           │
│ (Marks as IGNORED, hidden from list) │
│                                      │
│ [Delete]   - Remove permanently      │
│ (Soft delete, not recoverable)       │
│                                      │
└──────────────────────────────────────┘
```

---

## Validation Rules

### To Confirm Transaction:
```
✓ Account must be selected
  └─ Validation: accountPos != 0

✓ Category must be selected
  └─ Validation: catPos > 0 (not "— No Category —")

✓ Amount valid
  └─ Validated by SmsParser

✓ Type valid
  └─ Validated by SmsParser

If validation fails:
    Show Toast with error message
    Return to dialog (no action)
```

---

## Database Changes

### SmsImport Queries Updated
```sql
-- OLD:
SELECT * FROM sms_import WHERE status = 'PENDING'

-- NEW:
SELECT * FROM sms_import WHERE status = 'PENDING' AND deleted = 0
```

Both `getPending()` and `getPendingCount()` now exclude deleted SMS

### SmsImportRepository Methods
```java
// Existing:
- insert(smsImport)      // Store as PENDING
- confirm(uuid)          // Convert to Transaction
- ignore(uuid)           // Mark as IGNORED
- updateAccountAndCategory(uuid, accountId, categoryId)

// NEW:
- delete(uuid)           // Soft delete SMS import
```

### SmsImportViewModel Methods
```java
// Existing:
- confirmImport(uuid)    // Create transaction
- ignoreImport(uuid)     // Skip SMS

// NEW:
- deleteSmsImport(uuid)  // Delete SMS
```

---

## Transaction Creation Rules

Transaction is created ONLY when:
1. ✓ User clicks "Confirm" in dialog
2. ✓ Account is selected
3. ✓ Category is selected
4. ✓ SMS status = "PENDING"

If any validation fails → No transaction created, SMS stays PENDING

---

## File Changes Summary

### Modified Files:
1. **SmsReceiver.java**
   - Removed auto-confirmation logic
   - ALL SMS stored as PENDING
   - Always show notification

2. **SmsImportFragment.java**
   - Category selection is NOW required
   - Changed [Cancel] button to [Delete] button
   - Added validation for category selection

3. **SmsImportViewModel.java**
   - Added `deleteSmsImport(uuid)` method

4. **SmsImportRepository.java**
   - Added `delete(uuid)` method for soft delete

5. **SmsImportDao.java**
   - Updated queries to exclude deleted SMS
   - `getPending()` - Now filters deleted=0
   - `getPendingCount()` - Now filters deleted=0

---

## Status Workflow

### SMS Import Status

```
Status Flow:
├─ PENDING (Initial) 
│  └─ User must select category
│
├─ CONFIRMED (After user confirms with category)
│  └─ Transaction created
│  └─ Badge decreases
│  └─ Appears in Recent Transactions
│
├─ IGNORED (User clicks Ignore)
│  └─ Hidden from pending list
│  └─ No transaction created
│
└─ DELETED (User clicks Delete)
   └─ Soft deleted (marked deleted=1)
   └─ Hidden from pending list
   └─ Can't be recovered
```

---

## Example Scenario

```
Timeline:
─────────────────────────────────────

T1: 10:00 AM
SMS arrives: "₹500 debited from A/C •••1234"

T2: 10:00:05 AM
System processes:
├─ Extract: amount=500, type=EXPENSE, date=10:00 AM
├─ Extract account number: "1234"
├─ Match: Account HDFC (uuid=acc-001) found
├─ Store: SmsImport with:
│  └─ accountId = "acc-001" (pre-filled)
│  └─ categoryId = null (NOT pre-filled)
│  └─ status = "PENDING"
└─ Show: Notification & Badge

T3: 10:05 AM
User sees notification, taps it
└─ SMS Import screen opens

T4: 10:06 AM
User clicks ₹500 transaction
└─ Review dialog opens
├─ Account: HDFC Bank (pre-selected) ✓
└─ Category: [Empty - needs selection]

T5: 10:07 AM
User clicks Category dropdown
├─ Shows: Groceries, Utilities, Entertainment, etc.
└─ Selects: "Groceries"

T6: 10:08 AM
User clicks [Confirm]
System validates:
├─ Account selected? ✓ YES (HDFC)
└─ Category selected? ✓ YES (Groceries)
    ↓
Transaction created:
├─ Amount: ₹500
├─ Type: EXPENSE
├─ Account: HDFC (acc-001)
├─ Category: Groceries
└─ Note: "Auto-imported from SMS"

T7: 10:08:10 AM
Dashboard updates:
├─ Recent Transactions: Shows ₹500 Groceries
├─ Monthly Expense: ₹20,000 → ₹20,500
├─ HDFC Balance: ₹10,500 → ₹10,000
└─ Badge: 1 → 0 (no more pending)

T8: COMPLETE ✓
Transaction recorded with category
SMS marked as CONFIRMED
User sees it in dashboard
```

---

## Action Buttons Reference

### [Confirm] Button
- **When:** Category selected
- **What it does:** Creates transaction with selected account & category
- **Result:** SMS marked CONFIRMED, appears in dashboard
- **Message:** "Transaction recorded"

### [Ignore] Button (Neutral button)
- **When:** Always available
- **What it does:** Marks SMS as IGNORED
- **Result:** SMS hidden from pending list, no transaction created
- **Message:** "SMS ignored"
- **Note:** Can't be undone

### [Delete] Button (Negative button)
- **When:** Always available
- **What it does:** Soft deletes SMS import
- **Result:** SMS hidden from pending list, deleted=1, no transaction created
- **Message:** "SMS deleted"
- **Note:** Can't be recovered, no audit trail visible

---

## Advantages of This Approach

✅ **Better Data Quality**
- Forces users to categorize transactions
- Prevents incomplete transactions
- Ensures all transactions have account & category

✅ **User Control**
- No automatic conversions
- Explicit confirmation required
- Can review before committing

✅ **Flexibility**
- Can delete unwanted SMS
- Can ignore or confirm
- Clear action choices

✅ **Audit Trail**
- All SMS stored (including ignored/deleted)
- Can track user actions
- References maintained

✅ **Transparency**
- Badge shows ALL pending (not just unmatched)
- Users know when SMS needs review
- Clear notification flow

---

## Troubleshooting

### Q: Badge shows count but SMS doesn't appear
**A:** Check if deleted=1 in database. Deleted SMS hidden from list.

### Q: Can't confirm transaction without selecting category
**A:** Category is now REQUIRED. Select one from dropdown.

### Q: Deleted SMS not recoverable
**A:** Correct - soft delete marks deleted=1. Contact support if needed.

### Q: Why is my account not pre-selected?
**A:** Account number must match (last 4 digits). Check account settings.

### Q: Category dropdown empty
**A:** Create categories first (Menu → Categories).

---

## Summary

```
BEFORE (with auto-confirmation):
├─ SMS with account match → Auto-confirmed (no review)
├─ SMS without match → Requires manual review
└─ Problem: Badge but no transaction visible

AFTER (category-required model):
├─ ALL SMS → Stay PENDING
├─ User must select account & category
├─ Only then → Transaction created
└─ Solution: Clear workflow, better data quality
```

**Result:** All transactions now have proper account and category assignment. Users have full control over what gets recorded.

