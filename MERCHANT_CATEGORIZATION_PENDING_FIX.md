# Merchant Categorization - Pending Transactions Filter

## Overview
This feature ensures that when a merchant is already categorized, SMS messages from that merchant no longer appear in the pending transactions queue. Only SMS imports from merchants without a category assignment will be shown in the pending review list.

## Problem Statement
Previously, all SMS imports with "PENDING" status would appear in the pending transactions list, regardless of whether the merchant had already been categorized. This meant users would see duplicate categorization prompts for merchants they had already assigned categories to.

## Solution
Updated the database queries in `SmsImportDao` to perform a LEFT JOIN with the merchants table and filter out SMS imports where:
- The merchant already has a category assigned (`merchants.categoryId IS NOT NULL`)
- The merchant exists in the database

## Changes Made

### 1. SmsImportDao.java - getPending() Query

**Before:**
```java
@Query("SELECT * FROM sms_import WHERE status = 'PENDING' AND deleted = 0 ORDER BY createdAt DESC")
LiveData<List<SmsImport>> getPending();
```

**After:**
```java
@Query("SELECT s.* FROM sms_import s " +
        "LEFT JOIN merchants m ON LOWER(s.merchantName) = LOWER(m.name) AND m.deleted = 0 " +
        "WHERE s.status = 'PENDING' " +
        "AND s.deleted = 0 " +
        "AND (s.merchantName IS NULL OR s.merchantName = '' OR m.categoryId IS NULL) " +
        "ORDER BY s.createdAt DESC")
LiveData<List<SmsImport>> getPending();
```

### 2. SmsImportDao.java - getPendingCount() Query

**Before:**
```java
@Query("SELECT COUNT(*) FROM sms_import WHERE status = 'PENDING' AND deleted = 0")
LiveData<Integer> getPendingCount();
```

**After:**
```java
@Query("SELECT COUNT(*) FROM sms_import s " +
        "LEFT JOIN merchants m ON LOWER(s.merchantName) = LOWER(m.name) AND m.deleted = 0 " +
        "WHERE s.status = 'PENDING' " +
        "AND s.deleted = 0 " +
        "AND (s.merchantName IS NULL OR s.merchantName = '' OR m.categoryId IS NULL)")
LiveData<Integer> getPendingCount();
```

## How It Works

### Filter Logic
The updated queries include SMS imports that meet ANY of these conditions:
1. **No merchant name** - SMS with no extracted merchant name (always show)
2. **Empty merchant name** - SMS with empty merchant string (always show)
3. **Merchant not categorized** - Merchant exists but has no category assigned (`m.categoryId IS NULL`)

The queries EXCLUDE SMS imports where:
- The merchant name matches an existing merchant in the database (case-insensitive)
- AND that merchant has a category assigned (`m.categoryId IS NOT NULL`)

### Case-Insensitive Matching
The query uses `LOWER()` function to ensure merchants are matched regardless of case:
```sql
LOWER(s.merchantName) = LOWER(m.name)
```

This means "Amazon", "AMAZON", and "amazon" will all match the same merchant record.

## Affected Components

### View Models (Auto-Updated)
1. **SmsReviewViewModel** - Shows pending items for review
2. **SmsImportViewModel** - Displays pending SMS imports list
3. **DashboardViewModel** - Shows count of pending SMS imports

All these components automatically benefit from the updated queries without code changes.

### Workflow

#### Before Categorization
```
New SMS from "Amazon" received
↓
Merchant "Amazon" has NO category
↓
SMS appears in PENDING list
↓
User reviews and assigns category
```

#### After Categorization
```
New SMS from "Amazon" received
↓
Merchant "Amazon" has category = "Shopping"
↓
SMS does NOT appear in PENDING list (filtered out)
↓
User can see it in transaction history or auto-categorization
```

## Database Query Explanation

### JOIN Operation
```sql
LEFT JOIN merchants m ON LOWER(s.merchantName) = LOWER(m.name) AND m.deleted = 0
```
- Uses LEFT JOIN to keep SMS imports even if merchant doesn't exist
- Matches on merchant name (case-insensitive)
- Only considers active merchants (not deleted)

### WHERE Conditions
```sql
WHERE s.status = 'PENDING' 
  AND s.deleted = 0 
  AND (s.merchantName IS NULL OR s.merchantName = '' OR m.categoryId IS NULL)
```
- Must have PENDING status
- Must not be deleted
- Either:
  - No merchant name extracted, OR
  - Merchant name is empty, OR
  - Merchant exists but has no category

## Testing Scenarios

### Scenario 1: New Merchant (First Time)
1. SMS received from "Pizza Hut"
2. Merchant "Pizza Hut" doesn't exist
3. LEFT JOIN returns NULL for merchant
4. `m.categoryId IS NULL` condition is TRUE
5. ✅ SMS appears in pending list

### Scenario 2: Already Categorized Merchant
1. SMS received from "Amazon"
2. Merchant "Amazon" exists with categoryId = "shopping-uuid"
3. LEFT JOIN matches the merchant
4. `m.categoryId IS NULL` condition is FALSE
5. ❌ SMS does NOT appear in pending list

### Scenario 3: Merchant Without Category
1. SMS received from "Starbucks"
2. Merchant "Starbucks" exists with categoryId = NULL
3. LEFT JOIN matches the merchant
4. `m.categoryId IS NULL` condition is TRUE
5. ✅ SMS appears in pending list

## Benefits

1. **Cleaner Review Queue** - Users only see SMS that need categorization
2. **Better UX** - No duplicate category prompts for known merchants
3. **Auto-Categorization Support** - Once categorized, future SMS are hidden from manual review
4. **Data Consistency** - Uses existing merchant categorization logic
5. **Performance** - Efficient database query with proper JOIN and filtering

## Migration Notes

- No database schema changes required
- No data migration needed
- Existing SMS imports remain unchanged
- The query change is backward compatible

## Future Enhancements

1. **Auto-Categorization** - When merchant is categorized, auto-assign category to new SMS
2. **Notification** - Show categorized merchants separately (informational only)
3. **Override** - Allow users to re-categorize already-categorized merchants
4. **Bulk Operations** - Categorize multiple merchants at once

## Code Location

**File:** `app/src/main/java/com/financetracker/data/db/dao/SmsImportDao.java`

**Methods Updated:**
- `getPending()` - Line 16-22
- `getPendingCount()` - Line 24-30

## Related Files

- `SmsImportRepository.java` - Uses the DAO methods
- `SmsReviewViewModel.java` - Consumes pending items
- `SmsImportViewModel.java` - Displays pending SMS
- `DashboardViewModel.java` - Shows pending count
- `Merchant.java` - Entity with categoryId field
- `SmsImport.java` - Entity with merchantName field

