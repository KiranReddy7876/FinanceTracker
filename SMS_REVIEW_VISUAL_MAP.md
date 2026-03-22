# SMS Review Screen - Visual Map

## Where Everything Is Located

```
╔═══════════════════════════════════════════════════════════════════════╗
║                         YOUR APP MAIN MENU                            ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                        ║
║  ┌─── Dashboard                                                       ║
║  ├─── Accounts                                                        ║
║  ├─── Categories                                                      ║
║  ├─── Transactions                                                    ║
║  ├─► SMS IMPORTS ◄─────────────────┐ (Click here to review SMS)      ║
║  │   Pending SMS Transactions       │                                ║
║  ├─── Settings                      │                                ║
║  └─── Help                          │                                ║
║                                     │                                ║
║                                     │                                ║
║  OR from Dashboard:                 │                                ║
║  ┌────────────────────────────────┐ │                                ║
║  │ Recent Transactions        [1] │◄┘ (Red badge - click it)        ║
║  │ [Shows recent transactions]    │                                  ║
║  └────────────────────────────────┘                                  ║
║                                                                        ║
║  OR from Notification:                                               ║
║  ┌────────────────────────────────┐                                  ║
║  │ 🔔 SMS Transaction Detected    │                                  ║
║  │    1 pending - Tap to review   │ ◄─ Click notification             ║
║  └────────────────────────────────┘                                  ║
║                                                                        ║
╚═══════════════════════════════════════════════════════════════════════╝
                                    │
                                    ▼
╔═══════════════════════════════════════════════════════════════════════╗
║              SMS IMPORTS REVIEW SCREEN (MAIN SCREEN)                  ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                        ║
║  Title: Pending SMS Transactions                                      ║
║                                                                        ║
║  ┌──────────────────────────────────────────────────────────────┐    ║
║  │                                                              │    ║
║  │  List Item 1:                                               │    ║
║  │  ╔════════════════════════════════════════════════════════╗  │    ║
║  │  ║ ₹500.00                              EXPENSE           ║  │    ║
║  │  ║ SMS: Dear customer, ₹500 debited from A/C •••1234      ║  │    ║
║  │  ║ [Click to review] ►                                    ║  │    ║
║  │  ╚════════════════════════════════════════════════════════╝  │    ║
║  │                                                              │    ║
║  │  List Item 2:                                               │    ║
║  │  ╔════════════════════════════════════════════════════════╗  │    ║
║  │  ║ ₹1000.00                             INCOME            ║  │    ║
║  │  ║ SMS: Your salary ₹1000 credited to your account        ║  │    ║
║  │  ║ [Click to review] ►                                    ║  │    ║
║  │  ╚════════════════════════════════════════════════════════╝  │    ║
║  │                                                              │    ║
║  │  (More items if present, scroll down)                       │    ║
║  │                                                              │    ║
║  │  OR if empty:                                               │    ║
║  │  "No pending SMS transactions"                              │    ║
║  │                                                              │    ║
║  └──────────────────────────────────────────────────────────────┘    ║
║                                                                        ║
║  [Each item is clickable]                                             ║
║                                                                        ║
╚═══════════════════════════════════════════════════════════════════════╝
                                    │
                    Click any item in the list
                                    │
                                    ▼
╔═══════════════════════════════════════════════════════════════════════╗
║           REVIEW & APPROVAL DIALOG (DETAIL VIEW)                      ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                        ║
║  ╔─ TRANSACTION DETAILS ─────────────────────────────────────────╗   ║
║  ║                                                               ║   ║
║  ║  Amount: ₹500.00         Type: EXPENSE                       ║   ║
║  ║                                                               ║   ║
║  ║  SMS Text (Read-Only, Scrollable):                           ║   ║
║  ║  ╭─────────────────────────────────────────────────────────╮ ║   ║
║  ║  │ Dear customer, ₹500 debited from your account A/C      │ ║   ║
║  ║  │ •••1234 on Mar 19, 2026 at 10:00 AM. Your available   │ ║   ║
║  ║  │ balance is ₹10,000. If this was not authorized,       │ ║   ║
║  ║  │ please contact us immediately.                         │ ║   ║
║  ║  │                                                         │ ║   ║
║  ║  │ [scroll to see more if needed] ↕                       │ ║   ║
║  ║  ╰─────────────────────────────────────────────────────────╯ ║   ║
║  ║                                                               ║   ║
║  ║  SELECT ACCOUNT (REQUIRED): ◄─ User MUST select              ║   ║
║  ║  ╔─────────────────────────────────────────────────────────╗ ║   ║
║  ║  ║ HDFC Bank (Savings) ▼                                  ║ ║   ║
║  ║  ║ ├─ HDFC Bank (Savings)  ◄─ Already selected           ║ ║   ║
║  ║  ║ ├─ ICICI Bank (Current)                               ║ ║   ║
║  ║  ║ ├─ Axis Credit Card                                   ║ ║   ║
║  ║  ║ └─ Kotak Investment Account                           ║ ║   ║
║  ║  ║ (User can change if different account)                ║ ║   ║
║  ║  ╚─────────────────────────────────────────────────────────╝ ║   ║
║  ║                                                               ║   ║
║  ║  SELECT CATEGORY (OPTIONAL): ◄─ User CAN skip               ║   ║
║  ║  ╔─────────────────────────────────────────────────────────╗ ║   ║
║  ║  ║ Groceries ▼                                            ║ ║   ║
║  ║  ║ ├─ — No Category —  ◄─ Always available                ║ ║   ║
║  ║  ║ ├─ Groceries  ◄─ Selected                              ║ ║   ║
║  ║  ║ ├─ Utilities                                           ║ ║   ║
║  ║  ║ ├─ Entertainment                                       ║ ║   ║
║  ║  ║ └─ Transport                                           ║ ║   ║
║  ║  ║ (Only EXPENSE categories shown for EXPENSE SMS)        ║ ║   ║
║  ║  ╚─────────────────────────────────────────────────────────╝ ║   ║
║  ║                                                               ║   ║
║  ║  ACTION BUTTONS:                                             ║   ║
║  ║  ╔─────────╗  ╔─────────╗  ╔─────────╗                      ║   ║
║  ║  │ Confirm │  │ Ignore  │  │ Cancel  │                      ║   ║
║  ║  ╚─────────╝  ╚─────────╝  ╚─────────╝                      ║   ║
║  ║     ✓            ⊘            ✗                              ║   ║
║  ║  Approve      Discard      No Action                         ║   ║
║  ║                                                               ║   ║
║  ╚─────────────────────────────────────────────────────────────╝   ║
║                                                                        ║
║  User Flow:                                                            ║
║  1. Review transaction amount & SMS text                              ║
║  2. Select account from dropdown (if not pre-selected)                ║
║  3. Select category (optional)                                        ║
║  4. Click [Confirm] to approve                                        ║
║                                                                        ║
╚═══════════════════════════════════════════════════════════════════════╝
                                    │
                        User action (Confirm/Ignore/Cancel)
                                    │
                    ┌───────────────┼───────────────┐
                    │               │               │
        [Confirm]   │   [Ignore]    │   [Cancel]    │
                    │               │               │
                    ▼               ▼               ▼
            ✓ APPROVED          IGNORED         PENDING
            (CONFIRMED)         (HIDDEN)        (STAYS)
                    │               │               │
                    ▼               ▼               ▼
        Transaction Created   SMS Discarded    Back to List
        Inserted in DB        Can't recover    Can review later
                    │               │               │
                    ▼               ▼               ▼
        ✓ Appears in Dashboard
        ✓ Amount updated in totals
        ✓ Category applied
        ✓ Account balance updated
        ✓ Audit trail maintained (references SMS)
                    │
                    ▼
╔═══════════════════════════════════════════════════════════════════════╗
║                      DASHBOARD UPDATED                                ║
╠═══════════════════════════════════════════════════════════════════════╣
║                                                                        ║
║  Recent Transactions:                                                  ║
║  ├─ ₹500 EXPENSE (Groceries) ◄─ NEW! Just added                      ║
║  ├─ ₹1000 INCOME (Salary)                                             ║
║  └─ ...                                                               ║
║                                                                        ║
║  Monthly Income: ₹51,000 (Updated)                                    ║
║  Monthly Expense: ₹20,500 (Updated)                                   ║
║                                                                        ║
║  HDFC Bank Balance: ₹9,500 (Updated)                                  ║
║                                                                        ║
║  SMS Badge: [Gone or shows new count if more pending]                 ║
║                                                                        ║
╚═══════════════════════════════════════════════════════════════════════╝
```

