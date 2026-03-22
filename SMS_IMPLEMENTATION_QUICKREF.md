# SMS Transaction Capture - Quick Reference

## What Was Implemented

### 1. **Automatic SMS Reading & Processing**
- `SmsReceiver` class automatically listens for incoming SMS
- Background thread processing to avoid ANR
- Keywords-based filtering to identify transaction messages

### 2. **Account Number Extraction & Matching**
- Extracts last 4 digits from SMS (•••1234, A/C 1234, xxxx1234, etc.)
- Auto-matches with Account records using `accountNumberLast4` field
- Populates accountId in SmsImport record

### 3. **SMS Import Review Queue**
- SmsImport entity stores pending transactions
- `SmsImportFragment` shows review dialog per import
- User can confirm with selected account and category

### 4. **Automatic Transaction Creation**
- `SmsImportConversionService` converts confirmed SMS imports to Transactions
- Transaction created with amount, type, date, account, and optional category
- ReferenceId links back to SmsImport for audit trail

### 5. **User Notifications**
- `SmsImportNotificationService` shows notification on SMS arrival
- Tapping notification navigates to review screen
- Supports Android 13+ with notification channel

### 6. **Smart Category Selection**
- Categories filtered by transaction type (EXPENSE/INCOME)
- Optional category assignment (can be null)

## Code Changes Summary

### New Files Created
1. `SmsImportConversionService.java` - Convert SMS imports to transactions
2. `SmsImportNotificationService.java` - Show notifications

### Modified Files
1. `SmsReceiver.java` - Background processing, notifications
2. `SmsImportRepository.java` - Context parameter, conversion on confirm
3. `SmsImportDao.java` - Added getConfirmed() query
4. `SmsImportViewModel.java` - Added getCategoriesByType()
5. `SmsImportFragment.java` - Filter categories by type, improved UI
6. `AndroidManifest.xml` - Added POST_NOTIFICATIONS permission

### No Changes Needed
- Account.java (accountNumberLast4 field already exists)
- Transaction.java (referenceId field already exists)
- SmsImport.java (all fields already exist)
- SmsParser.java (already handles parsing)

## How to Test

### Prerequisites
1. Ensure account has `accountNumberLast4` value set (Settings → Accounts)
2. Grant SMS permission when prompted
3. Grant Notification permission when prompted

### Test Scenarios

**Test 1: Basic SMS Import**
```
Receive SMS: "Your A/C •••1234 has been debited with Rs. 500"
Expected: 
- SmsImport created with amount=500, type=EXPENSE
- Account auto-matched if accountNumberLast4="1234" exists
- Notification shown
```

**Test 2: Category Selection**
```
User opens SMS Review → clicks on EXPENSE transaction
Expected:
- Dialog shows amount, type, SMS text
- Category spinner shows only EXPENSE categories
- Can select category or leave empty
```

**Test 3: Transaction Creation**
```
User confirms SMS import with account and category selected
Expected:
- SmsImport status changed to CONFIRMED
- New Transaction created in transactions table
- Transaction has amount, type, accountId, categoryId
- referenceId = SmsImport UUID
```

**Test 4: Unmatched Account**
```
Receive SMS with last 4 digits that don't match any account
Expected:
- SmsImport created with accountId = null
- User must manually select account in review dialog
- Cannot confirm without selecting account
```

## API Methods

### SmsImportRepository
```java
void insert(SmsImport smsImport)                    // Add new SMS import
void confirm(String uuid)                           // Confirm & convert to transaction
void ignore(String uuid)                            // Mark as ignored
void updateAccountAndCategory(String id, 
    String accountId, String categoryId)            // Update selection
List<SmsImport> getConfirmed()                     // Get all confirmed (for sync)
```

### SmsImportViewModel
```java
LiveData<List<SmsImport>> pendingSmsImports         // Watch pending imports
LiveData<Integer> pendingCount                      // Watch pending count
void updateAccountAndCategory(String id, 
    String accountId, String categoryId)            // Update user selection
void confirmImport(String uuid)                     // Confirm import
void ignoreImport(String uuid)                      // Ignore import
LiveData<List<Category>> getCategoriesByType(String type)  // Get filtered categories
```

