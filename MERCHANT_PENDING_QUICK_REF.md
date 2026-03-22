# Quick Reference: Merchant Categorization Filter Implementation

## What Was Changed
Modified SMS pending transaction queries to exclude merchants that are already categorized.

## Files Modified
- **SmsImportDao.java** (2 methods)
  - `getPending()` - Lines 16-22
  - `getPendingCount()` - Lines 24-30

## The Problem
Users were seeing SMS messages from already-categorized merchants in the pending review queue, requiring them to re-categorize the same merchant multiple times.

## The Solution
Added a LEFT JOIN with the merchants table to filter out SMS imports where:
- The merchant exists in the database
- AND the merchant has a category already assigned

## How to Test

### Test Case 1: New Merchant (Should Show in Pending)
```
1. Delete or remove category from "TestMerchant" if it exists
2. Simulate incoming SMS from "TestMerchant"
3. Check SMS Review screen
4. ✅ SMS should appear in pending list
```

### Test Case 2: Already Categorized (Should NOT Show)
```
1. Ensure merchant "Amazon" exists with a category
2. Simulate incoming SMS from "Amazon"
3. Check SMS Review screen
4. ❌ SMS should NOT appear in pending list
5. ✅ But should appear if category is removed
```

### Test Case 3: Case-Insensitive Matching
```
1. Merchant "Starbucks" has category assigned
2. Incoming SMS with "STARBUCKS" or "starbucks"
3. ❌ Should still be filtered out (case-insensitive match)
```

## Database Impact
- No schema changes required
- No data migration needed
- Backward compatible
- Efficient query with proper JOIN

## Live Data Updates
All affected screens automatically update when:
1. A new SMS import is received
2. A merchant category is added/updated
3. An SMS import is confirmed/ignored

Because the queries use `LiveData`, changes are reflected immediately:
- SMS Review Screen
- SMS Import List Screen
- Dashboard pending count badge

## Performance Notes
- LEFT JOIN with merchants table on name matching
- Uses indexed columns (status, deleted)
- LOWER() function for case-insensitive matching
- Negligible performance impact

## Rollback (if needed)
To revert to the original behavior that shows all pending SMS:
```java
// In SmsImportDao.java, replace getPending() with:
@Query("SELECT * FROM sms_import WHERE status = 'PENDING' AND deleted = 0 ORDER BY createdAt DESC")
LiveData<List<SmsImport>> getPending();

// And replace getPendingCount() with:
@Query("SELECT COUNT(*) FROM sms_import WHERE status = 'PENDING' AND deleted = 0")
LiveData<Integer> getPendingCount();
```

## Related Code References

### Entity: Merchant.java
```java
public class Merchant {
    public String uuid;
    public String name;
    public String categoryId;  // This field is used for filtering
    // ...
}
```

### Entity: SmsImport.java
```java
public class SmsImport {
    public String uuid;
    public String merchantName;  // Matched against merchants.name
    public String status;        // Must be 'PENDING'
    // ...
}
```

## Future Enhancements
1. Auto-categorize SMS based on merchant category
2. Show categorized merchants separately
3. Allow re-categorization of merchant
4. Bulk merchant categorization

---
**Documentation:** MERCHANT_CATEGORIZATION_PENDING_FIX.md
**Last Updated:** March 2026

