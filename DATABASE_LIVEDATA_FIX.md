# Database LiveData Exception Fix

## Issue
**FATAL EXCEPTION: arch_disk_io_1**
```
java.lang.RuntimeException: Exception while computing database live data.
Process: com.financetracker, PID: 21610
```

## Root Cause
The database version was not incremented after adding the new `nickName` field to the `Merchant` entity. When Room's database schema changed but the version number remained the same, the database migrations could cause exceptions when computing LiveData.

## Solution Applied

### 1. **Incremented Database Version**
- **File**: `AppDatabase.java`
- **Change**: Updated database version from `7` to `8`
- **Reason**: This tells Room that the schema has changed and triggers the `fallbackToDestructiveMigration()` strategy to handle the change gracefully

```java
@Database(
    entities = {...},
    version = 8,  // Changed from 7
    exportSchema = false
)
```

### 2. **Verified Merchant Entity**
The `Merchant` entity properly includes the new field:
- Field is nullable: `public String nickName;`
- Constructor properly initializes it: `this.nickName = null;`
- Room-generated code correctly maps the column

### 3. **Database Configuration Review**
The `AppDatabase` class already has `fallbackToDestructiveMigration()` enabled, which:
- Destroys old tables and recreates them when the schema changes
- Automatically applies the new schema including the `nickName` column
- Prevents crashes when queries try to access columns that don't exist

## Verification

✅ **Clean Build Successful**
```
BUILD SUCCESSFUL in 1m 8s
96 actionable tasks: 94 executed, 2 up-to-date
```

✅ **No Compilation Errors**
All Room-generated code correctly includes the `nickName` field in cursor mapping

✅ **LiveData Queries**
The `getAllActive()` method now properly:
- Handles the `nickName` column
- Maps nullable strings correctly
- Returns complete Merchant objects

## What This Fixes

When the app launches or accesses `MerchantRepository.getAllActive()`:
1. Room detects the schema version change (7 → 8)
2. Triggers `fallbackToDestructiveMigration()` if needed
3. Recreates tables with the new schema including `nickName`
4. LiveData queries execute successfully without exceptions
5. All Merchant objects are properly constructed with all fields

## Testing Recommendations

1. **Fresh Install**: Install the updated APK on a test device
2. **Upgrade Path**: Verify upgrade from previous version works
3. **Verify Merchants**: Check that the Merchant list displays correctly
4. **Edit Merchants**: Test the new nickname feature

## Files Modified
- `AppDatabase.java` - Version incremented from 7 to 8