### SmsImportConversionService
```java
static void convertToTransaction(Context context, 
    SmsImport smsImport)                           // Convert single import
static void processAllConfirmed(Context context)   // Process all confirmed imports
```

## Database Queries

### Get pending SMS imports
```sql
SELECT * FROM sms_import WHERE status = 'PENDING' ORDER BY createdAt DESC
```

### Get confirmed SMS imports (not yet processed)
```sql
SELECT * FROM sms_import WHERE status = 'CONFIRMED' AND deleted = 0
```

### Match account by last 4 digits
```sql
SELECT * FROM accounts WHERE accountNumberLast4 = :last4 AND deleted = 0 LIMIT 1
```

## Threading Model

- **Main Thread**: UI interactions, navigation
- **Background Thread**: SMS processing, database queries for matching
- **Executor Service**: Repository operations (insert, update)

## Data Flow Diagram

```
Incoming SMS
    ↓
SmsReceiver (BroadcastReceiver)
    ↓
Background Thread Processing
    ↓
Parse Transaction (SmsParser)
    ├→ Extract amount, type, date
    └→ Extract account number last 4
    ↓
Match Account (AccountRepository.findByAccountNumber)
    ├→ Match found → populate accountId
    └→ No match → accountId = null
    ↓
Create SmsImport Record (status = PENDING)
    ↓
Store in Database
    ↓
Show Notification (SmsImportNotificationService)
    ↓
User Taps Notification
    ↓
SmsImportFragment Displayed
    ↓
User Reviews & Confirms
    ├→ Select/Verify Account
    └→ Select Category (optional)
    ↓
Update SmsImport (status = CONFIRMED, accountId, categoryId)
    ↓
SmsImportConversionService.convertToTransaction()
    ├→ Create Transaction record
    ├→ Set referenceId = SmsImport UUID
    └→ Insert into transactions table
    ↓
Transaction Appears in Dashboard
```

## Configuration Options for Users

1. **Account Settings**
   - Enter Account Number (last 4 digits)
   - Used for automatic SMS matching

2. **Category Setup**
   - Create categories for different types
   - EXPENSE and INCOME categories will be filtered automatically

3. **Permissions**
   - SMS: Required for receiving transactions
   - Notifications: Required to alert user about imports
   - Both needed for full functionality

## Known Limitations & Future Work

**Current Limitations:**
- Only extracts last 4 digits (bank limitation)
- No merchant name extraction
- No smart category prediction
- Manual review required for all imports
- No duplicate detection

**Planned Enhancements:**
- Merchant name extraction and matching
- Machine learning-based category prediction
- Auto-confirm rules based on patterns
- Duplicate detection using amount + date + account
- SMS filtering by sender
- Scheduled batch processing

## Debugging

### Enable Logging
```java
// In SmsReceiver.java
Log.d(TAG, "SMS imported successfully - Amount: " + parsed.amount);

// In SmsImportConversionService.java
Log.d(TAG, "Successfully converted SMS import to transaction");
```

### Check Database
```java
// Get pending SMS imports
List<SmsImport> pending = smsImportDao.getPending();

// Check account matching
Account account = accountDao.getByAccountNumber("1234");

// Verify transaction was created
Transaction txn = transactionDao.getById(uuid);
```

### Common Issues

| Issue | Cause | Solution |
|-------|-------|----------|
| SMS not imported | Not detected as transaction | Add keywords to SmsParser |
| Account not matched | accountNumberLast4 mismatch | Check SMS format and account setup |
| Notification not shown | Permission not granted | Grant POST_NOTIFICATIONS permission |
| Transaction not created | accountId not selected | User must select account before confirming |
| Category not shown | Wrong transaction type | SMS type must match category type |

## Support & Questions

For issues or questions:
1. Check logs: `Log.d(TAG, ...)`
2. Verify database state: `SmsImportDao`, `AccountDao`
3. Test with sample SMS messages
4. Check permissions are granted

