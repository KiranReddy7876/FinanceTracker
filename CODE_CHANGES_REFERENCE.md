# Code Changes Reference - Line by Line

## File 1: SmsReceiver.java

**Location:** `app/src/main/java/com/financetracker/service/SmsReceiver.java`

### Change: Removed auto-confirmation (Lines 85-100)

**BEFORE:**
```java
SmsImportRepository smsImportRepo = new SmsImportRepository(context);
smsImportRepo.insert(record);

// If account was auto-matched, automatically confirm and convert to transaction
if (matchedAccountId != null) {
    // Auto-confirm the SMS import since we have a valid account match
    smsImportRepo.confirmWithoutUserReview(record.uuid);
    Log.d(TAG, "Auto-confirmed SMS import with matched account: " + matchedAccountId);
} else {
    // Show notification to user about pending import requiring manual review
    SmsImportNotificationService.notifyPendingImport(context, 1);
    Log.d(TAG, "SMS import requires user review - no account match found");
}
```

**AFTER:**
```java
SmsImportRepository smsImportRepo = new SmsImportRepository(context);
smsImportRepo.insert(record);

// Show notification for ALL pending SMS imports
// User must review and select category before transaction is created
SmsImportNotificationService.notifyPendingImport(context, 1);

if (matchedAccountId != null) {
    Log.d(TAG, "SMS import stored as PENDING - auto-matched account: " + matchedAccountId + 
               " (user must select category)");
} else {
    Log.d(TAG, "SMS import stored as PENDING - user must select account and category");
}
```

**Impact:** All SMS now stored as PENDING, notification always shown

---

## File 2: SmsImportFragment.java

**Location:** `app/src/main/java/com/financetracker/ui/smsimport/SmsImportFragment.java`

### Change 1: Added category validation (Lines 135-144)

**BEFORE:**
```java
int catPos = spinnerCategory.getSelectedItemPosition();
if (catPos > 0 && catPos - 1 < typeCategories.size()) {
    selectedCategoryId = typeCategories.get(catPos - 1).uuid;
}
```

**AFTER:**
```java
int catPos = spinnerCategory.getSelectedItemPosition();

// REQUIRE category selection (catPos > 0 means not "— No Category —")
if (catPos <= 0) {
    Toast.makeText(requireContext(), "Please select a category", Toast.LENGTH_SHORT).show();
    return;
}

if (catPos > 0 && catPos - 1 < typeCategories.size()) {
    selectedCategoryId = typeCategories.get(catPos - 1).uuid;
}
```

**Impact:** Category is now REQUIRED for confirmation

### Change 2: Changed Cancel to Delete button (Lines 154-160)

**BEFORE:**
```java
.setNeutralButton("Ignore", (d, w) -> {
    viewModel.ignoreImport(smsImport.uuid);
    Toast.makeText(requireContext(), "SMS ignored", Toast.LENGTH_SHORT).show();
})
.setNegativeButton("Cancel", null)
.show();
```

**AFTER:**
```java
.setNeutralButton("Ignore", (d, w) -> {
    viewModel.ignoreImport(smsImport.uuid);
    Toast.makeText(requireContext(), "SMS ignored", Toast.LENGTH_SHORT).show();
})
.setNegativeButton("Delete", (d, w) -> {
    viewModel.deleteSmsImport(smsImport.uuid);
    Toast.makeText(requireContext(), "SMS deleted", Toast.LENGTH_SHORT).show();
})
.setOnDismissListener(dialog -> {
    // Refresh list when dialog closes
    // Data will update automatically via LiveData
})
.show();
```

**Impact:** Delete button available for pending SMS

---

## File 3: SmsImportViewModel.java

**Location:** `app/src/main/java/com/financetracker/ui/smsimport/SmsImportViewModel.java`

### Change: Added delete method (After line 65)

**BEFORE:**
```java
/**
 * Ignore SMS import - mark as IGNORED
 */
public void ignoreImport(String smsImportId) {
    smsImportRepo.ignore(smsImportId);
}
}
```