---

## Quick Navigation Map

```
┌──────────────────────────────────────────────────────────────────┐
│                      HOW TO ACCESS                               │
└──────────────────────────────────────────────────────────────────┘

METHOD 1: FROM MENU
────────────────────
Menu (☰) 
    ↓
SMS Imports
    ↓
SMS Import Screen appears


METHOD 2: FROM NOTIFICATION
──────────────────────────────
SMS arrives
    ↓
Notification: "SMS Transaction Detected"
    ↓
Tap notification
    ↓
SMS Import Screen appears


METHOD 3: FROM DASHBOARD BADGE
───────────────────────────────
Dashboard
    ↓
Look for: "Recent Transactions [1]" (red badge)
    ↓
Tap badge
    ↓
SMS Import Screen appears


ALL METHODS → SMS IMPORT SCREEN (Same destination)
        ↓
    Click item
        ↓
    Review Dialog appears
        ↓
    Select account & category
        ↓
    Click Confirm/Ignore/Cancel
        ↓
    Update Dashboard
```

---

## Category Filtering Example

```
EXPENSE Transaction (₹500 debited):
────────────────────────────────────

Category Dropdown Shows:
├─ — No Category —  ◄ Always available
├─ Groceries        ◄ EXPENSE category
├─ Utilities        ◄ EXPENSE category
├─ Entertainment    ◄ EXPENSE category
├─ Transport        ◄ EXPENSE category
└─ Healthcare       ◄ EXPENSE category

(Income categories NOT shown)


INCOME Transaction (₹5000 credited):
────────────────────────────────────

Category Dropdown Shows:
├─ — No Category —  ◄ Always available
├─ Salary           ◄ INCOME category
├─ Bonus            ◄ INCOME category
├─ Interest         ◄ INCOME category
├─ Dividend         ◄ INCOME category
└─ Gift             ◄ INCOME category

(Expense categories NOT shown)
```

