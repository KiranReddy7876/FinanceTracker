# Database Version Update - Quick Fix Reference

## The Error You Got
```
Room cannot verify the data integrity. 
Looks like you've changed schema but forgot to update the version number.
```

## The Fix (Already Applied ✅)

**File:** `AppDatabase.java` (Line 18)

```java
// CHANGED FROM:
version = 2

// CHANGED TO:
version = 3
```

## Why This Happened

You added a new field to the Account entity:
```java
public String accountNumberLast4;  // Last 4 digits of account number
```

But Room tracks schema changes by version number. You need to increment it when the schema changes.

## What Happens Next

1. **On First Run:**
   - Room detects version changed from 2 → 3
   - Sees schema is different
   - Since `fallbackToDestructiveMigration()` is enabled, it recreates the database
   - New column `accountNumberLast4` is created

2. **Your Data:**
   - Development/Testing: Database will be cleared (expected)
   - Any test accounts will be gone (recreate them if needed)

3. **The App:**
   - Will launch successfully
   - Account number feature ready to use

## Next Steps

```bash
# 1. Clean build
./gradlew.bat clean build

# 2. Run the app
# The error should be gone!

# 3. Test the feature
# Create account with account number "1234"
# Should display as "BANK •••1234"
```

## All Fixed Files

- ✅ `AppDatabase.java` - Version updated to 3
- ✅ `Account.java` - New field added
- ✅ `dialog_account.xml` - Input field added
- ✅ `AccountsFragment.java` - Read/write logic
- ✅ `AccountsViewModel.java` - Method updated
- ✅ `AccountAdapter.java` - Display updated
- ✅ `SmsAccountNumberExtractor.java` - Utility created

## Status: READY TO BUILD ✅

No more database errors!

---

For more details, see: DATABASE_SCHEMA_FIX.md