**AFTER:**
```java
/**
 * Ignore SMS import - mark as IGNORED
 */
public void ignoreImport(String smsImportId) {
    smsImportRepo.ignore(smsImportId);
}

/**
 * Delete SMS import (soft delete)
 */
public void deleteSmsImport(String smsImportId) {
    smsImportRepo.delete(smsImportId);
}
}
```

**Impact:** ViewModel can now trigger SMS deletion

---

## File 4: SmsImportRepository.java

**Location:** `app/src/main/java/com/financetracker/data/repository/SmsImportRepository.java`

### Change: Added delete method (After line 57)

**BEFORE:**
```java
public void ignore(String uuid) {
    executor.execute(() -> smsImportDao.updateStatus(uuid, "IGNORED", System.currentTimeMillis()));
}

public void updateAccountAndCategory(String smsImportId, String accountId, String categoryId) {
    executor.execute(() -> {
        smsImportDao.updateAccountAndCategory(smsImportId, accountId, categoryId, System.currentTimeMillis());
    });
}
```

**AFTER:**
```java
public void ignore(String uuid) {
    executor.execute(() -> smsImportDao.updateStatus(uuid, "IGNORED", System.currentTimeMillis()));
}

public void delete(String uuid) {
    // Soft delete SMS import
    executor.execute(() -> {
        SmsImport smsImport = smsImportDao.getById(uuid);
        if (smsImport != null) {
            smsImport.deleted = true;
            smsImport.updatedAt = System.currentTimeMillis();
            smsImportDao.update(smsImport);
        }
    });
}

public void updateAccountAndCategory(String smsImportId, String accountId, String categoryId) {
    executor.execute(() -> {
        smsImportDao.updateAccountAndCategory(smsImportId, accountId, categoryId, System.currentTimeMillis());
    });
}
```

**Impact:** SMS can be soft-deleted from repository

---

## File 5: SmsImportDao.java

**Location:** `app/src/main/java/com/financetracker/data/db/dao/SmsImportDao.java`

### Change: Updated queries to exclude deleted (Lines 16-20)

**BEFORE:**
```java
@Query("SELECT * FROM sms_import WHERE status = 'PENDING' ORDER BY createdAt DESC")
LiveData<List<SmsImport>> getPending();

@Query("SELECT COUNT(*) FROM sms_import WHERE status = 'PENDING'")
LiveData<Integer> getPendingCount();
```

**AFTER:**
```java
@Query("SELECT * FROM sms_import WHERE status = 'PENDING' AND deleted = 0 ORDER BY createdAt DESC")
LiveData<List<SmsImport>> getPending();

@Query("SELECT COUNT(*) FROM sms_import WHERE status = 'PENDING' AND deleted = 0")
LiveData<Integer> getPendingCount();
```

**Impact:** Deleted SMS won't appear in pending lists

---

## Summary of Changes

| File | Lines Changed | Type | Impact |
|------|---------------|------|--------|
| SmsReceiver.java | 85-100 | Logic | No auto-confirm, always notify |
| SmsImportFragment.java | 135-160 | Logic & UI | Category required, delete button |
| SmsImportViewModel.java | +8 | New method | Can trigger delete |
| SmsImportRepository.java | +13 | New method | Soft delete implementation |
| SmsImportDao.java | 16,19 | Query update | Exclude deleted records |

---

## No Changes Needed In

These files still work as-is:
- SmsImportConversionService.java
- TransactionRepository.java
- TransactionDao.java
- SmsImportNotificationService.java
- SmsParser.java
- All adapters and other files

---

## Total Changes
- **Files Modified:** 5
- **Lines Added:** ~30
- **Lines Changed:** ~20
- **Lines Removed:** ~10
- **Database Changes:** None (uses existing `deleted` column)
- **Schema Migrations:** None needed

All changes are minimal, focused, and non-breaking.

