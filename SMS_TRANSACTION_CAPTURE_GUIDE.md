# SMS Transaction Capture Implementation Guide

## Overview
The FinanceTracker app now automatically reads incoming SMS messages and converts them into financial transactions with automatic account matching and category assignment.

## Architecture & Flow

### 1. SMS Reception & Parsing
**File:** `SmsReceiver.java`
- BroadcastReceiver listens for incoming SMS messages
- Filters out non-transaction messages using keyword matching
- Parses transaction details (amount, type, date, merchant)
- Extracts last 4 digits of account number from SMS
- Processes in background thread to avoid ANR (Application Not Responding)

### 2. Account Matching
**Files:** `SmsReceiver.java`, `AccountRepository.java`, `Account.java`
- Uses `SmsAccountNumberExtractor` utility to extract last 4 digits
- Patterns supported:
  - Masked formats: "•••1234", "****1234"
  - Bank formats: "A/C 1234", "ACCOUNT 1234"
  - Generic format: "xxxx1234"
- Queries AccountDao to find matching account by `accountNumberLast4` field
- Auto-populates account in SmsImport record if match found

### 3. SMS Import Storage
**Files:** `SmsImport.java`, `SmsImportDao.java`
- Transaction details stored in `sms_import` table with status = "PENDING"
- Fields:
  - `uuid`: Unique identifier
  - `smsText`: Full SMS message for reference
  - `amount`: Extracted transaction amount
  - `detectedType`: EXPENSE or INCOME (auto-detected)
  - `date`: Transaction date (extracted or current time)
  - `accountId`: Auto-matched account (nullable)
  - `categoryId`: User-assigned category (nullable)
  - `status`: PENDING → CONFIRMED → IGNORED

### 4. User Review & Confirmation
**Files:** `SmsImportFragment.java`, `SmsImportViewModel.java`
- Displays pending SMS imports in a list
- Shows dialog for each import allowing user to:
  - Review transaction details (amount, type, SMS text)
  - Select or confirm auto-matched account
  - Select appropriate category (filtered by transaction type)
  - Confirm, Ignore, or Cancel

**Key Features:**
- Categories filtered by detected type (EXPENSE categories for expense transactions)
- No category selection defaults to null (can be added later)
- Validation ensures account is always selected before confirming

### 5. Transaction Conversion & Recording
**Files:** `SmsImportConversionService.java`, `TransactionRepository.java`
- When user confirms SMS import:
  1. Update SmsImport status to "CONFIRMED"
  2. Create new Transaction record with:
     - Account ID from SMS import
     - Amount and type from SMS import
     - Category ID (can be null)
     - Reference ID = SmsImport UUID (for audit trail)
     - Note = "Auto-imported from SMS"
  3. Insert transaction into database
  4. SmsImport record remains for sync/audit purposes

### 6. User Notifications
**File:** `SmsImportNotificationService.java`
- Shows notification when SMS import is received
- Notification title: "SMS Transaction Detected"
- Notification text: Shows count of pending imports
- Tap notification to navigate to SMS import review screen
- Creates notification channel for Android 13+ compatibility

## Database Schema Changes

### Account Entity
```
accountNumberLast4: String  // Last 4 digits of account number (e.g., "1234")
```
Used to match incoming SMS transactions to bank accounts.

### Transaction Entity (No changes needed)
```
referenceId: String  // Links to SmsImport UUID for audit trail
```

## Permission Requirements

### AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.RECEIVE_SMS"/>
<uses-permission android:name="android.permission.READ_SMS"/>
<uses-permission android:name="android.permission.POST_NOTIFICATIONS"/>
```

### Runtime Permissions (Required for Android 6.0+)
Request at runtime in MainActivity:
```java
// Permissions to request
String[] permissions = {
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.READ_SMS,
    Manifest.permission.POST_NOTIFICATIONS
};
```

## Usage Flow

### Setup (One-time)
1. User adds bank accounts with account number last 4 digits
   - Settings → Accounts → Edit Account → Enter "Account Number (last 4)"
2. Grant SMS and notification permissions when prompted

### Automatic Operation
1. User receives SMS from bank
2. SmsReceiver automatically processes:
   - Detects transaction SMS
   - Extracts amount, type, date
   - Extracts account number last 4 digits
   - Tries to auto-match account
   - Creates SmsImport record in PENDING status
3. Notification alert sent to user

### User Actions
1. User sees notification or opens "SMS Imports" section
2. Reviews pending imports one by one
3. For each import:
   - Verifies account (auto-matched or selects manually)
   - Selects category (optional)
   - Confirms to convert to transaction
4. Confirmed imports become actual transactions
5. Ignored imports marked as IGNORED (not converted)

## Error Handling

### Invalid/Unmatched SMS
- If amount or type cannot be detected: SMS is discarded
- If account number cannot be extracted: SMS import created with accountId = null
  - User must manually select account before confirming
- If account number doesn't match any existing account: accountId = null
  - User must manually select account before confirming

### Category Handling
- If no category selected: transaction created with categoryId = null
- User can add category later by editing transaction
- Categories filtered by type (EXPENSE/INCOME) for faster selection

### Database
- Uses Room's fallback destructive migration for schema changes
- Version bumped as needed
- SmsImport records kept for audit/sync purposes

## Testing Checklist

- [ ] SMS received with masked account (•••1234)
- [ ] SMS received with bank format (A/C 1234)
- [ ] Account auto-matched correctly
- [ ] Notification shown on SMS arrival
- [ ] SMS Import review dialog displays correct details
- [ ] Categories filtered by transaction type
- [ ] Transaction created with correct values
- [ ] Confirmed SMS marked as CONFIRMED in database
- [ ] Ignored SMS marked as IGNORED in database
- [ ] Missing account number - manual selection required
- [ ] Missing category - transaction created without category
- [ ] Category can be added/edited after transaction creation

## Security & Privacy Considerations

1. **Broadcast Receiver Priority**: Set to 999 to intercept SMS early
2. **SMS Permissions**: Only granted to this app after user confirmation
3. **Data Storage**: SMS text stored locally, synced with user's drive
4. **Account Number**: Only last 4 digits stored in Account record for matching
5. **Background Processing**: SMS processing done in background thread
6. **No Network**: Account matching done locally, no data sent to external services

## Future Enhancements

1. **Merchant Extraction**: Extract merchant name from SMS and create/match Merchant records
2. **Smart Category Assignment**: ML-based category prediction based on merchant/amount
3. **Duplicate Detection**: Prevent duplicate transactions from same SMS
4. **SMS Filtering**: User can configure which senders to monitor
5. **Auto-Confirm**: User can set rules to auto-confirm matching transactions
6. **SMS Archive**: Keep SMS records for reconciliation

## File Locations

**Service Layer:**
- `com.financetracker.service.SmsReceiver` - Broadcast receiver
- `com.financetracker.service.SmsParser` - Transaction parsing
- `com.financetracker.service.SmsImportConversionService` - Convert to transactions
- `com.financetracker.service.SmsImportNotificationService` - Show notifications

**Data Layer:**
- `com.financetracker.data.db.entity.SmsImport` - SMS import entity
- `com.financetracker.data.db.dao.SmsImportDao` - SMS import DAO
- `com.financetracker.data.repository.SmsImportRepository` - SMS import repository

**UI Layer:**
- `com.financetracker.ui.smsimport.SmsImportFragment` - Review UI
- `com.financetracker.ui.smsimport.SmsImportViewModel` - ViewModel
- `com.financetracker.ui.smsimport.SmsImportAdapter` - List adapter

**Utils:**
- `com.financetracker.utils.SmsAccountNumberExtractor` - Account number extraction

## Configuration

Users should:
1. Add account numbers (last 4 digits) to each bank account
   - Settings → Accounts → Edit Account
   - Field: "Account Number (last 4)"
2. Grant SMS permissions at runtime
3. Grant notification permissions (Android 13+)
4. Optionally configure default categories for different transaction types

## Troubleshooting

**SMS not being imported:**
- Check AndroidManifest.xml has SMS permissions
- Check SmsReceiver is registered with priority 999
- Check device allows app to receive SMS (some devices block it)
- Check SMS is detected as transaction (contains keywords like "debit", "credit", etc.)

**Account not auto-matching:**
- Verify account number last 4 digits match SMS text exactly
- Check SMS format is supported (masked •••1234, A/C 1234, xxxx1234, etc.)
- Check account is not deleted

**Notification not showing:**
- Check notification permission granted
- Check notification channel was created
- Check app has not been force-stopped

**Transaction not being created:**
- Check SMS import marked as CONFIRMED
- Check account was selected before confirming
- Check database has no schema issues

