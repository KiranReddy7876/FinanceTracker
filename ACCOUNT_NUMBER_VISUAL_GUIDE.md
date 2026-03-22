# Account Number Feature - Visual Guide & Examples

## User Interface Walkthrough

### 1. Account List (Default View)
```
┌─────────────────────────────────────────┐
│  Finance Tracker - Accounts             │
├─────────────────────────────────────────┤
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Checking                          │  │
│  │ BANK •••1234          $2,500.00   │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Savings                           │  │
│  │ BANK •••5678          $10,000.00  │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Credit Card                       │  │
│  │ CREDIT_CARD •••9012  -$500.00    │  │
│  └───────────────────────────────────┘  │
│                                         │
│  ┌───────────────────────────────────┐  │
│  │ Cash                              │  │
│  │ CASH                  $1,200.00   │  │
│  └───────────────────────────────────┘  │
│                                         │
│                                  [+]    │
└─────────────────────────────────────────┘
```

### 2. Create Account Dialog

**Before (Old)**
```
┌─────────────────────────────────────┐
│ Add Account                         │
├─────────────────────────────────────┤
│                                     │
│  Account Name                       │
│  ┌───────────────────────────────┐  │
│  │ Checking                      │  │
│  └───────────────────────────────┘  │
│                                     │
│  Account Type                       │
│  ┌───────────────────────────────┐  │
│  │ BANK         ▼                │  │
│  └───────────────────────────────┘  │
│                                     │
│  Opening Balance                    │
│  ┌───────────────────────────────┐  │
│  │ 5000                          │  │
│  └───────────────────────────────┘  │
│                                     │
│           [Save] [Cancel]           │
└─────────────────────────────────────┘
```

**After (New)**
```
┌─────────────────────────────────────┐
│ Add Account                         │
├─────────────────────────────────────┤
│                                     │
│  Account Name                       │
│  ┌───────────────────────────────┐  │
│  │ Checking                      │  │
│  └───────────────────────────────┘  │
│                                     │
│  Account Type                       │
│  ┌───────────────────────────────┐  │
│  │ BANK         ▼                │  │
│  └───────────────────────────────┘  │
│                                     │
│  Account Number (Last 4 Digits)    │
│  ┌───────────────────────────────┐  │
│  │ 1234                          │  │
│  └───────────────────────────────┘  │
│                                     │
│  Opening Balance                    │
│  ┌───────────────────────────────┐  │
│  │ 5000                          │  │
│  └───────────────────────────────┘  │
│                                     │
│           [Save] [Cancel]           │
└─────────────────────────────────────┘
```

### 3. Edit Account Dialog
```
┌─────────────────────────────────────┐
│ Edit Account                        │
├─────────────────────────────────────┤
│                                     │
│  Account Name                       │
│  ┌───────────────────────────────┐  │
│  │ Checking                      │  │
│  └───────────────────────────────┘  │
│                                     │
│  Account Type                       │
│  ┌───────────────────────────────┐  │
│  │ BANK         ▼                │  │
│  └───────────────────────────────┘  │
│                                     │
│  Account Number (Last 4 Digits)    │
│  ┌───────────────────────────────┐  │
│  │ 1234                          │  │
│  └───────────────────────────────┘  │
│                                     │
│  Opening Balance                    │
│  ┌───────────────────────────────┐  │
│  │ 5000                          │  │
│  └───────────────────────────────┘  │
│                                     │
│   [Save] [Delete] [Cancel]          │
└─────────────────────────────────────┘
```

## SMS Integration Examples

### Example 1: Bank of India SMS
```
INPUT SMS:
"Your A/C XXX1234 has been debited with Rs.100.00 on 15/03/2026.
Avl Bal: Rs.2500. For details call 1800-123456"

EXTRACTION:
├─ likelyContainsAccountNumber() → true ✓
├─ extractLast4Digits() → "1234" ✓
└─ isValidAccountNumber("1234") → true ✓

MATCHING:
└─ Checking (BANK •••1234) ✓ [AUTO-MATCHED]
```

### Example 2: HDFC Bank SMS
```
INPUT SMS:
"Dear Customer, Your Credit Card ending with 9012 has been charged
Rs.2,500 on 15-Mar-2026. Txn Ref: TXN123456"

EXTRACTION:
├─ likelyContainsAccountNumber() → true ✓
├─ extractLast4Digits() → "9012" ✓
└─ isValidAccountNumber("9012") → true ✓

MATCHING:
└─ Credit Card (CREDIT_CARD •••9012) ✓ [AUTO-MATCHED]
```

### Example 3: ICICI Bank SMS
```
INPUT SMS:
"•••5678 was credited with Rs.10000 on 15/03/2026.
Transfer reference: TRANS123"

EXTRACTION:
├─ likelyContainsAccountNumber() → true ✓
├─ extractLast4Digits() → "5678" ✓
└─ isValidAccountNumber("5678") → true ✓

MATCHING:
└─ Savings (BANK •••5678) ✓ [AUTO-MATCHED]
```

