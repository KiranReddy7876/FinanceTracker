# Transfer Balance Reflection Fix

## Problem
When a transfer was made between accounts, the new account balances were not reflected on the account screen. This was a critical bug that affected account reconciliation and balance tracking.

## Root Cause
The `getAllActiveWithBalance()` query in `AccountDao.java` was only calculating balances based on INCOME and EXPENSE transactions. **TRANSFER transactions were completely ignored**, even though they directly affect account balances:
- When a transfer is made FROM Account A, its balance decreases
- When a transfer is made TO Account B, its balance increases

### Original Query
```sql
SELECT a.*, 
       COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount ELSE 0 END), 0) as totalIncome,
       COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount ELSE 0 END), 0) as totalExpense
FROM accounts a
LEFT JOIN transactions t ON a.uuid = t.accountId AND t.deleted = 0
WHERE a.deleted = 0
GROUP BY a.uuid
ORDER BY a.name ASC
```

**Issues:**
1. Only joins transactions where `t.accountId` matches the account
2. Ignores the `transferToAccountId` field entirely
3. Doesn't count TRANSFER type transactions

## Solution
Updated the query to properly handle TRANSFER transactions:

### Fixed Query
```sql
SELECT a.*, 
       COALESCE(SUM(CASE WHEN t.type = 'INCOME' THEN t.amount 
                          WHEN t.type = 'TRANSFER' AND a.uuid = t.transferToAccountId THEN t.amount
                          ELSE 0 END), 0) as totalIncome,
       COALESCE(SUM(CASE WHEN t.type = 'EXPENSE' THEN t.amount 
                          WHEN t.type = 'TRANSFER' AND a.uuid = t.accountId THEN t.amount
                          ELSE 0 END), 0) as totalExpense
FROM accounts a
LEFT JOIN transactions t ON (
    (a.uuid = t.accountId AND t.type IN ('INCOME', 'EXPENSE', 'TRANSFER'))
    OR (a.uuid = t.transferToAccountId AND t.type = 'TRANSFER')
) AND t.deleted = 0
WHERE a.deleted = 0
GROUP BY a.uuid
ORDER BY a.name ASC
```

### Key Changes
1. **Transfer income calculation**: Added condition to include transfer amount when the account is the transfer recipient (`a.uuid = t.transferToAccountId`)
2. **Transfer expense calculation**: Added condition to include transfer amount when the account is the transfer source (`a.uuid = t.accountId` with `type = 'TRANSFER'`)
3. **Expanded JOIN condition**: Now joins transactions based on:
   - `accountId` match for INCOME, EXPENSE, and TRANSFER (source)
   - `transferToAccountId` match for TRANSFER (destination)

### Balance Formula (Unchanged)
The balance calculation formula in `AccountWithBalance.getCurrentBalance()` remains:
```
Current Balance = Opening Balance + Total Income - Total Expenses
```

With the fix, transfers are now properly included:
- From account: Transfer amount goes into `totalExpense` → decreases balance
- To account: Transfer amount goes into `totalIncome` → increases balance

## Files Modified
- `app/src/main/java/com/financetracker/data/db/dao/AccountDao.java`
  - Updated `getAllActiveWithBalance()` query method

## Testing
After this fix:
1. Create an account with opening balance of 1000
2. Transfer 100 to another account
3. The first account should now show 900 instead of 1000
4. The receiving account balance should increase by 100

## Build Status
✅ Build successful - No compilation errors

## Impact
This fix ensures that:
- Account balances reflect all transaction types correctly
- The UI displays accurate balances after transfers
- Account reconciliation is accurate
- User trust in the app's balance tracking is maintained

