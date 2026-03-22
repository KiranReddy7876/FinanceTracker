# Quick Start: Review & Approve SMS Transactions

## 🎯 What You Need to Know

**SMS transactions pending review appear in the "SMS Imports" screen**

### Two Workflows:

#### ✅ Automatic (No Action Needed)
```
SMS with matching account number
         ↓
Auto-confirmed immediately
         ↓
Appears in Recent Transactions
         ↓
Done! ✓
```

#### 📋 Manual (Your Review Required)
```
SMS without matching account
         ↓
Shows notification & badge
         ↓
You review in SMS Imports screen
         ↓
You select account & category
         ↓
You approve
         ↓
Done! ✓
```

---

## 🚀 How to Access SMS Review Screen

### Method 1: From Notification
```
Step 1: SMS arrives from bank
Step 2: See notification: "SMS Transaction Detected - 1 pending"
Step 3: Tap notification
Step 4: SMS Import screen opens ← Review here
```

### Method 2: From Badge on Dashboard
```
Step 1: Go to Dashboard
Step 2: Look for: Recent Transactions heading
Step 3: See red badge: "1"
Step 4: Tap badge
Step 5: SMS Import screen opens ← Review here
```

### Method 3: From Menu
```
Step 1: Open menu (hamburger icon)
Step 2: Select: SMS Imports
Step 3: SMS Import screen opens ← Review here
```

---

## 📱 SMS Import Screen (What You See)

```
┌────────────────────────────────────┐
│ Pending SMS Transactions           │ ← Title
├────────────────────────────────────┤
│                                    │
│ ₹500.00                    EXPENSE │ ← Transaction 1
│ SMS: Dear customer, ₹500...        │
│                                    │
├────────────────────────────────────┤
│                                    │
│ ₹1000.00                   INCOME  │ ← Transaction 2
│ SMS: Your salary of ₹1000...       │
│                                    │
├────────────────────────────────────┤
│ [No more pending transactions]     │ ← If empty
└────────────────────────────────────┘
```

**Each item shows:**
- Amount (₹)
- Type (EXPENSE/INCOME)
- SMS preview text

**Click any item to review**

---

## 🔍 Review Dialog (Details)

When you click a transaction:

```
┌─────────────────────────────────────┐
│ Review SMS Import Dialog            │
├─────────────────────────────────────┤
│                                     │
│ Transaction Details:                │
│ ┌───────────────┬─────────────────┐ │
│ │ Amount        │ Type            │ │
│ │ ₹500.00       │ EXPENSE         │ │
│ └───────────────┴─────────────────┘ │
│                                     │
│ SMS Text (full message):            │
│ ┌─────────────────────────────────┐ │
│ │ Dear customer, ₹500 debited     │ │
│ │ from your A/C •••1234 on ...    │ │
│ │ [scrollable if long]            │ │
│ └─────────────────────────────────┘ │
│                                     │
│ Select Account:                     │
│ ┌─────────────────────────────────┐ │
│ │ [HDFC Bank (Savings) ▼]         │ ← Dropdown
│ │  ├─ HDFC Bank                   │
│ │  ├─ ICICI Bank                  │
│ │  └─ Axis Bank                   │
│ └─────────────────────────────────┘ │
│                                     │
│ Select Category:                    │
│ ┌─────────────────────────────────┐ │
│ │ [— No Category — ▼]             │ ← Dropdown
│ │  ├─ Groceries                   │
│ │  ├─ Utilities                   │
│ │  ├─ Entertainment               │
│ │  └─ Shopping                    │
│ └─────────────────────────────────┘ │
│                                     │
│ [Confirm] [Ignore] [Cancel]         │
└─────────────────────────────────────┘
```

---

## ✍️ Step-by-Step Approval Process

### Step 1: Click Transaction
Click on any pending transaction in the list
→ Review Dialog opens

### Step 2: Verify Details
Check that the following are correct:
- ✓ Amount: Matches SMS
- ✓ Type: Correct (EXPENSE or INCOME)
- ✓ SMS Text: Full message shown for reference

### Step 3: Select Account (Required)
```
Click: Select Account dropdown
Shows: All your active accounts
├─ HDFC Bank (Savings)
├─ ICICI Bank (Current)
├─ Credit Card
└─ Investment Account

Action: Click the account you want
Result: Account selected (checkmark shown)
```

**Tips:**
- If account number was matched automatically, it's pre-selected
- You can still change it if needed
- Must select one to approve

### Step 4: Select Category (Optional)
```
Click: Select Category dropdown
Shows: Categories filtered by type
For EXPENSE:
├─ — No Category —
├─ Groceries
├─ Utilities
├─ Transport
├─ Entertainment
└─ Shopping

For INCOME:
├─ — No Category —
├─ Salary
├─ Bonus
├─ Interest
└─ Dividend

Action: Click a category or select "— No Category —"
Result: Category selected
```

**Tips:**
- Categories auto-filter based on transaction type
- Category is optional (can be "— No Category —")
- You can add category later when editing transaction

### Step 5: Confirm or Cancel
```
Three buttons:

1. [Confirm] ← Click this to approve
   Result: Transaction added to your records
           Amount reflected in dashboard
           Appears in Recent Transactions
           SMS marked as processed
           Dialog closes

2. [Ignore] ← Click to skip this SMS
   Result: SMS discarded
           Won't appear again
           Use carefully (can't undo)

3. [Cancel] ← Click to close without action
   Result: Dialog closes
           SMS stays pending
           You can review later
```

