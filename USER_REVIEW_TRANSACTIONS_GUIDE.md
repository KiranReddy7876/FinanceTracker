# User Review Transactions - Complete Guide

## Where to Review Pending Transactions

### Option 1: SMS Import Review Screen (Main Feature)

**Location:** Menu → SMS Imports (or via Notification)

This is the primary screen for reviewing pending SMS transactions before they are approved.

#### How to Access:

1. **From Dashboard Badge:**
   - Look for the red badge showing "1" next to "Recent Transactions"
   - This badge appears when there are pending SMS imports without a matched account
   - The badge is located in the SMS section above the Recent Transactions list

2. **From Notification:**
   - When an SMS without account match arrives, you'll see a notification
   - Tap the notification: "SMS Transaction Detected"
   - Opens the SMS Import Review screen directly

3. **From Menu:**
   - Open the app navigation menu
   - Select "SMS Imports"
   - Shows all pending SMS transactions

---

## SMS Import Review Screen - Detailed View

### What You See

```
┌─────────────────────────────────────────┐
│ Pending SMS Transactions                │
└─────────────────────────────────────────┘

┌─────────────────────────────────────────┐
│ ₹500 EXPENSE                            │ ← Amount & Type
│ SMS: Dear customer, ₹500 debited from   │ ← Full SMS Text
│ A/C ••••1234                            │
└─────────────────────────────────────────┘
        ↓ (Click to expand)

┌─ REVIEW DIALOG ─────────────────────────┐
│ Amount:     ₹500.00                     │
│ Type:       EXPENSE                     │
│ SMS Text:   (Full message shown)        │
│                                         │
│ Account:    [Dropdown ▼]                │
│             ├─ Bank XYZ (Savings)       │
│             ├─ Bank ABC (Current)       │
│             └─ Bank DEF (Credit Card)   │
│                                         │
│ Category:   [Dropdown ▼]                │
│             ├─ — No Category —          │
│             ├─ Groceries                │
│             ├─ Utilities                │
│             └─ Entertainment            │
│                                         │
│ [Confirm] [Ignore] [Cancel]             │
└─────────────────────────────────────────┘
```

---

## Step-by-Step: Review & Approve Transaction

### Step 1: Open SMS Import Screen
- Tap the badge or notification, OR
- Open Menu → SMS Imports

### Step 2: See List of Pending Transactions
```
List showing:
├─ Transaction 1: ₹500 EXPENSE
├─ Transaction 2: ₹1000 INCOME
└─ Transaction 3: ₹200 EXPENSE
```
- Each shows amount, type, and SMS preview
- No account or category shown (awaiting your review)

### Step 3: Click on Transaction to Review
- Tap any transaction in the list
- Opens the **Review Dialog** with full details

### Step 4: Verify Transaction Details
**Shows:**
- ✓ Amount: ₹500.00
- ✓ Type: EXPENSE
- ✓ Full SMS text for reference

### Step 5: Select Account
**Dropdown shows all your active accounts:**
- Bank name
- Account type (Savings, Current, Credit Card)

**Auto-matched accounts:**
- If system found matching account number in SMS, it's pre-selected
- You can still change it if needed

**How it matches:**
- Extracts last 4 digits from SMS (e.g., "•••1234" → "1234")
- Searches your accounts for `accountNumberLast4` field
- Pre-fills if found

### Step 6: Select Category (Optional)
**Dropdown shows categories filtered by type:**

**For EXPENSE transactions:**
- Groceries
- Utilities
- Entertainment
- Transport
- Healthcare
- Shopping
- Miscellaneous
- (Any custom categories you created)

**For INCOME transactions:**
- Salary
- Bonus
- Interest
- Dividend
- Miscellaneous
- (Any custom categories you created)

**Options:**
- Select a category → Amount will be attributed to it
- Select "— No Category —" → Transaction added without category
- Category is optional, you can add later

### Step 7: Confirm or Ignore

**Three Actions Available:**