### Example 4: International Transfer SMS
```
INPUT SMS:
"Transfer to A/C XXXXXX7890 completed. Amount: $100. Txn ID: TXN789"

EXTRACTION:
├─ likelyContainsAccountNumber() → true ✓
├─ extractLast4Digits() → "7890" ✓
└─ isValidAccountNumber("7890") → true ✓

MATCHING:
└─ Global (BANK •••7890) ✓ [AUTO-MATCHED]
```

### Example 5: No Account Found
```
INPUT SMS:
"Your account XXXX3333 has been credited with Rs.500"

EXTRACTION:
├─ likelyContainsAccountNumber() → true ✓
├─ extractLast4Digits() → "3333" ✓
└─ isValidAccountNumber("3333") → true ✓

MATCHING:
└─ No matching account found ✗ [USER SELECTS MANUALLY]
```

## Feature Comparison

### Before Implementation
```
┌──────────────────────────────────────────────┐
│ Checking        BANK              $2,500.00  │
│ Savings         BANK              $10,000.00 │
│ Credit          CREDIT_CARD       -$500.00   │
│ Cash            CASH              $1,200.00  │
└──────────────────────────────────────────────┘

❌ No account numbers visible
❌ Can't distinguish same bank accounts
❌ Can't auto-match SMS transactions
```

### After Implementation
```
┌──────────────────────────────────────────────┐
│ Checking        BANK •••1234      $2,500.00  │
│ Savings         BANK •••5678      $10,000.00 │
│ Credit          CREDIT_CARD •••9012 -$500.00 │
│ Cash            CASH              $1,200.00  │
└──────────────────────────────────────────────┘

✅ Account numbers visible (masked)
✅ Can easily distinguish accounts
✅ Can auto-match SMS transactions
✅ Privacy-focused (only last 4 digits)
✅ Optional field (backward compatible)
```

## User Stories

### Story 1: Manual Entry
```
User: "I have 2 checking accounts and want to track them separately"

Action:
1. Create first account "Checking - Office"
2. Enter account number "1234"
3. Create second account "Checking - Personal"  
4. Enter account number "5678"

Result:
- Checking - Office      BANK •••1234    $5,000.00
- Checking - Personal    BANK •••5678    $3,000.00

Benefit: Can now easily identify which account each transaction belongs to
```

### Story 2: SMS Auto-Population
```
User: "I receive SMS when money is transferred from my bank accounts"

Action:
1. Receives SMS: "Your A/C XXXX1234 debited Rs.100"
2. Opens SMS Import dialog
3. System detects account number "1234"
4. Auto-matches to "Checking - Office"
5. User confirms and imports

Result: Transaction is automatically assigned to correct account

Benefit: No need to manually select account for every transaction
```

### Story 3: Account Identification
```
User: "I need to know which bank account a transaction came from"

Before:
- Transaction: "Debit Rs.100"
- Can't tell which account it's from
- Need to check transaction details

After:
- Transaction: "Debit Rs.100 (BANK •••1234)"
- Can immediately see which account
- Account is identified in transaction list

Benefit: Instant account identification without opening transaction details
```

## Data Flow Diagram

```
┌─────────────────┐
│  SMS Received   │
│  "A/C •••1234   │
│   debited       │
│   Rs.100"       │
└────────┬────────┘
         │
         ▼
┌──────────────────────────────┐
│ SmsAccountNumberExtractor    │
│ .extractLast4Digits()        │
│ → "1234"                     │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ AccountRepository            │
│ .findByAccountNumber("1234") │
│ → Checking (BANK •••1234)   │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ SMS Import Dialog            │
│ Account field auto-populated │
│ User confirms and imports    │
└────────┬─────────────────────┘
         │
         ▼
┌──────────────────────────────┐
│ Transaction Created          │
│ - Amount: Rs.100             │
│ - Account: Checking          │
│ - Account#: •••1234          │
│ - Type: EXPENSE              │
└──────────────────────────────┘
```

## Regex Patterns Used

```
Pattern 1: Bullet/Asterisk Mask
[•*]{3,4}(\d{4})
Matches: •••1234, ****5678, ••••9012

Pattern 2: Account Prefix
(?:A/C|ACCOUNT|ACC|ACCT)[\s.:]*[X•]*[X•]*[X•]*[X•]?(\d{4})
Matches: A/C XXXX1234, ACCOUNT ••••5678, ACC 1234

Pattern 3: X-Masked Format
[Xx]{4}(\d{4})
Matches: xxxx1234, XXXX5678, Xxxx9012

Pattern 4: Full Account Number
\b(\d{10,16})\b
Matches: 1234567890123456 (extracts last 4: 3456)
```

---

**Visual Guide Complete** ✅