---

## 📊 What Happens When You Confirm

### Immediately:
```
✓ Success message: "Transaction recorded"
✓ Dialog closes
✓ SMS removed from pending list
✓ If list now empty: Shows "No pending SMS transactions"
```

### In Dashboard:
```
✓ Recent Transactions updated
✓ New transaction visible in list
✓ Amount updated in monthly totals
✓ Account balance updated (if applicable)
```

### In Account Details:
```
✓ Transaction appears in account's transaction history
✓ Amount correctly attributed
✓ Category shown (if selected)
```

### In Database:
```
Created: Transaction record with
├─ Amount from SMS
├─ Type from SMS
├─ Your selected account
├─ Your selected category (optional)
├─ Note: "Auto-imported from SMS"
└─ Link to original SMS for audit

Updated: SmsImport record
├─ Status: PENDING → CONFIRMED
└─ Timestamp
```

---

## ⚙️ Category System

### How Categories Are Filtered

**Transaction: ₹500 EXPENSE**
→ Category dropdown shows ONLY expense categories

**Transaction: ₹5000 INCOME**
→ Category dropdown shows ONLY income categories

### Creating New Categories

**If category you want doesn't exist:**

1. Open Menu → Categories
2. Click "Add Category" button
3. Enter:
   - Name: "Dining Out"
   - Type: "EXPENSE"
4. Click Save
5. Category becomes available immediately in next SMS review

---

## 🎯 Common Scenarios

### Scenario 1: SMS with Account Match
```
SMS: "₹500 debited from A/C •••1234"
Account "1234" exists in app
           ↓
Auto-matched and confirmed
           ↓
No notification needed
           ↓
Appears in Recent Transactions immediately
           ↓
✓ No manual action required
```

### Scenario 2: SMS without Account Match
```
SMS: "₹500 debited from A/C •••9999"
Account "9999" doesn't exist
           ↓
Notification shown
Badge shows "1"
           ↓
You tap notification
SMS Import screen opens
           ↓
You review:
- See transaction details
- See full SMS text
- Select account from dropdown
- Select category (optional)
           ↓
You click Confirm
           ↓
Transaction created and added to records
           ↓
✓ Done!
```

### Scenario 3: Wrong Amount
```
SMS shows: ₹500
You see: ₹500 in dialog
Problem: Amount is wrong in SMS
           ↓
Options:
A) Ignore this SMS and manually add correct amount
B) Confirm it (you can edit amount later)
           ↓
✓ Best practice: Ignore and manually add correct transaction
```

### Scenario 4: Ignore by Mistake
```
You clicked: [Ignore] by mistake
SMS discarded and can't be seen
           ↓
Current state: SMS is gone
           ↓
Workaround: Manually add the transaction
           ↓
Future improvement: Add "Undo" functionality
```

---

## 🔍 Data You Can/Can't Edit

| Field | Can Edit? | Notes |
|-------|-----------|-------|
| Amount | ❌ No | Fixed from SMS |
| Type | ❌ No | Auto-detected from SMS |
| Date | ❌ No | From SMS |
| SMS Text | ❌ No | Read-only reference |
| Account | ✅ Yes | You select from dropdown |
| Category | ✅ Yes | You select from dropdown |

**To Edit Amount/Type/Date:**
- Ignore this SMS
- Manually add new transaction via "Add Transaction" screen
- Enter correct details

---

## 💡 Tips & Best Practices

### Do's ✅
- ✅ Review SMS text carefully before approving
- ✅ Select correct account from dropdown
- ✅ Assign category if you have one
- ✅ Confirm immediately to update dashboard
- ✅ Create categories for better tracking

### Don'ts ❌
- ❌ Don't click Ignore if you're unsure (can't undo)
- ❌ Don't confirm wrong amounts (ignore and re-add)
- ❌ Don't skip required Account selection
- ❌ Don't leave too many pending (review regularly)

### When in Doubt ❓
- Read the full SMS text in the dialog
- Check account number matches your bank statement
- Compare amount with actual transaction
- Contact your bank if SMS looks fraudulent
- Ignore and manually verify if suspicious

---

## 📞 Support

### Where to Find Help

**In the App:**
- Menu → Help & Support
- In-app tooltips on each field

**Common Issues:**
- **"No accounts available"** → Create an account first (Menu → Accounts)
- **"Please select an account"** → Click dropdown and select account
- **Can't find category** → Create it first (Menu → Categories)
- **SMS won't appear** → Grant SMS permissions (Settings → Permissions)

---

## 🎬 Video Tutorial (If Available)

Check the app's help section for video walkthrough of:
- Setting up accounts with last 4 digits
- Receiving SMS notifications
- Reviewing pending transactions
- Approving with category selection
- Viewing in Recent Transactions

---

## Summary

| What | Where | How |
|------|-------|-----|
| See pending SMS | SMS Imports screen | Menu or Notification |
| Review details | Review dialog | Click any pending transaction |
| Select account | Account dropdown | Click and choose from list |
| Select category | Category dropdown | Click and choose from list |
| Approve | Confirm button | Click after selections |
| Skip | Ignore button | Click to discard SMS |
| Modify later | Edit Transaction | Open from Recent Transactions |

---

**💡 Remember:** 
- Automatic SMS with account matches skip this screen
- Manual SMS requires your review before adding to records
- You control which account and category each transaction gets
- Changes appear immediately in dashboard and recent transactions