1. **Confirm Button** ✅
   - Validates account is selected
   - Creates transaction in your records
   - Adds to Recent Transactions
   - Shows success message: "Transaction recorded"
   - SMS marked as processed

2. **Ignore Button** ⊘
   - Discards this SMS
   - Won't appear in pending list again
   - Can't be recovered (use carefully)
   - Shows message: "SMS ignored"

3. **Cancel Button** ✗
   - Closes dialog without action
   - SMS remains in pending list
   - Can review again later

---

## Example Workflow

### Scenario: SMS Without Account Match

```
Timeline:
─────────────────────────────────────────

T1: SMS Received
    "Dear customer, ₹500 debited from A/C •••9999"
    └─ Account number "9999" doesn't exist in app

T2: Notification Shown
    "SMS Transaction Detected - 1 pending"
    └─ Badge shows "1" on Recent Transactions

T3: User Taps Notification
    SMS Import screen opens

T4: User Sees Pending List
    ├─ Transaction shows:
    │  ├─ Amount: ₹500
    │  ├─ Type: EXPENSE
    │  └─ SMS: Full message text
    └─ Account: [Not shown - awaiting review]

T5: User Clicks Transaction
    Review Dialog opens

T6: User Reviews Details
    ✓ Amount: ₹500.00 ✓ Correct
    ✓ Type: EXPENSE ✓ Correct
    ✓ SMS Text: [Reviews full message]

T7: User Selects Account
    ├─ Opens dropdown
    ├─ Selects "HDFC Bank (Savings)" ← Manual selection
    └─ Confirmed

T8: User Selects Category
    ├─ Opens dropdown (shows EXPENSE categories)
    ├─ Selects "Groceries" ← Optional
    └─ Confirmed

T9: User Confirms
    └─ Clicks [Confirm] button

T10: Transaction Created
    ├─ Inserted into transactions table
    ├─ Amount: ₹500
    ├─ Account: HDFC Bank
    ├─ Category: Groceries
    ├─ Note: "Auto-imported from SMS"
    └─ referenceId: Links back to SMS

T11: User Sees Success
    └─ Toast: "Transaction recorded"

T12: Screens Update
    ├─ SMS Import screen: Transaction removed from pending list
    ├─ Recent Transactions: New transaction appears
    └─ Dashboard: Amount reflected in monthly totals
```

---

## Data You Can Review

### Transaction Details
| Field | Source | Editable |
|-------|--------|----------|
| Amount | Extracted from SMS | ❌ No |
| Type | Auto-detected (EXPENSE/INCOME) | ❌ No |
| Date | From SMS or current time | ❌ No |
| SMS Text | Full message shown for reference | ❌ No |
| Account | You select from dropdown | ✅ Yes |
| Category | You select from filtered dropdown | ✅ Yes |

### What You CAN Change
- ✅ Account (must select)
- ✅ Category (optional)

### What You CAN'T Change
- ❌ Amount (extracted from SMS)
- ❌ Type (auto-detected from SMS keywords)
- ❌ Date (from SMS)

---

## Category System

### How Categories Work

**Filtered by Transaction Type:**
- When you tap a transaction, categories in dropdown are automatically filtered
- EXPENSE transaction → Shows only EXPENSE categories
- INCOME transaction → Shows only INCOME categories

**Example:**
```
SMS Import 1: EXPENSE (₹500 debited)
  Category Dropdown shows:
  ├─ — No Category —
  ├─ Groceries ← EXPENSE category
  ├─ Utilities ← EXPENSE category
  ├─ Shopping ← EXPENSE category
  └─ Entertainment ← EXPENSE category
  (INCOME categories like Salary, Bonus NOT shown)

SMS Import 2: INCOME (₹5000 credited)
  Category Dropdown shows:
  ├─ — No Category —
  ├─ Salary ← INCOME category
  ├─ Bonus ← INCOME category
  ├─ Interest ← INCOME category
  └─ Dividend ← INCOME category
  (EXPENSE categories like Groceries, Utilities NOT shown)
```

### Creating New Categories

If the category you want doesn't exist:

1. **From Categories Screen:**
   - Open Menu → Categories
   - Click Add Category button
   - Enter:
     - Name: "Restaurant"
     - Type: "EXPENSE"
   - Save

2. **Category becomes available immediately:**
   - Next time you review an EXPENSE transaction
   - "Restaurant" appears in the category dropdown
   - Can select it for current or future transactions

---

## Important Notes

### Auto-Confirmation (New Feature)
With the latest fix, SMS with **account matches are automatically confirmed**:

```
SMS with Account Match (account "1234" exists):
├─ Parsed
├─ Account matched automatically
├─ Auto-confirmed and converted ✓
└─ Appears in Recent Transactions
   └─ NO manual review needed

SMS without Account Match (account "9999" doesn't exist):
├─ Parsed
├─ No account match
├─ Stored as PENDING
├─ Notification shown
└─ Requires manual review
   └─ YOU see it in SMS Import screen
```

### Validation Rules

**To Confirm:**
- ✓ Amount must be valid (required - from SMS)
- ✓ Account must be selected (required - you select)
- ✓ Category optional (can be "— No Category —")
- ✓ Transaction type must be auto-detected (required - from SMS)

**If validation fails:**
```
Error: "Please select an account"
└─ Try: Select account from dropdown
```

### Data Saved

When you confirm a transaction:
```
Transaction Record Created:
├─ uuid: Unique transaction ID
├─ accountId: Your selection ✓
├─ amount: From SMS (₹500)
├─ type: Auto-detected (EXPENSE)
├─ categoryId: Your selection (optional)
├─ date: From SMS
├─ note: "Auto-imported from SMS"
├─ referenceId: Links back to SMS import (for audit)
├─ createdAt: Timestamp
├─ updatedAt: Timestamp
└─ deleted: false

SmsImport Record Updated:
├─ status: PENDING → CONFIRMED
└─ Updated timestamp
```

---

## FAQ

**Q: Where do I see the badge for pending transactions?**
A: On the Dashboard, next to "Recent Transactions" heading. Red badge shows count of pending SMS imports without account match.

**Q: What if I ignore a transaction by mistake?**
A: Currently, ignored transactions cannot be recovered. Always review before clicking "Ignore".

**Q: Can I edit an amount after confirming?**
A: No, amount is locked from SMS. If wrong, ignore this transaction and manually add a new one via the Add Transaction screen.

**Q: What happens to ignored SMS?**
A: Marked with status "IGNORED" and hidden from the pending list. SMS import record remains in database for audit trail.

**Q: Can I add a category after confirming?**
A: Yes. Open the transaction from Recent Transactions or Account Details, edit it to add/change category.

**Q: Are there different review screens?**
A: Yes, there are 2:
- **SmsImportFragment** - Main pending review screen (accessed from menu or notification)
- **SmsReviewFragment** - Alternative review interface (same functionality)

Both work the same way, just different UI.

---

## Navigation Quick Reference

### To Review SMS Transactions:

**Option 1: From Dashboard**
```
Dashboard Screen
    ↓
Look for: Recent Transactions badge showing "1"
    ↓
Tap badge
    ↓
Opens: SMS Import Review Screen
    ↓
See: List of pending SMS imports
```

**Option 2: From Notification**
```
Notification: "SMS Transaction Detected"
    ↓
Tap notification
    ↓
Opens: SMS Import Review Screen
    ↓
See: List of pending SMS imports
```

**Option 3: From Menu**
```
Menu
    ↓
Select: SMS Imports
    ↓
Opens: SMS Import Review Screen
    ↓
See: List of pending SMS imports
```

---

## Key Takeaways

✅ **Where:** SMS Import screen (Menu → SMS Imports or notification)
✅ **What:** Review pending SMS transactions before approval
✅ **Actions:** Confirm (add to records), Ignore (discard), or Cancel (review later)
✅ **Selection:** Choose account (required) and category (optional)
✅ **Auto-Confirm:** SMS with account matches skip this screen entirely
✅ **Filtering:** Categories auto-filtered by transaction type
✅ **Edit:** Changes saved immediately on confirmation