---

## Data Flow Summary

```
┌─────────────┐
│   SMS Input │
└────────┬────┘
         │
         ▼
    ┌────────────────┐
    │ SmsReceiver    │  Parses SMS
    └────────┬───────┘
             │
             ▼
    ┌────────────────────┐
    │ Extract Details:   │
    │ • Amount           │
    │ • Type             │
    │ • Account Number   │
    └────────┬───────────┘
             │
        ┌────┴────┐
        │          │
        ▼          ▼
    Matched?   Not Matched?
        │          │
        ▼          ▼
    Auto-confirm  Show Badge
    Skip review   Notification
    (1-3 sec)     User reviews
                  (Manual)
        │          │
        └────┬─────┘
             │
             ▼
    ┌──────────────────────────┐
    │ SmsImport created in DB  │
    │ Status: PENDING          │
    └────────┬─────────────────┘
             │
             ▼
    ┌──────────────────────────┐
    │ User Reviews SMS         │
    │ (Via SMS Import Screen)  │
    └────────┬─────────────────┘
             │
        ┌────┼────┐
        │    │    │
        ▼    ▼    ▼
    Confirm Ignore Cancel
        │    │      │
        ▼    ▼      ▼
    CONFIRMED IGNORED PENDING
        │    │      │
        ▼    ▼      │
    Transaction   No Action
    Created       (stays)
        │
        ▼
    ✓ Dashboard Updated
    ✓ Recent Transactions updated
    ✓ Totals recalculated
    ✓ Account balance updated
```

---

## What Each Button Does

```
┌──────────────────────────────────────────────────────────────┐
│              REVIEW DIALOG BUTTONS                            │
├──────────────────────────────────────────────────────────────┤
│                                                               │
│  [Confirm] ✓                                                 │
│  ├─ Validates account is selected                            │
│  ├─ Creates Transaction record                               │
│  ├─ Inserts into transactions table                          │
│  ├─ Updates SmsImport status: CONFIRMED                      │
│  ├─ Applies selected category (if chosen)                    │
│  ├─ Closes dialog                                            │
│  ├─ Shows toast: "Transaction recorded"                      │
│  ├─ Removes item from SMS Import list                        │
│  └─ Dashboard updates automatically                          │
│                                                               │
│  [Ignore] ⊘                                                  │
│  ├─ Updates SmsImport status: IGNORED                        │
│  ├─ Hides SMS from list                                      │
│  ├─ Does NOT create transaction                              │
│  ├─ Shows toast: "SMS ignored"                               │
│  ├─ Cannot be undone (careful!)                              │
│  └─ Record kept for audit trail                              │
│                                                               │
│  [Cancel] ✗                                                  │
│  ├─ Closes dialog                                            │
│  ├─ Takes no action                                          │
│  ├─ SMS stays PENDING                                        │
│  ├─ Can review again later                                   │
│  └─ No changes made                                          │
│                                                               │
└──────────────────────────────────────────────────────────────┘
```

---

**Key Points:**
- ✅ Everything already exists in your app
- ✅ Users can review, select account, select category
- ✅ Categories auto-filter by transaction type
- ✅ Auto-confirmation for matched accounts (new feature)
- ✅ Manual review for unmatched accounts
- ✅ Dashboard updates automatically after approval

